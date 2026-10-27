package org.firstinspires.ftc.teamcode.mytry;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L020: read the two sticks and write down what they say. Nothing moves.
 *
 * <p>Every OpMode has four methods. {@code init} runs once when INIT is
 * pressed, {@code start} once when PLAY is pressed, {@code loop} over and over
 * until STOP, and {@code stop} once at the end. The {@code Before} and
 * {@code After} calls are the robot's own housekeeping: the hardware, the
 * flight log and the live values. The code between them is the lesson's.
 *
 * <p>Passes when: LessonsTest.l020_logsBothSticksTheWayTheGamepadGivesThem
 */
@TeleOp(name = "L020 Sticks", group = "Lessons")
// When on L020S020, see
// https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l020.html#l020s020
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L020SticksOpMode extends CorbelsTeleOp {

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
    public void loop() {double leftStickY = gamepad1. left_stick_y;

        // When on L020S030, see
        // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l020.html#l020s030
        // for what to do here.
double leftStickyY = gamepad1.left_stick_y;
        // When on L020S040, see
        // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l020.html#l020s040
        // for what to do here.
        Tracker.publish("stick/leftY",leftStickY);
        // When on L020S050, see
        // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l020.html#l020s050
        // for what to do here.
        double rightStickY = gamepad1.right_stick_y;
        Tracker.publish("stick/rightY", rightStickY);
        loopAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void stop() {
        stopAfter();
    }
}
