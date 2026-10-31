package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.commands.Commands;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;
// When on L090S020, see
// https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l090.html#l090s020
// for what to do here.
//import org.firstinspires.ftc.teamcode.mytry.l065.StickShaper;

/**
 * L090: mecanum drive. The left stick drives and slides, the right stick
 * turns, and each wheel gets its own speed.
 *
 * <p>Every OpMode has four methods. {@code init} runs once when INIT is
 * pressed, {@code start} once when PLAY is pressed, {@code loop} over and over
 * until STOP, and {@code stop} once at the end. The {@code Before} and
 * {@code After} calls are the robot's own housekeeping: the hardware, the
 * flight log and the live values. {@code start} schedules the lesson's code as
 * a command, and the scheduler runs it inside {@code loopAfter()}, once every
 * loop.
 *
 * <p>Passes when: LessonsTest.l090_theLeftStickSlidesLeft,
 * LessonsTest.l090_forwardAndTurnStillWork and
 * LessonsTest.l090_aCornerOfTheStickIsScaled
 */
@TeleOp(name = "L090 Mecanum", group = "Lessons")
// When on L090S020, see
// https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l090.html#l090s020
// for what to do here.
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L090MecanumOpMode extends CorbelsTeleOp {

    // When on L090S020, see
    // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l090.html#l090s020
    // for what to do here.
//    StickShaper stickShaper = new StickShaper(0.05);

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
        double leftStickX = gamepad1.left_stick_x;
        Tracker.publish("gamepad1/leftStickX", leftStickX);
        double rightStickX = gamepad1.right_stick_x;
        Tracker.publish("gamepad1/rightStickX", rightStickX);

        double forwardXSpeed = -leftStickY;
        // When on L090S020, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l090.html#l090s020
        // for what to do here.
//        forwardXSpeed = stickShaper.shaped(forwardXSpeed);
        Tracker.publish("drivetrain/forwardXSpeed", forwardXSpeed);
        double turnCcwSpeed = -rightStickX;
        // When on L090S020, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l090.html#l090s020
        // for what to do here.
//        turnCcwSpeed = stickShaper.shaped(turnCcwSpeed);
        Tracker.publish("drivetrain/turnCcwSpeed", turnCcwSpeed);

        // When on L090S030, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l090.html#l090s030
        // for what to do here.

        // When on L090S040, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l090.html#l090s040
        // for what to do here.
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void stop() {
        stopAfter();
    }
}
