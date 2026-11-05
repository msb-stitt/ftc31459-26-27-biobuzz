package org.firstinspires.ftc.teamcode.base;

import com.pedropathing.ivy.Command;
import com.pedropathing.math.Pose;

/**
 * One auto: the pose the robot starts from and the command it runs from
 * there. An auto that does nothing is a start pose and {@code Command.NOOP}.
 * Built during INIT, so its start pose is known before PLAY.
 */
// 'public' so OpModes outside base can make one. 'public' allows access from any code.
public class AutoRoutine {

    private final Pose startPose;
    private final Command command;

    // 'public' so OpModes outside base can make one. 'public' allows access from any code.
    public AutoRoutine(Pose startPose, Command command) {
        this.startPose = startPose;
        this.command = command;
    }

    /** Where the robot is placed before the match. */
    // 'public' so OpModes outside base can read it. 'public' allows access from any code.
    public Pose startPose() {
        return startPose;
    }

    /** What the robot does from there, scheduled at PLAY. */
    // 'public' so OpModes outside base can schedule it. 'public' allows access from any code.
    public Command command() {
        return command;
    }
}
