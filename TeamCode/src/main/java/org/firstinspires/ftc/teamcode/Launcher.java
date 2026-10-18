package org.firstinspires.ftc.teamcode;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.behaviors.EndCondition;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.firstinspires.ftc.teamcode.pedro.Constants;

/**
 * goBILDA's StarterBot launcher: the launcher motor spins up, and once it is
 * fast enough the windmill servo feeds it. The numbers are goBILDA's.
 *
 * <p>While the motor is told to spin, its target, its measured speed and its
 * current go to the flight log under {@code launcher/}, so a flight log shows
 * whether it holds its speed.
 */
public class Launcher {

    /** About 2678 RPM, in encoder ticks per second. */
    public static final double TARGET_TICKS_PER_SECOND = 1250;
    /** About 2571 RPM: the windmill feeds only above this. */
    public static final double FEED_ABOVE_TICKS_PER_SECOND = 1200;
    /** Added to the intake's power while feeding, as goBILDA's example does. */
    public static final double INTAKE_BOOST = 0.5;

    /** The launcher motor stopped. */
    public static final double STOPPED_TICKS_PER_SECOND = 0;
    /** The windmill turning to feed. */
    public static final double WINDMILL_FEEDING_POWER = 1;
    /** The windmill still. */
    public static final double WINDMILL_STOPPED_POWER = 0;

    /** goBILDA's velocity gains for the launcher motor: P, I, D and F. */
    public static final double VELOCITY_P = 40;
    public static final double VELOCITY_I = 0;
    public static final double VELOCITY_D = 0;
    public static final double VELOCITY_F = 12.5;

    private final DcMotorEx motor;
    private final CRServo windmill;
    private boolean feeding;

    public Launcher(DcMotorEx motor, CRServo windmill) {
        this.motor = motor;
        this.windmill = windmill;
        useTheMotorsOwnVelocityControl();
        windmill.setDirection(DcMotorSimple.Direction.REVERSE);
        stopTheWindmill();
    }

    /** The two devices, by the names in {@link Constants}. */
    public static Launcher from(HardwareMap map) {
        DcMotorEx launcherMotor = map.get(DcMotorEx.class, Constants.launcherName);
        CRServo windmillServo = map.get(CRServo.class, Constants.windmillServoName);
        Launcher launcher = new Launcher(launcherMotor, windmillServo);
        return launcher;
    }

    /** True while the windmill is feeding. */
    public boolean feeding() {
        return feeding;
    }

    /** Spins up and feeds until cancelled; then the motor and the windmill stop. */
    public Command fire() {
        Command spinUpAndFeed = Command.build()
                .setExecute(this::spinUpAndFeedOnceFastEnough)
                .setDone(this::runsUntilCancelled)
                .setEnd(this::stopTheMotorAndTheWindmill)
                .requiring(this);
        return spinUpAndFeed;
    }

    /** The motor's target, a speed it then holds by itself. */
    public void spinAt(double targetTicksPerSecond) {
        motor.setVelocity(targetTicksPerSecond);
        publishTheMotor(targetTicksPerSecond);
    }

    /** The motor and the windmill both stopped. */
    public void stop() {
        motor.setVelocity(STOPPED_TICKS_PER_SECOND);
        stopTheWindmill();
    }

    /** One loop of {@link #fire()}: the motor's target, then the windmill by its speed. */
    private void spinUpAndFeedOnceFastEnough() {
        spinAt(TARGET_TICKS_PER_SECOND);
        double measuredTicksPerSecond = motor.getVelocity();
        boolean fastEnoughToFeed = measuredTicksPerSecond > FEED_ABOVE_TICKS_PER_SECOND;
        if (fastEnoughToFeed) {
            startTheWindmill();
        } else {
            stopTheWindmill();
        }
    }

    /** {@link #fire()} never finishes by itself; letting go of its button cancels it. */
    private boolean runsUntilCancelled() {
        return false;
    }

    /** How {@link #fire()} ends, cancelled or not: everything stopped. */
    private void stopTheMotorAndTheWindmill(EndCondition howItEnded) {
        stop();
    }

    private void startTheWindmill() {
        windmill.setPower(WINDMILL_FEEDING_POWER);
        feeding = true;
    }

    private void stopTheWindmill() {
        windmill.setPower(WINDMILL_STOPPED_POWER);
        feeding = false;
    }

    /**
     * The hub holds the target by itself, from the motor's encoder, with
     * goBILDA's gains in place of the SDK's defaults.
     */
    private void useTheMotorsOwnVelocityControl() {
        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        PIDFCoefficients gains = new PIDFCoefficients(VELOCITY_P, VELOCITY_I, VELOCITY_D, VELOCITY_F);
        motor.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, gains);
    }

    /** The target, the measured speed and the current, for the flight log. */
    private void publishTheMotor(double targetTicksPerSecond) {
        double measuredTicksPerSecond = motor.getVelocity();
        double currentAmps = motor.getCurrent(CurrentUnit.AMPS);
        Tracker.publish("launcher/target_tps", targetTicksPerSecond);
        Tracker.publish("launcher/measured_tps", measuredTicksPerSecond);
        Tracker.publish("launcher/current_amps", currentAmps);
        Tracker.publish("launcher/feeding", feeding);
    }
}
