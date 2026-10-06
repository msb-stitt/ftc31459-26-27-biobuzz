package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L140: field relative. Push the stick away from you and the robot goes away
 * from you, whichever way it happens to be facing.
 *
 * <p>Every OpMode has four methods. {@code init} runs once when INIT is
 * pressed, {@code start} once when PLAY is pressed, {@code loop} over and over
 * until STOP, and {@code stop} once at the end. The {@code Before} and
 * {@code After} calls are the robot's own housekeeping: the hardware, the
 * flight log and the live values. The code between them is the lesson's.
 *
 * <p>Passes when: LessonsTest.l140_fieldRelativeIgnoresWhichWayTheRobotFaces
 */
@TeleOp(name = "L140 Field Relative", group = "Lessons")
// When on L140S020, see
// https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l140.html#l140s020
// for what to do here.
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L140FieldRelativeOpMode extends CorbelsTeleOp {

    boolean previousButtonA;

    CorbelsMecanum drivetrain;

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init() {
        initBefore();

        drivetrain = new CorbelsMecanum(hardware);

        initAfter(drivetrain);
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

        // When on L140S040, see
        // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l140.html#l140s040
        // for what to do here.
        drivetrain.sticks(forward, strafe, turn);

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

    // 'public' to match the parent class. 'public' allows access from any code.
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
