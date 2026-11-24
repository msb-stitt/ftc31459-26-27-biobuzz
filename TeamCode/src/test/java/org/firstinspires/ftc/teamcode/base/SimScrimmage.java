package org.firstinspires.ftc.teamcode.base;

import com.pedropathing.math.Pose;

import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The simulated robot moved by msb-stitt's fork of Scrimmage, a sim server for
 * several robots on one field, in place of {@link SimRobot}'s own motion, for
 * a run that asks for it.
 *
 * <p>{@code SIMRUN_SCRIMMAGE} names the server and this robot,
 * {@code host:port/name}, such as {@code localhost:3300/37523}. simRun
 * connects to {@code ws://host:port/wpilibws/name} before the OpMode's INIT, as
 * a WPILib robot's halsim_ws client does, and joins its lockstep: the server
 * sends this robot's sensors at a time T and {@code step_seq} = T, in
 * microseconds; simRun sets its clock to T, runs one pass, sends the powers
 * the code set and answers {@code done_seq} = T with {@code next_us}, the time
 * of its next pass. So simRun runs as fast as the server steps, and a pass
 * missed while the server waited on another robot is skipped, the clock
 * jumping to T.
 *
 * <p>Each drive motor is the server's {@code ScrimmageMotor}, named for its
 * hardware name with its spaces as underscores ({@code Front_Left}), sent
 * {@code <motorVoltage}, its power times the battery's voltage. The robot's
 * motion is the server's drivebase plugin's {@code ScrimmageDrivebase}
 * {@code drive}, in metres; {@link SimRobot.SimLocalizer#follow} reports it,
 * so the localizer, the encoders, the IMU and the Pinpoint all read the
 * server's robot. A pose the code sets is sent as the plugin's placing.
 *
 * <p>The launcher and intake are {@code mujocobiobuzz}'s plugin
 * {@code Mechanisms}, sent the values {@link SimMujoco} sends the link, on
 * {@code Biobuzz mechanisms}; the launcher's motor reads back the speed the
 * plugin's hub measured.
 *
 * <p>Passes when: SimScrimmageTest.
 */
final class SimScrimmage implements AutoCloseable {
    /** The environment variable that names the server and the robot. */
    static final String VARIABLE = "SIMRUN_SCRIMMAGE";
    /** The lockstep device, as the fork's lockstep extension makes it. */
    static final String LOCKSTEP_TYPE = "SimDevice";
    static final String LOCKSTEP = "ScrimmageLockstep";
    /** The drivebase plugin's device type and this robot's device. */
    static final String DRIVEBASE_TYPE = "ScrimmageDrivebase";
    static final String DRIVEBASE = "drive";
    static final String MOTOR_TYPE = "ScrimmageMotor";
    /** The launcher and intake, as {@code mujocobiobuzz.plugins.Mechanisms} reads them. */
    static final String MECHANISMS_TYPE = "Biobuzz";
    static final String MECHANISMS = "mechanisms";
    private static final String[] GAINS = {"<launcher_p", "<launcher_i", "<launcher_d", "<launcher_f"};
    private static final double IN = 0.0254;
    private static final String[] WHEELS = {OpModeHarness.FRONT_LEFT, OpModeHarness.FRONT_RIGHT,
            OpModeHarness.BACK_LEFT, OpModeHarness.BACK_RIGHT};

    final String host;
    final int port;
    final String name;
    private SimHalsimWs ws;
    private final Map<String, Map<String, Object>> devices = new HashMap<>();
    private volatile SimRobot.Motion latest;
    private volatile double launcherTps;

    SimScrimmage(String host, int port, String name) {
        this.host = host;
        this.port = port;
        this.name = name;
    }

    /** The server and robot {@code variable} names, or null when it is unset. */
    static SimScrimmage open(String variable) {
        if (variable == null) {
            return null;
        }
        String v = variable.trim();
        int colon = v.indexOf(':');
        int slash = v.indexOf('/', colon + 1);
        int port = -1;
        if (colon > 0 && slash > colon + 1 && slash < v.length() - 1) {
            try {
                port = Integer.parseInt(v.substring(colon + 1, slash));
            } catch (NumberFormatException e) {
                port = -1;
            }
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException(VARIABLE + "=" + variable
                    + " is not host:port/name, such as localhost:3300/37523");
        }
        return new SimScrimmage(v.substring(0, colon), port, v.substring(slash + 1));
    }

    /** Connects and joins the lockstep, then has {@code robot} report the server's motion. */
    void connect(SimRobot robot, PrintStream log) throws IOException {
        String path = "/wpilibws/" + name;
        log.println("Connecting to Scrimmage at ws://" + host + ":" + port + path);
        ws = SimHalsimWs.connect(host, port, path);
        lockstep(map("<engaged", false));
        robot.localizer.follow(() -> latest);
        SimMechanisms.launcherMeasured = () -> launcherTps;
        log.println("Scrimmage connected.");
    }

    /**
     * Waits for the server's next sync, taking the sensors sent before it, and
     * returns its time T in microseconds. It waits as long as the server
     * takes, so a robot held at a breakpoint holds this one when the server
     * waits for it; the server's {@code sync_timeout} says whether it does.
     */
    long awaitSync() throws IOException {
        while (true) {
            SimHalsimWs.Message m;
            try {
                m = ws.receive();
            } catch (EOFException e) {
                throw new IOException("Scrimmage closed the connection");
            }
            if (LOCKSTEP_TYPE.equals(m.type) && LOCKSTEP.equals(m.device)) {
                if (Boolean.TRUE.equals(m.data.get(">engage"))) {
                    lockstep(map("<engaged", true));
                }
                Object step = m.data.get(">step_seq");
                if (step instanceof Long) {
                    return (Long) step;
                }
                continue;
            }
            devices.computeIfAbsent(m.type + "/" + m.device, k -> new HashMap<>()).putAll(m.data);
            if (DRIVEBASE_TYPE.equals(m.type) && DRIVEBASE.equals(m.device)) {
                latest = motion(devices.get(m.type + "/" + m.device));
            }
            if (MECHANISMS_TYPE.equals(m.type) && MECHANISMS.equals(m.device)) {
                launcherTps = number(m.data, ">launcher_tps", launcherTps);
            }
        }
    }

    /**
     * The pass at T done: the powers the code set, a pose it set that the
     * server has not taken, then {@code done_seq} = T with the next pass's time.
     */
    void answer(OpModeHarness harness, long t, long nextUs) throws IOException {
        double vbus = number(devices.get(MOTOR_TYPE + "/" + device(WHEELS[0])), ">vbus", harness.batteryVolts);
        for (String wheel : WHEELS) {
            ws.send(MOTOR_TYPE, device(wheel), map("<motorVoltage", harness.motors.get(wheel).speed * vbus));
        }
        SimRobot.SimLocalizer localizer = harness.robot.localizer;
        Pose pending = localizer.posePending();
        if (pending != null) {
            Map<String, Object> place = map("<place_x", pending.x() * IN);
            place.put("<place_y", pending.y() * IN);
            place.put("<place_heading", pending.heading());
            place.put("<place_seq", localizer.poseSeq());
            ws.send(DRIVEBASE_TYPE, DRIVEBASE, place);
        }
        ws.send(MECHANISMS_TYPE, MECHANISMS, mechanisms(harness, SimMechanisms.launcherPidf));
        Map<String, Object> done = map("<next_us", nextUs);
        done.put("<done_seq", t);
        lockstep(done);
    }

    /**
     * The launcher's and intake's values, as {@link SimMujoco} sends them to
     * {@code mujocobiobuzz.link}, each {@code <} and its topic, and the gains the
     * code gave the launcher's motor, once it has.
     */
    static Map<String, Object> mechanisms(OpModeHarness harness, double[] launcherPidf) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : SimMujoco.values(harness).entrySet()) {
            if (e.getKey().startsWith("/")) {
                out.put("<" + e.getKey(), e.getValue());
            }
        }
        if (launcherPidf != null) {
            for (int i = 0; i < GAINS.length; i++) {
                out.put(GAINS[i], launcherPidf[i]);
            }
        }
        return out;
    }

    /** The server's device name for a hardware name: its spaces as underscores. */
    static String device(String hardwareName) {
        return hardwareName.replace(' ', '_');
    }

    /** The drivebase plugin's values as the motion {@link SimRobot.SimLocalizer} follows, in inches. */
    static SimRobot.Motion motion(Map<String, Object> d) {
        double[] rolled = new double[4];
        for (int i = 0; i < 4; i++) {
            rolled[i] = number(d, ">rolled" + i, 0) / IN;
        }
        return new SimRobot.Motion(number(d, ">x", 0) / IN, number(d, ">y", 0) / IN,
                number(d, ">heading", 0), number(d, ">vx", 0) / IN, number(d, ">vy", 0) / IN,
                number(d, ">omega", 0), number(d, ">turned", 0), rolled,
                number(d, ">travel_x", 0) / IN, number(d, ">travel_y", 0) / IN,
                (long) number(d, ">place_seq", 0));
    }

    private static double number(Map<String, Object> d, String key, double otherwise) {
        Object v = d == null ? null : d.get(key);
        return v instanceof Number ? ((Number) v).doubleValue() : otherwise;
    }

    private static Map<String, Object> map(String key, Object value) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put(key, value);
        return m;
    }

    private void lockstep(Map<String, Object> data) throws IOException {
        ws.send(LOCKSTEP_TYPE, LOCKSTEP, data);
    }

    @Override
    public void close() throws IOException {
        if (ws != null) {
            ws.close();
        }
    }
}
