package org.firstinspires.ftc.teamcode.lessons;

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

import org.firstinspires.ftc.teamcode.OpModeStorage;
import org.firstinspires.ftc.teamcode.base.CorbelsAuto;
import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L140: L135's auto, with a choice to do nothing, which hands its alliance to
 * L140's teleop.
 *
 * <p>{@code preselectTeleOp} names the teleop the Driver Station picks when
 * this auto ends, by itself or at the end of its 30 seconds. Pressing stop
 * cancels that. The drive team still presses INIT and start for the teleop.
 *
 * <p>Passes when: LessonsTest.l140_theAutoHandsItsAllianceToTheTeleOp
 */
@Autonomous(name = "L140 Auto", group = "Lessons", preselectTeleOp = "L140 Field Relative")
// When on L140S020, see
// https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s020
// for what to do here.
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L140AutoOpMode extends CorbelsAuto {

    // When on L140S020, see
    // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s020
    // for what to do here.
//    L135MenuItem red = new L135MenuItem("R", "Red");
//    L135MenuItem blue = new L135MenuItem("B", "Blue");
//    L135MenuRow allianceRow = new L135MenuRow("Alliance", red, blue);
//    L135MenuItem nothing = new L135MenuItem("N", "Nothing");
//    L135MenuItem forward24 = new L135MenuItem("F24", "Forward 24");
//    L135MenuItem forwardThenStrafe = new L135MenuItem("FS", "Forward Then Strafe");
//    L135MenuRow autoRow = new L135MenuRow("Auto", nothing, forward24, forwardThenStrafe);
//    L135OptionMenu optionMenu = new L135OptionMenu(allianceRow, autoRow);
//    L135DriverStationMenu menu = new L135DriverStationMenu(optionMenu);

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
        // When on L140S020, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s020
        // for what to do here.
//        menu.loop(gamepad1);
        initLoopAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void start() {
        startBefore();
        // When on L140S090, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s090
        // for what to do here.

        // When on L140S020, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s020
        // for what to do here.
//        String auto = menu.chosen("Auto");
//        Command chosenAuto = Command.NOOP;
//        switch (auto) {
//            case "Forward 24":
//                chosenAuto = driveForward24();
//                break;
//            case "Forward Then Strafe":
//                chosenAuto = driveForwardThenStrafe();
//                break;
//        }
//        Scheduler.schedule(chosenAuto);
        startAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();
        // When on L140S020, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s020
        // for what to do here.
//        Tracker.printToDs(menu.summary());
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
        // When on L140S020, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s020
        // for what to do here.
//        String alliance = menu.chosen("Alliance");
//        if (alliance.equals("Blue")) {
//            chosenPoses = redPoses.rotateAround(72.0, 72.0, 180.0);
//        }
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
