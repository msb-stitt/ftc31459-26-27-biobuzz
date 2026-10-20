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
 * fast enough the windmill servo feeds it. The numbers are goBILDA's.
 *
 * <p>Each time the motor is given a target, spinning or stopped, its target,
 * its measured speed, its current and whether the windmill is feeding go to
 * the flight log under {@code launcher/}, so a flight log shows whether it
 * holds its speed.
 */
class Launcher {

    /** About 2678 RPM, in encoder ticks per second. */
    static final double TARGET_TICKS_PER_SECOND = 1250;
    /** About 2571 RPM: the windmill feeds only above this. */
    static final double FEED_ABOVE_TICKS_PER_SECOND = 1200;
    /** Added to the intake's power while feeding, as goBILDA's example does. */
    static final double INTAKE_BOOST = 0.5;

    /** The launcher motor stopped. */
    static final double STOPPED_TICKS_PER_SECOND = 0;
    /** The windmill turning to feed. */
    static final double WINDMILL_FEEDING_POWER = 1;
    /** The windmill still. */
    static final double WINDMILL_STOPPED_POWER = 0;

    /**
     * The launcher motor's velocity gains: P, I, D and F. These hold its speed
     * without stuttering; goBILDA's example used 40, 0, 0 and 12.5.
     */
    static final double VELOCITY_P = 10;
    static final double VELOCITY_I = 0;
    static final double VELOCITY_D = 0;
    static final double VELOCITY_F = 12.5/4;

    final DcMotorEx motor;
    final CRServo windmill;
    boolean feeding;
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

    /** The motor's target, a speed it then holds by itself. */
    void spinAt(double targetTicksPerSecond) {
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
        windmill.setPower(WINDMILL_FEEDING_POWER);
        feeding = true;
    }

    void stopTheWindmill() {
        windmill.setPower(WINDMILL_STOPPED_POWER);
        feeding = false;
    }

    /**
     * The hub holds the target by itself, from the motor's encoder, with
     * goBILDA's gains in place of the SDK's defaults.
     */
    void useTheMotorsOwnVelocityControl() {
        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        PIDFCoefficients gains = new PIDFCoefficients(VELOCITY_P, VELOCITY_I, VELOCITY_D, VELOCITY_F);
        motor.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, gains);
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
