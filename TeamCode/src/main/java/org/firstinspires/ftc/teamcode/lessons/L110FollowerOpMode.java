package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.commands.Commands;
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
 * flight log and the live values. {@code start} schedules the lesson's code as
 * a command, and the scheduler runs it inside {@code loopAfter()}, once every
 * loop.
 *
 * <p>Passes when: LessonsTest.l110_theFollowerDrivesTheSticks and
 * LessonsTest.l110_aCornerOfTheStickIsStillScaled
 */
@TeleOp(name = "L110 Follower", group = "Lessons")
// When on L110S020, see
// https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l110.html#l110s020
// for what to do here.
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L110FollowerOpMode extends CorbelsTeleOp {

    // When on L110S040, see
    // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l110.html#l110s040
    // for what to do here.

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init() {
        initBefore();

        // When on L110S050, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l110.html#l110s050
        // for what to do here.

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
        double leftStickX = gamepad1.left_stick_x;
        Tracker.publish("gamepad1/leftStickX", leftStickX);
        double rightStickX = gamepad1.right_stick_x;
        Tracker.publish("gamepad1/rightStickX", rightStickX);

        double bandToSetToZero = 0.05;
        double forwardXSpeed = -leftStickY;
        forwardXSpeed = speedShaped(forwardXSpeed, bandToSetToZero);
        Tracker.publish("drivetrain/forwardXSpeed", forwardXSpeed);
        double turnCcwSpeed = -rightStickX;
        turnCcwSpeed = speedShaped(turnCcwSpeed, bandToSetToZero);
        Tracker.publish("drivetrain/turnCcwSpeed", turnCcwSpeed);

        double strafeYSpeed = -leftStickX;
        strafeYSpeed = speedShaped(strafeYSpeed, bandToSetToZero);
        Tracker.publish("drivetrain/strafeYSpeed", strafeYSpeed);

        // When on L110S060, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l110.html#l110s060
        // for what to do here.
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
