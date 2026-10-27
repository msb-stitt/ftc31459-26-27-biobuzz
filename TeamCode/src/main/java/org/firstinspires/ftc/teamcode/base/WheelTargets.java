package org.firstinspires.ftc.teamcode.base;

/**
 * Turns a command for the whole robot into a target for each wheel.
 *
 * <p>Everything here is in inches per second, and radians per second for the
 * turn. A mecanum robot moving forward at {@code f}, towards its own left at
 * {@code s} and turning counter-clockwise at {@code omega} needs its wheels to
 * travel at
 *
 * <pre>
 *   front left  = f - s - omega * radius
 *   front right = f + s + omega * radius
 *   back left   = f + s - omega * radius
 *   back right  = f - s + omega * radius
 * </pre>
 *
 * <p>Those are Pedro's four lines, and the directions are Pedro's too; see
 * {@link CorbelsDriveTrain}.
 *
 * where {@code radius} is half the sum of the track width and the wheelbase --
 * how far a wheel is from the middle, along the diagonal it pushes.
 *
 * <p>Note what this is not: a fraction of full speed. Nothing here has been
 * divided by a maximum or clamped to 1. These are speeds the wheels should actually travel at, and
 * turning them into a fraction of full speed for each motor is a separate job.
 */
public final class WheelTargets {

    private WheelTargets() {
    }

    /**
     * @param forwardX_ips how fast the robot should go forward, inches/second
     * @param strafeY_ips how fast it should go to its own left, inches/second
     * @param turnCcw_radps how fast it should turn counter-clockwise, radians/second
     * @param turnRadius_in how far a wheel sits from the centre, inches
     * @return front left, front right, back left, back right -- inches/second
     */
    public static double[] forMecanum(double forwardX_ips, double strafeY_ips,
                                      double turnCcw_radps, double turnRadius_in) {
        double edgeSpeed_ips = turnCcw_radps * turnRadius_in;
        return new double[]{
                forwardX_ips - strafeY_ips - edgeSpeed_ips,
                forwardX_ips + strafeY_ips + edgeSpeed_ips,
                forwardX_ips + strafeY_ips - edgeSpeed_ips,
                forwardX_ips - strafeY_ips + edgeSpeed_ips};
    }
}
