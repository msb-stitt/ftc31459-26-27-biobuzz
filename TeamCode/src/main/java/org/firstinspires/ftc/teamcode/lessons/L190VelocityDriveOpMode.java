package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.commands.Commands;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.StickShaper;
import org.firstinspires.ftc.teamcode.base.Tracker;

import static org.firstinspires.ftc.teamcode.base.CorbelsDriveTrain.*;

/**
 * L190: ask the wheels for a speed. Full stick means 40 inches per second, not
 * full speed, and the drivetrain measures the wheels and makes up the
 * difference.
 *
 * <p>Every OpMode has four methods. {@code init} runs once when INIT is
 * pressed, {@code start} once when PLAY is pressed, {@code loop} over and over
 * until STOP, and {@code stop} once at the end. The {@code Before} and
 * {@code After} calls are the robot's own housekeeping: the hardware, the
 * flight log and the live values. {@code start} schedules the lesson's code as
 * a command, and the scheduler runs it inside {@code loopAfter()}, once every
 * loop.
 *
 * <p>Passes when: LessonsTest.l190_theSticksCommandASpeedAndTheWheelsAreCorrectedTowardsIt,
 * LessonsTest.l190_whenTheWheelsAreUpToSpeedOnlyTheFeedforwardRemains and
 * LessonsTest.l190_turningAsksEachSideForOppositeSpeeds
 */
@TeleOp(name = "L190 Velocity Drive", group = "Lessons")
// When on L190S020, see
// https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l190.html#l190s020
// for what to do here.
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L190VelocityDriveOpMode extends CorbelsTeleOp {

    StickShaper stickShaper = new StickShaper(0.05);

    /** How fast full stick asks for, forward and sideways. Inches per second. */
    static final double MAX_IPS = 40.0;

    /** How fast full stick asks for in turn. Radians per second, half a turn. */
    static final double MAX_TURN_RADPS = Math.PI;

    // When on L190S030, see
    // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l190.html#l190s030
    // for what to do here.

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init() {
        initBefore();
        stickShaper.setShape(StickShaper.Shape.LINEAR);

        // When on L190S040, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l190.html#l190s040
        // for what to do here.

        initAfter();
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
        double leftStickY = gamepad1.left_stick_y;
        Tracker.publish("gamepad1/leftStickY", leftStickY);
        double leftStickX = gamepad1.left_stick_x;
        Tracker.publish("gamepad1/leftStickX", leftStickX);
        double rightStickX = gamepad1.right_stick_x;
        Tracker.publish("gamepad1/rightStickX", rightStickX);

        // When on L190S050, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l190.html#l190s050
        // for what to do here.
        double forwardX_ips = 0.0;
        Tracker.publish("drivetrain/forwardX_ips", forwardX_ips);
        double strafeY_ips = 0.0;
        Tracker.publish("drivetrain/strafeY_ips", strafeY_ips);
        double turn_radps = 0.0;
        Tracker.publish("drivetrain/turn_radps", turn_radps);
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void stop() {
        stopAfter();
    }
}
