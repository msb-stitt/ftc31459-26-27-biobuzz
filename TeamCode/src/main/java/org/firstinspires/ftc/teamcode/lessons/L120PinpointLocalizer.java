package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.localization.Localizer;
import com.pedropathing.localization.MotionState;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Twist;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;

import org.firstinspires.ftc.teamcode.base.odometry.PinpointSource;

/**
 * L120: work out where the robot is from the Pinpoint's two pods.
 *
 * <p>The Pinpoint works out where the robot is on its own chip. This localizer
 * does the same maths itself, from the pods' raw counts, the way L113's does
 * from the drive wheels: every loop it asks how far each pod rolled since last
 * time, takes out what turning rolled them, turns what is left to point the way
 * the robot faces, and adds it to where it thought it was.
 *
 * <p>The forward pod is the Pinpoint's x pod, and the strafe pod its y pod.
 * L117 measured where they sit and which way they count, in the robot's own
 * file in the {@code pedro} folder, and that is the config handed in here.
 *
 * <p>Passes when: L120PinpointLocalizerTest (all of it)
 */
class L120PinpointLocalizer implements Localizer {

    PoseFactory poses = PoseFactory.radians();

    PinpointSource source;
    PinpointConfig config;

    /** How many ticks a pod counts for each inch it rolls. */
    double ticksPerInch;
    /** 1.0, or -1.0 for a pod that counts down as the robot moves its way. */
    double xSign = 1.0;
    double ySign = 1.0;
    /** How far the forward pod sits left of the middle, and the strafe pod ahead of it, inches. */
    double xPodOffset_in;
    double yPodOffset_in;

    /** Where the robot thinks it is, in inches. */
    double x_in;
    double y_in;
    /** Added to the Pinpoint's heading, so that setPose can say which way the robot faces. */
    double headingOffset_rad;

    /** True until the first loop has been remembered. */
    boolean firstLoop = true;
    /** What the pods and the heading read last loop. */
    double lastForwardPod_in;
    double lastStrafePod_in;
    double lastRawHeading_rad;

    L120PinpointLocalizer(PinpointSource source, PinpointConfig config) {
        this.source = source;
        this.config = config;

        // When on L120S040, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l120.html#l120s040
        // for what to do here.
    }

    // 'public' to match Pedro's Localizer interface. 'public' allows access from any code.
    @Override
    public void update() {
        source.update();
        double forwardPod_in = 0.0;
        double strafePod_in = 0.0;
        double rawHeading_rad = source.getHeadingRadians();

        // When on L120S040, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l120.html#l120s040
        // for what to do here.

        // When on L120S050, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l120.html#l120s050
        // for what to do here.

        // When on L120S060, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l120.html#l120s060
        // for what to do here.

        lastForwardPod_in = forwardPod_in;
        lastStrafePod_in = strafePod_in;
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
        firstLoop = true;
    }
}
