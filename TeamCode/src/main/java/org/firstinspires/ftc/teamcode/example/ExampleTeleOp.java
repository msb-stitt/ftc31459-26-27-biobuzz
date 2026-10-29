package org.firstinspires.ftc.teamcode.example;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
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
 * </ul>
 *
 * <p>Between INIT and PLAY, gamepad 1's D-pad works a menu on the Driver
 * Station: the alliance, which starts as the one Example Auto handed over, and
 * where the robot starts: where the auto ended, or against the wall, for a
 * teleop run with no auto first.
 *
 * <p>Three commands start when PLAY is pressed and run until STOP: one drives
 * from the sticks, one runs the intake from the triggers, one fires the
 * launcher from the bumper. A command that needs the drivetrain, the intake or
 * the launcher sets the one using it aside, and that one comes back when it ends.
 *
 * <p>Every stick, trigger and button read, and the drive asked for, go to the
 * flight log. Pedro's pose is already there, under {@code Robot/Pose}, and the
 * intake and launcher log their own.
 */
@TeleOp(name = "Example TeleOp", group = "Example")
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

    double robotHalfLengthIn = 9.0;
    double fieldPerimeterWidthIn = 1.5;
    double botStartYIn = robotHalfLengthIn + fieldPerimeterWidthIn;

    CorbelsMecanum drivetrain;
    Intake intake;
    Launcher launcher;
    Command driveFromTheSticks;
    Command runTheIntakeFromTheTriggers;
    Command fireTheLauncherFromTheBumper;

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
        OpModeStorage.isRed = allianceRow.getSelected() == red;
        Tracker.publish("Alliance", allianceRow.getSelected().getFullName());
        if (startRow.getSelected() == wall) {
            PoseFactory poses = poses();
            Pose wallPose = poses.of(72.0, botStartYIn, 90.0);
            follower.setPose(wallPose);
        }
        driveFromTheSticks = driveWhileTheSticksAreRead();
        runTheIntakeFromTheTriggers = intake.run(this::intakeSpeedThisLoop);
        fireTheLauncherFromTheBumper = launcher.fireWhile(this::rightBumperIsHeld);
        Scheduler.schedule(driveFromTheSticks);
        Scheduler.schedule(runTheIntakeFromTheTriggers);
        Scheduler.schedule(fireTheLauncherFromTheBumper);
        startAfter();
    }

    /** The commands do the work, inside loopAfter(); this line is for the driver's screen. */
    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();
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
    double intakeSpeedThisLoop() {
        double pullInSpeed = gamepad1.right_trigger;
        double pushOutSpeed = gamepad1.left_trigger;
        Tracker.publish("trigger/right", pullInSpeed);
        Tracker.publish("trigger/left", pushOutSpeed);
        double triggersSpeed = pullInSpeed - pushOutSpeed;
        double boostWhileFeeding = 0;
        if (launcher.feeding()) {
            boostWhileFeeding = Launcher.INTAKE_BOOST;
        }
        double intakeSpeed = triggersSpeed + boostWhileFeeding;
        return intakeSpeed;
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
        double shapedSpeed = signedSquared(deadbanded);
        return shapedSpeed;
    }
}
