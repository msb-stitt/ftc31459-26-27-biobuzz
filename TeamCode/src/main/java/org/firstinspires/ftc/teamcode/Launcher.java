package org.firstinspires.ftc.teamcode;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.behaviors.EndCondition;
import com.pedropathing.ivy.behaviors.InterruptedBehavior;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.function.BooleanSupplier;

/**
 * goBILDA's StarterBot launcher: the launcher motor spins up, and once it is
 * fast enough the windmill servo feeds it. Its speeds and gains are the
 * robot's own, read from {@link Constants}.
 *
 * <p>The motor controller in the hub holds the speed itself, and its
 * feedforward is F × the target. So that the feedforward is a static part plus
 * a part per tick per second, kS + kV × target, the motor controller is given
 * a new F each time the target changes.
 *
 * <p>The motor controller's output is in rev motor controller units: full
 * power is 32767 of them, by REV's
 * <a href="https://docs.revrobotics.com/first-global/fgc-pid">PIDF Tuning Guide</a>,
 * "F = 32767 ÷ maxV". So F and P are in rev motor controller units per tick
 * per second.
 *
 * <p>Each time the motor is given a target, spinning or stopped, its target,
 * its measured speed, its current and whether the windmill is feeding go to
 * the flight log under {@code launcher/}, so a flight log shows whether it
 * holds its speed.
 */
// 'public' so example/'s OpModes, in another package, can use it. 'public' allows access from any code.
public class Launcher {

    /** The robot's target, in encoder ticks per second: goBILDA's 1250 is about 2678 RPM. */
    static final double TARGET_TICKS_PER_SECOND = Constants.launcherTargetTicksPerSecond;
    /** The windmill feeds only above this: goBILDA's 1200 is about 2571 RPM. */
    static final double FEED_ABOVE_TICKS_PER_SECOND = Constants.launcherFeedAboveTicksPerSecond;
    /** Added to the intake's speed while feeding, as goBILDA's example does. */
    // 'public' so example/'s OpModes, in another package, can use it. 'public' allows access from any code.
    public static final double INTAKE_BOOST = 0.5;

    /** The launcher motor stopped. */
    static final double STOPPED_TICKS_PER_SECOND = 0;
    /** The windmill turning to feed. */
    static final double WINDMILL_FEEDING_SPEED = 1;
    /** The windmill still. */
    static final double WINDMILL_STOPPED_SPEED = 0;

    /** Full power, in rev motor controller units. */
    static final double MOT_FULL_POWER_CTRL_UNITS = 32767;

    final DcMotorEx motor;
    final CRServo windmill;
    boolean feeding;
    /** The target the motor controller's F was last worked out for. */
    double motCtrlFeedforwardTargetTicksPerSecond;

    // 'public' so example/'s OpModes, in another package, can use it. 'public' allows access from any code.
    public Launcher(DcMotorEx motor, CRServo windmill) {
        this.motor = motor;
        this.windmill = windmill;
        useMotCtrlVelocityControl();
        windmill.setDirection(DcMotorSimple.Direction.REVERSE);
        stopTheWindmill();
    }

    /** The two devices, by the names in {@link Constants}. */
    // 'public' so example/'s OpModes, in another package, can use it. 'public' allows access from any code.
    public static Launcher from(HardwareMap map) {
        DcMotorEx launcherMotor = map.get(DcMotorEx.class, Constants.launcherName);
        CRServo windmillServo = map.get(CRServo.class, Constants.windmillServoName);
        Launcher launcher = new Launcher(launcherMotor, windmillServo);
        return launcher;
    }

    /** True while the windmill is feeding. */
    // 'public' so example/'s OpModes, in another package, can use it. 'public' allows access from any code.
    public boolean feeding() {
        return feeding;
    }

    /**
     * For as long as the OpMode runs: while the fire button is held, spins up
     * and feeds once fast enough; while it is not, the motor and the windmill
     * stop. A command that also needs the launcher sets this one aside while it
     * runs, and this one comes back when it ends.
     */
    // 'public' so example/'s OpModes, in another package, can use it. 'public' allows access from any code.
    public Command fireWhile(BooleanSupplier fireButton) {
        Command fireWhileTheButtonIsHeld = Command.build()
                .setExecute(() -> fireOrStopThisLoop(fireButton))
                .setDone(this::runsUntilCancelled)
                .setEnd(this::stopMotorAndWindmill)
                .requiring(this)
                .setInterruptedBehavior(InterruptedBehavior.SUSPEND);
        return fireWhileTheButtonIsHeld;
    }

    /**
     * The motor's target, a speed the motor controller then holds. A new
     * target to spin at gets its own F first.
     */
    void spinAt(double targetTicksPerSecond) {
        boolean targetChanged = targetTicksPerSecond != motCtrlFeedforwardTargetTicksPerSecond;
        boolean spinning = targetTicksPerSecond > STOPPED_TICKS_PER_SECOND;
        if (targetChanged && spinning) {
            giveMotCtrlGainsFor(targetTicksPerSecond);
        }
        motor.setVelocity(targetTicksPerSecond);
        publishMotorState(targetTicksPerSecond);
    }

    /** The motor and the windmill both stopped. */
    void stop() {
        stopTheWindmill();
        spinAt(STOPPED_TICKS_PER_SECOND);
    }

    /** One loop of {@link #fireWhile}: the button read now. */
    void fireOrStopThisLoop(BooleanSupplier fireButton) {
        boolean fireButtonHeldNow = fireButton.getAsBoolean();
        if (fireButtonHeldNow) {
            spinUpAndFeedOnceFastEnough();
        } else {
            stop();
        }
    }

    /** The motor's target, then the windmill by its speed. */
    void spinUpAndFeedOnceFastEnough() {
        double measuredTicksPerSecond = motor.getVelocity();
        boolean fastEnoughToFeed = measuredTicksPerSecond > FEED_ABOVE_TICKS_PER_SECOND;
        if (fastEnoughToFeed) {
            startTheWindmill();
        } else {
            stopTheWindmill();
        }
        spinAt(TARGET_TICKS_PER_SECOND);
    }

    /** {@link #fireWhile} never finishes by itself. */
    boolean runsUntilCancelled() {
        return false;
    }

    /** How {@link #fireWhile} ends, or is set aside: everything stopped. */
    void stopMotorAndWindmill(EndCondition howItEnded) {
        stop();
    }

    void startTheWindmill() {
        windmill.setPower(WINDMILL_FEEDING_SPEED);
        feeding = true;
    }

    void stopTheWindmill() {
        windmill.setPower(WINDMILL_STOPPED_SPEED);
        feeding = false;
    }

    /**
     * The motor controller holds the target by itself, from the motor's
     * encoder, with the robot's gains in place of the SDK's defaults.
     */
    void useMotCtrlVelocityControl() {
        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        giveMotCtrlGainsFor(TARGET_TICKS_PER_SECOND);
    }

    /** The robot's P, I and D, and the F that makes the feedforward right at this target. */
    void giveMotCtrlGainsFor(double targetTicksPerSecond) {
        double motFCtrlUnitsPerTickPerSecond = motFCtrlUnitsPerTickPerSecondAt(targetTicksPerSecond);
        PIDFCoefficients gains = new PIDFCoefficients(Constants.launcher_P_ctrlUnitsPerTickPerS,
                Constants.launcher_I, Constants.launcher_D, motFCtrlUnitsPerTickPerSecond);
        motor.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, gains);
        motCtrlFeedforwardTargetTicksPerSecond = targetTicksPerSecond;
    }

    /**
     * The motor controller's F at a target: kS + kV × target, as a fraction of
     * full power, in rev motor controller units, divided by the target the
     * motor controller multiplies it by.
     */
    static double motFCtrlUnitsPerTickPerSecondAt(double targetTicksPerSecond) {
        double powerFractionForEachTick = Constants.launcher_kV_powerFractionPerTickPerS * targetTicksPerSecond;
        double powerFractionAtTarget = Constants.launcher_kS_powerFraction + powerFractionForEachTick;
        double ctrlUnitsAtTarget = powerFractionAtTarget * MOT_FULL_POWER_CTRL_UNITS;
        double motFCtrlUnitsPerTickPerSecond = ctrlUnitsAtTarget / targetTicksPerSecond;
        return motFCtrlUnitsPerTickPerSecond;
    }

    /** The target, the measured speed, the current and the windmill, for the flight log. */
    void publishMotorState(double targetTicksPerSecond) {
        double measuredTicksPerSecond = motor.getVelocity();
        double currentAmps = motor.getCurrent(CurrentUnit.AMPS);
        Tracker.publish("launcher/target_tps", targetTicksPerSecond);
        Tracker.publish("launcher/measured_tps", measuredTicksPerSecond);
        Tracker.publish("launcher/current_amps", currentAmps);
        Tracker.publish("launcher/feeding", feeding);
    }
}
