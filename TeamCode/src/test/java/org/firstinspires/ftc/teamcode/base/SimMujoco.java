package org.firstinspires.ftc.teamcode.base;

import com.pedropathing.math.Pose;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The simulated robot moved by MuJoCo, in place of {@link SimRobot}'s own
 * motion, for a run that asks for it.
 *
 * <p>{@code SIMRUN_MUJOCO} names a port on localhost. simRun listens there and
 * waits, before the OpMode's INIT, for mujocobiobuzz's
 * {@code python -m mujocobiobuzz.link} to connect. Before each pass of the
 * OpMode it sends one line, the pass's simulated time and the powers the code
 * last set, and waits for one line back, what the robot's sensors read then.
 * {@link SimRobot.SimLocalizer#follow} reports that in place of its own motion,
 * so the localizer, the encoders, the IMU and the Pinpoint all read MuJoCo's
 * robot, and the OpMode's pass sees what MuJoCo made of its last powers.
 *
 * <p>The line sent is JSON: {@code t}, the simulated time in seconds;
 * {@code values}, the topics mujocobiobuzz turns into motor powers, by the
 * names simRun publishes them under; and {@code pose}, x and y in inches,
 * heading in radians and its number, when the code has set a pose that MuJoCo
 * has not yet taken; {@code robot}, the robot's number; and {@code launcher},
 * the launcher motor's P, I, D and F once the code has set them, which MuJoCo's
 * model of the hub's velocity loop holds the launcher's target with. The line
 * back is the numbers {@link #parse} reads; its last, the launcher's speed as
 * that hub measures it, is what the launcher motor's {@code getVelocity}
 * reports.
 *
 * <p>It is a socket rather than NetworkTables because the server simRun
 * serves, corbelsflightlog's {@code Nt4Server}, keeps no reader of a value a
 * client publishes.
 *
 * <p>Passes when: SimMujocoTest.
 */
final class SimMujoco implements AutoCloseable {

    /** The environment variable that names the port. */
    static final String VARIABLE = "SIMRUN_MUJOCO";

    /** How long simRun waits for MuJoCo to connect. */
    static final int CONNECT_TIMEOUT_MS = 120_000;

    /** How long a pass waits for MuJoCo's answer before the run fails. */
    static final int ANSWER_TIMEOUT_MS = 10_000;

    /** The four wheels' topics, in {@link OpModeHarness}'s order of their motors. */
    private static final String[][] WHEELS = {
            {"sim/wheels/frontLeft", OpModeHarness.FRONT_LEFT},
            {"sim/wheels/frontRight", OpModeHarness.FRONT_RIGHT},
            {"sim/wheels/backLeft", OpModeHarness.BACK_LEFT},
            {"sim/wheels/backRight", OpModeHarness.BACK_RIGHT}};

    /** The mechanisms' topics, by the names {@link Tracker} holds them under. */
    private static final String[] TRACKED = {
            "intake/speed", "launcher/feeding", "launcher/target_tps"};

    private final ServerSocket server;
    private Socket socket;
    private BufferedReader in;
    private OutputStream out;
    private volatile SimRobot.Motion latest;
    private volatile double launcherTps;

    private SimMujoco(ServerSocket server) {
        this.server = server;
    }

    /**
     * Opens the port {@code variable} names, or returns null when it is unset.
     * Port 0 asks the system for a free one, which {@link #port} names. A value
     * that is not a port is an {@link IllegalArgumentException}.
     */
    static SimMujoco open(String variable) throws IOException {
        if (variable == null) {
            return null;
        }
        int port;
        try {
            port = Integer.parseInt(variable.trim());
        } catch (NumberFormatException e) {
            port = -1;
        }
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException(VARIABLE + "=" + variable + " is not a port number");
        }
        return new SimMujoco(new ServerSocket(port, 1, InetAddress.getLoopbackAddress()));
    }

    int port() {
        return server.getLocalPort();
    }

    /** Waits for MuJoCo to connect, then has {@code robot} report MuJoCo's motion. */
    void connect(SimRobot robot, PrintStream log) throws IOException {
        log.println("Waiting for MuJoCo on port " + port() + ": in mujocobiobuzz,"
                + " uv run python -m mujocobiobuzz.link --port " + port());
        server.setSoTimeout(CONNECT_TIMEOUT_MS);
        try {
            socket = server.accept();
        } catch (SocketTimeoutException e) {
            throw new IOException("MuJoCo did not connect on port " + port() + " within "
                    + CONNECT_TIMEOUT_MS / 1000 + " s");
        }
        socket.setTcpNoDelay(true);
        socket.setSoTimeout(ANSWER_TIMEOUT_MS);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = socket.getOutputStream();
        robot.localizer.follow(() -> latest);
        SimMechanisms.launcherMeasured = () -> launcherTps;
        log.println("MuJoCo connected.");
    }

    /** One pass's exchange: the time {@code t} and the harness's powers out, MuJoCo's reading back. */
    void step(OpModeHarness harness, double t) throws IOException {
        SimRobot.SimLocalizer localizer = harness.robot.localizer;
        String line = request(t, values(harness), localizer.posePending(), localizer.poseSeq(),
                robotNumber(), SimMechanisms.launcherPidf);
        out.write(line.getBytes(StandardCharsets.UTF_8));
        out.flush();
        String answer;
        try {
            answer = in.readLine();
        } catch (SocketTimeoutException e) {
            throw new IOException("MuJoCo did not answer within " + ANSWER_TIMEOUT_MS / 1000 + " s");
        }
        if (answer == null) {
            throw new IOException("MuJoCo closed the link");
        }
        latest = parse(answer);
        launcherTps = launcherTps(answer);
    }

    /** The robot's number, as {@code BOT_37523} names it: 37523. */
    static String robotNumber() {
        return org.firstinspires.ftc.teamcode.pedro.Constants.thisRobot.name().replace("BOT_", "");
    }

    /** The topics MuJoCo turns into powers, as the harness's motors and {@link Tracker} hold them now. */
    static Map<String, Object> values(OpModeHarness harness) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (String[] wheel : WHEELS) {
            values.put(wheel[0], harness.motors.get(wheel[1]).speed);
        }
        Map<String, Object> tracked = Tracker.values();
        for (String name : TRACKED) {
            Object v = tracked.get(name);
            if (v instanceof Number || v instanceof Boolean) {
                values.put("/" + name, v);
            }
        }
        return values;
    }

    /** One line to MuJoCo, ending in a newline. */
    static String request(double t, Map<String, Object> values, Pose pending, long seq) {
        return request(t, values, pending, seq, null, null);
    }

    /** One line to MuJoCo, with the robot's number and the launcher's gains when given. */
    static String request(double t, Map<String, Object> values, Pose pending, long seq,
            String robot, double[] launcherPidf) {
        StringBuilder b = new StringBuilder("{\"t\": ").append(number(t)).append(", \"values\": {");
        String sep = "";
        for (Map.Entry<String, Object> e : values.entrySet()) {
            Object v = e.getValue();
            b.append(sep).append('"').append(e.getKey()).append("\": ")
                    .append(v instanceof Boolean ? v.toString() : number(((Number) v).doubleValue()));
            sep = ", ";
        }
        b.append('}');
        if (pending != null) {
            b.append(", \"pose\": [").append(number(pending.x())).append(", ")
                    .append(number(pending.y())).append(", ").append(number(pending.heading()))
                    .append(", ").append(seq).append(']');
        }
        if (robot != null) {
            b.append(", \"robot\": \"").append(robot).append('"');
        }
        if (launcherPidf != null) {
            b.append(", \"launcher\": [");
            for (int i = 0; i < launcherPidf.length; i++) {
                b.append(i == 0 ? "" : ", ").append(number(launcherPidf[i]));
            }
            b.append(']');
        }
        return b.append("}\n").toString();
    }

    /** A number JSON can carry: a value that is not finite goes as 0. */
    private static String number(double v) {
        return Double.isFinite(v) ? Double.toString(v) : "0.0";
    }

    /**
     * MuJoCo's line: t, seq, x, y, heading, vx, vy, omega, turned, the four
     * wheels' roll, forward and leftward travel, as {@link SimRobot.Motion}
     * names them, and the launcher's speed, space-separated.
     */
    static SimRobot.Motion parse(String line) {
        String[] f = line.trim().split("\\s+");
        if (f.length != 16) {
            throw new IllegalArgumentException("MuJoCo sent " + f.length + " numbers, not 16: " + line);
        }
        double[] v = new double[15];
        for (int i = 0; i < 15; i++) {
            v[i] = Double.parseDouble(f[i]);
        }
        return new SimRobot.Motion(v[2], v[3], v[4], v[5], v[6], v[7], v[8],
                new double[]{v[9], v[10], v[11], v[12]}, v[13], v[14], (long) v[1]);
    }

    /** The launcher's speed, ticks a second, the last number of MuJoCo's line. */
    static double launcherTps(String line) {
        String[] f = line.trim().split("\\s+");
        return Double.parseDouble(f[f.length - 1]);
    }

    @Override
    public void close() throws IOException {
        SimMechanisms.launcherMeasured = null;
        if (socket != null) {
            socket.close();
        }
        server.close();
    }
}
