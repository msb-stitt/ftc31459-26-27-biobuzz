package org.firstinspires.ftc.teamcode.lessons;

import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.RobotHardware;

/**
 * L190: the drivetrain that asks the wheels for a speed.
 *
 * <p>{@link CorbelsMecanum} already mixes, scales and sends powers. This adds
 * the one door that takes a speed in inches per second and gets it, two ways at
 * once: a feedforward guess at the power the speed needs, plus a feedback
 * correction from what the encoders measure.
 *
 * <p>Passes when: LessonsTest.l190_theSticksCommandASpeedAndTheWheelsAreCorrectedTowardsIt
 * and LessonsTest.l190_whenTheWheelsAreUpToSpeedOnlyTheFeedforwardRemains
 */
class L190SpeedDriveTrain extends CorbelsMecanum {

    L190SpeedDriveTrain(RobotHardware hardware) {
        super(hardware);
    }

    /**
     * Drives each wheel at the speed given, in inches per second.
     * {@code wanted[FL]} is the front left wheel's speed, and {@code FR},
     * {@code BL} and {@code BR} name the other three.
     */
    // public: CorbelsDriveTrain's own setCommandedWheelSpeeds is public, so this one has to be too.
    @Override
    public void setCommandedWheelSpeeds(double frontLeftInPerS, double frontRightInPerS,
                                        double backLeftInPerS, double backRightInPerS) {
        double[] wanted = new double[4];
        wanted[FL] = frontLeftInPerS;
        wanted[FR] = frontRightInPerS;
        wanted[BL] = backLeftInPerS;
        wanted[BR] = backRightInPerS;

        // When on L190S060, see
        // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l190.html#l190s060
        // for what to do here.
    }
}
