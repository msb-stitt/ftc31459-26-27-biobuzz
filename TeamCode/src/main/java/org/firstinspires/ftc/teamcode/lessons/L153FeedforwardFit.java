package org.firstinspires.ftc.teamcode.lessons;

/**
 * L153: the straight line closest to some points of volts against speed.
 *
 * <p>Each steady run gives one point: the speed the robot settled at, and the
 * volts it took. A straight line through the points crosses zero speed at kS,
 * the volts it takes to start moving, and climbs kV volts for each unit of
 * speed. The points never sit exactly on a line, so this finds the line that
 * misses them least, called a least-squares fit.
 *
 * <p>Passes when: L153FeedforwardFitTest (all of it)
 */
class L153FeedforwardFit {

    /** How many points, and their sums, which is all the fit needs. */
    int count;
    double sumSpeed;
    double sumVolts;
    double sumSpeedSquared;
    double sumSpeedTimesVolts;

    /** One steady run: the volts it took, and the speed it settled at. */
    void add(double volts, double speed) {
        // When on L153S030, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l153.html#l153s030
        // for what to do here.
    }

    /** How many more volts each unit of speed takes: the line's slope. */
    double kV() {
        double kV = 0.0;
        // When on L153S030, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l153.html#l153s030
        // for what to do here.
        return kV;
    }

    /** The volts it takes to start moving: where the line crosses zero speed. */
    double kS() {
        double kS = 0.0;
        // When on L153S040, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l153.html#l153s040
        // for what to do here.
        return kS;
    }
}
