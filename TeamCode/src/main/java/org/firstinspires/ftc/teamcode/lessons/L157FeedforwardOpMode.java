package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.commands.Commands;
import com.pedropathing.math.Twist;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.firstinspires.ftc.teamcode.pedro.Constants;

// When on L157S030, see
// https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l157.html#l157s030
// for what to do here.
//import org.firstinspires.ftc.teamcode.mytry.l157.StickShaper;

/**
 * L157: the sticks ask for a speed, and the drivetrain sends the volts it
 * takes. The sticks mean the field, until the right bumper is held. While it
 * is held, they mean the robot.
 *
 * <p>Each stick is shaped, then multiplied by the fastest that way can go, so
 * a stick all the way over asks for top speed. The fastest is worked out from
 * L153's kS and kV on a 12 V battery.
 *
 * <p>Passes when: LessonsTest.l157_theRobotGoesTheSpeedTheStickAsksFor
 */
@TeleOp(name = "L157 Feedforward", group = "Lessons")
// When on L157S020, see
// https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l157.html#l157s020
// for what to do here.
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L157FeedforwardOpMode extends CorbelsTeleOp {

    /** The battery the top speeds are worked out for, in volts. */
    static final double NOMINAL_VOLTS = 12.0;

    // When on L157S030, see
    // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l157.html#l157s030
    // for what to do here.
//    StickShaper stickShaper = new StickShaper(0.05);

    L157FeedforwardDriveTrain drivetrain;

    /** The fastest each way can go: kS takes its volts, and kV turns the rest into speed. */
    double maxForward_ips = (NOMINAL_VOLTS - Constants.kSForwardVolts)
            / Constants.kVForwardVoltSecondsPerInch;
    double maxStrafe_ips = (NOMINAL_VOLTS - Constants.kSStrafeVolts)
            / Constants.kVStrafeVoltSecondsPerInch;
    double maxTurn_radps = (NOMINAL_VOLTS - Constants.kSTurnVolts)
            / Constants.kVTurnVoltSecondsPerRadian;

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init() {
        initBefore();

        drivetrain = new L157FeedforwardDriveTrain(hardware);
        // When on L157S040, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l157.html#l157s040
        // for what to do here.

        initAfter(drivetrain);
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void start() {
        startBefore();
        Command driveFromTheSticksEveryLoop = Commands.infinite(this::driveFromTheSticks);
        Scheduler.schedule(driveFromTheSticksEveryLoop);
        startAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();
        loopAfter();
    }

    /** Once every loop, from PLAY until STOP: the scheduler runs it. */
    void driveFromTheSticks() {
        double forwardFraction = -gamepad1.left_stick_y;
        double leftFraction = -gamepad1.left_stick_x;
        double turnFraction = -gamepad1.right_stick_x;
        // When on L157S030, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l157.html#l157s030
        // for what to do here.
//        forwardFraction = stickShaper.shaped(forwardFraction);
//        leftFraction = stickShaper.shaped(leftFraction);
//        turnFraction = stickShaper.shaped(turnFraction);

        double turnCcw_radps = turnFraction * maxTurn_radps;
        double forwardX_ips;
        double strafeY_ips;
        if (gamepad1.right_bumper) {
            forwardX_ips = forwardFraction * maxForward_ips;
            strafeY_ips = leftFraction * maxStrafe_ips;
        } else {
            // Up the field and across it both go to the forward top speed. Turned
            // into the robot's own directions, a request that has become mostly
            // strafe can be more than the robot has, and the drivetrain cuts it.
            double fieldX_ips = forwardFraction * maxForward_ips;
            double fieldY_ips = leftFraction * maxForward_ips;
            double heading_rad = follower.pose().heading();
            double sinHeading = Math.sin(heading_rad);
            double cosHeading = Math.cos(heading_rad);
            forwardX_ips = fieldX_ips * cosHeading + fieldY_ips * sinHeading;
            strafeY_ips = -fieldX_ips * sinHeading + fieldY_ips * cosHeading;
        }
        drivetrain.speeds(forwardX_ips, strafeY_ips, turnCcw_radps);

        Twist actual = follower.localizer.twist();
        Tracker.publish("feedforward/askedForward_ips", forwardX_ips);
        Tracker.publish("feedforward/actualForward_ips", actual.vx);
        Tracker.publish("feedforward/askedStrafe_ips", strafeY_ips);
        Tracker.publish("feedforward/actualStrafe_ips", actual.vy);
        Tracker.publish("feedforward/askedTurn_radps", turnCcw_radps);
        Tracker.publish("feedforward/actualTurn_radps", actual.omega);
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void stop() {
        stopAfter();
    }
}
