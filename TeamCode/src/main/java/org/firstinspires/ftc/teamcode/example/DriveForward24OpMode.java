package org.firstinspires.ftc.teamcode.example;

import static com.pedropathing.api.Paths.line;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.base.AutoRoutine;
import org.firstinspires.ftc.teamcode.base.CorbelsAuto;
import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * Minimal autonomous: drive 24 inches forward, holding heading. Built with Ivy so
 * adding a second step later is one more line in the sequential() block.
 *
 * <p>NOTE ON HEADING: this uses {@code .constant()}, not the {@code .linear()}
 * the Pedro docs show. This path does not change heading at all, so constant
 * interpolation says what it means. Issues #176 and #181 report .linear()
 * interpolating heading backwards on line paths in Pedro 3.0.0; 3.0.1 fixes it.
 */
@Autonomous(name = "Auto: Drive 24in", group = "3 Old")
public class DriveForward24OpMode extends CorbelsAuto {

    /** How far to drive, in inches. */
    private static final double DISTANCE_IN = 24.0;

    private static final PoseFactory POSES = PoseFactory.degrees();
    double robotHalfLengthIn = 9.0;
    double fieldPerimeterWidthIn = 1.5;
    double botStartYIn = robotHalfLengthIn + fieldPerimeterWidthIn;
    private final Pose start = POSES.of(60, botStartYIn, -90);
    private final Pose end = POSES.of(60, botStartYIn+DISTANCE_IN, -90);

    @Override
    public void init() {
        initBefore();
        initAfter(new CorbelsMecanum(hardware));
        AutoRoutine forward = routine();
        select(forward);
    }

    @Override
    public void start() {
        startBefore();
        Command chosenAuto = selectedAuto.command();
        Scheduler.schedule(chosenAuto);
        startAfter();
    }

    public AutoRoutine routine() {
        Command forward = sequential(
                follow(follower, driveForward())
                // Add more steps here; each runs after the previous finishes.
        );
        AutoRoutine forwardRoutine = new AutoRoutine(start, forward);
        return forwardRoutine;
    }

    @Override
    public void loop() {
        loopBefore();

        Tracker.printToDs("Mode  %s", follower.mode());
        Tracker.printPoseSpeedLoopToDs(follower);

        loopAfter();
    }

    private Path driveForward() {
        return line(start, end).constant(start);
    }
}
