package org.firstinspires.ftc.teamcode.base;

import com.pedropathing.localization.MotionState;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Twist;

import io.github.mikestitt.corbelsflightlog.FlightLog;
import io.github.mikestitt.corbelsflightlog.nt.Nt4Server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.firstinspires.ftc.teamcode.pedro.Constants;

/**
 * What the simulated robot is doing, on NetworkTables, so AdvantageScope can
 * draw it.
 *
 * <p>This is a NetworkTables <b>server</b>, the part the robot plays on the
 * field. AdvantageScope and Glass are both clients: point either at
 * {@code 127.0.0.1} and it connects. Nothing here draws anything.
 *
 * <p>The server is the robot's own, {@code Nt4Server} from corbelsflightlog,
 * and the run's flight log is mirrored onto it as {@code Tracker.serveLive()}
 * mirrors it on the robot. So a simulator run exercises the robot's live path.
 * It serves NetworkTables 4 only, not 3.
 *
 * <p>The names are the robot's names. {@code corbelsflightlog-pedro} writes
 * {@code /Pose}, {@code /Mode} and a {@code /vel/...} breakdown into the flight
 * log, and the same leaves appear here under {@code sim/}, so someone who has
 * learnt where to look in a robot log looks in the same place here. The sticks
 * use the names {@code L040TankOpMode} already publishes to Panels.
 *
 * <p>Everything the flight log records goes out too, under the name it has in
 * the log: {@code Tracker.publish("stick/leftY", ...)} is {@code /stick/leftY}
 * here and in the log. So a student watches live what their own code wrote, and
 * finds it in the same place in the log afterwards. Each name {@link Tracker}
 * holds is also set once a loop, so a value published before the mirror began
 * goes out too.
 *
 * <p>The pose goes out as a WPILib {@code struct:Pose2d}, which is three
 * little-endian doubles and a schema that says so. AdvantageScope draws a bare
 * {@code double[]} too, and calls it the legacy numeric array format: it warns
 * on it in 2026 and removes it in 2027. The flight log has always written the
 * struct, so this is the same 24 bytes {@code FlightLog} writes for
 * {@code /Pose}, on a topic instead of in a file.
 *
 * <p>Passes when: SimPublisherTest
 */
public final class SimPublisher implements AutoCloseable {

    /** The NT4 port AdvantageScope looks for. */
    public static final int NT4_PORT = Nt4Server.DEFAULT_PORT;

    private final OpModeHarness harness;
    private final Nt4Server server;
    private final boolean ownsServer;
    private FlightLog mirrored;

    /** The struct schemas a {@code struct:Pose2d} is built out of, innermost
     *  first. The same table is {@code FlightLog.SCHEMAS}, which
     *  {@code mirrorTo} sends too; it is not public, so the three a pose needs
     *  are named here for {@code sim/Pose}. A wrong one is loud: AdvantageScope
     *  draws nothing. */
    private static final String[][] POSE_SCHEMAS = {
            {"struct:Translation2d", "double x;double y"},
            {"struct:Rotation2d", "double value"},
            {"struct:Pose2d", "Translation2d translation;Rotation2d rotation"}};

    private static final int POSE_BYTES = 3 * Double.BYTES;

    private final ByteBuffer poseBytes =
            ByteBuffer.allocate(POSE_BYTES).order(ByteOrder.LITTLE_ENDIAN);

    /** On {@link #NT4_PORT}, sharing the one server {@code Tracker.serveLive()} uses. */
    public SimPublisher(OpModeHarness harness) throws IOException {
        this(harness, Nt4Server.shared(), false);
    }

    /** On another port, for a test that must not collide with a real one. */
    public SimPublisher(OpModeHarness harness, int nt4Port) throws IOException {
        this(harness, Nt4Server.start(nt4Port), true);
    }

    private SimPublisher(OpModeHarness harness, Nt4Server server, boolean ownsServer) {
        this.harness = harness;
        this.server = server;
        this.ownsServer = ownsServer;
        for (String[] schema : POSE_SCHEMAS) {
            server.set("/.schema/" + schema[0], "structschema",
                    schema[1].getBytes(StandardCharsets.UTF_8));
        }
    }

    /** The server, for a test that wants its port. */
    public Nt4Server server() {
        return server;
    }

    /** One snapshot of the simulated robot. Call it once a loop. */
    public void publish() {
        if (Tracker.flightlog != mirrored) {
            mirrored = Tracker.flightlog;
            mirrored.mirrorTo(server);
        }

        MotionState state = harness.robot.localizer.state();
        Pose p = state.pose();
        server.set("sim/Pose", "struct:Pose2d", packed(FieldPose.of(p.x(), p.y(), p.heading())));
        server.set("sim/Mode", "string", String.valueOf(harness.robot.follower.mode()));

        number("sim/wheels/frontLeft", harness.motors.get(OpModeHarness.FRONT_LEFT).power);
        number("sim/wheels/frontRight", harness.motors.get(OpModeHarness.FRONT_RIGHT).power);
        number("sim/wheels/backLeft", harness.motors.get(OpModeHarness.BACK_LEFT).power);
        number("sim/wheels/backRight", harness.motors.get(OpModeHarness.BACK_RIGHT).power);

        number("sim/stick/leftY", harness.gamepad1.left_stick_y);
        number("sim/stick/leftX", harness.gamepad1.left_stick_x);
        number("sim/stick/rightY", harness.gamepad1.right_stick_y);
        number("sim/stick/rightX", harness.gamepad1.right_stick_x);

        Twist twist = state.twist();
        number("sim/vel/forward_ips", twist.vx);
        number("sim/vel/strafe_ips", twist.vy);
        number("sim/vel/omega_radps", twist.omega);

        publishTracked();
    }

    private void number(String topic, double value) {
        server.set(topic, "double", value + 0.0);
    }

    /** The last value of every name the OpMode published through {@link Tracker}. */
    private void publishTracked() {
        for (Map.Entry<String, Object> e : Tracker.values().entrySet()) {
            String topic = "/" + e.getKey();
            Object value = e.getValue();
            if (value instanceof Number) {
                number(topic, ((Number) value).doubleValue());
            } else if (value instanceof Boolean) {
                server.set(topic, "boolean", value);
            } else if (value instanceof String) {
                server.set(topic, "string", value);
            }
        }
    }

    /** {@code {x, y, headingRad}} as a {@code struct:Pose2d}'s three
     *  little-endian doubles. */
    private byte[] packed(double[] fieldPose) {
        poseBytes.clear();
        for (double value : fieldPose) {
            poseBytes.putDouble(value + 0.0);
        }
        return poseBytes.array().clone();
    }

    /** Stops mirroring, and closes the server if this made its own. The
     *  shared one stays up for the life of the program, as on the robot. */
    @Override
    public void close() {
        if (mirrored != null) mirrored.mirrorTo(null);
        if (ownsServer) server.close();
    }
}
