package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.localization.Localizer;
import com.pedropathing.localization.MotionState;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Twist;

import org.firstinspires.ftc.teamcode.base.odometry.WheelSource;

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
    double x;
    double y;
    /** Added to the IMU's heading, so that setPose can say which way the robot faces. */
    double headingOffset;

    /** What the wheels and the IMU read last loop. */
    double[] lastWheels;
    double lastRawHeading;

    L120EncoderLocalizer(WheelSource source) {
        this.source = source;
    }

    // public: Pedro's Localizer says update is public, so this one has to be too.
    @Override
    public void update() {
        double[] wheels = source.wheelInches();
        double rawHeading = source.headingRadians();

        // When on L120S040, see
        // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l120.html#l120s040
        // for what to do here.

        // When on L120S050, see
        // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l120.html#l120s050
        // for what to do here.

        lastWheels = wheels.clone();
        lastRawHeading = rawHeading;
    }

    /**
     * Where the robot thinks it is. It only runs beside the robot's own
     * localizer, which logs where it is and never how fast, so the speed it
     * reports is zero.
     */
    // public: Pedro's Localizer says state is public, so this one has to be too.
    @Override
    public MotionState state() {
        return MotionState.ofTwist(
                poses.of(x, y, lastRawHeading + headingOffset), new Twist(0, 0, 0));
    }

    // public: Pedro's Localizer says setPose is public, so this one has to be too.
    @Override
    public void setPose(Pose pose) {
        x = pose.x();
        y = pose.y();
        headingOffset = pose.heading() - lastRawHeading;
    }

    // public: Pedro's Localizer says reset is public, so this one has to be too.
    @Override
    public void reset() {
        x = 0;
        y = 0;
        headingOffset = -lastRawHeading;
        lastWheels = null;
    }
}
