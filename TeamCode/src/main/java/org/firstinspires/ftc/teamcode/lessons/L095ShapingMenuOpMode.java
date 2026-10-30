package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.commands.Commands;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;
// When on L095S020, see
// https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l095.html#l095s020
// for what to do here.
//import org.firstinspires.ftc.teamcode.mytry.l065.StickShaper;
//import org.firstinspires.ftc.teamcode.mytry.l093.DriverStationMenu;
//import org.firstinspires.ftc.teamcode.mytry.l093.MenuItem;
//import org.firstinspires.ftc.teamcode.mytry.l093.MenuRow;
//import org.firstinspires.ftc.teamcode.mytry.l093.OptionMenu;

/**
 * L095: L090's mecanum drive, with L093's menu choosing the deadband and the
 * shape of L065's StickShaper between INIT and PLAY.
 *
 * <p>Passes when: LessonsTest.l095_theMenuStartsAtTheDefault and
 * LessonsTest.l095_theMenuChoosesTheBandAndTheShape
 */
@TeleOp(name = "L095 Shaping Menu", group = "Lessons")
// When on L095S020, see
// https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l095.html#l095s020
// for what to do here.
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L095ShapingMenuOpMode extends CorbelsTeleOp {

    // When on L095S020, see
    // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l095.html#l095s020
    // for what to do here.
//    StickShaper stickShaper = new StickShaper(0.05);

    // When on L095S030, see
    // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l095.html#l095s030
    // for what to do here.

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init() {
        initBefore();

        hardware.frontLeft.setDirection(hardware.mecanumConfig.frontLeftDirection.get());
        hardware.frontRight.setDirection(hardware.mecanumConfig.frontRightDirection.get());
        hardware.backLeft.setDirection(hardware.mecanumConfig.backLeftDirection.get());
        hardware.backRight.setDirection(hardware.mecanumConfig.backRightDirection.get());

        // When on L095S030, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l095.html#l095s030
        // for what to do here.
        initAfter();
    }

    /** Again and again between INIT and PLAY. */
    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init_loop() {
        // When on L095S030, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l095.html#l095s030
        // for what to do here.
        initLoopAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void start() {
        startBefore();
        // When on L095S040, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l095.html#l095s040
        // for what to do here.
        Command driveFromTheSticksEveryLoop = Commands.infinite(this::driveFromTheSticks);
        Scheduler.schedule(driveFromTheSticksEveryLoop);
        startAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();
        // When on L095S030, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l095.html#l095s030
        // for what to do here.
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
        // When on L095S020, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l095.html#l095s020
        // for what to do here.
//        forwardXSpeed = stickShaper.shaped(forwardXSpeed);
        Tracker.publish("drivetrain/forwardXSpeed", forwardXSpeed);
        double turnCcwSpeed = -rightStickX;
        // When on L095S020, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l095.html#l095s020
        // for what to do here.
//        turnCcwSpeed = stickShaper.shaped(turnCcwSpeed);
        Tracker.publish("drivetrain/turnCcwSpeed", turnCcwSpeed);
        double strafeYSpeed = -leftStickX;
        // When on L095S020, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l095.html#l095s020
        // for what to do here.
//        strafeYSpeed = stickShaper.shaped(strafeYSpeed);
        Tracker.publish("drivetrain/strafeYSpeed", strafeYSpeed);

        double fl_mixedSpeed = forwardXSpeed - strafeYSpeed - turnCcwSpeed;
        double fr_mixedSpeed = forwardXSpeed + strafeYSpeed + turnCcwSpeed;
        double bl_mixedSpeed = forwardXSpeed + strafeYSpeed - turnCcwSpeed;
        double br_mixedSpeed = forwardXSpeed - strafeYSpeed + turnCcwSpeed;
        double fl_size = Math.abs(fl_mixedSpeed);
        double fr_size = Math.abs(fr_mixedSpeed);
        double bl_size = Math.abs(bl_mixedSpeed);
        double br_size = Math.abs(br_mixedSpeed);
        double biggest = Math.max(fl_size, fr_size);
        biggest = Math.max(biggest, bl_size);
        biggest = Math.max(biggest, br_size);
        double biggestOrOne = Math.max(1.0, biggest);
        double fl_speed = fl_mixedSpeed / biggestOrOne;
        double fr_speed = fr_mixedSpeed / biggestOrOne;
        double bl_speed = bl_mixedSpeed / biggestOrOne;
        double br_speed = br_mixedSpeed / biggestOrOne;
        Tracker.publish("drivetrain/fl_speed", fl_speed);
        Tracker.publish("drivetrain/fr_speed", fr_speed);
        Tracker.publish("drivetrain/bl_speed", bl_speed);
        Tracker.publish("drivetrain/br_speed", br_speed);

        hardware.frontLeft.setPower(fl_speed);
        hardware.frontRight.setPower(fr_speed);
        hardware.backLeft.setPower(bl_speed);
        hardware.backRight.setPower(br_speed);
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void stop() {
        stopAfter();
    }
}
