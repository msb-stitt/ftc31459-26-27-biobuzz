package org.firstinspires.ftc.teamcode.lessons;

import static com.pedropathing.api.Paths.line;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.ivy.Command;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

import org.firstinspires.ftc.teamcode.base.CorbelsAuto;
import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L125: the first autonomous. Drive 24 inches forward, and stop there.
 *
 * <p>An autonomous puts its driving in {@code routine}, and the follower does
 * the driving. Nobody touches a stick.
 *
 * <p>Passes when: LessonsTest.l125_autoDrives24InchesForwardAndStops
 */
@Autonomous(name = "L125 Drive 24", group = "Lessons")
// When on L125S020, see
// https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l125.html#l125s020
// for what to do here.
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L125Drive24OpMode extends CorbelsAuto {

    PoseFactory poses = PoseFactory.degrees();

    double robotHalfLengthIn = 9.0;
    double fieldPerimeterWidthIn = 1.5;
    double botStartYIn = robotHalfLengthIn + fieldPerimeterWidthIn;
    Pose start = poses.of(72, botStartYIn, 90);
    Pose end = poses.of(72, botStartYIn + 24, 90);

    CorbelsMecanum drivetrain;

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

    /**
     * The scheduler runs {@link #routine()} from inside {@code loopAfter()}. The
     * one line here is for the driver's screen, which shows nothing unless a
     * lesson asks it to.
     */
    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();
        Tracker.printPoseSpeedLoopToDs(follower);
        loopAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void stop() {
        stopAfter();
    }

    // 'protected' to match the parent class. 'protected' allows access from this package and from sub-classes.
    @Override
    protected Pose startPose() {
        return start;
    }

    // 'protected' to match the parent class. 'protected' allows access from this package and from sub-classes.
    @Override
    protected Command routine() {
        // When on L125S030, see
        // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l125.html#l125s030
        // for what to do here.
        return Command.NOOP;
    }
}
