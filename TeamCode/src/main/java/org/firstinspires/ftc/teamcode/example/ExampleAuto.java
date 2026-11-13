package org.firstinspires.ftc.teamcode.example;

import static com.pedropathing.api.Paths.line;
import static com.pedropathing.ivy.commands.Commands.lazy;
import static com.pedropathing.ivy.commands.Commands.waitUntil;
import static com.pedropathing.ivy.groups.Groups.deadline;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;
import static com.pedropathing.ivy.pedro.PedroCommands.hold;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.Intake;
import org.firstinspires.ftc.teamcode.Launcher;
import org.firstinspires.ftc.teamcode.OpModeStorage;
import org.firstinspires.ftc.teamcode.base.AutoRoutine;
import org.firstinspires.ftc.teamcode.base.CorbelsAuto;
import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.DriverStationMenu;
import org.firstinspires.ftc.teamcode.base.MenuItem;
import org.firstinspires.ftc.teamcode.base.MenuRow;
import org.firstinspires.ftc.teamcode.base.OptionMenu;
import org.firstinspires.ftc.teamcode.base.Tracker;

import java.util.function.Function;
import java.util.function.LongSupplier;

/**
 * An example autonomous.
 *
 * <p>Between INIT and PLAY, gamepad 1's D-pad works a menu on the Driver
 * Station: the alliance, and the auto, which is Nothing until another is
 * chosen. Each auto's route says where it starts. When a choice changes, the
 * robot's pose is set to the chosen auto's start, so the flight log shows it
 * before PLAY. The poses are written for the red alliance. For the blue
 * alliance, {@code poses()} turns each one a half-turn about the field's
 * middle, at (72, 72). Left, right, forward and backward are as the audience
 * sees them, from below y = 0 looking toward +y. The field's x and y:
 * https://coding.stitt.cc/ftc-lessons-java/guide/tasks/e143.html
 *
 * <p>At PLAY the alliance is handed to Example TeleOp, which the Driver
 * Station picks when this auto ends, by itself or at the end of its 30
 * seconds. Pressing stop cancels that.
 */
@Autonomous(name = "Example Auto", group = "0 Competition", preselectTeleOp = "Example TeleOp")
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class ExampleAuto extends CorbelsAuto {

    /** Where the mechanisms come from. Tests hand in fakes. */
    static Function<HardwareMap, Intake> intakes = Intake::from;
    static Function<HardwareMap, Launcher> launchers = Launcher::from;
    /** The clock the waits read, in nanoseconds. Tests hand in the simulator's. */
    static LongSupplier clock = System::nanoTime;

    MenuItem red = new MenuItem("R", "Red");
    MenuItem blue = new MenuItem("B", "Blue");
    MenuRow allianceRow = new MenuRow("Alliance");
    MenuItem nothing = new MenuItem("N", "Nothing");
    MenuItem forward24 = new MenuItem("F24", "Forward 24");
    MenuItem forwardThenStrafe = new MenuItem("FS", "Forward Then Strafe");
    MenuItem movShotStrafeIn = new MenuItem("MVSI", "Move Shot Strafe In");
    // LOOKHERE - add new MenuItem for YOUR_NEW_AUTO_HERE here.
    // e.g. MenuItem YOUR_NEW_AUTO_HERE = new MenuItem( "SF", "Shoot Then Forward");
    MenuItem waitShootPark = new MenuItem("WSP", "WaitShootPark");
    MenuItem SAP = new MenuItem("SAP","Shoot And Park");
    MenuRow autoRow = new MenuRow("Auto");
    OptionMenu optionMenu = new OptionMenu();
    DriverStationMenu menu = new DriverStationMenu(optionMenu);

    double robotHalfLengthIn = 9.0;
    double fieldPerimeterWidthIn = 1.5;
    double botStartYIn = robotHalfLengthIn + fieldPerimeterWidthIn;

    double shootSeconds = 10.0;
    double waitAfterShootingSeconds = 3.0;
    double intakeSeconds = 3.0;

    CorbelsMecanum drivetrain;
    Intake intake;
    Launcher launcher;

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init() {
        allianceRow.add(red);
        allianceRow.add(blue);
        autoRow.add(nothing);
        autoRow.add(forward24);
        autoRow.add(forwardThenStrafe);
        autoRow.add(movShotStrafeIn);
        // LOOKHERE - add MenuItem for YOUR_NEW_AUTO_HERE to autoRow here
        // e.g:
        // autoRow.add(YOUR_NEW_AUTO_HERE);
        autoRow.add(waitShootPark);
        autoRow.add(SAP);
        optionMenu.add(allianceRow);
        optionMenu.add(autoRow);

        initBefore();
        // Live in AdvantageScope from INIT, at 192.168.43.1, only when the
        // Live NetworkTables switch OpMode has turned it on. For practice only:
        // FTC rule R704 forbids it at competitions.
        Tracker.serveLive();
        intake = intakes.apply(hardwareMap);
        launcher = launchers.apply(hardwareMap);
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
        if (auto == movShotStrafeIn) {
            chosenAuto = shootThenIntake();
        }
        // LOOKHERE - add if (auto == YOUR_NEW_AUTO_HERE)  to assign YOUR_NEW_ROUTINE to chosenAuto
        // here e.g.:
        //         if (auto == YOUR_NEW_AUTO_HERE) {
        //            chosenAuto = YOUR_NEW_ROUTINE();
        //         }
        if (auto == SAP) {
            chosenAuto = shootAndPark();
        }
        if (auto == waitShootPark){
            chosenAuto = waitShootParkCommand();
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

    /** L125's auto: forward 24 inches from the wall. */
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

    // Start: (60, 10.5), against the audience's wall, facing -y, toward the
    // audience. v is the way the robot faces.
    //
    //        y (in)
    //        ^
    //   16.5 +
    //   10.5 +             [v]
    //        |
    //      0 +-----+-----+-----+-----+-----+-----+--> x (in)
    //        0     24    48    72    96    120   144
    //                      audience
    //
    // Forward 6 in, to +y, away from the audience: the robot drives backward.
    //
    //        y (in)
    //        ^
    //   16.5 +             [v]
    //   10.5 +             ( )
    //        |
    //      0 +-----+-----+-----+-----+-----+-----+--> x (in)
    //        0     24    48    72    96    120   144
    //
    // Shoot for 10 s at (60, 16.5), then wait 3 s, holding there.
    //
    //        y (in)
    //        ^
    //   16.5 +             [v]
    //   10.5 +
    //        |
    //      0 +-----+-----+-----+-----+-----+-----+--> x (in)
    //        0     24    48    72    96    120   144
    //
    // Left 12 in, to -x, toward the red wall: the robot strafes to its own right.
    //
    //        y (in)
    //        ^
    //   16.5 +          [v]( )
    //   10.5 +
    //        |
    //      0 +-----+-----+-----+-----+-----+-----+--> x (in)
    //        0     24    48    72    96    120   144
    //
    // Stop at (48, 16.5) and intake for 3 s, holding there.
    //
    //        y (in)
    //        ^
    //   16.5 +          [v]
    //   10.5 +
    //        |
    //      0 +-----+-----+-----+-----+-----+-----+--> x (in)
    //        0     24    48    72    96    120   144

    /**
     * Forward 6 inches and hold; shoot for 10 seconds, then stop; wait 3
     * seconds; strafe 12 inches left and hold; intake for 3 seconds. The robot
     * faces -y, toward the audience, so it drives backward and strafes to its
     * own right.
     */
    AutoRoutine shootThenIntake() {
        PoseFactory poses = poses();
        Pose start = poses.of(60.0, botStartYIn, -90.0);
        Pose shootFrom = poses.of(60.0, botStartYIn + 6.0, -90.0);
        Pose intakeAt = poses.of(48.0, botStartYIn + 6.0, -90.0);

        Command driveForwardToShoot = follow(follower, line(start, shootFrom).constant(start));
        Command holdWhileShooting = hold(follower, shootFrom);
        Command shoot = shootFor(shootSeconds);
        Command waitAfterShooting = waitSeconds(waitAfterShootingSeconds);
        Command strafeLeftToIntake = follow(follower, line(shootFrom, intakeAt).constant(start));
        Command holdWhileIntaking = hold(follower, intakeAt);
        Command runTheIntake = intakeFor(intakeSeconds);
        Command shootThenIntakeRoute = sequential(driveForwardToShoot, holdWhileShooting, shoot,
                waitAfterShooting, strafeLeftToIntake, holdWhileIntaking, runTheIntake);
        AutoRoutine shootThenIntakeRoutine = new AutoRoutine(start, shootThenIntakeRoute);
        return shootThenIntakeRoutine;
    }

    AutoRoutine waitShootParkCommand() {
        PoseFactory poses = poses();
        Pose start = poses.of(60.0, 144.0-botStartYIn, +90.0);
        Pose firststop = poses.of(60.0, 120.0, +90.0);
        Pose stopPose = poses.of(18.0,  120.0, +90.0);

        Command wait = waitSeconds(10);
        Command shoot = shootFor(8);

        Command drivetofirststop = follow(follower, line(start,firststop).constant(start));
        Command driveToStop = follow(follower, line(firststop, stopPose).constant(start));
        Command wsp = sequential( wait,shoot,
                drivetofirststop, driveToStop);
        AutoRoutine shootAndParkRoutine = new AutoRoutine(start, wsp);
        return shootAndParkRoutine;
    }

    // LOOKHERE - for your new auto YOUR_NEW_AUTO_HERE define YOUR_NEW_ROUTINE here, with
    // its own start:
    // e.g.:
    //AutoRoutine YOUR_NEW_ROUTINE() {
    //    PoseFactory poses = poses();
    //    Pose startPose = poses.of(72.0, botStartYIn, -90.0);
    //    Pose stopPose = poses.of(72.0, botStartYIn + 6.0, -90.0);
    //
    //    Command holdWhileShooting = hold(follower, startPose);
    //    Command shoot = shootFor(shootSeconds);
    //    Command waitAfterShooting = waitSeconds(waitAfterShootingSeconds);
    //    Command driveForwardAfterShot = follow(follower, line(startPose, stopPose).constant(startPose));
    //    Command shootThenMoveRoute = sequential(holdWhileShooting, shoot,
    //            waitAfterShooting, driveForwardAfterShot);
    //    AutoRoutine shootThenMoveRoutine = new AutoRoutine(startPose, shootThenMoveRoute);
    //    return shootThenMoveRoutine;
    //}
    AutoRoutine shootAndPark() {
        PoseFactory poses = poses();
        Pose startPose = poses.of(60.0, botStartYIn, -90.0);
        Pose firststop= poses.of(24,51,-90.0);
        Pose stopPose = poses.of(18,  96, -90.0);

        Command shoot = shootFor(shootSeconds);
        Command waitAfterShooting = waitSeconds(waitAfterShootingSeconds);
        Command drivetofirststop = follow(follower, line(startPose,firststop).constant(startPose));
        Command driveToStop = follow(follower, line(firststop, stopPose).constant(startPose));
        Command shootThenMoveRoute = sequential( shoot,
                waitAfterShooting, drivetofirststop, driveToStop);
        AutoRoutine shootAndParkRoutine = new AutoRoutine(startPose, shootThenMoveRoute);
        return shootAndParkRoutine;
    }


    /**
     * The launcher spun up and fed once fast enough, and the intake boosted
     * while it feeds, for {@code seconds}; then both stop.
     */
    Command shootFor(double seconds) {
        Command fire = launcher.fireWhile(this::alwaysFire);
        Command boostTheIntakeWhileFeeding = intake.run(this::intakeBoostWhileFeeding);
        Command shootUntilTheTimeIsUp = deadline(waitSeconds(seconds), fire,
                boostTheIntakeWhileFeeding);
        return shootUntilTheTimeIsUp;
    }

    /** The intake pulling in at full speed for {@code seconds}; then it stops. */
    Command intakeFor(double seconds) {
        Command pullIn = intake.run(this::fullSpeedIn);
        Command intakeUntilTheTimeIsUp = deadline(waitSeconds(seconds), pullIn);
        return intakeUntilTheTimeIsUp;
    }

    /**
     * Done once {@code seconds} have passed on {@link #clock}, counted from
     * when it starts. Ivy's own waitMs reads the wall clock, which a test's
     * simulated clock does not move.
     */
    Command waitSeconds(double seconds) {
        long waitNanos = (long) (seconds * 1e9);
        Command waitFromWhenItStarts = lazy(() -> {
            long doneAt = clock.getAsLong() + waitNanos;
            return waitUntil(() -> clock.getAsLong() >= doneAt);
        });
        return waitFromWhenItStarts;
    }

    /** {@link #shootFor} fires the whole time; its deadline stops it. */
    boolean alwaysFire() {
        return true;
    }

    /** goBILDA's boost while the launcher feeds, and nothing otherwise. */
    double intakeBoostWhileFeeding() {
        double boost = 0;
        if (launcher.feeding()) {
            boost = Launcher.INTAKE_BOOST_SPEED_FRACTION;
        }
        return boost;
    }

    /** {@link #intakeFor} pulls in at full speed. */
    double fullSpeedIn() {
        return Intake.FULL_IN_SPEED_FRACTION;
    }
}
