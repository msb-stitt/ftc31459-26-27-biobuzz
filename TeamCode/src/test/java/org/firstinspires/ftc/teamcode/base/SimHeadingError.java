package org.firstinspires.ftc.teamcode.base;

import java.util.Random;

/**
 * A heading's error on the simulated robot: a steady drift, its rate drawn
 * once a run, and noise on each reading. It reads true where it starts.
 */
public final class SimHeadingError {

    /** The spread of a normal draw whose median size is 1. */
    static final double MEDIAN_OF_SIZE = 0.6744897501960817;

    /** How fast the heading drifts, radians a second. */
    final double rate;
    /** The spread of each reading's noise, radians. */
    final double noise;
    private final Random random;

    SimHeadingError(double rate, double noise, Random random) {
        this.rate = rate;
        this.noise = noise;
        this.random = random;
    }

    /** A drift rate drawn with a median size of {@code rateMedian}, and noise of spread {@code noise}. */
    static SimHeadingError drawn(Random random, double rateMedian, double noise) {
        return new SimHeadingError(random.nextGaussian() * rateMedian / MEDIAN_OF_SIZE, noise, random);
    }

    /** The heading read {@code seconds} after it read true, of a robot that has turned {@code turned}. */
    double read(double turned, double seconds) {
        return turned + rate * seconds + (noise == 0 ? 0 : random.nextGaussian() * noise);
    }
}
