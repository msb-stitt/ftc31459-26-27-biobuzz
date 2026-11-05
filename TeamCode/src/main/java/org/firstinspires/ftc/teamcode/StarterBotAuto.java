package org.firstinspires.ftc.teamcode;

import static com.pedropathing.api.Paths.line;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;
import static com.pedropathing.ivy.pedro.PedroCommands.hold;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.base.AutoRoutine;
import org.firstinspires.ftc.teamcode.base.CorbelsAuto;
import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * The StarterBot's autonomous, on the red alliance: from the default start,
 * forward 24 inches from the wall, then 24 inches left without turning, and
 * hold there, facing the wall the whole way. Left and forward are as the
 * audience sees them.
 *
 * <p>The drive is one {@link AutoRoutine}, built and selected in
 * {@code init()}, and its command scheduled in {@code start()} when PLAY is
 * pressed. Each wheel's speed goes to the flight log; Pedro's pose is already
 * there, under {@code Robot/Pose}.
 */
@Autonomous(name = "StarterBot Auto", group = "1 Experiments")
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class StarterBotAuto extends CorbelsAuto {

    PoseFactory poses = PoseFactory.degrees();

    double robotHalfLengthIn = 9.0;
    double fieldPerimeterWidthIn = 1.5;
    double botStartYIn = robotHalfLengthIn + fieldPerimeterWidthIn;
    Pose start = poses.of(60.0, botStartYIn, -90.0);
    Pose corner = poses.of(60.0, botStartYIn + 24.0, -90.0);
    Pose end = poses.of(36.0, botStartYIn + 24.0, -90.0);

    CorbelsMecanum drivetrain;

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init() {
        initBefore();
        // Live in AdvantageScope from INIT, at 192.168.43.1, only when the
        // Live NetworkTables switch OpMode has turned it on. For practice only:
        // FTC rule R704 forbids it at competitions.
        Tracker.serveLive();
        drivetrain = new CorbelsMecanum(hardware);
        initAfter(drivetrain);
        AutoRoutine forwardThenStrafeThenHold = driveForwardThenStrafeThenHold();
        select(forwardThenStrafeThenHold);
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void start() {
        startBefore();
        Command chosenAuto = selectedAuto.command();
        Scheduler.schedule(chosenAuto);
        startAfter();
    }

    /** The drive runs inside loopAfter(); this line is for the driver's screen. */
    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();
        Tracker.printPoseSpeedLoopToDs(follower);
        loopAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void stop() {
        stopAfter();
    }

    /** Forward to the corner, sideways to the end, then hold the end. */
    AutoRoutine driveForwardThenStrafeThenHold() {
        Command driveForwardToTheCorner = follow(follower, line(start, corner).constant(start));
        Command strafeLeftToTheEnd = follow(follower, line(corner, end).constant(start));
        Command holdTheEnd = hold(follower, end);
        Command forwardThenStrafe = sequential(driveForwardToTheCorner, strafeLeftToTheEnd, holdTheEnd);
        AutoRoutine forwardThenStrafeRoutine = new AutoRoutine(start, forwardThenStrafe);
        return forwardThenStrafeRoutine;
    }
}
