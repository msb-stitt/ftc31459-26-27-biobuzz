package org.firstinspires.ftc.teamcode;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.behaviors.EndCondition;
import com.pedropathing.ivy.behaviors.InterruptedBehavior;
import com.pedropathing.ivy.commands.Commands;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

import java.util.function.Function;

/**
 * The StarterBot's launcher alone, to see whether it holds its speed. The robot
 * is not driven and the windmill never turns, so nothing is launched.
 *
 * <ul>
 *   <li>Holding the right bumper spins the launcher at a fraction of its target.</li>
 *   <li>D-pad up and down step the fraction by 0.1, from 0.1 to 1.0. It starts at 0.2.</li>
 * </ul>
 *
 * <p>One command starts when PLAY is pressed and runs until STOP: it spins the
 * launcher while the bumper is held. {@code loop()} watches the D-pad, and each
 * press schedules one step.
 *
 * <p>The bumper and the D-pad, and the fraction, go to the flight log; the
 * launcher logs its own target, measured speed and current under
 * {@code launcher/}.
 */
@TeleOp(name = "Launcher Check", group = "StarterBot")
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class LauncherCheck extends CorbelsTeleOp {

    /** A fifth of the target, where the check starts. */
    static final double FIRST_FRACTION = 0.2;
    static final double FRACTION_STEP = 0.1;
    static final double SMALLEST_FRACTION = 0.1;
    static final double LARGEST_FRACTION = 1.0;

    /** Where the launcher comes from. Tests hand in a fake. */
    static Function<HardwareMap, Launcher> launchers = Launcher::from;

    Launcher launcher;
    double fractionOfTarget;
    Command oneStepFaster;
    Command oneStepSlower;
    boolean dpadUpWasDown;
    boolean dpadDownWasDown;

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init() {
        initBefore();
        // Live in AdvantageScope from INIT, at 192.168.43.1, only when the
        // Live NetworkTables switch OpMode has turned it on. For practice only:
        // FTC rule R704 forbids it at competitions.
        Tracker.serveLive();
        launcher = launchers.apply(hardwareMap);
        fractionOfTarget = FIRST_FRACTION;
        initAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void start() {
        startBefore();
        oneStepFaster = Commands.instant(this::stepFaster);
        oneStepSlower = Commands.instant(this::stepSlower);
        Command spinFromTheBumper = spinWhileTheBumperIsHeld();
        Scheduler.schedule(spinFromTheBumper);
        startAfter();
    }

    /** Each press of D-pad up or down schedules one step. */
    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();

        boolean dpadUpIsDown = gamepad1.dpad_up;
        Tracker.publish("button/dpadUp", dpadUpIsDown);
        boolean dpadUpJustPressed = dpadUpIsDown && !dpadUpWasDown;
        if (dpadUpJustPressed) {
            Scheduler.schedule(oneStepFaster);
        }
        dpadUpWasDown = dpadUpIsDown;

        boolean dpadDownIsDown = gamepad1.dpad_down;
        Tracker.publish("button/dpadDown", dpadDownIsDown);
        boolean dpadDownJustPressed = dpadDownIsDown && !dpadDownWasDown;
        if (dpadDownJustPressed) {
            Scheduler.schedule(oneStepSlower);
        }
        dpadDownWasDown = dpadDownIsDown;

        Tracker.publish("launcher/fraction_of_target", fractionOfTarget);
        Tracker.printToDs("Launcher Check: hold the right bumper; D-pad up and down change the speed.");
        Tracker.printToDs("Fraction of target: %.1f", fractionOfTarget);
        Tracker.printToDs("Target ticks/s: %.0f", targetTicksPerSecond());
        loopAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void stop() {
        launcher.stop();
        stopAfter();
    }

    /**
     * For as long as the OpMode runs: while the right bumper is held, the
     * launcher spins at the fraction; while it is not, it stops. It never feeds.
     */
    Command spinWhileTheBumperIsHeld() {
        Command spinWhileTheBumperIsHeld = Command.build()
                .setExecute(this::spinOrStopThisLoop)
                .setDone(this::runsUntilCancelled)
                .setEnd(this::stopTheLauncher)
                .requiring(launcher)
                .setInterruptedBehavior(InterruptedBehavior.SUSPEND);
        return spinWhileTheBumperIsHeld;
    }

    /** One loop of {@link #spinWhileTheBumperIsHeld()}: the bumper read now. */
    void spinOrStopThisLoop() {
        boolean rightBumper = gamepad1.right_bumper;
        Tracker.publish("button/rightBumper", rightBumper);
        if (rightBumper) {
            double targetTicksPerSecond = targetTicksPerSecond();
            launcher.spinAt(targetTicksPerSecond);
        } else {
            launcher.stop();
        }
    }

    /** The spin never finishes by itself. */
    boolean runsUntilCancelled() {
        return false;
    }

    /** How the spin ends, or is set aside: the launcher stopped. */
    void stopTheLauncher(EndCondition howItEnded) {
        launcher.stop();
    }

    /** The fraction now, as a speed. */
    double targetTicksPerSecond() {
        double targetTicksPerSecond = Launcher.TARGET_TICKS_PER_SECOND * fractionOfTarget;
        return targetTicksPerSecond;
    }

    double fractionOfTarget() {
        return fractionOfTarget;
    }

    void stepFaster() {
        double steppedUp = fractionOfTarget + FRACTION_STEP;
        fractionOfTarget = toAStepWithinTheRange(steppedUp);
    }

    void stepSlower() {
        double steppedDown = fractionOfTarget - FRACTION_STEP;
        fractionOfTarget = toAStepWithinTheRange(steppedDown);
    }

    /** Rounded to a tenth, so the steps stay 0.1 apart, and kept from 0.1 to 1.0. */
    static double toAStepWithinTheRange(double fraction) {
        double tenths = Math.round(fraction * 10);
        double roundedToATenth = tenths / 10;
        double withinTheRange = Range.clip(roundedToATenth, SMALLEST_FRACTION, LARGEST_FRACTION);
        return withinTheRange;
    }
}
