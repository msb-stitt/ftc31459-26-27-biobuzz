package org.firstinspires.ftc.teamcode.base;

/**
 * The arithmetic behind measuring a constant, kept apart from the OpModes that
 * collect the numbers so it can be tested on a laptop.
 *
 * <p>Both measurements work the same way: push the robot by hand, let the
 * Pinpoint say what the robot did in inches and radians, and ask the drive
 * encoders what they counted meanwhile. The ratio is the constant.
 *
 * <p>Before either is measured, L113 works both out from the parts:
 * {@link #ticksPerInchFromTheParts} and {@link #turnRadiusFromTheWheels}.
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

    /**
     * Encoder ticks per inch, worked out from the parts: one turn of the wheel
     * rolls it its circumference, pi times its diameter.
     *
     * @param ticksPerTurn     what the motor's encoder counts for one turn of the wheel
     * @param wheelDiameter_mm the wheel's diameter, as its maker gives it, in millimetres
     */
    public static double ticksPerInchFromTheParts(double ticksPerTurn, double wheelDiameter_mm) {
        double wheelDiameter_in = wheelDiameter_mm / 25.4;
        return ticksPerTurn / (Math.PI * wheelDiameter_in);
    }

    /**
     * How far a mecanum wheel's rim rolls for each radian the robot turns, in
     * inches, worked out from where the wheels sit: A + B, not the distance
     * from the middle to the wheel. A wheel's centre moves B forward and A
     * sideways for each radian; its rim rolls both, at 45 degrees.
     *
     * @param halfLength_in A: half the distance between the front and back wheels' centres
     * @param halfWidth_in  B: half the distance between the left and right wheels' centres
     */
    public static double turnRadiusFromTheWheels(double halfLength_in, double halfWidth_in) {
        return halfLength_in + halfWidth_in;
    }
}
