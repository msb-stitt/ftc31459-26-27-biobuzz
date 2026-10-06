package org.firstinspires.ftc.teamcode.lessons;

import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.RobotHardware;

/**
 * L195: the drivetrain that says what each wheel's encoder has counted.
 *
 * <p>{@link CorbelsMecanum} already sends powers to the four motors. This adds
 * the door the other way: four numbers back from the encoders, so a
 * measurement can compare what the wheels counted with what the Pinpoint saw.
 *
 * <p>Passes when: LessonsTest.l195_theTicksDoorReportsWhatEachEncoderCounted
 */
class L195TicksDriveTrain extends CorbelsMecanum {

    L195TicksDriveTrain(RobotHardware hardware) {
        super(hardware);
    }

    /**
     * What each encoder has counted, in ticks. {@code ticks[FL]} is the front
     * left wheel's count, and {@code FR}, {@code BL} and {@code BR} name the
     * other three.
     */
    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public int[] wheelTicks() {
        int[] ticks = new int[4];
        // When on L195S030, see
        // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l195.html#l195s030
        // for what to do here.
        return ticks;
    }
}
