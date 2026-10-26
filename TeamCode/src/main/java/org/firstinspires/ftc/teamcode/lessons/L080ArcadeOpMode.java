package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.commands.Commands;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L080: arcade drive. The left stick drives forward and back, the right stick
 * turns, and a request for more speed than a motor has is scaled down, not cut
 * off.
 *
 * <p>Every OpMode has four methods. {@code init} runs once when INIT is
 * pressed, {@code start} once when PLAY is pressed, {@code loop} over and over
 * until STOP, and {@code stop} once at the end. The {@code Before} and
 * {@code After} calls are the robot's own housekeeping: the hardware, the
 * flight log and the live values. {@code start} schedules the lesson's code as
 * a command, and the scheduler runs it inside {@code loopAfter()}, once every
 * loop.
 *
 * <p>Passes when: LessonsTest.l080_theLeftStickDrivesBothSides,
 * LessonsTest.l080_theRightStickTurns and
 * LessonsTest.l080_tooMuchIsScaledNotCut
 */
@TeleOp(name = "L080 Arcade", group = "Lessons")
// When on L080S020, see
// https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l080.html#l080s020
// for what to do here.
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L080ArcadeOpMode extends CorbelsTeleOp {

    boolean previousButtonA;

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

        double leftStickX = gamepad1.left_stick_x;
        Tracker.publish("gamepad1/leftStickX", leftStickX);
        double rightStickX = gamepad1.right_stick_x;
        Tracker.publish("gamepad1/rightStickX", rightStickX);

        // When on L080S040, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l080.html#l080s040
        // for what to do here.

        // When on L080S050, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l080.html#l080s050
        // for what to do here.

        boolean buttonA = gamepad1.a;
        Tracker.publish("gamepad1/buttonA", buttonA);

        if (buttonA != previousButtonA) {
            if (buttonA) {
                Tracker.publish("gamepad1/buttonAEvent", "A pressed");
            } else {
                Tracker.publish("gamepad1/buttonAEvent", "A released");
            }
        }
        previousButtonA = buttonA;
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
        double squaredWithItsSign = value * Math.abs(value);
        return squaredWithItsSign;
    }

    // When on L080S030, see
    // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l080.html#l080s030
    // for what to do here.
}
