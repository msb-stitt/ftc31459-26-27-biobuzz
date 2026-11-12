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
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

/**
 * goBILDA's StarterBot launcher: the launcher motor spins up, and once it is
 * fast enough the windmill servo feeds it. Its speeds and gains are the
 * robot's own, read from {@link Constants}.
 *
 * <p>The motor controller in the hub holds the speed itself, and its
 * feedforward is F × the target. So that the feedforward is a static part plus
 * a part per tick per second, kS + kV × target, the motor controller is given
 * a new F at INIT, at each spin-up from below 50 ticks/s, and at each change of
 * target while spinning. kS and kV are for
 * {@link Constants#NOMINAL_BATTERY_VOLTAGE}, so F is scaled by that over the
 * battery's reading at INIT or at the spin-up. The battery is not read again
 * for F while the launcher spins.
 *
 * <p>The motor controller's output is in rev motor controller units: full
 * power is 32767 of them, by REV's
 * <a href="https://docs.revrobotics.com/first-global/fgc-pid">PIDF Tuning Guide</a>,
 * "F = 32767 ÷ maxV". So F and P are in rev motor controller units per tick
 * per second.
 *
 * <p>Each time the motor is given a target, spinning or stopped, its target,
 * its measured speed, its current, whether the windmill is feeding and the
 * battery's reading go to the flight log under {@code launcher/}, so a flight
 * log shows whether it holds its speed. Each F given goes there too, with the
 * battery's reading it was worked out from.
 */
// 'public' so example/'s OpModes, in another package, can use it. 'public' allows access from any code.
public class Launcher {

    /** The robot's target, in encoder ticks per second: goBILDA's 1250 is about 2678 RPM. */
    static final double TARGET_TICKS_PER_SECOND = Constants.launcherTargetTicksPerSecond;
    /** The windmill feeds only above this: goBILDA's 1200 is about 2571 RPM. */
    static final double FEED_ABOVE_TICKS_PER_SECOND = Constants.launcherFeedAboveTicksPerSecond;
    /** Added to the intake's speed while feeding, as goBILDA's example does. */
    // 'public' so example/'s OpModes, in another package, can use it. 'public' allows access from any code.
    public static final double INTAKE_BOOST_SPEED_FRACTION = 0.5;

    /** The launcher motor stopped. */
    static final double STOPPED_TICKS_PER_SECOND = 0;
    /** A target below this counts as stopped, so a target from below it is a spin-up. */
    static final double SPIN_UP_FROM_BELOW_TICKS_PER_SECOND = 50;
    /** The windmill turning to feed. */
    static final double WINDMILL_FEEDING_SPEED_FRACTION = 1;
    /** The windmill still. */
    static final double WINDMILL_STOPPED_SPEED_FRACTION = 0;

    /** Full power, in rev motor controller units. */
    static final double MOT_FULL_POWER_CTRL_UNITS = 32767;

    final DcMotorEx motor;
    final CRServo windmill;
    final VoltageSensor battery;
    boolean feeding;
    /** The target the motor controller's F was last worked out for. */
    double motCtrlFeedforwardTargetTicksPerSecond;
    /** The battery's reading the motor controller's F was last worked out from, in volts. */
    double motCtrlFeedforwardBatteryVolts;
    /** The target the motor was last given; stopped until it is given one. */
    double lastTargetTicksPerSecond = STOPPED_TICKS_PER_SECOND;
    /** The battery's reading this loop, in volts. */
    double batteryVolts;

    // 'public' so example/'s OpModes, in another package, can use it. 'public' allows access from any code.
    public Launcher(DcMotorEx motor, CRServo windmill, VoltageSensor battery) {
        this.motor = motor;
        this.windmill = windmill;
        this.battery = battery;
        motCtrlFeedforwardBatteryVolts = battery.getVoltage();
        useMotCtrlVelocityControl();
        windmill.setDirection(DcMotorSimple.Direction.REVERSE);
        stopTheWindmill();
    }

    /**
     * The two devices, by the names in {@link Constants}, and the battery. The
     * battery is not looked up by name: the SDK offers every voltage sensor as
     * a group, and the first is the Control Hub's own.
     */
    // 'public' so example/'s OpModes, in another package, can use it. 'public' allows access from any code.
    public static Launcher from(HardwareMap map) {
        DcMotorEx launcherMotor = map.get(DcMotorEx.class, Constants.launcherName);
        CRServo windmillServo = map.get(CRServo.class, Constants.windmillServoName);
        VoltageSensor battery = map.voltageSensor.iterator().next();
        Launcher launcher = new Launcher(launcherMotor, windmillServo, battery);
        return launcher;
    }

    /** True while the windmill is feeding. */
    // 'public' so example/'s OpModes, in another package, can use it. 'public' allows access from any code.
    public boolean feeding() {
        return feeding;
    }

    /**
     * For as long as the OpMode runs: while the fire button is held, spins up
     * to the robot's target and feeds once fast enough; while it is not, the
     * motor and the windmill stop. A command that also needs the launcher sets
     * this one aside while it runs, and this one comes back when it ends.
     */
    // 'public' so example/'s OpModes, in another package, can use it. 'public' allows access from any code.
    public Command fireWhile(BooleanSupplier fireButton) {
        Command fireWhileTheButtonIsHeld = fireAtWhile(() -> TARGET_TICKS_PER_SECOND,
                () -> FEED_ABOVE_TICKS_PER_SECOND, fireButton);
        return fireWhileTheButtonIsHeld;
    }

    /**
     * As {@link #fireWhile}, at the target given, feeding above the speed
     * given; both are read each loop.
     */
    Command fireAtWhile(DoubleSupplier targetTicksPerSecond, DoubleSupplier feedAboveTicksPerSecond,
                        BooleanSupplier fireButton) {
        Command fireWhileTheButtonIsHeld = Command.build()
                .setExecute(() -> fireOrStopThisLoop(targetTicksPerSecond, feedAboveTicksPerSecond,
                        fireButton))
                .setDone(this::runsUntilCancelled)
                .setEnd(this::stopMotorAndWindmill)
                .requiring(this)
                .setInterruptedBehavior(InterruptedBehavior.SUSPEND);
        return fireWhileTheButtonIsHeld;
    }

    /**
     * The motor's target, a speed the motor controller then holds. The battery
     * is read each time. A spin-up gets an F from that reading; a new target
     * while spinning gets an F from the spin-up's reading.
     */
    void spinAt(double targetTicksPerSecond) {
        batteryVolts = battery.getVoltage();
        boolean spinning = targetTicksPerSecond > STOPPED_TICKS_PER_SECOND;
        boolean wasStopped = lastTargetTicksPerSecond < SPIN_UP_FROM_BELOW_TICKS_PER_SECOND;
        boolean targetChanged = targetTicksPerSecond != motCtrlFeedforwardTargetTicksPerSecond;
        if (spinning && wasStopped) {
            motCtrlFeedforwardBatteryVolts = batteryVolts;
            giveMotCtrlGainsFor(targetTicksPerSecond);
        } else if (spinning && targetChanged) {
            giveMotCtrlGainsFor(targetTicksPerSecond);
        }
        lastTargetTicksPerSecond = targetTicksPerSecond;
        motor.setVelocity(targetTicksPerSecond);
        publishMotorState(targetTicksPerSecond);
    }

    /** The motor and the windmill both stopped. */
    void stop() {
        stopTheWindmill();
        spinAt(STOPPED_TICKS_PER_SECOND);
    }

    /** One loop of {@link #fireAtWhile}: the button, the target and the feed speed read now. */
    void fireOrStopThisLoop(DoubleSupplier targetTicksPerSecond, DoubleSupplier feedAboveTicksPerSecond,
                            BooleanSupplier fireButton) {
        boolean fireButtonHeldNow = fireButton.getAsBoolean();
        if (fireButtonHeldNow) {
            spinUpAndFeedOnceFastEnough(targetTicksPerSecond.getAsDouble(),
                    feedAboveTicksPerSecond.getAsDouble());
        } else {
            stop();
        }
    }

    /** The motor's target, then the windmill by its speed. */
    void spinUpAndFeedOnceFastEnough(double targetTicksPerSecond, double feedAboveTicksPerSecond) {
        double measuredTicksPerSecond = motor.getVelocity();
        boolean fastEnoughToFeed = measuredTicksPerSecond > feedAboveTicksPerSecond;
        if (fastEnoughToFeed) {
            startTheWindmill();
        } else {
            stopTheWindmill();
        }
        spinAt(targetTicksPerSecond);
    }

    /** {@link #fireAtWhile} never finishes by itself. */
    boolean runsUntilCancelled() {
        return false;
    }

    /** How {@link #fireAtWhile} ends, or is set aside: everything stopped. */
    void stopMotorAndWindmill(EndCondition howItEnded) {
        stop();
    }

    void startTheWindmill() {
        windmill.setPower(WINDMILL_FEEDING_SPEED_FRACTION);
        feeding = true;
    }

    void stopTheWindmill() {
        windmill.setPower(WINDMILL_STOPPED_SPEED_FRACTION);
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

    /**
     * The robot's P, I and D, and the F that makes the feedforward right at
     * this target, from {@link #motCtrlFeedforwardBatteryVolts}. The F and the
     * volts go to the flight log.
     */
    void giveMotCtrlGainsFor(double targetTicksPerSecond) {
        double motFCtrlUnitsPerTickPerSecond = motFCtrlUnitsPerTickPerSecondAt(targetTicksPerSecond,
                motCtrlFeedforwardBatteryVolts);
        PIDFCoefficients gains = new PIDFCoefficients(Constants.launcher_P_ctrlUnitsPerTickPerS,
                Constants.launcher_I, Constants.launcher_D, motFCtrlUnitsPerTickPerSecond);
        motor.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, gains);
        motCtrlFeedforwardTargetTicksPerSecond = targetTicksPerSecond;
        Tracker.publish("launcher/f_ctrl_units", motFCtrlUnitsPerTickPerSecond);
        Tracker.publish("launcher/f_battery_volts", motCtrlFeedforwardBatteryVolts);
    }

    /**
     * The motor controller's F at a target and a battery reading: kS + kV ×
     * target, as a fraction of full power, in rev motor controller units,
     * divided by the target the motor controller multiplies it by, and scaled
     * by the nominal volts over the battery's.
     */
    static double motFCtrlUnitsPerTickPerSecondAt(double targetTicksPerSecond, double batteryVolts) {
        double powerFractionForEachTick = Constants.launcher_kV_powerFractionPerTickPerS * targetTicksPerSecond;
        double powerFractionAtTarget = Constants.launcher_kS_powerFraction + powerFractionForEachTick;
        double ctrlUnitsAtTarget = powerFractionAtTarget * MOT_FULL_POWER_CTRL_UNITS;
        double batteryScale = Constants.NOMINAL_BATTERY_VOLTAGE / batteryVolts;
        double motFCtrlUnitsPerTickPerSecond = ctrlUnitsAtTarget / targetTicksPerSecond * batteryScale;
        return motFCtrlUnitsPerTickPerSecond;
    }

    /**
     * The target, the measured speed, the current, the windmill and the
     * battery, for the flight log.
     */
    void publishMotorState(double targetTicksPerSecond) {
        double measuredTicksPerSecond = motor.getVelocity();
        double currentAmps = motor.getCurrent(CurrentUnit.AMPS);
        Tracker.publish("launcher/target_tps", targetTicksPerSecond);
        Tracker.publish("launcher/measured_tps", measuredTicksPerSecond);
        Tracker.publish("launcher/current_amps", currentAmps);
        Tracker.publish("launcher/feeding", feeding);
        Tracker.publish("launcher/battery_volts", batteryVolts);
    }
}
