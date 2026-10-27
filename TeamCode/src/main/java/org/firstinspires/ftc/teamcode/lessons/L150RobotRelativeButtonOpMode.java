package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.commands.Commands;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L150: give the driver both. The sticks mean the field, until the right
 * bumper is held. While it is held, they mean the robot.
 *
 * <p>Every OpMode has four methods. {@code init} runs once when INIT is
 * pressed, {@code start} once when PLAY is pressed, {@code loop} over and over
 * until STOP, and {@code stop} once at the end. The {@code Before} and
 * {@code After} calls are the robot's own housekeeping: the hardware, the
 * flight log and the live values. {@code start} schedules the lesson's code as
 * a command, and the scheduler runs it inside {@code loopAfter()}, once every
 * loop.
 *
 * <p>Passes when: LessonsTest.l150_theBumperSwitchesToRobotRelative
 */
@TeleOp(name = "L150 Robot Relative Button", group = "Lessons")
// When on L150S020, see
// https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l150.html#l150s020
// for what to do here.
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L150RobotRelativeButtonOpMode extends CorbelsTeleOp {

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

        double bandToSetToZero = 0.05;
        double fieldXSpeed = -leftStickY;
        fieldXSpeed = speedShaped(fieldXSpeed, bandToSetToZero);
        Tracker.publish("drivetrain/fieldXSpeed", fieldXSpeed);
        double turnCcwSpeed = -rightStickX;
        turnCcwSpeed = speedShaped(turnCcwSpeed, bandToSetToZero);
        Tracker.publish("drivetrain/turnCcwSpeed", turnCcwSpeed);

        double fieldYSpeed = -leftStickX;
        fieldYSpeed = speedShaped(fieldYSpeed, bandToSetToZero);
        Tracker.publish("drivetrain/fieldYSpeed", fieldYSpeed);

        double robotForwardXSpeed;
        double robotStrafeYSpeed;
        // When on L150S030, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l150.html#l150s030
        // for what to do here.
        double heading_rad = follower.pose().heading();
        double sinHeading = Math.sin(heading_rad);
        double cosHeading = Math.cos(heading_rad);
        robotForwardXSpeed = fieldXSpeed * cosHeading + fieldYSpeed * sinHeading;
        robotStrafeYSpeed = -fieldXSpeed * sinHeading + fieldYSpeed * cosHeading;
        Tracker.publish("drivetrain/robotForwardXSpeed", robotForwardXSpeed);
        Tracker.publish("drivetrain/robotStrafeYSpeed", robotStrafeYSpeed);
        drivetrain.sticks(robotForwardXSpeed, robotStrafeYSpeed, turnCcwSpeed);
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
        double alwaysPositiveValue = Math.abs(value);
        double valueSquaredKeepingTheSign = value * alwaysPositiveValue;
        return valueSquaredKeepingTheSign;
    }

    double speedShaped(double value, double band) {
        double deadbanded = deadband(value, band);
        double shapedSpeed = signedSquared(deadbanded);
        return shapedSpeed;
    }
}
