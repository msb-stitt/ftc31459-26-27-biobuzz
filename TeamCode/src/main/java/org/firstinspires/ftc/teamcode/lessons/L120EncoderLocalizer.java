package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.localization.Localizer;
import com.pedropathing.localization.MotionState;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Twist;

import org.firstinspires.ftc.teamcode.base.odometry.WheelSource;

import static org.firstinspires.ftc.teamcode.base.CorbelsDriveTrain.*;

/**
 * L120: work out where the robot is from the drive wheels and the IMU.
 *
 * <p>Pedro has localizers for the Pinpoint and for dead wheels, but not for
 * drive encoders, so this one is yours to write. Every loop it asks how far
 * each wheel rolled since last time, turns that into forward and left for the
 * robot, turns that pair to point the way the robot faces, and adds it to where
 * it thought it was. That is dead reckoning: no wheel ever admits to slipping,
 * so the error only grows.
 *
 * <p>Wheel order is front left, front right, back left, back right, in inches.
 *
 * <p>Passes when: L120EncoderLocalizerTest (all of it)
 */
class L120EncoderLocalizer implements Localizer {

    PoseFactory poses = PoseFactory.radians();

    WheelSource source;

    /** Where the robot thinks it is, in inches. */
    double x_in;
    double y_in;
    /** Added to the IMU's heading, so that setPose can say which way the robot faces. */
    double headingOffset_rad;

    /** What the wheels and the IMU read last loop. */
    double[] lastWheels_in;
    double lastRawHeading_rad;

    L120EncoderLocalizer(WheelSource source) {
        this.source = source;
    }

    // 'public' to match Pedro's Localizer interface. 'public' allows access from any code.
    @Override
    public void update() {
        double[] wheels_in = source.wheelInches();
        double rawHeading_rad = source.headingRadians();

        // When on L120S040, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l120.html#l120s040
        // for what to do here.

        // When on L120S050, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l120.html#l120s050
        // for what to do here.

        lastWheels_in = wheels_in.clone();
        lastRawHeading_rad = rawHeading_rad;
    }

    /**
     * Where the robot thinks it is. It only runs beside the robot's own
     * localizer, which logs where it is and never how fast, so the speed it
     * reports is zero.
     */
    // 'public' to match Pedro's Localizer interface. 'public' allows access from any code.
    @Override
    public MotionState state() {
        Pose where = poses.of(x_in, y_in, lastRawHeading_rad + headingOffset_rad);
        Twist standingStill = new Twist(0.0, 0.0, 0.0);
        MotionState state = MotionState.ofTwist(where, standingStill);
        return state;
    }

    // 'public' to match Pedro's Localizer interface. 'public' allows access from any code.
    @Override
    public void setPose(Pose pose) {
        x_in = pose.x();
        y_in = pose.y();
        headingOffset_rad = pose.heading() - lastRawHeading_rad;
    }

    // 'public' to match Pedro's Localizer interface. 'public' allows access from any code.
    @Override
    public void reset() {
        x_in = 0.0;
        y_in = 0.0;
        headingOffset_rad = -lastRawHeading_rad;
        lastWheels_in = null;
    }
}
