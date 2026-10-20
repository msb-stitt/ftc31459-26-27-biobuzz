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

import org.firstinspires.ftc.teamcode.base.CorbelsAuto;
import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * The StarterBot's autonomous: L130's drive. Forward from the wall to the
 * middle of the field, then sideways to the right without turning, and hold
 * there, facing away from the wall the whole way.
 *
 * <p>The drive is one command, built and scheduled in {@code start()} when PLAY
 * is pressed, the way StarterBot TeleOp schedules its three. Each wheel's
 * power goes to the flight log; Pedro's pose is already there, under
 * {@code Robot/Pose}.
 */
@Autonomous(name = "StarterBot Auto", group = "StarterBot")
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class StarterBotAuto extends CorbelsAuto {

    PoseFactory poses = PoseFactory.degrees();

    double robotHalfLengthIn = 9.0;
    double fieldPerimeterWidthIn = 1.5;
    double botStartYIn = robotHalfLengthIn + fieldPerimeterWidthIn;
    Pose start = poses.of(72.0, botStartYIn, 90.0);
    Pose corner = poses.of(72.0, 72.0, 90.0);
    Pose end = poses.of(96.0, 72.0, 90.0);

    CorbelsMecanum drivetrain;
    Command forwardThenStrafeThenHold;

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init() {
        initBefore();
        drivetrain = new CorbelsMecanum(hardware);
        initAfter(drivetrain);
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void start() {
        startBefore();
        forwardThenStrafeThenHold = driveForwardThenStrafeThenHold();
        Scheduler.schedule(forwardThenStrafeThenHold);
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

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public Pose startPose() {
        return start;
    }

    /** Forward to the corner, sideways to the end, then hold the end. */
    Command driveForwardThenStrafeThenHold() {
        Command driveForwardToTheCorner = follow(follower, line(start, corner).constant(start));
        Command strafeRightToTheEnd = follow(follower, line(corner, end).constant(start));
        Command holdTheEnd = hold(follower, end);
        Command forwardThenStrafe = sequential(driveForwardToTheCorner, strafeRightToTheEnd, holdTheEnd);
        return forwardThenStrafe;
    }
}
