package org.firstinspires.ftc.teamcode.base;

import com.pedropathing.controllers.Controller;
import com.pedropathing.follower.Follower;

/**
 * Keeps the robot pointing the way the driver left it.
 *
 * <p>While the turn stick is pushed, the driver steers and this remembers the
 * heading. When the stick is released, the remembered heading becomes a target
 * and a controller supplies the turn speed.
 *
 * <p>The controller is the SAME one the autonomous uses, taken from the tuned
 * Foresight settings -- so tuning the robot improves teleop too.
 */
public final class HeadingHold {

    private final Controller controller;
    private final double stickDeadband;
    private Double target;

    public HeadingHold(Controller controller) {
        this(controller, 0.05);
    }

    public HeadingHold(Controller controller, double stickDeadband) {
        this.controller = controller;
        this.stickDeadband = stickDeadband;
    }

    /** The turn speed to use this loop, given what the driver is asking for. */
    public double turn(Follower follower, double turnStick) {
        double heading = follower.pose().heading();
        if (Math.abs(turnStick) > stickDeadband) {
            target = null;              // the driver is steering
            return turnStick;
        }
        if (target == null) {
            target = heading;           // just let go: remember where we are
            controller.reset();
        }
        return controller.calculate(target, wrap(target - heading));
    }

    /**
     * Points at a heading the driver asked for, rather than the one they left.
     * The turn stick still wins: nudging it takes control back, as always.
     */
    public void aimAt(double radians) {
        target = radians;
        controller.reset();
    }

    /** True once the held heading is within {@code tolerance} radians. */
    public boolean atTarget(Follower follower, double tolerance) {
        return target != null
                && Math.abs(wrap(target - follower.pose().heading())) <= tolerance;
    }

    /** Forgets the target, e.g. when switching drive modes. */
    public void release() {
        target = null;
    }

    /** The heading being held, or null when the driver is steering. */
    public Double target() {
        return target;
    }

    /** Puts an angle in [-pi, pi), so turning is always the short way round. */
    public static double wrap(double radians) {
        double a = (radians + Math.PI) % (2 * Math.PI);
        if (a < 0) a += 2 * Math.PI;
        return a - Math.PI;
    }
}
