package org.firstinspires.ftc.teamcode.base;

import io.github.mikestitt.corbelsflightlog.FlightLog;

/**
 * A Pedro pose in AdvantageScope's field frame: metres from the centre of the
 * field, turned to match the season's field image.
 *
 * <p><b>This file is a copy, and it goes away.</b> The same arithmetic is
 * {@code FlightLog.fieldPose} in corbelsflightlog, which is written but not
 * released. Delete this class and call the library when it is: two copies of
 * this conversion is one too many, and the mistake the second copy invites is a
 * robot drawn a quarter turn from where it is. That happened once already
 * inside the library, which is why it has a test called
 * {@code everyQuarterTurnPutsTheThreeDPoseWhereTheTwoDOneIs}.
 *
 * <p>Only the arithmetic is copied. How far the field is turned is read from
 * {@link FlightLog#fieldQuarterTurns}, so the picture and the logs cannot
 * disagree about that.
 *
 * <p>Passes when: FieldPoseTest
 */
final class FieldPose {

    /** Inches from a field corner to the middle of the field. */
    private static final double FIELD_CENTER_IN = 72.0;

    private static final double METERS_PER_INCH = 0.0254;

    private FieldPose() {
    }

    /**
     * Pedro's inches from a corner and radians counter-clockwise, as
     * {@code {x, y, heading_rad}} in metres from the centre.
     */
    static double[] of(double x_in, double y_in, double heading_rad) {
        double x = (x_in - FIELD_CENTER_IN) * METERS_PER_INCH;
        double y = (y_in - FIELD_CENTER_IN) * METERS_PER_INCH;
        int turns = Math.floorMod(FlightLog.fieldQuarterTurns, 4);
        double fx;
        double fy;
        switch (turns) {
            case 1:  fx = -y; fy = x;  break;
            case 2:  fx = -x; fy = -y; break;
            case 3:  fx = y;  fy = -x; break;
            default: fx = x;  fy = y;  break;
        }
        double fieldHeading_rad = (heading_rad + turns * Math.PI / 2) % (2 * Math.PI);
        if (fieldHeading_rad < 0) fieldHeading_rad += 2 * Math.PI;
        // + 0.0 turns -0.0 into 0.0
        return new double[]{fx + 0.0, fy + 0.0, fieldHeading_rad};
    }
}
