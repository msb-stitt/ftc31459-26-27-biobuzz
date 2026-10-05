package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L110: hand the wheels to the path follower. The robot drives on the sticks
 * as it did in L090, and the follower is holding the drivetrain.
 *
 * <p>Every OpMode has four methods. {@code init} runs once when INIT is
 * pressed, {@code start} once when PLAY is pressed, {@code loop} over and over
 * until STOP, and {@code stop} once at the end. The {@code Before} and
 * {@code After} calls are the robot's own housekeeping: the hardware, the
 * flight log and the live values. The code between them is the lesson's.
 *
 * <p>Passes when: LessonsTest.l110_theFollowerDrivesTheSticks and
 * LessonsTest.l110_aCornerOfTheStickIsStillScaled
 */
@TeleOp(name = "L110 Follower", group = "Lessons")
// When on L110S020, see
// https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l110.html#l110s020
// for what to do here.
@Disabled
// public: the robot only runs an OpMode that is public.
public class L110FollowerOpMode extends CorbelsTeleOp {

    boolean previousButtonA;

    // When on L110S040, see
    // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l110.html#l110s040
    // for what to do here.

    // public: OpMode's own init is public, so this one has to be too.
    @Override
    public void init() {
        initBefore();

        // When on L110S050, see
        // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l110.html#l110s050
        // for what to do here.

        initAfter();
    }

    // public: OpMode's own start is public, so this one has to be too.
    @Override
    public void start() {
        startBefore();
        startAfter();
    }

    // public: OpMode's own loop is public, so this one has to be too.
    @Override
    public void loop() {
        loopBefore();

        double leftStickY = gamepad1.left_stick_y;
        Tracker.publish("stick/leftY", leftStickY);
        double rightStickY = gamepad1.right_stick_y;
        Tracker.publish("stick/rightY", rightStickY);

        double leftStickX = gamepad1.left_stick_x;
        Tracker.publish("stick/leftX", leftStickX);
        double rightStickX = gamepad1.right_stick_x;
        Tracker.publish("stick/rightX", rightStickX);

        double band = 0.05;
        double forward = squared(deadband(-leftStickY, band));
        Tracker.publish("arcade/forward", forward);
        double turn = squared(deadband(-rightStickX, band));
        Tracker.publish("arcade/turn", turn);

        double strafe = squared(deadband(-leftStickX, band));
        Tracker.publish("arcade/strafe", strafe);

        // When on L110S060, see
        // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l110.html#l110s060
        // for what to do here.

        boolean buttonA = gamepad1.a;
        Tracker.publish("button/a", buttonA);

        if (buttonA != previousButtonA) {
            if (buttonA) {
                Tracker.publish("button/event", "A pressed");
            } else {
                Tracker.publish("button/event", "A released");
            }
        }
        previousButtonA = buttonA;

        loopAfter();
    }

    // public: OpMode's own stop is public, so this one has to be too.
    @Override
    public void stop() {
        stopAfter();
    }

    double deadband(double value, double band) {
        if (Math.abs(value) < band) {
            return 0;
        }
        return value;
    }

    double squared(double value) {
        return value * Math.abs(value);
    }
}
