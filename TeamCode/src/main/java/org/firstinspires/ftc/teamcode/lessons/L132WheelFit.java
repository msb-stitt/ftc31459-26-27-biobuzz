package org.firstinspires.ftc.teamcode.lessons;

import static org.firstinspires.ftc.teamcode.base.CorbelsDriveTrain.*;

/**
 * L132: what the wheels rolled beside what the Pinpoint says the robot did,
 * added up over a run.
 *
 * <p>A mecanum wheel's rollers push at an angle, α, from the wheel's axle.
 * Sliding the robot one inch sideways rolls each wheel cot α inches; at 45°,
 * cot α is 1. Turning the robot one radian rolls each wheel R inches, the turn
 * radius. Add up the wheels' slide and the robot's slide over a run, and the
 * one divided by the other is cot α. Turning gives R the same way.
 *
 * <p>Wheel order is front left, front right, back left, back right, in inches.
 *
 * <p>Passes when: L132WheelFitTest (all of it)
 */
class L132WheelFit {

    /** Added up over the run: the wheels' slide and turn, in inches of rim. */
    double wheelStrafe_in;
    double wheelTurn_in;
    /** Added up over the run: the robot's slide and turn, as the Pinpoint saw them. */
    double robotStrafe_in;
    double robotTurn_rad;

    /** One loop's roll of each wheel, in inches. */
    void addWheels(double[] wheelSteps_in) {
        // When on L132S090, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s090
        // for what to do here.

        // When on L132S120, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s120
        // for what to do here.
    }

    /** One loop's move of the robot, the robot's way, as the Pinpoint saw it. */
    void addRobot(double strafe_in, double turn_rad) {
        // When on L132S090, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s090
        // for what to do here.

        // When on L132S120, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s120
        // for what to do here.
    }

    /** How far the wheels rolled for each inch the robot slid. */
    double cotAlpha() {
        double cotAlpha = 0.0;
        // When on L132S090, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s090
        // for what to do here.
        return cotAlpha;
    }

    /** How far the wheels rolled for each radian the robot turned. */
    double turnRadius_in() {
        double turnRadius_in = 0.0;
        // When on L132S120, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s120
        // for what to do here.
        return turnRadius_in;
    }
}
