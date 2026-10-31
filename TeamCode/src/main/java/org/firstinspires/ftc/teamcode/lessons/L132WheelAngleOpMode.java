package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.commands.Commands;
import com.pedropathing.math.Pose;
import com.pedropathing.utils.Angle;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

import org.firstinspires.ftc.teamcode.base.CorbelsAuto;
import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.firstinspires.ftc.teamcode.base.odometry.HardwareWheelSource;

/**
 * L132: drive at a fixed power, and compare what the wheels rolled with what
 * the Pinpoint says the robot did.
 *
 * <p>The robot starts in the middle of the field. Each run drives with
 * {@code follower.manual}, at a fixed power and with no path, then stops and
 * waits for the robot to settle. Every loop, {@code measure} adds what the
 * wheels rolled and what the Pinpoint saw to the run's {@link L132WheelFit}.
 *
 * <p>Passes when: LessonsTest.l132_theRunsFindTheWheelAngleAndTurnRadius
 */
@Autonomous(name = "L132 Wheel Angle", group = "Lessons")
// When on L132S080, see
// https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s080
// for what to do here.
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L132WheelAngleOpMode extends CorbelsAuto {

    PoseFactory poses = PoseFactory.degrees();

    Pose start = poses.of(72.0, 72.0, 90.0);

    CorbelsMecanum drivetrain;
    HardwareWheelSource wheelSource;

    /** The strafe run's sums, and the mixed run's. */
    L132WheelFit strafeFit = new L132WheelFit();
    L132WheelFit mixedFit = new L132WheelFit();
    /** The run measure() adds to: none until the first run starts. */
    L132WheelFit fitting;

    /** What the wheels and the Pinpoint read last loop. */
    double[] lastWheels_in;
    Pose lastPose;

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init() {
        initBefore();
        drivetrain = new CorbelsMecanum(hardware);
        wheelSource = new HardwareWheelSource(hardware);
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

    /** The runs, one after another. */
    Command theRuns() {
        // When on L132S100, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s100
        // for what to do here.

        // When on L132S120, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s120
        // for what to do here.
        return Command.NOOP;
    }

    // When on L132S100, see
    // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s100
    // for what to do here.

    // When on L132S120, see
    // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s120
    // for what to do here.

    /** All four wheels to zero, so the robot stops. */
    void stopTheRun() {
        follower.manual(0.0, 0.0, 0.0);
    }

    /** What each run found, for AdvantageScope. */
    void publishTheFits() {
        // When on L132S100, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s100
        // for what to do here.

        // When on L132S120, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s120
        // for what to do here.
    }

    /**
     * Every loop: how far each wheel rolled since last loop, and how far the
     * Pinpoint says the robot went, the robot's way, both added to the run.
     * The follower read the Pinpoint at the end of last loop, a loop before the
     * wheels are read here. Added up over a run that ends standing still, the
     * two come out even.
     */
    void measure() {
        double[] wheels_in = wheelSource.wheelInches();
        Pose pose = follower.pose();

        if (fitting != null) {
            double[] wheelSteps_in = new double[wheels_in.length];
            for (int i = 0; i < wheels_in.length; i++) {
                wheelSteps_in[i] = wheels_in[i] - lastWheels_in[i];
            }
            fitting.addWheels(wheelSteps_in);

            double xStep_in = pose.x() - lastPose.x();
            double yStep_in = pose.y() - lastPose.y();
            double turn_rad = Angle.normalizeSigned(pose.heading() - lastPose.heading());
            double heading_rad = lastPose.heading() + turn_rad / 2.0;
            double sinHeading = Math.sin(heading_rad);
            double cosHeading = Math.cos(heading_rad);
            double strafe_in = -xStep_in * sinHeading + yStep_in * cosHeading;
            fitting.addRobot(strafe_in, turn_rad);
        }

        lastWheels_in = wheels_in;
        lastPose = pose;
    }
}
