package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.commands.Commands;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.StickShaper;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L160: hold the heading the driver left. Let go of the turn stick and the
 * robot keeps pointing where it was left.
 *
 * <p>Every OpMode has four methods. {@code init} runs once when INIT is
 * pressed, {@code start} once when PLAY is pressed, {@code loop} over and over
 * until STOP, and {@code stop} once at the end. The {@code Before} and
 * {@code After} calls are the robot's own housekeeping: the hardware, the
 * flight log and the live values. {@code start} schedules the lesson's code as
 * a command, and the scheduler runs it inside {@code loopAfter()}, once every
 * loop.
 *
 * <p>Passes when: LessonsTest.l160_theRobotHoldsItsHeadingWhenTheStickIsReleased
 */
@TeleOp(name = "L160 Heading Hold", group = "Lessons")
// When on L160S020, see
// https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l160.html#l160s020
// for what to do here.
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L160HeadingHoldOpMode extends CorbelsTeleOp {

    StickShaper stickShaper = new StickShaper(0.05);

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
        double leftStickX = gamepad1.left_stick_x;
        Tracker.publish("gamepad1/leftStickX", leftStickX);
        double rightStickX = gamepad1.right_stick_x;
        Tracker.publish("gamepad1/rightStickX", rightStickX);

        double fieldXSpeed = -leftStickY;
        fieldXSpeed = stickShaper.shaped(fieldXSpeed);
        Tracker.publish("drivetrain/fieldXSpeed", fieldXSpeed);
        double turnCcwSpeed = -rightStickX;
        turnCcwSpeed = stickShaper.shaped(turnCcwSpeed);
        Tracker.publish("drivetrain/turnCcwSpeed", turnCcwSpeed);
        // When on L160S040, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l160.html#l160s040
        // for what to do here.

        double fieldYSpeed = -leftStickX;
        fieldYSpeed = stickShaper.shaped(fieldYSpeed);
        Tracker.publish("drivetrain/fieldYSpeed", fieldYSpeed);

        double robotForwardXSpeed;
        double robotStrafeYSpeed;
        boolean robotRelative = gamepad1.right_bumper;
        Tracker.publish("drivetrain/robotRelative", robotRelative);
        if (robotRelative) {
            robotForwardXSpeed = -leftStickY;
            robotForwardXSpeed = stickShaper.shaped(robotForwardXSpeed);
            robotStrafeYSpeed = -leftStickX;
            robotStrafeYSpeed = stickShaper.shaped(robotStrafeYSpeed);
        } else {
            double heading_rad = follower.pose().heading();
            double sinHeading = Math.sin(heading_rad);
            double cosHeading = Math.cos(heading_rad);
            robotForwardXSpeed = fieldXSpeed * cosHeading + fieldYSpeed * sinHeading;
            robotStrafeYSpeed = -fieldXSpeed * sinHeading + fieldYSpeed * cosHeading;
        }
        Tracker.publish("drivetrain/robotForwardXSpeed", robotForwardXSpeed);
        Tracker.publish("drivetrain/robotStrafeYSpeed", robotStrafeYSpeed);
        drivetrain.sticks(robotForwardXSpeed, robotStrafeYSpeed, turnCcwSpeed);
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void stop() {
        stopAfter();
    }
}
