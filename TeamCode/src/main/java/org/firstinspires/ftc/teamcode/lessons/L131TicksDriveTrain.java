package org.firstinspires.ftc.teamcode.lessons;

import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.RobotHardware;

/**
 * L131: the drivetrain that says what each wheel's encoder has counted.
 *
 * <p>{@link CorbelsMecanum} already sends speeds to the four motors. This adds
 * the door the other way: four numbers back from the encoders, so a
 * measurement can compare what the wheels counted with what the Pinpoint saw.
 *
 * <p>Passes when: LessonsTest.l131_theTicksDoorReportsWhatEachEncoderCounted
 */
class L131TicksDriveTrain extends CorbelsMecanum {

    L131TicksDriveTrain(RobotHardware hardware) {
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
        // When on L131S030, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l131.html#l131s030
        // for what to do here.
        return ticks;
    }
}
