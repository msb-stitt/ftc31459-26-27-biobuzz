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
 * always at one speed, set up as goBILDA's example sets them.
 *
 * <p>Each speed set goes to the flight log as {@code intake/speed}.
 */
// 'public' so example/'s OpModes, in another package, can use it. 'public' allows access from any code.
public class Intake {

    /** The intake still. */
    static final double STOPPED_SPEED = 0;
    /** Full speed, out or in. */
    static final double FULL_SPEED_OUT = -1;
    // 'public' so example/'s OpModes, in another package, can use it. 'public' allows access from any code.
    public static final double FULL_SPEED_IN = 1;

    final DcMotor motor;
    final CRServo leftServo;
    final CRServo rightServo;

    // 'public' so example/'s OpModes, in another package, can use it. 'public' allows access from any code.
    public Intake(DcMotor motor, CRServo leftServo, CRServo rightServo) {
        this.motor = motor;
        this.leftServo = leftServo;
        this.rightServo = rightServo;
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightServo.setDirection(DcMotorSimple.Direction.REVERSE);
        setPower(STOPPED_SPEED);
    }

    /** The three devices, by the names in {@link Constants}. */
    // 'public' so example/'s OpModes, in another package, can use it. 'public' allows access from any code.
    public static Intake from(HardwareMap map) {
        DcMotor intakeMotor = map.get(DcMotor.class, Constants.intakeName);
        CRServo leftIntakeServo = map.get(CRServo.class, Constants.leftIntakeServoName);
        CRServo rightIntakeServo = map.get(CRServo.class, Constants.rightIntakeServoName);
        Intake intake = new Intake(intakeMotor, leftIntakeServo, rightIntakeServo);
        return intake;
    }

    /** One speed for all three, clipped to -1 to 1. */
    void setPower(double speed) {
        double clippedSpeed = Range.clip(speed, FULL_SPEED_OUT, FULL_SPEED_IN);
        motor.setPower(clippedSpeed);
        leftServo.setPower(clippedSpeed);
        rightServo.setPower(clippedSpeed);
        Tracker.publish("intake/speed", clippedSpeed);
    }

    /**
     * Runs at the speed given, read every loop, for as long as the OpMode runs.
     * A command that also needs the intake sets this one aside while it runs,
     * and this one comes back when it ends.
     */
    // 'public' so example/'s OpModes, in another package, can use it. 'public' allows access from any code.
    public Command run(DoubleSupplier speed) {
        Command runAtTheSpeedGiven = Command.build()
                .setExecute(() -> runAtTheSpeedGivenThisLoop(speed))
                .setDone(this::runsUntilCancelled)
                .setEnd(this::stopWhenItEnds)
                .requiring(this)
                .setInterruptedBehavior(InterruptedBehavior.SUSPEND);
        return runAtTheSpeedGiven;
    }

    /** One loop of {@link #run}: the speed read now, and set. */
    void runAtTheSpeedGivenThisLoop(DoubleSupplier speed) {
        double speedThisLoop = speed.getAsDouble();
        setPower(speedThisLoop);
    }

    /** {@link #run} never finishes by itself. */
    boolean runsUntilCancelled() {
        return false;
    }

    /** How {@link #run} ends, or is set aside: the intake stopped. */
    void stopWhenItEnds(EndCondition howItEnded) {
        setPower(STOPPED_SPEED);
    }
}
