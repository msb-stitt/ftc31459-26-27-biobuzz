package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.api.PoseFactory;
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
 * flight log and the live values. The code between them is the lesson's.
 *
 * <p>Passes when: LessonsTest.l180_everythingTogether
 */
@TeleOp(name = "L180 Combined", group = "Lessons")
// When on L180S020, see
// https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l180.html#l180s020
// for what to do here.
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L180CombinedOpMode extends CorbelsTeleOp {

    boolean previousButtonA;

    CorbelsMecanum drivetrain;
    HeadingHold hold = new HeadingHold(Constants.foresightConfig.headingFeedback.get());

    PoseFactory poses = PoseFactory.degrees();
    Pose target = poses.of(120.0, 72.0, 90.0);
    boolean drivingItself;

    // 'protected' to match the parent class. 'protected' allows access from this package and from sub-classes.
    @Override
    protected void bindings() {
        buttons.whenPressed(() -> gamepad1.y, Commands.instant(() -> {
            follower.hold(target);
            drivingItself = true;
        }));
        buttons.whenPressed(() -> gamepad1.a, Commands.instant(() -> {
            drivingItself = false;
            hold.aimAt(Math.toRadians(45.0));
        }));
    }

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
        startAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();

        driveTheRobot();

        boolean buttonA = gamepad1.a;
        Tracker.publish("button/a", buttonA);

        if (buttonA != previousButtonA) {
            if (buttonA) {
                Tracker.publish("button/event", "A pressed");
            } else {
                Tracker.publish("button/event", "A released");
            }
        }
        previousButtonA = buttonA;

        loopAfter();
    }

    // 'protected' to match the parent class. 'protected' allows access from this package and from sub-classes.
    @Override
    protected void shadowLocalizers() {
        // When on L180S030, see
        // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l180.html#l180s030
        // for what to do here.
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void stop() {
        stopAfter();
    }

    /**
     * The driving, in its own method so that it can stop early with return.
     * loop() still goes on to loopAfter(), where the follower updates and the
     * flight log is written.
     */
    void driveTheRobot() {
        double leftStickY = gamepad1.left_stick_y;
        Tracker.publish("stick/leftY", leftStickY);
        double rightStickY = gamepad1.right_stick_y;
        Tracker.publish("stick/rightY", rightStickY);

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

        boolean driverWantsControl = Math.abs(leftStickY) > band
                || Math.abs(leftStickX) > band || Math.abs(rightStickX) > band;
        if (drivingItself) {
            if (!driverWantsControl) {
                Tracker.publish("drive/mode", "AUTO");
                drivetrain.releaseCommandedWheels();
                return;
            }
            drivingItself = false;
            follower.manual(0.0, 0.0, 0.0);
            hold.release();
        }
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
