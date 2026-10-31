package org.firstinspires.ftc.teamcode.base.odometry;

import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.base.RobotHardware;
import org.firstinspires.ftc.teamcode.pedro.Constants;

/**
 * Reads the four drive motors' encoders and the Control Hub's IMU.
 *
 * <p>The only part of the encoder localizer that touches hardware, so the maths
 * can be tested on a laptop. It looks nothing up: the devices come from
 * {@link RobotHardware}, resolved once at init.
 *
 * <p>Ticks become inches at {@link Constants#ticksPerInch}, the number in the
 * robot's own file: worked out in L113, and measured in L131.
 */
public class HardwareWheelSource implements WheelSource {

    private final DcMotorEx frontLeft;
    private final DcMotorEx frontRight;
    private final DcMotorEx backLeft;
    private final DcMotorEx backRight;
    private final HeadingSource heading;

    /** Where the heading comes from; separated so tests can supply one. */
    public interface HeadingSource {
        double radians();
    }

    /** The usual case: the robot's own motors and IMU. */
    public HardwareWheelSource(RobotHardware hardware) {
        this(hardware.frontLeft, hardware.frontRight, hardware.backLeft, hardware.backRight,
                () -> hardware.imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS));
    }

    public HardwareWheelSource(DcMotorEx frontLeft, DcMotorEx frontRight,
                               DcMotorEx backLeft, DcMotorEx backRight, HeadingSource heading) {
        this.frontLeft = frontLeft;
        this.frontRight = frontRight;
        this.backLeft = backLeft;
        this.backRight = backRight;
        this.heading = heading;
    }

    @Override
    public double[] wheelInches() {
        return new double[]{
                frontLeft.getCurrentPosition() / Constants.ticksPerInch,
                frontRight.getCurrentPosition() / Constants.ticksPerInch,
                backLeft.getCurrentPosition() / Constants.ticksPerInch,
                backRight.getCurrentPosition() / Constants.ticksPerInch};
    }

    @Override
    public double headingRadians() {
        return heading.radians();
    }
}
