package org.firstinspires.ftc.teamcode.base;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.drivetrain.Drivetrain;

import java.util.Collections;
import java.util.Map;

/**
 * A drivetrain the follower can hold but cannot drive.
 *
 * <p>Used by the early lessons, where the OpMode sets the motors itself. The
 * follower still runs -- it reads the localizer, reports the pose and feeds
 * Panels and the flight log -- and every call that would reach a motor does
 * nothing here.
 *
 * <p>Pedro needs a {@link Drivetrain}, not a null one: in its IDLE mode
 * {@code Follower.update()} calls {@code stop()} every loop, which would zero
 * the motors the lesson just set.
 */
public final class PassiveDriveTrain implements Drivetrain {

    @Override
    public void drive(DrivePowers speeds, boolean manual) {
    }

    @Override
    public void stop() {
    }

    @Override
    public void stop(boolean brake) {
    }

    /** Full scaling: nothing here saturates, because nothing here moves. */
    @Override
    public double maxScaling(DrivePowers current, DrivePowers delta) {
        return 1.0;
    }

    /** Pedro's own formula, so a path algorithm asking is not given nonsense. */
    @Override
    public double interpolateVelocity(double xRadius, double yRadius, double theta) {
        return 1.0 / (Math.abs(Math.cos(theta)) / xRadius + Math.abs(Math.sin(theta)) / yRadius);
    }

    @Override
    public Map<String, Object> debug() {
        return Collections.singletonMap("passive", true);
    }
}
