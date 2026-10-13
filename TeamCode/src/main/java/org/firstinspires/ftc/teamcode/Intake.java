package org.firstinspires.ftc.teamcode;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.commands.Commands;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.function.DoubleSupplier;

/**
 * goBILDA's StarterBot intake: the intake motor and the two Gecko Wheel servos,
 * always at one power, set up as goBILDA's example sets them.
 */
public class Intake {

    private final DcMotor motor;
    private final CRServo leftServo;
    private final CRServo rightServo;

    public Intake(DcMotor motor, CRServo leftServo, CRServo rightServo) {
        this.motor = motor;
        this.leftServo = leftServo;
        this.rightServo = rightServo;
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightServo.setDirection(DcMotorSimple.Direction.REVERSE);
        setPower(0);
    }

    /** The three devices, by the names in {@link Constants}. */
    public static Intake from(HardwareMap map) {
        return new Intake(map.get(DcMotor.class, Constants.intakeName),
                map.get(CRServo.class, Constants.leftIntakeServoName),
                map.get(CRServo.class, Constants.rightIntakeServoName));
    }

    /** One power for all three, clipped to -1 to 1. */
    public void setPower(double power) {
        double clipped = Range.clip(power, -1, 1);
        motor.setPower(clipped);
        leftServo.setPower(clipped);
        rightServo.setPower(clipped);
    }

    /** Runs at the power given, read every loop, until cancelled; then stops. */
    public Command run(DoubleSupplier power) {
        return Commands.infinite(() -> setPower(power.getAsDouble()))
                .setEnd(end -> setPower(0))
                .requiring(this);
    }
}
