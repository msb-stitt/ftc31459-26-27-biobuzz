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
 * L122: drive around while three localizers disagree. Your localizer from L113
 * reads the four drive wheels, your localizer from L120 reads the Pinpoint's two
 * pods, and the robot's own reads the Pinpoint's own answer. Each is published
 * under Localizer/, so they can be graphed against each other and checked with
 * a tape measure.
 *
 * <p>Every OpMode has four methods. {@code init} runs once when INIT is
 * pressed, {@code start} once when PLAY is pressed, {@code loop} over and over
 * until STOP, and {@code stop} once at the end. The {@code Before} and
 * {@code After} calls are the robot's own housekeeping: the hardware, the
 * flight log and the live values. {@code start} schedules the lesson's code as
 * a command, and the scheduler runs it inside {@code loopAfter()}, once every
 * loop.
 *
 * <p>Passes when: LessonsTest.l122_yourLocalizersAreLogged
 */
@TeleOp(name = "L122 Compare Localizers", group = "Lessons")
// When on L122S020, see
// https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l122.html#l122s020
// for what to do here.
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L122CompareLocalizersOpMode extends CorbelsTeleOp {

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

        double forwardXSpeed = -leftStickY;
        forwardXSpeed = stickShaper.shaped(forwardXSpeed);
        Tracker.publish("drivetrain/forwardXSpeed", forwardXSpeed);
        double turnCcwSpeed = -rightStickX;
        turnCcwSpeed = stickShaper.shaped(turnCcwSpeed);
        Tracker.publish("drivetrain/turnCcwSpeed", turnCcwSpeed);

        double strafeYSpeed = -leftStickX;
        strafeYSpeed = stickShaper.shaped(strafeYSpeed);
        Tracker.publish("drivetrain/strafeYSpeed", strafeYSpeed);

        drivetrain.sticks(forwardXSpeed, strafeYSpeed, turnCcwSpeed);
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void shadowLocalizers() {
        // When on L122S030, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l122.html#l122s030
        // for what to do here.
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void stop() {
        stopAfter();
    }
}
