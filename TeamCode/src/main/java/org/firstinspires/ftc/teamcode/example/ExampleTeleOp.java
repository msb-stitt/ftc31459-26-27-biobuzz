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
 *   <li>Holding A drives to the shooting pose near the driver at the alliance's
 *   hive, at the cell on the robot's side of the middle, and holds it there,
 *   whatever the sticks do. Letting go gives the sticks the wheels.</li>
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
 * launcher from the bumper. A starts a command that drives to the shooting
 * pose, which needs the drivetrain. A command that needs the drivetrain, the intake or
 * the launcher sets the one using it aside, and that one comes back when it ends.
 *
 * <p>Every stick, trigger and button read, and the drive asked for, go to the
 * flight log. Pedro's pose is already there, under {@code Robot/Pose}, and the
 * intake and launcher log their own.
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

    CorbelsMecanum drivetrain;
    Intake intake;
    Launcher launcher;
    Command driveFromTheSticks;
    Command runTheIntakeFromTheTriggers;
    Command fireTheLauncherFromTheBumper;
    Command driveToTheShootingPose;
    boolean aBefore;

    /**
     * Red's shooting pose at the hive's cell that is up at the start, near the
     * driver, in inches and degrees. The launcher fires out of the robot's back.
     */
    static final double A_X_IN = 15.0;
    static final double A_Y_IN = 12.0;
    static final double A_HEADING_DEG = -135.0;

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
        driveToTheShootingPose = Command.build()
                .setStart(this::startDrivingToTheShootingPose)
                .setDone(() -> !gamepad1.a)
                .setEnd(this::giveTheDriverTheWheels)
                .requiring(drivetrain);
        startAfter();
    }

    /**
     * The commands do the work, inside loopAfter(); pressing A starts the drive
     * to the shooting pose. This line is for the driver's screen.
     */
    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();
        boolean a = gamepad1.a;
        if (a && !aBefore) {
            Scheduler.schedule(driveToTheShootingPose);
        }
        aBefore = a;
        Tracker.printToDs(menu.getSummary());
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

        double bandToSetToZero = 0.05;
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

    /** The shooting pose on the robot's side, held by the follower. */
    void startDrivingToTheShootingPose() {
        Pose shootingPose = shootingPoseOnTheRobotsSide();
        follower.hold(shootingPose);
        drivetrain.releaseCommandedWheels();
    }

    /** How the drive to the shooting pose ends: the follower stops, and the sticks drive again. */
    void giveTheDriverTheWheels(EndCondition howItEnded) {
        follower.manual(0.0, 0.0, 0.0);
    }

    /**
     * A's pose at the alliance's hive, at the cell up at the start or the
     * other, whichever is on the robot's side of the hive's tip axis,
     * y = 72 in. The other is mirrored about it. Blue's are red's turned a
     * half-turn about the middle.
     */
    Pose shootingPoseOnTheRobotsSide() {
        PoseFactory poses = PoseFactory.degrees();
        if (allianceRow.getSelected() != red) {
            poses = poses.rotateAround(72.0, 72.0, 180.0);
        }
        Pose atTheCellUpAtTheStart = poses.of(A_X_IN, A_Y_IN, A_HEADING_DEG);
        Pose atTheOtherCell = poses.mirrorY(72.0).of(A_X_IN, A_Y_IN, A_HEADING_DEG);
        boolean robotOnTheFarSide = follower.pose().y() > 72.0;
        Pose shootingPose = atTheOtherCell;
        if ((atTheCellUpAtTheStart.y() > 72.0) == robotOnTheFarSide) {
            shootingPose = atTheCellUpAtTheStart;
        }
        return shootingPose;
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
