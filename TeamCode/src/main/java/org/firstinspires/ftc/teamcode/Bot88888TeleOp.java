package org.firstinspires.ftc.teamcode;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.behaviors.InterruptedBehavior;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.DriverStationMenu;
import org.firstinspires.ftc.teamcode.base.MenuRow;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * Robot 88888's TeleOp: L140 Field Relative, as solutions-l140-alliances.2
 * solves it, since 88888 is a Pinpoint and a drivetrain with no intake,
 * launcher or windmill. A Robot Controller name starting 88888, such as
 * 88888-RC, picks 88888's values; on a Robot Controller named otherwise, set
 * {@code Constants.thisRobot} to {@code BOT_88888} before running it.
 *
 * <ul>
 *   <li>Left stick drives the field: push it away from you and the robot goes
 *   away from you, whichever way it happens to be facing. The right stick's x
 *   turns.</li>
 * </ul>
 *
 * <p>Between INIT and PLAY, gamepad 1's D-pad works a menu on the Driver
 * Station: the alliance, which starts as the one 88888 Auto handed over, and
 * where the robot starts: where the auto ended, or against the wall, for a
 * teleop run with no auto first.
 *
 * <p>One command starts when PLAY is pressed and drives from the sticks until
 * STOP. A command that needs the drivetrain sets it aside, and it comes back
 * when that one ends.
 *
 * <p>Every stick read and the drive asked for go to the flight log. Pedro's
 * pose is already there, under {@code Robot/Pose}.
 */
@TeleOp(name = "88888 TeleOp", group = "88888")
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class Bot88888TeleOp extends CorbelsTeleOp {

    DriverStationMenu menu = new DriverStationMenu(
            new MenuRow("Alliance", "Red", "Blue"),
            new MenuRow("Start", "Auto End", "Wall"));

    double robotHalfLengthIn = 9.0;
    double fieldPerimeterWidthIn = 1.5;
    double botStartYIn = robotHalfLengthIn + fieldPerimeterWidthIn;

    CorbelsMecanum drivetrain;
    Command driveFromTheSticks;

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
        menu.startAt("Alliance", OpModeStorage.alliance);
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
        Tracker.publish("Alliance", alliance);
        String startPlace = menu.chosen("Start");
        if (startPlace.equals("Wall")) {
            PoseFactory poses = poses();
            Pose wall = poses.of(72.0, botStartYIn, 90.0);
            follower.setPose(wall);
        }
        driveFromTheSticks = driveWhileTheSticksAreRead();
        Scheduler.schedule(driveFromTheSticks);
        startAfter();
    }

    /** The command does the work, inside loopAfter(); this line is for the driver's screen. */
    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();
        Tracker.printToDs(menu.summary());
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

        double bandToSetToZero = 0.05;
        double fieldXSpeed;
        double fieldYSpeed;
        String alliance = menu.chosen("Alliance");
        if (alliance.equals("Red")) {
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

        double turnCcwSpeed = -rightStickX;
        turnCcwSpeed = speedShaped(turnCcwSpeed, bandToSetToZero);
        Tracker.publish("drivetrain/turnCcwSpeed", turnCcwSpeed);

        double heading_rad = follower.pose().heading();
        double sinHeading = Math.sin(heading_rad);
        double cosHeading = Math.cos(heading_rad);
        double robotForwardXSpeed = fieldXSpeed * cosHeading + fieldYSpeed * sinHeading;
        double robotStrafeYSpeed = -fieldXSpeed * sinHeading + fieldYSpeed * cosHeading;
        Tracker.publish("drivetrain/robotForwardXSpeed", robotForwardXSpeed);
        Tracker.publish("drivetrain/robotStrafeYSpeed", robotStrafeYSpeed);
        drivetrain.sticks(robotForwardXSpeed, robotStrafeYSpeed, turnCcwSpeed);
    }

    /** The drive never finishes by itself. */
    boolean runsUntilCancelled() {
        return false;
    }

    /** The red alliance's poses as they are, or turned for the blue alliance. */
    PoseFactory poses() {
        PoseFactory redPoses = PoseFactory.degrees();
        PoseFactory chosenPoses = redPoses;
        String alliance = menu.chosen("Alliance");
        if (!alliance.equals("Red")) {
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
