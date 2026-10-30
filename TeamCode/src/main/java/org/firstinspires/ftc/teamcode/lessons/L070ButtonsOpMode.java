package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.commands.Commands;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;
// When on L070S020, see
// https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l070.html#l070s020
// for what to do here.
//import org.firstinspires.ftc.teamcode.mytry.l065.StickShaper;

/**
 * L070: the A button, the moment it changes, and both sticks sideways, all
 * written down while the robot drives.
 *
 * <p>Every OpMode has four methods. {@code init} runs once when INIT is
 * pressed, {@code start} once when PLAY is pressed, {@code loop} over and over
 * until STOP, and {@code stop} once at the end. The {@code Before} and
 * {@code After} calls are the robot's own housekeeping: the hardware, the
 * flight log and the live values. {@code start} schedules the lesson's code as
 * a command, and the scheduler runs it inside {@code loopAfter()}, once every
 * loop.
 *
 * <p>Passes when: LessonsTest.l070_logsTheAButton,
 * LessonsTest.l070_saysWhenTheAButtonIsPressedAndReleased and
 * LessonsTest.l070_logsBothSticksSideways
 */
@TeleOp(name = "L070 Buttons", group = "Lessons")
// When on L070S020, see
// https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l070.html#l070s020
// for what to do here.
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L070ButtonsOpMode extends CorbelsTeleOp {

    // When on L070S020, see
    // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l070.html#l070s020
    // for what to do here.
//    StickShaper stickShaper = new StickShaper(0.05);

    // When on L070S040, see
    // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l070.html#l070s040
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
        Command driveFromTheSticksEveryLoop = Commands.infinite(this::driveFromTheSticks);
        Scheduler.schedule(driveFromTheSticksEveryLoop);
        startAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();
        loopAfter();
    }

    /** Once every loop, from PLAY until STOP: the scheduler runs it. */
    void driveFromTheSticks() {
        double leftStickY = gamepad1.left_stick_y;
        Tracker.publish("gamepad1/leftStickY", leftStickY);
        double rightStickY = gamepad1.right_stick_y;
        Tracker.publish("gamepad1/rightStickY", rightStickY);

        // When on L070S060, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l070.html#l070s060
        // for what to do here.

        double leftSpeed = -leftStickY;
        Tracker.publish("drivetrain/leftSpeed", leftSpeed);
        double rightSpeed = -rightStickY;
        Tracker.publish("drivetrain/rightSpeed", rightSpeed);

        // When on L070S020, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l070.html#l070s020
        // for what to do here.
//        double leftShapedSpeed = stickShaper.shaped(leftSpeed);
//        Tracker.publish("drivetrain/leftShapedSpeed", leftShapedSpeed);
//        double rightShapedSpeed = stickShaper.shaped(rightSpeed);
//        Tracker.publish("drivetrain/rightShapedSpeed", rightShapedSpeed);
//
//        hardware.frontLeft.setPower(leftShapedSpeed);
//        hardware.backLeft.setPower(leftShapedSpeed);
//
//        hardware.frontRight.setPower(rightShapedSpeed);
//        hardware.backRight.setPower(rightShapedSpeed);

        // When on L070S030, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l070.html#l070s030
        // for what to do here.

        // When on L070S050, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l070.html#l070s050
        // for what to do here.
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void stop() {
        stopAfter();
    }
}
