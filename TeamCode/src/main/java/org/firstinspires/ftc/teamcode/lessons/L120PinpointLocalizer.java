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
 * the robot faces, and adds it to where it thought it was. Dividing each step
 * by the time since last loop gives how fast the robot is going.
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

    /** How fast the robot is going, forward, to its left and turning, per second. */
    double forwardSpeed_ips;
    double strafeSpeed_ips;
    double turnSpeed_radps;

    /** True until the first loop has been remembered. */
    boolean firstLoop = true;
    /** What the pods and the heading read last loop. */
    double lastForwardPod_in;
    double lastStrafePod_in;
    double lastRawHeading_rad;
    double lastReadTime_s;

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
        double readTime_s = source.getReadTimeSeconds();

        // When on L120S040, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l120.html#l120s040
        // for what to do here.

        // When on L120S050, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l120.html#l120s050
        // for what to do here.

        // When on L120S060, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l120.html#l120s060
        // for what to do here.

        // When on L120S070, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l120.html#l120s070
        // for what to do here.

        lastForwardPod_in = forwardPod_in;
        lastStrafePod_in = strafePod_in;
        lastRawHeading_rad = rawHeading_rad;
        lastReadTime_s = readTime_s;
    }

    /**
     * Where the robot thinks it is, and how fast it is going: forward, to its
     * own left and turning, as Pedro's {@code Twist} holds them.
     */
    // 'public' to match Pedro's Localizer interface. 'public' allows access from any code.
    @Override
    public MotionState state() {
        Pose where = poses.of(x_in, y_in, lastRawHeading_rad + headingOffset_rad);
        Twist speed = new Twist(forwardSpeed_ips, strafeSpeed_ips, turnSpeed_radps);
        MotionState state = MotionState.ofTwist(where, speed);
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
