package org.firstinspires.ftc.teamcode.example;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.behaviors.EndCondition;
import com.pedropathing.ivy.behaviors.InterruptedBehavior;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.Intake;
import org.firstinspires.ftc.teamcode.Launcher;
import org.firstinspires.ftc.teamcode.OpModeStorage;
import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.DriverStationMenu;
import org.firstinspires.ftc.teamcode.base.MenuItem;
import org.firstinspires.ftc.teamcode.base.MenuRow;
import org.firstinspires.ftc.teamcode.base.OptionMenu;
import org.firstinspires.ftc.teamcode.base.Tracker;

import java.util.function.BooleanSupplier;
import java.util.function.Function;

/**
 * An example TeleOp: 88888 TeleOp's drive and menu, with goBILDA's StarterBot
 * intake and launcher, for a robot that has them, such as 37523. On a robot
 * without them it stops at INIT, when it looks them up.
 *
 * <ul>
 *   <li>Left stick drives the field: push it away from you and the robot goes
 *   away from you, whichever way it happens to be facing. The right stick's x
 *   turns.</li>
 *   <li>Hold the left bumper to drive the robot instead: push the left stick
 *   away from you and the robot drives its front forward, whichever way it
 *   faces.</li>
 *   <li>The right trigger, less the left trigger, runs the intake.</li>
 *   <li>Holding the right bumper spins the launcher up and feeds it.</li>
 *   <li>Holding X, Y or A, with the sticks centred, drives to that button's
 *   shooting pose at the alliance's hive and shoots there: X square to the
 *   hive, A nearer the driver, Y A's mirror. The pose is the one within 36 in
 *   of the robot, at either of the hive's cells; with none, nothing happens.
 *   The launcher spins up the first time the robot is within 2 in and 3° of
 *   the pose, and feeds while it is within them. A stick pushed ends it all;
 *   with the button still held, centring the sticks starts it again.</li>
 * </ul>
 *
 * <p>Between INIT and PLAY, gamepad 1's D-pad works a menu on the Driver
 * Station: the alliance, which starts as the one Example Auto handed over, and
 * where the robot starts: where the auto ended, or against the wall, for a
 * teleop run with no auto first. Until an auto has run, where the auto ended
 * is the wall too. The pose is set during INIT, each time the menu's choice
 * changes, so the flight log shows it before PLAY.
 *
 * <p>Three commands start when PLAY is pressed and run until STOP: one drives
 * from the sticks, one runs the intake from the triggers, one fires the
 * launcher from the bumper. X, Y and A each start a command that drives to its
 * pose and fires, which needs the drivetrain and the launcher. A command that needs the drivetrain, the intake or
 * the launcher sets the one using it aside, and that one comes back when it ends.
 *
 * <p>Every stick, trigger and button read, and the drive asked for, go to the
 * flight log. Pedro's pose is already there, under {@code Robot/Pose}, and the
 * intake and launcher log their own. The Driver Station shows the shooting
 * pose chosen, 0, 0 and 0° with none, and whether the robot is within it.
 */
@TeleOp(name = "Example TeleOp", group = "0 Competition")
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class ExampleTeleOp extends CorbelsTeleOp {

    /** Where the mechanisms come from. Tests hand in fakes. */
    static Function<HardwareMap, Intake> intakes = Intake::from;
    static Function<HardwareMap, Launcher> launchers = Launcher::from;

    MenuItem red = new MenuItem("R", "Red");
    MenuItem blue = new MenuItem("B", "Blue");
    MenuRow allianceRow = new MenuRow("Alliance");
    MenuItem autoEnd = new MenuItem("A", "Auto End");
    MenuItem wall = new MenuItem("W", "Wall");
    MenuRow startRow = new MenuRow("Start");
    OptionMenu optionMenu = new OptionMenu();
    DriverStationMenu menu = new DriverStationMenu(optionMenu);

    /** A stick nearer centre than this reads as centred, and drives nothing. */
    static final double BAND_TO_SET_TO_ZERO = 0.05;
    /** A button's shooting pose is chosen only within this of the robot, in inches. */
    static final double CHOOSE_A_POSE_WITHIN_IN = 36.0;
    /** The robot is at its shooting pose within this, in inches... */
    static final double AT_THE_POSE_WITHIN_IN = 2.0;
    /** ...and this, in degrees. */
    static final double AT_THE_POSE_WITHIN_DEG = 3.0;

    /**
     * Red's shooting poses at the hive's cell that is up at the start: X square
     * to the hive, A nearer the driver, and Y A mirrored about X's line. The
     * launcher fires out of the robot's back.
     */
    static final PoseFactory RED_POSES = PoseFactory.degrees();
    static final Pose RED_X = RED_POSES.of(60.0, 10.5, -90.0);
    static final Pose RED_A = RED_POSES.of(15.0, 12.0, -135.0);
    static final Pose RED_Y = mirrored(RED_A, RED_X);
    /** The red hive's tip axis, y = 72 in: the other cell's poses are mirrored about it. */
    static final Pose RED_TIP_AXIS = RED_POSES.of(72.0, 72.0, 0.0);

    CorbelsMecanum drivetrain;
    Intake intake;
    Launcher launcher;
    Command driveFromTheSticks;
    Command runTheIntakeFromTheTriggers;
    Command fireTheLauncherFromTheBumper;
    Command shootFromX;
    Command shootFromY;
    Command shootFromA;
    boolean xWantedBefore;
    boolean yWantedBefore;
    boolean aWantedBefore;
    /** The shooting pose being driven to; null with none. */
    Pose shootingPose;
    /** True from the first loop at the shooting pose until the command ends. */
    boolean reachedTheShootingPose;

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init() {
        // The alliance the auto handed over is added first, so it starts selected.
        if (OpModeStorage.isRed) {
            allianceRow.add(red);
            allianceRow.add(blue);
        } else {
            allianceRow.add(blue);
            allianceRow.add(red);
        }
        startRow.add(autoEnd);
        startRow.add(wall);
        optionMenu.add(allianceRow);
        optionMenu.add(startRow);

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

    /** Again and again between INIT and PLAY: the menu, and the pose it chooses. */
    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init_loop() {
        menu.loop(gamepad1);
        if (menu.choiceChanged()) {
            Pose startPose = startPose();
            follower.setPose(startPose);
        }
        initLoopAfter();
    }

    /** Where the auto ended, once an auto has run; otherwise against the wall. */
    Pose startPose() {
        boolean fromTheAutosEnd = startRow.getSelected() == autoEnd && OpModeStorage.autoHasRun;
        Pose startPose = wallPose();
        if (fromTheAutosEnd) {
            startPose = OpModeStorage.autonomousEndPose;
        }
        return startPose;
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void start() {
        startBefore();
        OpModeStorage.isRed = allianceRow.getSelected() == red;
        Tracker.publish("Alliance", allianceRow.getSelected().getFullName());
        driveFromTheSticks = driveWhileTheSticksAreRead();
        runTheIntakeFromTheTriggers = intake.run(this::intakeSpeedFractionThisLoop);
        fireTheLauncherFromTheBumper = launcher.fireWhile(this::rightBumperIsHeld);
        Scheduler.schedule(driveFromTheSticks);
        Scheduler.schedule(runTheIntakeFromTheTriggers);
        Scheduler.schedule(fireTheLauncherFromTheBumper);
        shootFromX = shootFrom(RED_X, () -> gamepad1.x);
        shootFromY = shootFrom(RED_Y, () -> gamepad1.y);
        shootFromA = shootFrom(RED_A, () -> gamepad1.a);
        startAfter();
    }

    /**
     * The commands do the work, inside loopAfter(). X, Y and A each start theirs
     * when wanted: held with the sticks centred. The lines are for the driver's
     * screen.
     */
    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();
        xWantedBefore = startWhenFirstWanted(shootFromX, gamepad1.x, xWantedBefore);
        yWantedBefore = startWhenFirstWanted(shootFromY, gamepad1.y, yWantedBefore);
        aWantedBefore = startWhenFirstWanted(shootFromA, gamepad1.a, aWantedBefore);
        Tracker.printToDs(menu.getSummary());
        printTheShootingPoseToDs();
        loopAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void stop() {
        stopAfter();
    }

    /**
     * Drives from the sticks for as long as the OpMode runs. A command that
     * also needs the drivetrain sets it aside, and it comes back when that ends.
     */
    Command driveWhileTheSticksAreRead() {
        Command driveWhileTheSticksAreRead = Command.build()
                .setExecute(this::driveFromTheSticksThisLoop)
                .setDone(this::runsUntilCancelled)
                .requiring(drivetrain)
                .setInterruptedBehavior(InterruptedBehavior.SUSPEND);
        return driveWhileTheSticksAreRead;
    }

    /** One loop of {@link #driveWhileTheSticksAreRead()}: the sticks read now, and driven. */
    void driveFromTheSticksThisLoop() {
        double leftStickY = gamepad1.left_stick_y;
        Tracker.publish("gamepad1/leftStickY", leftStickY);
        double leftStickX = gamepad1.left_stick_x;
        Tracker.publish("gamepad1/leftStickX", leftStickX);
        double rightStickX = gamepad1.right_stick_x;
        Tracker.publish("gamepad1/rightStickX", rightStickX);
        boolean leftBumper = gamepad1.left_bumper;
        Tracker.publish("gamepad1/leftBumper", leftBumper);

        double bandToSetToZero = BAND_TO_SET_TO_ZERO;
        double turnCcwSpeed = -rightStickX;
        turnCcwSpeed = speedShaped(turnCcwSpeed, bandToSetToZero);
        Tracker.publish("drivetrain/turnCcwSpeed", turnCcwSpeed);

        double robotForwardXSpeed;
        double robotStrafeYSpeed;
        if (leftBumper) {
            // Robot relative: away from the driver is the robot's front, on either alliance.
            robotForwardXSpeed = speedShaped(-leftStickY, bandToSetToZero);
            robotStrafeYSpeed = speedShaped(-leftStickX, bandToSetToZero);
        } else {
            double fieldXSpeed;
            double fieldYSpeed;
            if (allianceRow.getSelected() == red) {
                fieldXSpeed = -leftStickY;
                fieldYSpeed = -leftStickX;
            } else {
                fieldXSpeed = leftStickY;
                fieldYSpeed = leftStickX;
            }

            fieldXSpeed = speedShaped(fieldXSpeed, bandToSetToZero);
            Tracker.publish("drivetrain/fieldXSpeed", fieldXSpeed);
            fieldYSpeed = speedShaped(fieldYSpeed, bandToSetToZero);
            Tracker.publish("drivetrain/fieldYSpeed", fieldYSpeed);

            double heading_rad = follower.pose().heading();
            double sinHeading = Math.sin(heading_rad);
            double cosHeading = Math.cos(heading_rad);
            robotForwardXSpeed = fieldXSpeed * cosHeading + fieldYSpeed * sinHeading;
            robotStrafeYSpeed = -fieldXSpeed * sinHeading + fieldYSpeed * cosHeading;
        }
        Tracker.publish("drivetrain/robotForwardXSpeed", robotForwardXSpeed);
        Tracker.publish("drivetrain/robotStrafeYSpeed", robotStrafeYSpeed);
        drivetrain.sticks(robotForwardXSpeed, robotStrafeYSpeed, turnCcwSpeed);
    }

    /** The drive never finishes by itself. */
    boolean runsUntilCancelled() {
        return false;
    }

    /** The right bumper spins the launcher up while it is held. */
    boolean rightBumperIsHeld() {
        boolean rightBumper = gamepad1.right_bumper;
        Tracker.publish("button/rightBumper", rightBumper);
        return rightBumper;
    }

    /**
     * The right trigger pulls in, the left pushes out, and while the launcher
     * feeds the intake gets goBILDA's boost.
     */
    double intakeSpeedFractionThisLoop() {
        double pullInSpeedFraction = gamepad1.right_trigger;
        double pushOutSpeedFraction = gamepad1.left_trigger;
        Tracker.publish("trigger/right", pullInSpeedFraction);
        Tracker.publish("trigger/left", pushOutSpeedFraction);
        double triggersSpeedFraction = pullInSpeedFraction - pushOutSpeedFraction;
        double boostWhileFeeding = 0;
        if (launcher.feeding()) {
            boostWhileFeeding = Launcher.INTAKE_BOOST_SPEED_FRACTION;
        }
        double intakeSpeedFraction = triggersSpeedFraction + boostWhileFeeding;
        return intakeSpeedFraction;
    }

    /**
     * A button's command: drives to its shooting pose, chosen as it starts, and
     * fires there, until the button is let go or a stick is pushed.
     */
    Command shootFrom(Pose redPose, BooleanSupplier button) {
        Command driveToTheShootingPose = Command.build()
                .setStart(() -> startDrivingToTheShootingPose(redPose))
                .setExecute(this::noteWhetherAtTheShootingPose)
                .setDone(() -> shootingPose == null || !wanted(button.getAsBoolean()))
                .setEnd(this::giveTheDriverTheWheels)
                .requiring(drivetrain);
        Command fireAtTheShootingPose = launcher.fireWhile(() -> reachedTheShootingPose,
                this::atTheShootingPose);
        Command shootFrom = driveToTheShootingPose.raceWith(fireAtTheShootingPose);
        return shootFrom;
    }

    /** Starts the command the first loop it is wanted; gives back whether it is wanted now. */
    boolean startWhenFirstWanted(Command command, boolean button, boolean wantedBefore) {
        boolean wantedNow = wanted(button);
        if (wantedNow && !wantedBefore) {
            Scheduler.schedule(command);
        }
        return wantedNow;
    }

    /** A button's command is wanted while the button is held and the sticks are centred. */
    boolean wanted(boolean button) {
        boolean wanted = button && sticksAreCentred();
        return wanted;
    }

    /** Each stick read as zero after the deadband, so it drives nothing. */
    boolean sticksAreCentred() {
        boolean leftStickYCentred = deadband(gamepad1.left_stick_y, BAND_TO_SET_TO_ZERO) == 0.0;
        boolean leftStickXCentred = deadband(gamepad1.left_stick_x, BAND_TO_SET_TO_ZERO) == 0.0;
        boolean rightStickXCentred = deadband(gamepad1.right_stick_x, BAND_TO_SET_TO_ZERO) == 0.0;
        boolean centred = leftStickYCentred && leftStickXCentred && rightStickXCentred;
        return centred;
    }

    /** The button's pose within reach, if any, held by the follower. */
    void startDrivingToTheShootingPose(Pose redPose) {
        shootingPose = shootingPoseWithinReach(redPose);
        reachedTheShootingPose = false;
        if (shootingPose != null) {
            follower.hold(shootingPose);
            drivetrain.releaseCommandedWheels();
        }
    }

    /** Each loop at the pose: from the first, the launcher spins up. */
    void noteWhetherAtTheShootingPose() {
        if (atTheShootingPose()) {
            reachedTheShootingPose = true;
        }
    }

    /** How the command ends: the follower stops, and the sticks drive again. */
    void giveTheDriverTheWheels(EndCondition howItEnded) {
        if (shootingPose != null) {
            follower.manual(0.0, 0.0, 0.0);
        }
        shootingPose = null;
        reachedTheShootingPose = false;
    }

    /**
     * Of the button's two poses at the alliance's hive, one at each cell, the
     * one within {@link #CHOOSE_A_POSE_WITHIN_IN} of the robot; null with none.
     */
    Pose shootingPoseWithinReach(Pose redPose) {
        Pose atTheCellUpAtTheStart = forTheAlliance(redPose);
        Pose atTheOtherCell = forTheAlliance(mirrored(redPose, RED_TIP_AXIS));
        Pose robot = follower.pose();
        Pose withinReach = null;
        double nearest_in = CHOOSE_A_POSE_WITHIN_IN;
        for (Pose candidate : new Pose[]{atTheCellUpAtTheStart, atTheOtherCell}) {
            double away_in = Math.hypot(candidate.x() - robot.x(), candidate.y() - robot.y());
            if (away_in <= nearest_in) {
                withinReach = candidate;
                nearest_in = away_in;
            }
        }
        return withinReach;
    }

    /** True while the robot is within 2 in and 3° of the shooting pose. */
    boolean atTheShootingPose() {
        if (shootingPose == null) {
            return false;
        }
        Pose robot = follower.pose();
        double away_in = Math.hypot(shootingPose.x() - robot.x(), shootingPose.y() - robot.y());
        double turn_rad = shootingPose.heading() - robot.heading();
        double away_deg = Math.abs(Math.toDegrees(Math.atan2(Math.sin(turn_rad), Math.cos(turn_rad))));
        boolean atThePose = away_in <= AT_THE_POSE_WITHIN_IN && away_deg <= AT_THE_POSE_WITHIN_DEG;
        return atThePose;
    }

    /** The shooting pose, 0, 0 and 0° with none, and whether the robot is at it. */
    void printTheShootingPoseToDs() {
        Pose shown = new Pose(0.0, 0.0, 0.0);
        if (shootingPose != null) {
            shown = shootingPose;
        }
        double heading_rad = Math.atan2(Math.sin(shown.heading()), Math.cos(shown.heading()));
        Tracker.printToDs("Shooting pose  x %.1f  y %.1f  h %.0f deg  at it %b",
                shown.x(), shown.y(), Math.toDegrees(heading_rad), atTheShootingPose());
    }

    /** A red pose as it is, or turned a half-turn about the field's middle for blue. */
    Pose forTheAlliance(Pose redPose) {
        PoseFactory poses = RED_POSES;
        if (allianceRow.getSelected() != red) {
            poses = RED_POSES.rotateAround(72.0, 72.0, 180.0);
        }
        Pose forTheAlliance = poses.of(redPose.x(), redPose.y(), Math.toDegrees(redPose.heading()));
        return forTheAlliance;
    }

    /**
     * A pose mirrored about a line, given as a pose on the line facing along
     * it: its place reflected across the line, and its heading too.
     */
    static Pose mirrored(Pose pose, Pose line) {
        double twiceTheLine_rad = 2.0 * line.heading();
        double cos = Math.cos(twiceTheLine_rad);
        double sin = Math.sin(twiceTheLine_rad);
        double dx = pose.x() - line.x();
        double dy = pose.y() - line.y();
        double x = line.x() + dx * cos + dy * sin;
        double y = line.y() + dx * sin - dy * cos;
        double heading_rad = twiceTheLine_rad - pose.heading();
        double wrapped_rad = Math.atan2(Math.sin(heading_rad), Math.cos(heading_rad));
        Pose mirrored = new Pose(x, y, wrapped_rad);
        return mirrored;
    }

    /** Against the alliance's wall, facing it: the default start, where Example Auto starts. */
    Pose wallPose() {
        boolean isRed = allianceRow.getSelected() == red;
        Pose wallPose = OpModeStorage.defaultStartPose(isRed);
        return wallPose;
    }

    /** A value close to zero, inside the band, is zero; any other is as it is. */
    double deadband(double value, double band) {
        double alwaysPositiveMagnitude = Math.abs(value);
        boolean isCloseToZero = alwaysPositiveMagnitude < band;
        if (isCloseToZero) {
            return 0.0;
        }
        return value;
    }

    /** The value squared, keeping its sign, for finer control at low speed. */
    double signedSquared(double value) {
        double alwaysPositiveValue = Math.abs(value);
        double valueSquaredKeepingTheSign = value * alwaysPositiveValue;
        return valueSquaredKeepingTheSign;
    }

    /** The deadband, then the signed square. */
    double speedShaped(double value, double band) {
        double deadbanded = deadband(value, band);

        // LOOKHERE - change shapedSpeed to be
        // deadbanded * deadbanded * deadbanded
        // to have finer low speed control
        // e.g.:
        // double shapedSpeed = deadbanded * deadbanded * deadbanded;
        // this is finer because
        // 1/4 * 1/4 = 1/16
        // while
        // 1/4 * 1/4 * 1/4 = 1/64
        double shapedSpeed = signedSquared(deadbanded);
        return shapedSpeed;
    }
}
