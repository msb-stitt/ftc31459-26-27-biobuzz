package org.firstinspires.ftc.teamcode.example;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import io.github.mikestitt.corbelsflightlog.FlightLog;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Publishes every type the flight log records, under a nested hierarchy, for
 * {@code NtApiCheck} to read back over NetworkTables with nobody at the robot.
 *
 * <p>No motor is driven and no gamepad is read. The fixed values are recorded
 * in init, so they are there before any client connects; the changing ones are
 * all worked out from one loop counter, so a client can check each on its own.
 *
 * <p>For practice only: FTC rule R704 forbids third-party telemetry over Wi-Fi
 * at competitions, and this OpMode turns the server on.
 *
 * <p>Passes when: NtApiCheckOpModeTest, and {@code ./gradlew :TeamCode:ntApiCheck}
 * against it running
 */
@TeleOp(name = "NetworkTables API check", group = "Corbels")
public class NtApiCheckOpMode extends CorbelsTeleOp {

    /** One value the OpMode records and a client should read back. */
    public static final class Entry {
        /** The NetworkTables name, which is the log's name with a leading slash. */
        public final String name;
        /** The NetworkTables type. */
        public final String type;
        /**
         * What a client should read: a Boolean, Double, Float, Long or String; a
         * boolean[], double[], long[], String[] or byte[]; or, for a struct, its
         * fields as a double[] in the order they are packed.
         */
        public final Object expected;
        final Consumer<FlightLog> record;

        Entry(String key, String type, Object expected, Consumer<FlightLog> record) {
            this.name = "/" + key;
            this.type = type;
            this.expected = expected;
            this.record = record;
        }
    }

    /** Recorded before {@code serveLive()}, so it reaches a client only by the replay. */
    public static final Entry BEFORE_SERVE = new Entry("apicheck/before_serve", "string",
            "recorded before the server",
            log -> log.recordOutput("apicheck/before_serve", "recorded before the server"));

    /** Every fixed value, recorded once in init. */
    public static final List<Entry> FIXED = Collections.unmodifiableList(Arrays.asList(
            new Entry("apicheck/types/scalar/boolean", "boolean", true,
                    log -> log.recordOutput("apicheck/types/scalar/boolean", true)),
            new Entry("apicheck/types/scalar/double", "double", 3.25,
                    log -> log.recordOutput("apicheck/types/scalar/double", 3.25)),
            new Entry("apicheck/types/scalar/float", "float", 1.5f,
                    log -> log.recordOutput("apicheck/types/scalar/float", 1.5f)),
            new Entry("apicheck/types/scalar/int", "int", 1234567890123L,
                    log -> log.recordOutput("apicheck/types/scalar/int", 1234567890123L)),
            new Entry("apicheck/types/scalar/string", "string", "hello, robot",
                    log -> log.recordOutput("apicheck/types/scalar/string", "hello, robot")),
            new Entry("apicheck/types/array/boolean", "boolean[]", new boolean[]{true, false, true},
                    log -> log.recordOutput("apicheck/types/array/boolean", new boolean[]{true, false, true})),
            new Entry("apicheck/types/array/double", "double[]", new double[]{0.5, -2.0, 1e9},
                    log -> log.recordOutput("apicheck/types/array/double", new double[]{0.5, -2.0, 1e9})),
            new Entry("apicheck/types/array/int", "int[]", new long[]{-1, 0, 1L << 40},
                    log -> log.recordOutput("apicheck/types/array/int", new long[]{-1, 0, 1L << 40})),
            new Entry("apicheck/types/array/string", "string[]", new String[]{"a", "", "c d"},
                    log -> log.recordOutput("apicheck/types/array/string", new String[]{"a", "", "c d"})),
            new Entry("apicheck/types/raw", "raw", new byte[]{0, 1, (byte) 0xff, 0x7f},
                    log -> log.recordOutput("apicheck/types/raw", new byte[]{0, 1, (byte) 0xff, 0x7f})),
            new Entry("apicheck/types/struct/translation2d", "struct:Translation2d", new double[]{1, 2},
                    log -> log.translation2d("apicheck/types/struct/translation2d", 1, 2)),
            new Entry("apicheck/types/struct/rotation2d", "struct:Rotation2d", new double[]{0.75},
                    log -> log.rotation2d("apicheck/types/struct/rotation2d", 0.75)),
            new Entry("apicheck/types/struct/pose2d", "struct:Pose2d", new double[]{1, 2, 0.5},
                    log -> log.pose2d("apicheck/types/struct/pose2d", 1, 2, 0.5)),
            new Entry("apicheck/types/struct/twist2d", "struct:Twist2d", new double[]{0.1, 0.2, 0.3},
                    log -> log.twist2d("apicheck/types/struct/twist2d", 0.1, 0.2, 0.3)),
            new Entry("apicheck/types/struct/chassis_speeds", "struct:ChassisSpeeds", new double[]{1.5, -0.5, 2},
                    log -> log.chassisSpeeds("apicheck/types/struct/chassis_speeds", 1.5, -0.5, 2)),
            new Entry("apicheck/types/struct/wheel_speeds", "struct:MecanumDriveWheelSpeeds",
                    new double[]{1, 2, 3, 4},
                    log -> log.mecanumWheelSpeeds("apicheck/types/struct/wheel_speeds", 1, 2, 3, 4)),
            new Entry("apicheck/types/struct/translation3d", "struct:Translation3d", new double[]{1, 2, 3},
                    log -> log.translation3d("apicheck/types/struct/translation3d", 1, 2, 3)),
            new Entry("apicheck/types/struct/quaternion", "struct:Quaternion", new double[]{1, 0, 0, 0},
                    log -> log.quaternion("apicheck/types/struct/quaternion", 1, 0, 0, 0)),
            new Entry("apicheck/types/struct/rotation3d", "struct:Rotation3d", new double[]{0, 1, 0, 0},
                    log -> log.rotation3d("apicheck/types/struct/rotation3d", 0, 1, 0, 0)),
            new Entry("apicheck/types/struct/pose3d", "struct:Pose3d", new double[]{1, 2, 3, 1, 0, 0, 0},
                    log -> log.pose3d("apicheck/types/struct/pose3d", 1, 2, 3, 1, 0, 0, 0)),
            // Pedro inches in, WPILib metres out: the field's centre is WPILib's
            // origin, turned by FlightLog.fieldQuarterTurns, which is 1.
            new Entry("apicheck/types/struct/poses", "struct:Pose2d[]",
                    new double[]{0, 0, Math.PI / 2, 0, 0, 0},
                    log -> log.poses("apicheck/types/struct/poses",
                            new double[]{72, 72, 0, 72, 72, -Math.PI / 2})),
            new Entry("apicheck/deep/a/b/c/d/e/leaf", "string", "five levels down",
                    log -> log.recordOutput("apicheck/deep/a/b/c/d/e/leaf", "five levels down")),
            new Entry("apicheck/deep/a/b/sibling", "double", -7.0,
                    log -> log.recordOutput("apicheck/deep/a/b/sibling", -7.0))));

    /** The loop counter every changing value is worked out from. */
    public static final String STEP = "/apicheck/changing/step";
    /** {@code step / 2}. */
    public static final String HALF = "/apicheck/changing/half";
    /** {@code step} is even. */
    public static final String EVEN = "/apicheck/changing/even";
    /** {@code "step " + step}. */
    public static final String WORD = "/apicheck/changing/word";
    /** {@code {step, step + 1, step + 2}}. */
    public static final String TRIPLE = "/apicheck/changing/triple";
    /** A Pose2d at x = step / 1000 m. */
    public static final String POSE = "/apicheck/changing/pose";
    /** A new value for each run of the OpMode, so a client can tell one run from the next. */
    public static final String RUN = "/apicheck/run";

    private boolean serving;
    private long step;

    @Override
    public void init() {
        initBefore();
        FlightLog log = Tracker.flightlog;
        BEFORE_SERVE.record.accept(log);
        serving = Tracker.serveLive();
        for (Entry e : FIXED) e.record.accept(log);
        log.recordOutput(RUN.substring(1), Long.toString(System.currentTimeMillis()));
        initAfter();
    }

    @Override
    public void loop() {
        loopBefore();
        step++;
        FlightLog log = Tracker.flightlog;
        log.recordOutput(STEP.substring(1), step);
        log.recordOutput(HALF.substring(1), step / 2.0);
        log.recordOutput(EVEN.substring(1), step % 2 == 0);
        log.recordOutput(WORD.substring(1), "step " + step);
        log.recordOutput(TRIPLE.substring(1), new long[]{step, step + 1, step + 2});
        log.pose2d(POSE.substring(1), step / 1000.0, 0, 0);
        Tracker.printToDs("NetworkTables %s; step %d", serving ? "on" : "OFF", step);
        loopAfter();
    }
}
