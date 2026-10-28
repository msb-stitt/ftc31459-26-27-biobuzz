package org.firstinspires.ftc.teamcode.mytry;

import static com.pedropathing.api.Paths.line;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;
import static com.pedropathing.ivy.pedro.PedroCommands.hold;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

import org.firstinspires.ftc.teamcode.base.CorbelsAuto;
import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L135: choose the alliance and the auto on the Driver Station between INIT
 * and PLAY, from L125's and L130's.
 *
 * <p>The poses are written for the red alliance. For the blue alliance,
 * {@code poses()} turns each one a half-turn about the field's middle, at
 * (72, 72), which is where the blue alliance's twin of each place is.
 *
 * <p>Passes when: LessonsTest.l135_theMenuChoosesTheAllianceAndTheAuto
 */
@Disabled
@Autonomous(name = "L135 Choose Auto", group = "Lessons")
// When on L135S020, see
// https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s020
// for what to do here.
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L135ChooseAutoOpMode extends CorbelsAuto {

    // When on L135S030, see
    // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s030
    // for what to do here.
    L135AutoMenu menu = new L135AutoMenu(
            new L135Row("Alliance", "Red", "Blue"),
            new L135Row("Auto", "F24-Forward 24", "FS-Forward Then Strafe"));

    double robotHalfLength_in = 9.0;
    double fieldPerimeterWidth_in = 1.5;
    double botStartY_in = robotHalfLength_in + fieldPerimeterWidth_in;

    CorbelsMecanum drivetrain;

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init() {
        initBefore();
        drivetrain = new CorbelsMecanum(hardware);
        initAfter(drivetrain);
    }

    /** Again and again between INIT and PLAY. */
    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init_loop() {
        // When on L135S030, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s030
        // for what to do here.
        menu.loop(gamepad1);
        initLoopAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void start() {
        startBefore();
        // When on L135S060, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s060
        // for what to do here.
        String auto = menu.chosen("Auto");
        Command chosenAuto = Command.NOOP;
        switch (auto) {
            case "F24-Forward 24":
                chosenAuto = driveForward24();
                break;
            case "FS-Forward Then Strafe":
                chosenAuto = driveForwardThenStrafe();
                break;
        }
        Scheduler.schedule(chosenAuto);
        startAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();
        // When on L135S060, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s060
        // for what to do here.
        Tracker.printToDs(menu.summary());
        Tracker.printPoseSpeedLoopToDs(follower);
        loopAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void stop() {
        stopAfter();
    }

    /** The red alliance's poses as they are, or turned for the blue alliance. */
    PoseFactory poses() {
        PoseFactory redPoses = PoseFactory.degrees();
        PoseFactory chosenPoses = redPoses;
        // When on L135S070, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s070
        // for what to do here.
        String alliance = menu.chosen("Alliance");
        if (alliance.equals("Blue")) {
            chosenPoses = redPoses.rotateAround(72.0, 72.0, 180.0);
        }
        return chosenPoses;
    }

    /** Set at INIT, and again at PLAY, once the alliance is chosen. */
    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public Pose startPose() {
        PoseFactory poses = poses();
        Pose start = poses.of(72.0, botStartY_in, 90.0);
        return start;
    }

    /** L125's auto. */
    Command driveForward24() {
        PoseFactory poses = poses();
        Pose start = poses.of(72.0, botStartY_in, 90.0);
        Pose end = poses.of(72.0, botStartY_in + 24.0, 90.0);

        Command forward24 = sequential(follow(follower, line(start, end).constant(start)));
        return forward24;
    }

    /** L130's auto. */
    Command driveForwardThenStrafe() {
        PoseFactory poses = poses();
        Pose start = poses.of(72.0, botStartY_in, 90.0);
        Pose corner = poses.of(72.0, 72.0, 90.0);
        Pose end = poses.of(96.0, 72.0, 90.0);

        Command forwardThenStrafeRoute = sequential(
                follow(follower, line(start, corner).constant(start)),
                follow(follower, line(corner, end).constant(start)),
                hold(follower, end)
        );
        return forwardThenStrafeRoute;
    }
}
