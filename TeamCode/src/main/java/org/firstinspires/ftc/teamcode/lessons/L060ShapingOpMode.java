package org.firstinspires.ftc.teamcode.lessons;

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
 * flight log and the live values. The code between them is the lesson's.
 *
 * <p>Passes when: LessonsTest.l060_aLetGoStickCountsAsCentred and
 * LessonsTest.l060_halfAStickIsAQuarterOfThePower
 */
@TeleOp(name = "L060 Shaping", group = "Lessons")
// When on L060S020, see
// https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l060.html#l060s020
// for what to do here.
@Disabled
// public: the robot only runs an OpMode that is public.
public class L060ShapingOpMode extends CorbelsTeleOp {

    // public: OpMode's own init is public, so this one has to be too.
    @Override
    public void init() {
        initBefore();

        hardware.frontLeft.setDirection(hardware.mecanumConfig.frontLeftDirection.get());
        hardware.frontRight.setDirection(hardware.mecanumConfig.frontRightDirection.get());
        hardware.backLeft.setDirection(hardware.mecanumConfig.backLeftDirection.get());
        hardware.backRight.setDirection(hardware.mecanumConfig.backRightDirection.get());

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

        double leftSpeed = -leftStickY;
        Tracker.publish("speed/left", leftSpeed);
        double rightSpeed = -rightStickY;
        Tracker.publish("speed/right", rightSpeed);

        // When on L060S040, see
        // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l060.html#l060s040
        // for what to do here.

        hardware.frontLeft.setPower(leftSpeed);
        hardware.backLeft.setPower(leftSpeed);

        hardware.frontRight.setPower(rightSpeed);
        hardware.backRight.setPower(rightSpeed);

        loopAfter();
    }

    // public: OpMode's own stop is public, so this one has to be too.
    @Override
    public void stop() {
        stopAfter();
    }

    // When on L060S030, see
    // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l060.html#l060s030
    // for what to do here.

    // When on L060S050, see
    // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l060.html#l060s050
    // for what to do here.
}
