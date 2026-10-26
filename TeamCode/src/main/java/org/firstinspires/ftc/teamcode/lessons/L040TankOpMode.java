package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.commands.Commands;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L040: tank drive. The left stick drives the left wheels and the right stick
 * the right wheels.
 *
 * <p>Every OpMode has four methods. {@code init} runs once when INIT is
 * pressed, {@code start} once when PLAY is pressed, {@code loop} over and over
 * until STOP, and {@code stop} once at the end. The {@code Before} and
 * {@code After} calls are the robot's own housekeeping: the hardware, the
 * flight log and the live values. {@code start} schedules the lesson's code as
 * a command, and the scheduler runs it inside {@code loopAfter()}, once every
 * loop.
 *
 * <p>Passes when: LessonsTest.l040_eachSpeedDrivesItsOwnSide and
 * LessonsTest.l040_bothSticksForwardDriveTheSimulatedRobotForward
 */
@TeleOp(name = "L040 Tank", group = "Lessons")
// When on L040S020, see
// https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l040.html#l040s020
// for what to do here.
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L040TankOpMode extends CorbelsTeleOp {

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init() {
        initBefore();
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

        // When on L040S030, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l040.html#l040s030
        // for what to do here.

        // When on L040S040, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l040.html#l040s040
        // for what to do here.
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void stop() {
        stopAfter();
    }
}
