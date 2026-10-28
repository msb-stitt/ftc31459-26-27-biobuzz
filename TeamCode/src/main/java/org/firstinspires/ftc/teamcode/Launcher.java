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
 * <p>The hub holds the speed itself, and its feedforward is F × the target.
 * So that the feedforward is a static part plus a part per tick per second,
 * kS + kV × target, the hub is given a new F each time the target changes.
 *
 * <p>Each time the motor is given a target, spinning or stopped, its target,
 * its measured speed, its current and whether the windmill is feeding go to
 * the flight log under {@code launcher/}, so a flight log shows whether it
 * holds its speed.
 */
class Launcher {

    /** The robot's target, in encoder ticks per second: goBILDA's 1250 is about 2678 RPM. */
    static final double TARGET_TICKS_PER_SECOND = Constants.launcherTargetTicksPerSecond;
    /** The windmill feeds only above this: goBILDA's 1200 is about 2571 RPM. */
    static final double FEED_ABOVE_TICKS_PER_SECOND = Constants.launcherFeedAboveTicksPerSecond;
    /** Added to the intake's speed while feeding, as goBILDA's example does. */
    static final double INTAKE_BOOST = 0.5;

    /** The launcher motor stopped. */
    static final double STOPPED_TICKS_PER_SECOND = 0;
    /** The windmill turning to feed. */
    static final double WINDMILL_FEEDING_SPEED = 1;
    /** The windmill still. */
    static final double WINDMILL_STOPPED_SPEED = 0;

    /** Full power, in the hub's own units for the feedforward. */
    static final double HUB_FULL_POWER = 32767;

    final DcMotorEx motor;
    final CRServo windmill;
    boolean feeding;
    /** The target the hub's F was last worked out for. */
    double targetTheHubsFeedforwardIsFor;
    /** Where {@link #fireWhile} reads the fire button each loop. */
    BooleanSupplier fireButtonIsHeld;

    Launcher(DcMotorEx motor, CRServo windmill) {
        this.motor = motor;
        this.windmill = windmill;
        useTheMotorsOwnVelocityControl();
        windmill.setDirection(DcMotorSimple.Direction.REVERSE);
        stopTheWindmill();
    }

    /** The two devices, by the names in {@link Constants}. */
    static Launcher from(HardwareMap map) {
        DcMotorEx launcherMotor = map.get(DcMotorEx.class, Constants.launcherName);
        CRServo windmillServo = map.get(CRServo.class, Constants.windmillServoName);
        Launcher launcher = new Launcher(launcherMotor, windmillServo);
        return launcher;
    }

    /** True while the windmill is feeding. */
    boolean feeding() {
        return feeding;
    }

    /**
     * For as long as the OpMode runs: while the fire button is held, spins up
     * and feeds once fast enough; while it is not, the motor and the windmill
     * stop. A command that also needs the launcher sets this one aside while it
     * runs, and this one comes back when it ends.
     */
    Command fireWhile(BooleanSupplier fireButton) {
        fireButtonIsHeld = fireButton;
        Command fireWhileTheButtonIsHeld = Command.build()
                .setExecute(this::fireOrStopThisLoop)
                .setDone(this::runsUntilCancelled)
                .setEnd(this::stopTheMotorAndTheWindmill)
                .requiring(this)
                .setInterruptedBehavior(InterruptedBehavior.SUSPEND);
        return fireWhileTheButtonIsHeld;
    }

    /**
     * The motor's target, a speed it then holds by itself. A new target to
     * spin at gets its own F first.
     */
    void spinAt(double targetTicksPerSecond) {
        boolean targetChanged = targetTicksPerSecond != targetTheHubsFeedforwardIsFor;
        boolean spinning = targetTicksPerSecond > STOPPED_TICKS_PER_SECOND;
        if (targetChanged && spinning) {
            giveTheHubItsGainsFor(targetTicksPerSecond);
        }
        motor.setVelocity(targetTicksPerSecond);
        publishTheMotor(targetTicksPerSecond);
    }

    /** The motor and the windmill both stopped. */
    void stop() {
        stopTheWindmill();
        spinAt(STOPPED_TICKS_PER_SECOND);
    }

    /** One loop of {@link #fireWhile}: the button read now. */
    void fireOrStopThisLoop() {
        boolean fireButtonHeldNow = fireButtonIsHeld.getAsBoolean();
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
    void stopTheMotorAndTheWindmill(EndCondition howItEnded) {
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
     * The hub holds the target by itself, from the motor's encoder, with the
     * robot's gains in place of the SDK's defaults.
     */
    void useTheMotorsOwnVelocityControl() {
        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        giveTheHubItsGainsFor(TARGET_TICKS_PER_SECOND);
    }

    /** The robot's P, I and D, and the F that makes the feedforward right at this target. */
    void giveTheHubItsGainsFor(double targetTicksPerSecond) {
        double hubF = hubFeedforwardAt(targetTicksPerSecond);
        PIDFCoefficients gains = new PIDFCoefficients(Constants.launcherVelocityP,
                Constants.launcherVelocityI, Constants.launcherVelocityD, hubF);
        motor.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, gains);
        targetTheHubsFeedforwardIsFor = targetTicksPerSecond;
    }

    /**
     * The hub's F at a target: kS + kV × target, as a fraction of full power,
     * in the hub's units, divided by the target the hub multiplies it by.
     */
    static double hubFeedforwardAt(double targetTicksPerSecond) {
        double powerForEachTick = Constants.launcherPowerPerTickPerSecond * targetTicksPerSecond;
        double powerAtTarget = Constants.launcherStaticPower + powerForEachTick;
        double hubUnitsAtTarget = powerAtTarget * HUB_FULL_POWER;
        double hubF = hubUnitsAtTarget / targetTicksPerSecond;
        return hubF;
    }

    /** The target, the measured speed, the current and the windmill, for the flight log. */
    void publishTheMotor(double targetTicksPerSecond) {
        double measuredTicksPerSecond = motor.getVelocity();
        double currentAmps = motor.getCurrent(CurrentUnit.AMPS);
        Tracker.publish("launcher/target_tps", targetTicksPerSecond);
        Tracker.publish("launcher/measured_tps", measuredTicksPerSecond);
        Tracker.publish("launcher/current_amps", currentAmps);
        Tracker.publish("launcher/feeding", feeding);
    }
}
