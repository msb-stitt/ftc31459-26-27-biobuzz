package org.firstinspires.ftc.teamcode.base;

import com.pedropathing.math.Pose;

/**
 * An autonomous: a starting pose, on top of {@link CorbelsOpMode}.
 *
 * <p>A lesson supplies {@link #startPose()} -- where the robot is placed -- and
 * builds and schedules its own commands in {@code start()}, as a teleop does.
 */
public abstract class CorbelsAuto extends CorbelsOpMode {

    @Override
    protected final boolean isAuto() { return true; }

    /** Where the robot is placed before the match. */
    public abstract Pose startPose();

    @Override
    protected final void onInit() {
        follower.setPose(startPose());
    }
}
