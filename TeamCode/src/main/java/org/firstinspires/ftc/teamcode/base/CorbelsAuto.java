package org.firstinspires.ftc.teamcode.base;

import com.pedropathing.ivy.Command;
import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.OpModeStorage;

/**
 * An autonomous: a starting pose, on top of {@link CorbelsOpMode}.
 *
 * <p>An auto {@link #select}s the {@link AutoRoutine} it will run, once its
 * follower exists, and again whenever its menu's choice changes; until then
 * it does nothing from the default start. Its {@code start()} schedules
 * {@link #selectedAuto}'s command. A lesson's auto supplies
 * {@link #startPose()} itself -- where the robot is placed -- and builds and
 * schedules its own commands in {@code start()}, as a teleop does. The pose is
 * set at INIT, again at each {@link #select}, and again at PLAY.
 */
public abstract class CorbelsAuto extends CorbelsOpMode {

    /** The auto chosen: its start pose and its command. */
    protected AutoRoutine selectedAuto = new AutoRoutine(
            OpModeStorage.defaultStartPose(OpModeStorage.isRed), Command.NOOP);

    @Override
    protected final boolean isAuto() { return true; }

    /** Where the robot is placed before the match: the selected auto's start. */
    public Pose startPose() {
        Pose startPose = selectedAuto.startPose();
        return startPose;
    }

    /** The auto to run, and the robot placed at its start, so the log shows it during INIT. */
    protected final void select(AutoRoutine chosen) {
        selectedAuto = chosen;
        Pose startPose = chosen.startPose();
        follower.setPose(startPose);
    }

    @Override
    protected final void onInit() {
        follower.setPose(startPose());
    }

    /**
     * The start pose again, at PLAY, for an autonomous whose start pose depends
     * on what was chosen between INIT and PLAY, such as the alliance.
     */
    @Override
    protected final void onStart() {
        follower.setPose(startPose());
    }
}
