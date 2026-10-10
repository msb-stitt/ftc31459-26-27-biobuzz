package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L190: ask the wheels for a speed. Full stick means 40 inches per second, not
 * full power, and the drivetrain measures the wheels and makes up the
 * difference.
 *
 * <p>Every OpMode has four methods. {@code init} runs once when INIT is
 * pressed, {@code start} once when PLAY is pressed, {@code loop} over and over
 * until STOP, and {@code stop} once at the end. The {@code Before} and
 * {@code After} calls are the robot's own housekeeping: the hardware, the
 * flight log and the live values. The code between them is the lesson's.
 *
 * <p>Passes when: LessonsTest.l190_theSticksCommandASpeedAndTheWheelsAreCorrectedTowardsIt,
 * LessonsTest.l190_whenTheWheelsAreUpToSpeedOnlyTheFeedforwardRemains and
 * LessonsTest.l190_turningAsksEachSideForOppositeSpeeds
 */
@TeleOp(name = "L190 Velocity Drive", group = "Lessons")
// When on L190S020, see
// https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l190.html#l190s020
// for what to do here.
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L190VelocityDriveOpMode extends CorbelsTeleOp {

    /** How fast full stick asks for, forward and sideways. Inches per second. */
    static final double MAX_IPS = 40.0;

    /** How fast full stick asks for in turn. Radians per second, half a turn. */
    static final double MAX_TURN_RADPS = Math.PI;

    // When on L190S030, see
    // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l190.html#l190s030
    // for what to do here.

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init() {
        initBefore();

        // When on L190S040, see
        // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l190.html#l190s040
        // for what to do here.

        initAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void start() {
        startBefore();
        startAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();

        double leftStickY = gamepad1.left_stick_y;
        Tracker.publish("stick/leftY", leftStickY);
        double leftStickX = gamepad1.left_stick_x;
        Tracker.publish("stick/leftX", leftStickX);
        double rightStickX = gamepad1.right_stick_x;
        Tracker.publish("stick/rightX", rightStickX);

        double band = 0.05;
        // When on L190S050, see
        // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l190.html#l190s050
        // for what to do here.
        double forwardInPerS = 0.0;
        double strafeInPerS = 0.0;
        double turnRadPerS = 0.0;
        Tracker.publish("command/forward_ips", forwardInPerS);
        Tracker.publish("command/left_ips", strafeInPerS);
        Tracker.publish("command/turn_radps", turnRadPerS);

        loopAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void stop() {
        stopAfter();
    }

    double deadband(double value, double band) {
        double alwaysPositiveMagnitude = Math.abs(value);
        boolean isCloseToZero = alwaysPositiveMagnitude < band;
        if (isCloseToZero) {
            return 0.0;
        }
        return value;
    }
}
