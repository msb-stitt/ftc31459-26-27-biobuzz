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
import org.firstinspires.ftc.teamcode.base.DriverStationMenu;
import org.firstinspires.ftc.teamcode.base.MenuRow;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * Robot 88888's autonomous: L140 Auto, as solutions-l140-alliances.2 solves
 * it, on 88888's Pinpoint and drivetrain. A Robot Controller name starting
 * 88888, such as 88888-RC, picks 88888's values; on a Robot Controller named
 * otherwise, set {@code Constants.thisRobot} to {@code BOT_88888} before
 * running it.
 *
 * <p>Between INIT and PLAY, gamepad 1's D-pad works a menu on the Driver
 * Station: the alliance, and the auto, which is Nothing until another is
 * chosen. The poses are written for the red alliance. For the blue alliance,
 * {@code poses()} turns each one a half-turn about the field's middle, at
 * (72, 72).
 *
 * <p>At PLAY the alliance is handed to 88888 TeleOp, which the Driver Station
 * picks when this auto ends, by itself or at the end of its 30 seconds.
 * Pressing stop cancels that.
 */
@Autonomous(name = "88888 Auto", group = "88888", preselectTeleOp = "88888 TeleOp")
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class Bot88888Auto extends CorbelsAuto {

    DriverStationMenu menu = new DriverStationMenu(
            new MenuRow("Alliance", "Red", "Blue"),
            new MenuRow("Auto", "N-Nothing", "F24-Forward 24", "FS-Forward Then Strafe"));

    double robotHalfLengthIn = 9.0;
    double fieldPerimeterWidthIn = 1.5;
    double botStartYIn = robotHalfLengthIn + fieldPerimeterWidthIn;

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
    }

    /** Again and again between INIT and PLAY: the menu. */
    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init_loop() {
        menu.loop(gamepad1);
        initLoopAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void start() {
        startBefore();
        String alliance = menu.chosen("Alliance");
        OpModeStorage.alliance = alliance;

        String auto = menu.chosen("Auto");
        Command chosenAuto = Command.NOOP;
        switch (auto) {
            case "F24-Forward 24":
                chosenAuto = driveForward24();
                break;
            case "FS-Forward Then Strafe":
                chosenAuto = driveForwardThenStrafe();
                break;
        }
        Scheduler.schedule(chosenAuto);
        startAfter();
    }

    /** The drive runs inside loopAfter(); these lines are for the driver's screen. */
    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();
        Tracker.printToDs(menu.summary());
        Tracker.printPoseSpeedLoopToDs(follower);
        loopAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void stop() {
        stopAfter();
    }

    /** The red alliance's poses as they are, or turned for the blue alliance. */
    PoseFactory poses() {
        PoseFactory redPoses = PoseFactory.degrees();
        PoseFactory chosenPoses = redPoses;
        String alliance = menu.chosen("Alliance");
        if (alliance.equals("Blue")) {
            chosenPoses = redPoses.rotateAround(72.0, 72.0, 180.0);
        }
        return chosenPoses;
    }

    /** Set at INIT, and again at PLAY, once the alliance is chosen. */
    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public Pose startPose() {
        PoseFactory poses = poses();
        Pose start = poses.of(72.0, botStartYIn, 90.0);
        return start;
    }

    /** L125's auto: forward 24 inches from the wall. */
    Command driveForward24() {
        PoseFactory poses = poses();
        Pose start = poses.of(72.0, botStartYIn, 90.0);
        Pose end = poses.of(72.0, botStartYIn + 24.0, 90.0);

        Command forward24 = sequential(follow(follower, line(start, end).constant(start)));
        return forward24;
    }

    /** L130's auto: forward to the middle, sideways to the right, then hold there. */
    Command driveForwardThenStrafe() {
        PoseFactory poses = poses();
        Pose start = poses.of(72.0, botStartYIn, 90.0);
        Pose corner = poses.of(72.0, 72.0, 90.0);
        Pose end = poses.of(96.0, 72.0, 90.0);

        Command driveForwardToTheCorner = follow(follower, line(start, corner).constant(start));
        Command strafeRightToTheEnd = follow(follower, line(corner, end).constant(start));
        Command holdTheEnd = hold(follower, end);
        Command forwardThenStrafeRoute = sequential(driveForwardToTheCorner, strafeRightToTheEnd, holdTheEnd);
        return forwardThenStrafeRoute;
    }
}
