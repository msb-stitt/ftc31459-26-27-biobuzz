package org.firstinspires.ftc.teamcode.base;

import java.util.Random;
import java.util.SplittableRandom;

/**
 * The Pinpoint's error on the simulated robot, a tenth of what waggle's
 * {@code docs/drift.rst} found on 31459's and 37523's logs: each pod reads its
 * travel scaled by its own error, drawn once a run, and the pods read short by
 * {@link #SLIP_IN} over the robot's first {@link #SLIP_OVER_IN} of travel. The
 * heading reads true.
 *
 * <p>Waggle found the logged place drift a median 0.64 % of the distance moved
 * between points 80 s apart, so each pod's scale error is drawn with a median
 * size of {@link #SCALE_MEDIAN}. It found one slip of about 4 in in Auto's first
 * dash of 96 in, held through the match.
 */
public final class SimOdometryError {

    /** The median size of a pod's scale error, a tenth of waggle's 0.64 %. */
    public static final double SCALE_MEDIAN = 0.00064 / 10;
    /** How far the pods read short over the first {@link #SLIP_OVER_IN}, a tenth of waggle's 4 in. */
    public static final double SLIP_IN = 0.4;
    public static final double SLIP_OVER_IN = 96;
    /** Sets the seed of a {@code simRun}'s error, or {@code exact} for none. */
    public static final String VARIABLE = "SIMRUN_PINPOINT_SEED";

    /** The spread of a normal draw whose median size is 1. */
    private static final double MEDIAN_OF_SIZE = 0.6744897501960817;

    final double forwardScale;
    final double strafeScale;
    private final double slipIn;
    private final double slipOverIn;
    private double travelledIn;

    public SimOdometryError(double forwardScale, double strafeScale, double slipIn, double slipOverIn) {
        this.forwardScale = forwardScale;
        this.strafeScale = strafeScale;
        this.slipIn = slipIn;
        this.slipOverIn = slipOverIn;
    }

    /** A run's error, its pods' scale errors drawn from {@code seed}. */
    public static SimOdometryError seeded(long seed) {
        // Random's first draws follow its seed, so neighbouring seeds are mixed first.
        Random r = new Random(new SplittableRandom(seed).nextLong());
        double sd = SCALE_MEDIAN / MEDIAN_OF_SIZE;
        return new SimOdometryError(r.nextGaussian() * sd, r.nextGaussian() * sd, SLIP_IN, SLIP_OVER_IN);
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
