package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L070: the A button, the moment it changes, and both sticks sideways, all
 * written down while the robot drives.
 *
 * <p>Every OpMode has four methods. {@code init} runs once when INIT is
 * pressed, {@code start} once when PLAY is pressed, {@code loop} over and over
 * until STOP, and {@code stop} once at the end. The {@code Before} and
 * {@code After} calls are the robot's own housekeeping: the hardware, the
 * flight log and the live values. The code between them is the lesson's.
 *
 * <p>Passes when: LessonsTest.l070_logsTheAButton,
 * LessonsTest.l070_saysWhenTheAButtonIsPressedAndReleased and
 * LessonsTest.l070_logsBothSticksSideways
 */
@TeleOp(name = "L070 Buttons", group = "Lessons")
// When on L070S020, see
// https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l070.html#l070s020
// for what to do here.
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L070ButtonsOpMode extends CorbelsTeleOp {

    // When on L070S040, see
    // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l070.html#l070s040
    // for what to do here.

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init() {
        initBefore();

        hardware.frontLeft.setDirection(hardware.mecanumConfig.frontLeftDirection.get());
        hardware.frontRight.setDirection(hardware.mecanumConfig.frontRightDirection.get());
        hardware.backLeft.setDirection(hardware.mecanumConfig.backLeftDirection.get());
        hardware.backRight.setDirection(hardware.mecanumConfig.backRightDirection.get());

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
        double rightStickY = gamepad1.right_stick_y;
        Tracker.publish("stick/rightY", rightStickY);

        // When on L070S060, see
        // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l070.html#l070s060
        // for what to do here.

        double leftSpeed = -leftStickY;
        Tracker.publish("speed/left", leftSpeed);
        double rightSpeed = -rightStickY;
        Tracker.publish("speed/right", rightSpeed);

        double band = 0.05;
        double leftPower = deadband(leftSpeed, band);
        leftPower = signedSquared(leftPower);
        Tracker.publish("power/left", leftPower);
        double rightPower = deadband(rightSpeed, band);
        rightPower = signedSquared(rightPower);
        Tracker.publish("power/right", rightPower);

        hardware.frontLeft.setPower(leftPower);
        hardware.backLeft.setPower(leftPower);

        hardware.frontRight.setPower(rightPower);
        hardware.backRight.setPower(rightPower);

        // When on L070S030, see
        // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l070.html#l070s030
        // for what to do here.

        // When on L070S050, see
        // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l070.html#l070s050
        // for what to do here.

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

    double signedSquared(double value) {
        return value * Math.abs(value);
    }
}
