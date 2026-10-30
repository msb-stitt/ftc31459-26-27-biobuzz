package org.firstinspires.ftc.teamcode.lessons.l093;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L093: try the menu L093's classes make, between INIT and PLAY, with three
 * rows: Light, Speed and Color. It drives nothing. After PLAY, the Driver
 * Station shows the status line, the short names selected.
 *
 * <p>Passes when: LessonsTest.l093_eachRowStartsAtItsFirstChoice and
 * LessonsTest.l093_thePressesMoveTheMenu
 */
@TeleOp(name = "L093 Menu", group = "Lessons")
// When on L093S020, see
// https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l093.html#l093s020
// for what to do here.
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L093MenuOpMode extends CorbelsTeleOp {

    // When on L093S110, see
    // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l093.html#l093s110
    // for what to do here.

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init() {
        // When on L093S110, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l093.html#l093s110
        // for what to do here.
        initBefore();
        initAfter();
    }

    /** Again and again between INIT and PLAY. */
    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init_loop() {
        // When on L093S110, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l093.html#l093s110
        // for what to do here.
        initLoopAfter();
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
        // When on L093S110, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l093.html#l093s110
        // for what to do here.
        loopAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void stop() {
        stopAfter();
    }
}
