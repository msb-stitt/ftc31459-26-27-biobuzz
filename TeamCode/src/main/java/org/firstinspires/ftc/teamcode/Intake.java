package org.firstinspires.ftc.teamcode;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.behaviors.EndCondition;
import com.pedropathing.ivy.behaviors.InterruptedBehavior;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.base.Tracker;
import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.function.DoubleSupplier;

/**
 * goBILDA's StarterBot intake: the intake motor and the two Gecko Wheel servos,
 * always at one power, set up as goBILDA's example sets them.
 *
 * <p>Each power set goes to the flight log as {@code intake/power}.
 */
class Intake {

    /** The intake still. */
    static final double STOPPED_POWER = 0;
    /** The most power, out or in. */
    static final double MOST_POWER_OUT = -1;
    static final double MOST_POWER_IN = 1;

    final DcMotor motor;
    final CRServo leftServo;
    final CRServo rightServo;
    /** Where {@link #run} reads its power each loop. */
    DoubleSupplier powerToRunAt;

    Intake(DcMotor motor, CRServo leftServo, CRServo rightServo) {
        this.motor = motor;
        this.leftServo = leftServo;
        this.rightServo = rightServo;
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightServo.setDirection(DcMotorSimple.Direction.REVERSE);
        setPower(STOPPED_POWER);
    }

    /** The three devices, by the names in {@link Constants}. */
    static Intake from(HardwareMap map) {
        DcMotor intakeMotor = map.get(DcMotor.class, Constants.intakeName);
        CRServo leftIntakeServo = map.get(CRServo.class, Constants.leftIntakeServoName);
        CRServo rightIntakeServo = map.get(CRServo.class, Constants.rightIntakeServoName);
        Intake intake = new Intake(intakeMotor, leftIntakeServo, rightIntakeServo);
        return intake;
    }

    /** One power for all three, clipped to -1 to 1. */
    void setPower(double power) {
        double clippedPower = Range.clip(power, MOST_POWER_OUT, MOST_POWER_IN);
        motor.setPower(clippedPower);
        leftServo.setPower(clippedPower);
        rightServo.setPower(clippedPower);
        Tracker.publish("intake/power", clippedPower);
    }

    /**
     * Runs at the power given, read every loop, for as long as the OpMode runs.
     * A command that also needs the intake sets this one aside while it runs,
     * and this one comes back when it ends.
     */
    Command run(DoubleSupplier power) {
        powerToRunAt = power;
        Command runAtThePowerGiven = Command.build()
                .setExecute(this::runAtThePowerGivenThisLoop)
                .setDone(this::runsUntilCancelled)
                .setEnd(this::stopWhenItEnds)
                .requiring(this)
                .setInterruptedBehavior(InterruptedBehavior.SUSPEND);
        return runAtThePowerGiven;
    }

    /** One loop of {@link #run}: the power read now, and set. */
    void runAtThePowerGivenThisLoop() {
        double powerThisLoop = powerToRunAt.getAsDouble();
        setPower(powerThisLoop);
    }

    /** {@link #run} never finishes by itself. */
    boolean runsUntilCancelled() {
        return false;
    }

    /** How {@link #run} ends, or is set aside: the intake stopped. */
    void stopWhenItEnds(EndCondition howItEnded) {
        setPower(STOPPED_POWER);
    }
}
