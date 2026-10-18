package org.firstinspires.ftc.teamcode;

import com.pedropathing.ivy.Command;
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
 * <p>The target, the measured speed and the current go to the flight log under
 * {@code launcher/}, as {@link Launcher} publishes them.
 */
@TeleOp(name = "Launcher Check", group = "StarterBot")
public class LauncherCheck extends CorbelsTeleOp {

    /** A fifth of the target, where the check starts. */
    public static final double FIRST_FRACTION = 0.2;
    public static final double FRACTION_STEP = 0.1;
    public static final double SMALLEST_FRACTION = 0.1;
    public static final double LARGEST_FRACTION = 1.0;

    /** Where the launcher comes from. Tests hand in a fake. */
    static Function<HardwareMap, Launcher> launchers = Launcher::from;

    private Launcher launcher;
    private double fractionOfTarget;

    @Override
    public void init() {
        initBefore();
        launcher = launchers.apply(hardwareMap);
        fractionOfTarget = FIRST_FRACTION;
        initAfter();
    }

    @Override
    public void bindings() {
        Command oneStepFaster = Commands.instant(this::stepFaster);
        Command oneStepSlower = Commands.instant(this::stepSlower);
        buttons.whenPressed(this::dpadUpIsPressed, oneStepFaster);
        buttons.whenPressed(this::dpadDownIsPressed, oneStepSlower);
    }

    @Override
    public void loop() {
        loopBefore();
        boolean spinning = gamepad1.right_bumper;
        if (spinning) {
            double targetTicksPerSecond = targetTicksPerSecond();
            launcher.spinAt(targetTicksPerSecond);
        } else {
            launcher.stop();
        }
        Tracker.publish("launcher/fraction_of_target", fractionOfTarget);
        Tracker.printToDs("Launcher Check: hold the right bumper; D-pad up and down change the speed.");
        Tracker.printToDs("Fraction of target: %.1f", fractionOfTarget);
        Tracker.printToDs("Target ticks/s: %.0f", targetTicksPerSecond());
        loopAfter();
    }

    @Override
    public void stop() {
        launcher.stop();
        stopAfter();
    }

    /** The fraction now, as a speed. */
    double targetTicksPerSecond() {
        double targetTicksPerSecond = Launcher.TARGET_TICKS_PER_SECOND * fractionOfTarget;
        return targetTicksPerSecond;
    }

    double fractionOfTarget() {
        return fractionOfTarget;
    }

    private boolean dpadUpIsPressed() {
        return gamepad1.dpad_up;
    }

    private boolean dpadDownIsPressed() {
        return gamepad1.dpad_down;
    }

    private void stepFaster() {
        double steppedUp = fractionOfTarget + FRACTION_STEP;
        fractionOfTarget = toAStepWithinTheRange(steppedUp);
    }

    private void stepSlower() {
        double steppedDown = fractionOfTarget - FRACTION_STEP;
        fractionOfTarget = toAStepWithinTheRange(steppedDown);
    }

    /** Rounded to a tenth, so the steps stay 0.1 apart, and kept from 0.1 to 1.0. */
    private static double toAStepWithinTheRange(double fraction) {
        double tenths = Math.round(fraction * 10);
        double roundedToATenth = tenths / 10;
        double withinTheRange = Range.clip(roundedToATenth, SMALLEST_FRACTION, LARGEST_FRACTION);
        return withinTheRange;
    }
}
