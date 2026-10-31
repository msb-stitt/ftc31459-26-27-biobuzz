package org.firstinspires.ftc.teamcode.lessons;

import static com.pedropathing.ivy.commands.Commands.instant;
import static com.pedropathing.ivy.commands.Commands.waitUntil;
import static com.pedropathing.ivy.groups.Groups.sequential;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.commands.Commands;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Twist;
import com.pedropathing.utils.Angle;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

import org.firstinspires.ftc.teamcode.base.CorbelsAuto;
import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L153: drive at fixed powers, and fit volts against speed for each way the
 * robot moves.
 *
 * <p>Each run holds one way of moving, forward, strafe or turn, at a fixed
 * power with {@code follower.manual} and no path. It stops early enough that
 * braking ends inside {@link #RUN_IN}, then holds where it is until the robot
 * stands still. The forward runs go there and back, and so do the strafe runs,
 * so the robot needs 72 inches of floor ahead of it and to its left.
 *
 * <p>Every loop, {@code measure} adds up the speed and volts in two windows,
 * the third and the last quarter of the run. A run whose two windows read
 * about the same speed was steady, and gives the fit one point.
 *
 * <p>Passes when: LessonsTest.l153_theRunsFindTheSimulatorsKsAndKv
 */
@Autonomous(name = "L153 Feedforward", group = "Lessons")
// When on L153S020, see
// https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l153.html#l153s020
// for what to do here.
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L153FeedforwardOpMode extends CorbelsAuto {

    /** The ways the robot moves: forward and back, sideways, and turning on the spot. */
    enum Axis { FORWARD, STRAFE, TURN }

    /** How far a forward or strafe run may go, braking included, in inches. */
    static final double RUN_IN = 72.0;
    /** How far a turning run turns before it stops: one whole turn, in radians. */
    static final double TURN_RAD = 2 * Math.PI;
    /** How close the two windows' speeds must be for a run to count as steady. */
    static final double STEADY_FRACTION = 0.03;

    PoseFactory poses = PoseFactory.degrees();

    /** Near a corner, facing along the wall, with the field ahead and to the left. */
    Pose start = poses.of(24.0, 24.0, 0.0);

    CorbelsMecanum drivetrain;

    /** One fit for each way the robot moves. */
    L153FeedforwardFit forwardFit = new L153FeedforwardFit();
    L153FeedforwardFit strafeFit = new L153FeedforwardFit();
    L153FeedforwardFit turnFit = new L153FeedforwardFit();

    /** The run under way: which way, at what power, and where it started. */
    Axis axis = Axis.FORWARD;
    double power;
    Pose runStart;
    double lastHeading_rad;
    boolean driving;

    /** How far the run has gone, the way it is driving, inches or radians. */
    double travelled;
    double farthest;

    /** The speed and the volts, added up over the run's two windows. */
    double earlySpeedSum;
    int earlyLoops;
    double lateSpeedSum;
    double lateVoltsSum;
    int lateLoops;

    /** Braking: the speed when the stop began, how far the run had gone, and how hard it slows. */
    double speedAtStop;
    double travelledAtStop;
    double forwardBraking_ips2 = 20.0;
    double strafeBraking_ips2 = 20.0;

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
        Command measureEveryLoop = Commands.infinite(this::measure);
        Scheduler.schedule(measureEveryLoop);
        Command runs = theRuns();
        Scheduler.schedule(runs);
        startAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();
        printTheFitsToDs();
        loopAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void stop() {
        stopAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public Pose startPose() {
        return start;
    }

    /** The runs, one after another, then the fits. */
    Command theRuns() {
        // When on L153S050, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l153.html#l153s050
        // for what to do here.
        return Command.NOOP;
    }

    /** One run: drive at a fixed power, stop in time, hold until still, and keep what it found. */
    Command oneRun(Axis runAxis, double runPower) {
        Command run = sequential(
                instant(() -> startTheRun(runAxis, runPower)),
                waitUntil(this::timeToStop),
                instant(this::stopTheRun),
                waitUntil(this::standingStill),
                instant(this::finishTheRun));
        return run;
    }

    /** Starts driving one way at one power, and clears the sums. */
    void startTheRun(Axis runAxis, double runPower) {
        axis = runAxis;
        power = runPower;
        runStart = follower.pose();
        lastHeading_rad = runStart.heading();
        travelled = 0.0;
        farthest = 0.0;
        earlySpeedSum = 0.0;
        earlyLoops = 0;
        lateSpeedSum = 0.0;
        lateVoltsSum = 0.0;
        lateLoops = 0;
        driving = true;
        if (axis == Axis.FORWARD) {
            follower.manual(power, 0.0, 0.0);
        } else if (axis == Axis.STRAFE) {
            follower.manual(0.0, power, 0.0);
        } else {
            follower.manual(0.0, 0.0, power);
        }
    }

    /** Where to stop driving: a whole turn, or early enough that braking ends inside the run. */
    double stopAt() {
        double stopAt = TURN_RAD;
        if (axis == Axis.FORWARD) {
            stopAt = RUN_IN - speed() * speed() / (2 * forwardBraking_ips2);
        } else if (axis == Axis.STRAFE) {
            stopAt = RUN_IN - speed() * speed() / (2 * strafeBraking_ips2);
        }
        return stopAt;
    }

    boolean timeToStop() {
        return travelled >= stopAt();
    }

    /** Holds the pose where the robot is now, so it brakes and stays. */
    void stopTheRun() {
        driving = false;
        speedAtStop = speed();
        travelledAtStop = travelled;
        follower.hold(follower.pose());
    }

    /** True once the robot has stopped sliding and turning. */
    boolean standingStill() {
        Twist speed = follower.localizer.twist();
        boolean still = Math.abs(speed.vx) < 0.5 && Math.abs(speed.vy) < 0.5
                && Math.abs(speed.omega) < 0.01;
        return still;
    }

    /** How fast the robot goes the way it is driving: inches or radians per second. */
    double speed() {
        Twist twist = follower.localizer.twist();
        double speed = Math.abs(twist.omega);
        if (axis == Axis.FORWARD) {
            speed = Math.abs(twist.vx);
        } else if (axis == Axis.STRAFE) {
            speed = Math.abs(twist.vy);
        }
        return speed;
    }

    /** Every loop: how far the run has gone, and the speed and volts in each window. */
    void measure() {
        if (runStart == null) {
            return;
        }
        Pose pose = follower.pose();
        double xStep_in = pose.x() - runStart.x();
        double yStep_in = pose.y() - runStart.y();
        double sinHeading = Math.sin(runStart.heading());
        double cosHeading = Math.cos(runStart.heading());
        lastHeading_rad += Angle.normalizeSigned(pose.heading() - lastHeading_rad);
        double along = lastHeading_rad - runStart.heading();
        if (axis == Axis.FORWARD) {
            along = xStep_in * cosHeading + yStep_in * sinHeading;
        } else if (axis == Axis.STRAFE) {
            along = -xStep_in * sinHeading + yStep_in * cosHeading;
        }
        travelled = Math.abs(along);
        farthest = Math.max(farthest, travelled);

        if (driving) {
            double stopAt = stopAt();
            if (travelled >= 0.5 * stopAt && travelled < 0.75 * stopAt) {
                earlySpeedSum += speed();
                earlyLoops += 1;
            } else if (travelled >= 0.75 * stopAt) {
                lateSpeedSum += speed();
                lateVoltsSum += Math.abs(power) * hardware.batteryVolts();
                lateLoops += 1;
            }
        }
    }

    /** After a run: how hard it braked, and, if it was steady, a point for its fit. */
    void finishTheRun() {
        double brakingDistance = farthest - travelledAtStop;
        if (brakingDistance > 0 && axis == Axis.FORWARD) {
            forwardBraking_ips2 = speedAtStop * speedAtStop / (2 * brakingDistance);
        } else if (brakingDistance > 0 && axis == Axis.STRAFE) {
            strafeBraking_ips2 = speedAtStop * speedAtStop / (2 * brakingDistance);
        }

        // When on L153S060, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l153.html#l153s060
        // for what to do here.
    }

    /** What each fit found, for AdvantageScope. */
    void publishTheFits() {
        Tracker.publish("Fit/forward/runs", forwardFit.count);
        Tracker.publish("Fit/forward/kS_V", forwardFit.kS());
        Tracker.publish("Fit/forward/kV_Vs_per_in", forwardFit.kV());
        Tracker.publish("Fit/strafe/runs", strafeFit.count);
        Tracker.publish("Fit/strafe/kS_V", strafeFit.kS());
        Tracker.publish("Fit/strafe/kV_Vs_per_in", strafeFit.kV());
        Tracker.publish("Fit/turn/runs", turnFit.count);
        Tracker.publish("Fit/turn/kS_V", turnFit.kS());
        Tracker.publish("Fit/turn/kV_Vs_per_rad", turnFit.kV());
    }

    /** Every loop, on the Driver Station: each fit so far, NaN until it has two steady runs. */
    void printTheFitsToDs() {
        Tracker.printToDs("forward  %d runs  kS %.3f V  kV %.4f V s/in",
                forwardFit.count, forwardFit.kS(), forwardFit.kV());
        Tracker.printToDs("strafe  %d runs  kS %.3f V  kV %.4f V s/in",
                strafeFit.count, strafeFit.kS(), strafeFit.kV());
        Tracker.printToDs("turn  %d runs  kS %.3f V  kV %.4f V s/rad",
                turnFit.count, turnFit.kS(), turnFit.kV());
    }
}
