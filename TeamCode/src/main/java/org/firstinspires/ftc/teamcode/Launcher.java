package org.firstinspires.ftc.teamcode;

import com.pedropathing.ivy.Command;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

import org.firstinspires.ftc.teamcode.pedro.Constants;

/**
 * goBILDA's StarterBot launcher: the launcher motor spins up, and once it is
 * fast enough the windmill servo feeds it. The numbers are goBILDA's.
 */
public class Launcher {

    /** About 2678 RPM, in encoder ticks per second. */
    public static final double TARGET_TICKS_PER_SECOND = 1250;
    /** About 2571 RPM: the windmill feeds only above this. */
    public static final double FEED_ABOVE_TICKS_PER_SECOND = 1200;
    /** Added to the intake's power while feeding, as goBILDA's example does. */
    public static final double INTAKE_BOOST = 0.5;

    private final DcMotorEx motor;
    private final CRServo windmill;
    private boolean feeding;

    public Launcher(DcMotorEx motor, CRServo windmill) {
        this.motor = motor;
        this.windmill = windmill;
        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motor.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER,
                new PIDFCoefficients(40, 0, 0, 12.5));
        windmill.setDirection(DcMotorSimple.Direction.REVERSE);
        windmill.setPower(0);
    }

    /** The two devices, by the names in {@link Constants}. */
    public static Launcher from(HardwareMap map) {
        return new Launcher(map.get(DcMotorEx.class, Constants.launcherName),
                map.get(CRServo.class, Constants.windmillServoName));
    }

    /** True while the windmill is feeding. */
    public boolean feeding() {
        return feeding;
    }

    /** Spins up and feeds until cancelled; then the motor and the windmill stop. */
    public Command fire() {
        return Command.build()
                .setExecute(() -> {
                    motor.setVelocity(TARGET_TICKS_PER_SECOND/5);
                    feeding = motor.getVelocity() > FEED_ABOVE_TICKS_PER_SECOND;
                    feeding = false;
                    windmill.setPower(feeding ? 1 : 0);
                })
                .setDone(() -> false)
                .setEnd(end -> {
                    motor.setVelocity(0);
                    windmill.setPower(0);
                    feeding = false;
                })
                .requiring(this);
    }
}
