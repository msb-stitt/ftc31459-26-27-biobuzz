package org.firstinspires.ftc.teamcode;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.commands.Commands;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

import java.util.function.Function;

/**
 * The StarterBot's launcher alone, at a target set from the D-pad, to see
 * whether it holds its speed and feeds. The robot is not driven.
 *
 * <ul>
 *   <li>Holding the right bumper fires: the launcher spins at the target, and
 *   the windmill feeds once it is fast enough.</li>
 *   <li>D-pad up and down step the target by 50 ticks/s, and right and left by
 *   5 ticks/s, from 50 ticks/s to 2500 ticks/s. It starts at 1000 ticks/s.</li>
 * </ul>
 *
 * <p>The windmill feeds above the lower of two speeds: the target scaled as
 * the robot's own feed speed is to its own target, and the target less
 * 20 ticks/s. The targets of the other OpModes, and their feed speeds, are
 * left as they are.
 *
 * <p>One command starts when PLAY is pressed and runs until STOP: it fires
 * while the bumper is held. {@code loop()} watches the D-pad, and each press
 * schedules one step.
 *
 * <p>The bumper, the D-pad and the target set go to the flight log; the
 * launcher logs its own target, measured speed, current and whether it is
 * feeding under {@code launcher/}.
 */
@TeleOp(name = "Launcher Check", group = "1 Experiments")
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class LauncherCheck extends CorbelsTeleOp {

    static final double FIRST_TARGET_TICKS_PER_SECOND = 1000;
    /** D-pad up and down. */
    static final double BIG_STEP_TICKS_PER_SECOND = 50;
    /** D-pad right and left. */
    static final double SMALL_STEP_TICKS_PER_SECOND = 5;
    static final double SMALLEST_TARGET_TICKS_PER_SECOND = 50;
    static final double LARGEST_TARGET_TICKS_PER_SECOND = 2500;
    /** The feed speed is at least this far below the target. */
    static final double SMALLEST_FEED_GAP_TICKS_PER_SECOND = 20;

    /** Where the launcher comes from. Tests hand in a fake. */
    static Function<HardwareMap, Launcher> launchers = Launcher::from;

    Launcher launcher;
    double targetTicksPerSecond;
    Command oneBigStepFaster;
    Command oneBigStepSlower;
    Command oneSmallStepFaster;
    Command oneSmallStepSlower;
    boolean dpadUpWasDown;
    boolean dpadDownWasDown;
    boolean dpadRightWasDown;
    boolean dpadLeftWasDown;

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init() {
        initBefore();
        // Live in AdvantageScope from INIT, at 192.168.43.1, only when the
        // Live NetworkTables switch OpMode has turned it on. For practice only:
        // FTC rule R704 forbids it at competitions.
        Tracker.serveLive();
        launcher = launchers.apply(hardwareMap);
        targetTicksPerSecond = FIRST_TARGET_TICKS_PER_SECOND;
        initAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void start() {
        startBefore();
        oneBigStepFaster = Commands.instant(() -> stepBy(BIG_STEP_TICKS_PER_SECOND));
        oneBigStepSlower = Commands.instant(() -> stepBy(-BIG_STEP_TICKS_PER_SECOND));
        oneSmallStepFaster = Commands.instant(() -> stepBy(SMALL_STEP_TICKS_PER_SECOND));
        oneSmallStepSlower = Commands.instant(() -> stepBy(-SMALL_STEP_TICKS_PER_SECOND));
        Command fireFromTheBumper = launcher.fireAtWhile(this::targetTicksPerSecond,
                this::feedAboveTicksPerSecond, this::rightBumperIsHeld);
        Scheduler.schedule(fireFromTheBumper);
        startAfter();
    }

    /** Each press of the D-pad schedules one step. */
    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();

        boolean dpadUpIsDown = gamepad1.dpad_up;
        scheduleOnThePress("button/dpadUp", dpadUpIsDown, dpadUpWasDown, oneBigStepFaster);
        dpadUpWasDown = dpadUpIsDown;

        boolean dpadDownIsDown = gamepad1.dpad_down;
        scheduleOnThePress("button/dpadDown", dpadDownIsDown, dpadDownWasDown, oneBigStepSlower);
        dpadDownWasDown = dpadDownIsDown;

        boolean dpadRightIsDown = gamepad1.dpad_right;
        scheduleOnThePress("button/dpadRight", dpadRightIsDown, dpadRightWasDown, oneSmallStepFaster);
        dpadRightWasDown = dpadRightIsDown;

        boolean dpadLeftIsDown = gamepad1.dpad_left;
        scheduleOnThePress("button/dpadLeft", dpadLeftIsDown, dpadLeftWasDown, oneSmallStepSlower);
        dpadLeftWasDown = dpadLeftIsDown;

        double measuredTicksPerSecond = launcher.motor.getVelocity();
        Tracker.publish("launcher/check_target_tps", targetTicksPerSecond);
        Tracker.printToDs("Launcher Check: hold the right bumper to fire; the D-pad changes the target.");
        Tracker.printToDs("Target ticks/s: %.0f", targetTicksPerSecond);
        Tracker.printToDs("Measured ticks/s: %.0f", measuredTicksPerSecond);
        loopAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void stop() {
        launcher.stop();
        stopAfter();
    }

    /** The button logged, and the step scheduled on the loop it goes down. */
    void scheduleOnThePress(String logKey, boolean isDown, boolean wasDown, Command step) {
        Tracker.publish(logKey, isDown);
        boolean justPressed = isDown && !wasDown;
        if (justPressed) {
            Scheduler.schedule(step);
        }
    }

    boolean rightBumperIsHeld() {
        boolean rightBumper = gamepad1.right_bumper;
        Tracker.publish("button/rightBumper", rightBumper);
        return rightBumper;
    }

    double targetTicksPerSecond() {
        return targetTicksPerSecond;
    }

    double feedAboveTicksPerSecond() {
        double feedAboveTicksPerSecond = feedAboveTicksPerSecondAt(targetTicksPerSecond);
        return feedAboveTicksPerSecond;
    }

    /**
     * The speed the windmill feeds above: the lower of the target scaled as the
     * robot's own feed speed is to its own target, and the target less the gap.
     */
    static double feedAboveTicksPerSecondAt(double targetTicksPerSecond) {
        double scaledToTheTarget = targetTicksPerSecond
                * Launcher.FEED_ABOVE_TICKS_PER_SECOND / Launcher.TARGET_TICKS_PER_SECOND;
        double belowByTheGap = targetTicksPerSecond - SMALLEST_FEED_GAP_TICKS_PER_SECOND;
        double feedAboveTicksPerSecond = Math.min(scaledToTheTarget, belowByTheGap);
        return feedAboveTicksPerSecond;
    }

    /** The target moved by a step, and kept from 50 ticks/s to 2500 ticks/s. */
    void stepBy(double stepTicksPerSecond) {
        double stepped = targetTicksPerSecond + stepTicksPerSecond;
        targetTicksPerSecond = Range.clip(stepped, SMALLEST_TARGET_TICKS_PER_SECOND,
                LARGEST_TARGET_TICKS_PER_SECOND);
    }
}
