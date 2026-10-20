package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.commands.Commands;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;

/**
 * L020: read the two sticks and write down what they say. Nothing moves.
 *
 * <p>Every OpMode has four methods. {@code init} runs once when INIT is
 * pressed, {@code start} once when PLAY is pressed, {@code loop} over and over
 * until STOP, and {@code stop} once at the end. The {@code Before} and
 * {@code After} calls are the robot's own housekeeping: the hardware, the
 * flight log and the live values. {@code start} schedules the lesson's code as
 * a command, and the scheduler runs it inside {@code loopAfter()}, once every
 * loop.
 *
 * <p>Passes when: LessonsTest.l020_logsBothSticksTheWayTheGamepadGivesThem
 */
@TeleOp(name = "L020 Sticks", group = "Lessons")
// When on L020S020, see
// https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l020.html#l020s020
// for what to do here.
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
        Command readTheSticksEveryLoop = Commands.infinite(this::readTheSticks);
        Scheduler.schedule(readTheSticksEveryLoop);
        startAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();
        loopAfter();
    }

    /** Once every loop, from PLAY until STOP: the scheduler runs it. */
    void readTheSticks() {
        // When on L020S030, see
        // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l020.html#l020s030
        // for what to do here.

        // When on L020S040, see
        // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l020.html#l020s040
        // for what to do here.

        // When on L020S050, see
        // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l020.html#l020s050
        // for what to do here.
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void stop() {
        stopAfter();
    }
}
