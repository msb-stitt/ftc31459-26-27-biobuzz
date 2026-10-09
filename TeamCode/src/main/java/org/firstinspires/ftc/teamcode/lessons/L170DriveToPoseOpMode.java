package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.HeadingHold;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.firstinspires.ftc.teamcode.pedro.Constants;

/**
 * L170: drive to a pose on a button. Press Y and the robot drives itself to
 * the target. Push a stick and the driver has it back.
 *
 * <p>Every OpMode has four methods. {@code init} runs once when INIT is
 * pressed, {@code start} once when PLAY is pressed, {@code loop} over and over
 * until STOP, and {@code stop} once at the end. The {@code Before} and
 * {@code After} calls are the robot's own housekeeping: the hardware, the
 * flight log and the live values. The code between them is the lesson's.
 *
 * <p>Passes when: LessonsTest.l170_pressingYDrivesToAPoseAndTheDriverCanTakeOver
 */
@TeleOp(name = "L170 Drive To Pose", group = "Lessons")
// When on L170S020, see
// https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l170.html#l170s020
// for what to do here.
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L170DriveToPoseOpMode extends CorbelsTeleOp {

    boolean previousButtonA;

    CorbelsMecanum drivetrain;
    HeadingHold hold = new HeadingHold(Constants.foresightConfig.headingFeedback.get());

    PoseFactory poses = PoseFactory.degrees();
    Pose target = poses.of(120.0, 72.0, 90.0);
    boolean drivingItself;

    // 'protected' to match the parent class. 'protected' allows access from this package and from sub-classes.
    @Override
    protected void bindings() {
        // When on L170S030, see
        // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l170.html#l170s030
        // for what to do here.
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

        double strafe = deadband(-leftStickX, band);
        strafe = signedSquared(strafe);
        Tracker.publish("arcade/strafe", strafe);

        boolean driverWantsControl = Math.abs(leftStickY) > band
                || Math.abs(leftStickX) > band || Math.abs(rightStickX) > band;
        // When on L170S040, see
        // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l170.html#l170s040
        // for what to do here.
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
