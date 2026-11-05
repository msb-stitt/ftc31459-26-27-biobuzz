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
import org.firstinspires.ftc.teamcode.base.DriverStationMenu;
import org.firstinspires.ftc.teamcode.base.MenuItem;
import org.firstinspires.ftc.teamcode.base.MenuRow;
import org.firstinspires.ftc.teamcode.base.OptionMenu;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * Robot 88888's autonomous, on 88888's Pinpoint and drivetrain. A Robot
 * Controller name starting 88888, such as 88888-RC, picks 88888's values; on a
 * Robot Controller named otherwise, set {@code Constants.thisRobot} to
 * {@code BOT_88888} before running it.
 *
 * <p>Between INIT and PLAY, gamepad 1's D-pad works a menu on the Driver
 * Station: the alliance, and the auto, which is Nothing until another is
 * chosen. Each auto's route says where it starts. When a choice changes, the
 * robot's pose is set to the chosen auto's start, so the flight log shows it
 * before PLAY. The poses are written for the red alliance. For the blue
 * alliance, {@code poses()} turns each one a half-turn about the field's
 * middle, at (72, 72). Left, right, forward and backward are as the audience
 * sees them, from below y = 0 looking toward +y.
 *
 * <p>At PLAY the alliance is handed to 88888 TeleOp, which the Driver Station
 * picks when this auto ends, by itself or at the end of its 30 seconds.
 * Pressing stop cancels that.
 */
@Autonomous(name = "88888 Auto", group = "1 Experiments", preselectTeleOp = "88888 TeleOp")
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class Bot88888Auto extends CorbelsAuto {

    MenuItem red = new MenuItem("R", "Red");
    MenuItem blue = new MenuItem("B", "Blue");
    MenuRow allianceRow = new MenuRow("Alliance");
    MenuItem nothing = new MenuItem("N", "Nothing");
    MenuItem forward24 = new MenuItem("F24", "Forward 24");
    MenuItem forwardThenStrafe = new MenuItem("FS", "Forward Then Strafe");
    MenuRow autoRow = new MenuRow("Auto");
    OptionMenu optionMenu = new OptionMenu();
    DriverStationMenu menu = new DriverStationMenu(optionMenu);

    double robotHalfLengthIn = 9.0;
    double fieldPerimeterWidthIn = 1.5;
    double botStartYIn = robotHalfLengthIn + fieldPerimeterWidthIn;

    CorbelsMecanum drivetrain;

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init() {
        allianceRow.add(red);
        allianceRow.add(blue);
        autoRow.add(nothing);
        autoRow.add(forward24);
        autoRow.add(forwardThenStrafe);
        optionMenu.add(allianceRow);
        optionMenu.add(autoRow);

        initBefore();
        // Live in AdvantageScope from INIT, at 192.168.43.1, only when the
        // Live NetworkTables switch OpMode has turned it on. For practice only:
        // FTC rule R704 forbids it at competitions.
        Tracker.serveLive();
        drivetrain = new CorbelsMecanum(hardware);
        initAfter(drivetrain);
    }

    /** Again and again between INIT and PLAY: the menu, and the chosen auto when it changes. */
    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init_loop() {
        menu.loop(gamepad1);
        if (menu.choiceChanged()) {
            MenuItem auto = autoRow.getSelected();
            AutoRoutine chosen = autoRoutineFor(auto);
            select(chosen);
        }
        initLoopAfter();
    }

    /** The route for a menu item: Nothing, the default start doing nothing, unless another. */
    AutoRoutine autoRoutineFor(MenuItem auto) {
        boolean isRed = allianceRow.getSelected() == red;
        Pose defaultStart = OpModeStorage.defaultStartPose(isRed);
        AutoRoutine chosenAuto = new AutoRoutine(defaultStart, Command.NOOP);
        if (auto == forward24) {
            chosenAuto = driveForward24();
        }
        if (auto == forwardThenStrafe) {
            chosenAuto = driveForwardThenStrafe();
        }
        return chosenAuto;
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void start() {
        startBefore();
        Tracker.publish("menu/alliance", allianceRow.getSelected().getFullName());
        OpModeStorage.isRed = allianceRow.getSelected() == red;

        MenuItem auto = autoRow.getSelected();
        Tracker.publish("menu/auto", auto.getFullName());
        Command chosenAuto = selectedAuto.command();
        Scheduler.schedule(chosenAuto);
        startAfter();
    }

    /** The drive runs inside loopAfter(); these lines are for the driver's screen. */
    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();
        Tracker.printToDs(menu.getSummary());
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
        if (allianceRow.getSelected() == blue) {
            chosenPoses = redPoses.rotateAround(72.0, 72.0, 180.0);
        }
        return chosenPoses;
    }

    /** Forward 24 inches from the wall. */
    AutoRoutine driveForward24() {
        PoseFactory poses = poses();
        Pose start = poses.of(60.0, botStartYIn, -90.0);
        Pose end = poses.of(60.0, botStartYIn + 24.0, -90.0);

        Command forward24 = sequential(follow(follower, line(start, end).constant(start)));
        AutoRoutine forward24Routine = new AutoRoutine(start, forward24);
        return forward24Routine;
    }

    /** Forward 24 inches, left 24 inches, then hold there, on its own alliance's side. */
    AutoRoutine driveForwardThenStrafe() {
        PoseFactory poses = poses();
        Pose start = poses.of(60.0, botStartYIn, -90.0);
        Pose corner = poses.of(60.0, botStartYIn + 24.0, -90.0);
        Pose end = poses.of(36.0, botStartYIn + 24.0, -90.0);

        Command driveForwardToTheCorner = follow(follower, line(start, corner).constant(start));
        Command strafeLeftToTheEnd = follow(follower, line(corner, end).constant(start));
        Command holdTheEnd = hold(follower, end);
        Command forwardThenStrafeRoute = sequential(driveForwardToTheCorner, strafeLeftToTheEnd, holdTheEnd);
        AutoRoutine forwardThenStrafeRoutine = new AutoRoutine(start, forwardThenStrafeRoute);
        return forwardThenStrafeRoutine;
    }
}
