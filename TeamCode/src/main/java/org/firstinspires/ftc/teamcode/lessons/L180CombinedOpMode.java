package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.behaviors.EndCondition;
import com.pedropathing.ivy.behaviors.InterruptedBehavior;
import com.pedropathing.ivy.commands.Commands;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.HeadingHold;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.firstinspires.ftc.teamcode.pedro.Constants;

/**
 * L180: everything at once. The sticks drive field relative and hold the
 * heading they were left at. The right bumper means the robot's own
 * directions. A aims at 45 degrees while the driver drives. Y drives to the
 * target until a stick moves. Your encoder localizer runs beside the robot's
 * own.
 *
 * <p>Every OpMode has four methods. {@code init} runs once when INIT is
 * pressed, {@code start} once when PLAY is pressed, {@code loop} over and over
 * until STOP, and {@code stop} once at the end. The {@code Before} and
 * {@code After} calls are the robot's own housekeeping: the hardware, the
 * flight log and the live values. {@code start} schedules the lesson's code as
 * a command, and the scheduler runs it inside {@code loopAfter()}, once every
 * loop.
 *
 * <p>Passes when: LessonsTest.l180_everythingTogether
 */
@TeleOp(name = "L180 Combined", group = "Lessons")
// When on L180S020, see
// https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l180.html#l180s020
// for what to do here.
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L180CombinedOpMode extends CorbelsTeleOp {

    boolean previousButtonY;
    boolean previousButtonAForAiming;

    CorbelsMecanum drivetrain;
    HeadingHold hold = new HeadingHold(Constants.foresightConfig.headingFeedback.get());

    PoseFactory poses = PoseFactory.degrees();
    Pose target = poses.of(120.0, 72.0, 90.0);
    Command driveToTheTarget;
    Command aimAtFortyFive;

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
        Command driveFromTheSticksEveryLoop = Commands.infinite(this::driveFromTheSticks)
                .requiring(drivetrain)
                .setInterruptedBehavior(InterruptedBehavior.SUSPEND);
        Scheduler.schedule(driveFromTheSticksEveryLoop);
        driveToTheTarget = Command.build()
                .setStart(this::startDrivingToTheTarget)
                .setExecute(this::letTheFollowerDrive)
                .setDone(this::driverWantsControl)
                .setEnd(this::giveTheDriverTheWheels)
                .requiring(drivetrain);
        aimAtFortyFive = Commands.instant(this::aimAtFortyFiveDegrees);
        startAfter();
    }

    /** The buttons that schedule a command. */
    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();

        boolean buttonY = gamepad1.y;
        boolean buttonYJustPressed = buttonY && !previousButtonY;
        if (buttonYJustPressed) {
            Scheduler.schedule(driveToTheTarget);
        }
        previousButtonY = buttonY;

        boolean buttonA = gamepad1.a;
        boolean buttonAJustPressed = buttonA && !previousButtonAForAiming;
        if (buttonAJustPressed) {
            Scheduler.schedule(aimAtFortyFive);
        }
        previousButtonAForAiming = buttonA;

        loopAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void shadowLocalizers() {
        // When on L180S030, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l180.html#l180s030
        // for what to do here.
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void stop() {
        stopAfter();
    }

    /**
     * Once every loop, from PLAY until STOP. A command that also needs the
     * drivetrain sets it aside while it runs, and it comes back when that ends.
     */
    void driveFromTheSticks() {
        double leftStickY = gamepad1.left_stick_y;
        Tracker.publish("stick/leftY", leftStickY);
        double leftStickX = gamepad1.left_stick_x;
        Tracker.publish("stick/leftX", leftStickX);
        double rightStickX = gamepad1.right_stick_x;
        Tracker.publish("stick/rightX", rightStickX);

        double band = 0.05;
        double forward = deadband(-leftStickY, band);
        forward = signedSquared(forward);
        Tracker.publish("arcade/forward", forward);
        double turn = deadband(-rightStickX, band);
        turn = signedSquared(turn);
        Tracker.publish("arcade/turn", turn);
        turn = hold.turn(follower, turn);
        Tracker.publish("heading/holding", hold.target() != null);
        if (hold.target() != null) {
            Tracker.publish("heading/target_deg", Math.toDegrees(hold.target()));
        }

        double strafe = deadband(-leftStickX, band);
        strafe = signedSquared(strafe);
        Tracker.publish("arcade/strafe", strafe);

        Tracker.publish("drive/mode", "DRIVER");
        double heading = follower.pose().heading();
        double robotForward = forward * Math.cos(heading) + strafe * Math.sin(heading);
        double robotStrafe = -forward * Math.sin(heading) + strafe * Math.cos(heading);
        boolean robotRelative = gamepad1.right_bumper;
        Tracker.publish("drive/robotRelative", robotRelative);
        if (robotRelative) {
            drivetrain.sticks(forward, strafe, turn);
        } else {
            drivetrain.sticks(robotForward, robotStrafe, turn);
        }
    }

    /** How the drive to the target begins. */
    void startDrivingToTheTarget() {
        follower.hold(target);
        drivetrain.releaseCommandedWheels();
    }

    /** Each loop of the drive to the target: the follower drives, not the sticks. */
    void letTheFollowerDrive() {
        Tracker.publish("drive/mode", "AUTO");
    }

    /** The drive to the target is done when any stick is pushed past the deadband. */
    boolean driverWantsControl() {
        double band = 0.05;
        double leftStickYMagnitude = Math.abs(gamepad1.left_stick_y);
        double leftStickXMagnitude = Math.abs(gamepad1.left_stick_x);
        double rightStickXMagnitude = Math.abs(gamepad1.right_stick_x);
        boolean driverWantsControl = leftStickYMagnitude > band
                || leftStickXMagnitude > band || rightStickXMagnitude > band;
        return driverWantsControl;
    }

    /**
     * How the drive to the target ends: the follower stops, the heading it held
     * before Y is forgotten, and the sticks drive again.
     */
    void giveTheDriverTheWheels(EndCondition howItEnded) {
        follower.manual(0.0, 0.0, 0.0);
        hold.release();
        Tracker.publish("drive/mode", "DRIVER");
    }

    /** A's command: any drive to the target is cancelled, and the hold aims at 45 degrees. */
    void aimAtFortyFiveDegrees() {
        Scheduler.cancel(driveToTheTarget);
        double fortyFiveDegrees = Math.toRadians(45.0);
        hold.aimAt(fortyFiveDegrees);
    }

    double deadband(double value, double band) {
        double alwaysPositiveMagnitude = Math.abs(value);
        boolean isCloseToZero = alwaysPositiveMagnitude < band;
        if (isCloseToZero) {
            return 0.0;
        }
        return value;
    }

    double signedSquared(double value) {
        return value * Math.abs(value);
    }
}
