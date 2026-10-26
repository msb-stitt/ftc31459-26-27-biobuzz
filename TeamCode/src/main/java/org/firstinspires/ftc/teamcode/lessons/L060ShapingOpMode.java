package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.commands.Commands;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L060: tank drive that ignores a stick that was let go, and makes a gentle
 * push mean gently.
 *
 * <p>Every OpMode has four methods. {@code init} runs once when INIT is
 * pressed, {@code start} once when PLAY is pressed, {@code loop} over and over
 * until STOP, and {@code stop} once at the end. The {@code Before} and
 * {@code After} calls are the robot's own housekeeping: the hardware, the
 * flight log and the live values. {@code start} schedules the lesson's code as
 * a command, and the scheduler runs it inside {@code loopAfter()}, once every
 * loop.
 *
 * <p>Passes when: LessonsTest.l060_aLetGoStickCountsAsCentred and
 * LessonsTest.l060_halfAStickIsAQuarterOfTheSpeed
 */
@TeleOp(name = "L060 Shaping", group = "Lessons")
// When on L060S020, see
// https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l060.html#l060s020
// for what to do here.
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L060ShapingOpMode extends CorbelsTeleOp {

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

        double leftSpeed = -leftStickY;
        Tracker.publish("drivetrain/leftSpeed", leftSpeed);
        double rightSpeed = -rightStickY;
        Tracker.publish("drivetrain/rightSpeed", rightSpeed);

        // When on L060S040, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l060.html#l060s040
        // for what to do here.

        hardware.frontLeft.setPower(leftSpeed);
        hardware.backLeft.setPower(leftSpeed);

        hardware.frontRight.setPower(rightSpeed);
        hardware.backRight.setPower(rightSpeed);
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void stop() {
        stopAfter();
    }

    // When on L060S030, see
    // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l060.html#l060s030
    // for what to do here.

    // When on L060S050, see
    // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l060.html#l060s050
    // for what to do here.
}
