package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.commands.Commands;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.OpModeStorage;
import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L140: field relative. Push the stick away from you and the robot goes away
 * from you, whichever way it happens to be facing.
 *
 * <p>Every OpMode has four methods. {@code init} runs once when INIT is
 * pressed, {@code start} once when PLAY is pressed, {@code loop} over and over
 * until STOP, and {@code stop} once at the end. The {@code Before} and
 * {@code After} calls are the robot's own housekeeping: the hardware, the
 * flight log and the live values. {@code start} schedules the lesson's code as
 * a command, and the scheduler runs it inside {@code loopAfter()}, once every
 * loop.
 *
 * <p>Between INIT and PLAY, L135's menu chooses the alliance, which starts as
 * the one L140's auto handed over, and where the robot starts: where the auto
 * ended, or against the wall, for a teleop run with no auto first.
 *
 * <p>Passes when: LessonsTest.l140_fieldRelativeIgnoresWhichWayTheRobotFaces,
 * l140_fromTheBlueAllianceAreaTheStickMeansTheOtherWay and
 * l140_theTeleOpMenuChoosesTheAllianceAndTheWall
 */
@TeleOp(name = "L140 Field Relative", group = "Lessons")
// When on L140S020, see
// https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s020
// for what to do here.
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L140FieldRelativeOpMode extends CorbelsTeleOp {

    L135AutoMenu menu = new L135AutoMenu(
            new L135Row("Alliance", "Red", "Blue"),
            new L135Row("Start", "Auto End", "Wall"));

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
        menu.startAt("Alliance", OpModeStorage.alliance);
    }

    /** Again and again between INIT and PLAY. */
    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init_loop() {
        menu.loop(gamepad1);
        initLoopAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void start() {
        startBefore();
        String startPlace = menu.chosen("Start");
        if (startPlace.equals("Wall")) {
            PoseFactory poses = poses();
            Pose wall = poses.of(72.0, botStartY_in, 90.0);
            follower.setPose(wall);
        }
        Command driveFromTheSticksEveryLoop = Commands.infinite(this::driveFromTheSticks);
        Scheduler.schedule(driveFromTheSticksEveryLoop);
        startAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();
        Tracker.printToDs(menu.summary());
        loopAfter();
    }

    /** Once every loop, from PLAY until STOP: the scheduler runs it. */
    void driveFromTheSticks() {
        double leftStickY = gamepad1.left_stick_y;
        Tracker.publish("gamepad1/leftStickY", leftStickY);
        double leftStickX = gamepad1.left_stick_x;
        Tracker.publish("gamepad1/leftStickX", leftStickX);
        double rightStickX = gamepad1.right_stick_x;
        Tracker.publish("gamepad1/rightStickX", rightStickX);

        double bandToSetToZero = 0.05;
        double fieldXSpeed = -leftStickY;
        double fieldYSpeed = -leftStickX;
        // When on L140S050, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s050
        // for what to do here.

        fieldXSpeed = speedShaped(fieldXSpeed, bandToSetToZero);
        Tracker.publish("drivetrain/fieldXSpeed", fieldXSpeed);
        fieldYSpeed = speedShaped(fieldYSpeed, bandToSetToZero);
        Tracker.publish("drivetrain/fieldYSpeed", fieldYSpeed);

        double turnCcwSpeed = -rightStickX;
        turnCcwSpeed = speedShaped(turnCcwSpeed, bandToSetToZero);
        Tracker.publish("drivetrain/turnCcwSpeed", turnCcwSpeed);

        // When on L140S040, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s040
        // for what to do here.
        drivetrain.sticks(fieldXSpeed, fieldYSpeed, turnCcwSpeed);
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
        String alliance = menu.chosen("Alliance");
        if (alliance.equals("Blue")) {
            chosenPoses = redPoses.rotateAround(72.0, 72.0, 180.0);
        }
        return chosenPoses;
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
        double alwaysPositiveValue = Math.abs(value);
        double valueSquaredKeepingTheSign = value * alwaysPositiveValue;
        return valueSquaredKeepingTheSign;
    }

    double speedShaped(double value, double band) {
        double deadbanded = deadband(value, band);
        double shapedSpeed = signedSquared(deadbanded);
        return shapedSpeed;
    }
}
