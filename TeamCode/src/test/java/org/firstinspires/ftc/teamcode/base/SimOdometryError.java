package org.firstinspires.ftc.teamcode.base;

import java.util.Random;
import java.util.SplittableRandom;

/**
 * The Pinpoint's error on the simulated robot, a tenth of what waggle's
 * {@code docs/drift.rst} found on 31459's and 37523's logs: each pod reads its
 * travel scaled by its own error, drawn once a run, and the pods read short by
 * {@link #SLIP_IN} over the robot's first {@link #SLIP_OVER_IN} of travel; its
 * heading drifts, from true where its pose was set; and each reading carries
 * noise. The IMU's heading is {@link #imu}.
 *
 * <p>Waggle found the logged place drift a median 0.64 % of the distance moved
 * between points 80 s apart, so each pod's scale error is drawn with a median
 * size of {@link #SCALE_MEDIAN}. It found one slip of about 4 in in Auto's first
 * dash of 96 in, held through the match. Its quick place error was about 3 in
 * each frame, which holds the camera's error too. The logged heading turned 1.3°
 * in 100 s, give or take 1.8°, in 37523's Q9, and lay within 1° of the robot's
 * level lines while it stood still at the start, within 3° late in a match.
 */
public final class SimOdometryError {

    /** The median size of a pod's scale error, a tenth of waggle's 0.64 %. */
    public static final double SCALE_MEDIAN = 0.00064 / 10;
    /** How far the pods read short over the first {@link #SLIP_OVER_IN}, a tenth of waggle's 4 in. */
    public static final double SLIP_IN = 0.4;
    public static final double SLIP_OVER_IN = 96;
    /** The spread of each reading's place noise, each way, a tenth of waggle's 3 in. */
    public static final double PLACE_NOISE_IN = 0.3;
    /** The median size of the heading's drift rate, a tenth of waggle's 1.3° in 100 s. */
    public static final double HEADING_RATE_MEDIAN = Math.toRadians(1.3 / 100) / 10;
    /** The spread of each heading reading's noise, a tenth of waggle's 1°. */
    public static final double HEADING_NOISE = Math.toRadians(1.0) / 10;
    /** How much noisier and driftier the IMU's heading is than the Pinpoint's. */
    public static final double IMU_TIMES = 3;
    /** Sets the seed of a {@code simRun}'s error, or {@code exact} for none. */
    public static final String VARIABLE = "SIMRUN_PINPOINT_SEED";

    final double forwardScale;
    final double strafeScale;
    private final double slipIn;
    private final double slipOverIn;
    final double placeNoise;
    final SimHeadingError heading;
    private final Random random;
    private double travelledIn;

    public SimOdometryError(double forwardScale, double strafeScale, double slipIn, double slipOverIn) {
        this(forwardScale, strafeScale, slipIn, slipOverIn, 0, new SimHeadingError(0, 0, null), null);
    }

    SimOdometryError(double forwardScale, double strafeScale, double slipIn, double slipOverIn,
            double placeNoise, SimHeadingError heading, Random random) {
        this.forwardScale = forwardScale;
        this.strafeScale = strafeScale;
        this.slipIn = slipIn;
        this.slipOverIn = slipOverIn;
        this.placeNoise = placeNoise;
        this.heading = heading;
        this.random = random;
    }

    /** A run's error, its pods' scale errors drawn from {@code seed}. */
    public static SimOdometryError seeded(long seed) {
        // Random's first draws follow its seed, so neighbouring seeds are mixed first.
        Random r = new Random(new SplittableRandom(seed).nextLong());
        double sd = SCALE_MEDIAN / SimHeadingError.MEDIAN_OF_SIZE;
        return new SimOdometryError(r.nextGaussian() * sd, r.nextGaussian() * sd, SLIP_IN, SLIP_OVER_IN,
                PLACE_NOISE_IN, SimHeadingError.drawn(r, HEADING_RATE_MEDIAN, HEADING_NOISE), r);
    }

    /** The IMU's heading error of a run, {@link #IMU_TIMES} the Pinpoint's, drawn from {@code seed}. */
    public static SimHeadingError imu(long seed) {
        Random r = new Random(new SplittableRandom(seed).split().nextLong());
        return SimHeadingError.drawn(r, IMU_TIMES * HEADING_RATE_MEDIAN, IMU_TIMES * HEADING_NOISE);
    }

    /** {@code simRun}'s IMU error, from the same {@link #VARIABLE} as the Pinpoint's. */
    public static SimHeadingError imuFromVariable(String value) {
        if (value == null || value.isEmpty()) return imu(1);
        if (value.equals("exact")) return null;
        return imu(Long.parseLong(value));
    }

    /** One reading's place noise, inches, each way. */
    double placeNoise() {
        return placeNoise == 0 ? 0 : random.nextGaussian() * placeNoise;
    }

    /** {@code simRun}'s error: seed 1 unless {@code value} names one, none for {@code exact}. */
    public static SimOdometryError fromVariable(String value) {
        if (value == null || value.isEmpty()) return seeded(1);
        if (value.equals("exact")) return null;
        return seeded(Long.parseLong(value));
    }

    /**
     * What the pods read of a move of {@code forward} and {@code strafe}, inches
     * along the robot and to its left.
     */
    double[] read(double forward, double strafe) {
        double moved = Math.hypot(forward, strafe);
        double slipping = Math.max(0, Math.min(moved, slipOverIn - travelledIn));
        travelledIn += moved;
        double shortBy = moved == 0 ? 0 : slipIn / slipOverIn * slipping / moved;
        return new double[]{
                forward * (1 + forwardScale - shortBy),
                strafe * (1 + strafeScale - shortBy)};
    }
}
