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
 * L130: two legs in a row. Drive forward, then slide sideways to the right
 * without turning, and hold there.
 *
 * <p>Passes when: LessonsTest.l130_autoDrivesForwardThenStrafesSideways
 */
@Autonomous(name = "L130 Forward Then Strafe", group = "Lessons")
// When on L130S020, see
// https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l130.html#l130s020
// for what to do here.
@Disabled
// public: the robot only runs an OpMode that is public.
public class L130ForwardThenStrafeOpMode extends CorbelsAuto {

    PoseFactory poses = PoseFactory.degrees();

    double robotHalfLengthIn = 9.0;
    double fieldPerimeterWidthIn = 1.5;
    double botStartYIn = robotHalfLengthIn + fieldPerimeterWidthIn;
    Pose start = poses.of(72, botStartYIn, 90);
    Pose corner = poses.of(72, 72, 90);
    Pose end = poses.of(96, 72, 90);

    CorbelsMecanum drivetrain;

    // public: OpMode's own init is public, so this one has to be too.
    @Override
    public void init() {
        initBefore();
        drivetrain = new CorbelsMecanum(hardware);
        initAfter(drivetrain);
    }

    // public: OpMode's own start is public, so this one has to be too.
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
    // public: OpMode's own loop is public, so this one has to be too.
    @Override
    public void loop() {
        loopBefore();
        Tracker.printPoseSpeedLoopToDs(follower);
        loopAfter();
    }

    // public: OpMode's own stop is public, so this one has to be too.
    @Override
    public void stop() {
        stopAfter();
    }

    // protected: CorbelsAuto's own startPose is protected, so this one has to be too.
    @Override
    protected Pose startPose() {
        return start;
    }

    // protected: CorbelsAuto's own routine is protected, so this one has to be too.
    @Override
    protected Command routine() {
        return sequential(
                follow(follower, line(start, corner).constant(start))
                // When on L130S030, see
                // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l130.html#l130s030
                // for what to do here.
        );
    }
}
