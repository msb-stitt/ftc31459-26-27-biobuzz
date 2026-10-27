package org.firstinspires.ftc.teamcode.base;

/**
 * The arithmetic behind measuring a constant, kept apart from the OpModes that
 * collect the numbers so it can be tested on a laptop.
 *
 * <p>Both measurements work the same way: push the robot by hand, let the
 * Pinpoint say what the robot did in inches and radians, and ask the drive
 * encoders what they counted meanwhile. The ratio is the constant.
 */
public final class Calibration {

    private Calibration() {
    }

    /**
     * The part of four wheel readings that means "the robot went forward".
     * All four wheels turn the same way, so they average.
     */
    public static double forwardPart(double frontLeft, double frontRight,
                                     double backLeft, double backRight) {
        return (frontLeft + frontRight + backLeft + backRight) / 4;
    }

    /**
     * The part that means "the robot turned". Turning counter-clockwise drives
     * the left wheels backwards and the right wheels forwards, so the left ones
     * subtract.
     */
    public static double turnPart(double frontLeft, double frontRight,
                                  double backLeft, double backRight) {
        return (-frontLeft + frontRight - backLeft + backRight) / 4;
    }

    /**
     * Encoder ticks per inch of travel.
     *
     * @param ticks  what the encoders counted, as a forward part
     * @param inches what the Pinpoint says the robot actually travelled
     */
    public static double ticksPerInch(double ticks, double inches) {
        if (Math.abs(inches) < 1e-9) return Double.NaN;
        return Math.abs(ticks / inches);
    }

    /**
     * How far a wheel sits from the middle of the robot, in inches -- the
     * number that turns radians per second into inches per second at the wheel.
     *
     * @param wheel_in how far a wheel travelled, as a turn part
     * @param turn_rad what the Pinpoint says the robot actually turned
     */
    public static double turnRadiusInches(double wheel_in, double turn_rad) {
        if (Math.abs(turn_rad) < 1e-9) return Double.NaN;
        return Math.abs(wheel_in / turn_rad);
    }

    /** Keeps an angle's total change, in radians, across the -pi/pi wrap. */
    public static double unwrap(double previous, double current) {
        double delta = current - previous;
        while (delta > Math.PI) delta -= 2 * Math.PI;
        while (delta < -Math.PI) delta += 2 * Math.PI;
        return delta;
    }
}
