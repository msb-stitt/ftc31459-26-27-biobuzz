package org.firstinspires.ftc.teamcode.base.odometry;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;

/**
 * What the Pinpoint reads: its two pods' raw counts and its heading.
 *
 * <p>The same names as the SDK's {@link GoBildaPinpointDriver}, so a localizer
 * reads like code written against the driver, and can be tested on a laptop
 * with a simulated Pinpoint instead.
 */
public interface PinpointSource {

    /** Reads the Pinpoint once. Call it once each loop, before the getters. */
    void update();

    /** The forward pod's raw count, in ticks, as the pod is mounted. */
    int getEncoderX();

    /** The strafe pod's raw count, in ticks, as the pod is mounted. */
    int getEncoderY();

    /** Which way the robot faces, in radians, counterclockwise positive, not wrapped at a turn. */
    double getHeadingRadians();

    /** When {@link #update} last read the Pinpoint, in seconds, from a clock that only goes forward. */
    double getReadTimeSeconds();

    /**
     * Ticks per millimetre of a goBILDA pod: the SDK's own numbers, which its
     * driver keeps private. Hardware 12.0.0's {@code GoBildaPinpointDriver}.
     */
    static double ticksPerMm(GoBildaPinpointDriver.GoBildaOdometryPods pods) {
        if (pods == GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_SWINGARM_POD) {
            return 13.26291192;
        }
        return 19.89436789;
    }
}
