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
 * An example autonomous: 88888 Auto's menu and routes, and one more, Shoot
 * Then Intake, for a robot with goBILDA's StarterBot intake and launcher, such
 * as 37523. On a robot without them it stops at INIT, when it looks them up.
 *
 * <p>Shoot Then Intake drives forward 6 inches and holds there; shoots for 10
 * seconds, then stops shooting; waits 3 seconds; strafes 24 inches to the
 * robot's left and holds there; then runs the intake in for 3 seconds.
 * Shooting is what Example TeleOp's held right bumper does: the launcher spins
 * up, the windmill feeds once it is fast enough, and the intake gets goBILDA's
 * boost while it feeds.
 *
 * <p>Between INIT and PLAY, gamepad 1's D-pad works a menu on the Driver
 * Station: the alliance, and the auto, which is Nothing until another is
 * chosen. The poses are written for the red alliance. For the blue alliance,
 * {@code poses()} turns each one a half-turn about the field's middle, at
 * (72, 72).
 *
 * <p>At PLAY the alliance is handed to Example TeleOp, which the Driver
 * Station picks when this auto ends, by itself or at the end of its 30
 * seconds. Pressing stop cancels that.
 */
@Autonomous(name = "Example Auto", group = "Example", preselectTeleOp = "Example TeleOp")
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
    MenuItem shootThenIntake = new MenuItem("SI", "Shoot Then Intake");
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
        autoRow.add(shootThenIntake);
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
        Tracker.publish("menu/alliance", allianceRow.getSelected().getFullName());
        OpModeStorage.isRed = allianceRow.getSelected() == red;

        MenuItem auto = autoRow.getSelected();
        Tracker.publish("menu/auto", auto.getFullName());
        Command chosenAuto = Command.NOOP;
        if (auto == forward24) {
            chosenAuto = driveForward24();
        }
        if (auto == forwardThenStrafe) {
            chosenAuto = driveForwardThenStrafe();
        }
        if (auto == shootThenIntake) {
            chosenAuto = shootThenIntake();
        }
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

    /**
     * Forward 6 inches and hold; shoot for 10 seconds, then stop; wait 3
     * seconds; strafe 24 inches to the robot's left and hold; intake for 3
     * seconds. The robot faces away from the wall, +y, so its left is -x.
     */
    Command shootThenIntake() {
        PoseFactory poses = poses();
        Pose start = poses.of(72.0, botStartYIn, 90.0);
        Pose shootFrom = poses.of(72.0, botStartYIn + 6.0, 90.0);
        Pose intakeAt = poses.of(48.0, botStartYIn + 6.0, 90.0);

        Command driveForwardToShoot = follow(follower, line(start, shootFrom).constant(start));
        Command holdWhileShooting = hold(follower, shootFrom);
        Command shoot = shootFor(shootSeconds);
        Command waitAfterShooting = waitSeconds(waitAfterShootingSeconds);
        Command strafeLeftToIntake = follow(follower, line(shootFrom, intakeAt).constant(start));
        Command holdWhileIntaking = hold(follower, intakeAt);
        Command runTheIntake = intakeFor(intakeSeconds);
        Command shootThenIntakeRoute = sequential(driveForwardToShoot, holdWhileShooting, shoot,
                waitAfterShooting, strafeLeftToIntake, holdWhileIntaking, runTheIntake);
        return shootThenIntakeRoute;
    }

    /**
     * The launcher spun up and fed once fast enough, and the intake boosted
     * while it feeds, for {@code seconds}; then both stop.
     *
     * <p>Each mechanism's command is built when it starts, not when the route
     * is: {@code Intake.run} and {@code Launcher.fireWhile} keep what they are
     * given in a field, so a second one built would replace the first's.
     */
    Command shootFor(double seconds) {
        Command fire = lazy(this::fireTheWholeTime);
        Command boostTheIntakeWhileFeeding = lazy(this::boostTheIntakeWhileFeeding);
        Command shootUntilTheTimeIsUp = deadline(waitSeconds(seconds), fire,
                boostTheIntakeWhileFeeding);
        return shootUntilTheTimeIsUp;
    }

    /**
     * The intake pulling in at full speed for {@code seconds}, built when it
     * starts, as {@link #shootFor}'s are; then it stops.
     */
    Command intakeFor(double seconds) {
        Command pullIn = lazy(this::pullInAtFullSpeed);
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

    /** {@link #shootFor}'s launcher command. */
    Command fireTheWholeTime() {
        Command fire = launcher.fireWhile(this::alwaysFire);
        return fire;
    }

    /** {@link #shootFor}'s intake command. */
    Command boostTheIntakeWhileFeeding() {
        Command boost = intake.run(this::intakeBoostWhileFeeding);
        return boost;
    }

    /** {@link #intakeFor}'s intake command. */
    Command pullInAtFullSpeed() {
        Command pullIn = intake.run(this::fullSpeedIn);
        return pullIn;
    }

    /** {@link #shootFor} fires the whole time; its deadline stops it. */
    boolean alwaysFire() {
        return true;
    }

    /** goBILDA's boost while the launcher feeds, and nothing otherwise. */
    double intakeBoostWhileFeeding() {
        double boost = 0;
        if (launcher.feeding()) {
            boost = Launcher.INTAKE_BOOST;
        }
        return boost;
    }

    /** {@link #intakeFor} pulls in at full speed. */
    double fullSpeedIn() {
        return Intake.FULL_SPEED_IN;
    }
}
