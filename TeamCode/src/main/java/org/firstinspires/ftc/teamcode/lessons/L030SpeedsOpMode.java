package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L030: turn each stick into a wheel speed, forward positive. Nothing moves.
 *
 * <p>Every OpMode has four methods. {@code init} runs once when INIT is
 * pressed, {@code start} once when PLAY is pressed, {@code loop} over and over
 * until STOP, and {@code stop} once at the end. The {@code Before} and
 * {@code After} calls are the robot's own housekeeping: the hardware, the
 * flight log and the live values. The code between them is the lesson's.
 *
 * <p>Passes when: LessonsTest.l030_eachSpeedIsItsStickTurnedRound
 */
@TeleOp(name = "L030 Speeds", group = "Lessons")
// When on L030S020, see
// https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l030.html#l030s020
// for what to do here.
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L030SpeedsOpMode extends CorbelsTeleOp {

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
        startAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();

        double leftStickY = gamepad1.left_stick_y;
        Tracker.publish("stick/leftY", leftStickY);
        double rightStickY = gamepad1.right_stick_y;
        Tracker.publish("stick/rightY", rightStickY);

        // When on L030S030, see
        // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l030.html#l030s030
        // for what to do here.

        // When on L030S040, see
        // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l030.html#l030s040
        // for what to do here.

        loopAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void stop() {
        stopAfter();
    }
}
