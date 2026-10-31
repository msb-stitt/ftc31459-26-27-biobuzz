package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

/**
 * Robot 31459's own values: a StarterBot. {@link Constants#thisRobot} picks
 * this file when it names {@code BOT_31459}, and {@link Constants} sets what
 * every robot shares on top.
 *
 * <p>Each value but {@code drivetrainConfig} is 37523's, as a start. MEASURE
 * each: paste over a line below what the AutoTune procedure named above it
 * prints. AutoTune is the page the robot serves at 192.168.43.1:10158.
 */
class Bot31459 {

    // Mecanum Tuner.
    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set("Front Left");
        c.frontRightName.set("Front Right");
        c.backLeftName.set("Back Left");
        c.backRightName.set("Back Right");
        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
    });

    // Pinpoint Tuner.
    public static PinpointConfig localizerConfig = Bot37523.localizerConfig;

    // Foresight Tuner.
    public static ForesightConfig foresightConfig = Bot37523.foresightConfig;

    /** L113 works both out, and L131 measures them. */
    static double ticksPerInch = Bot37523.ticksPerInch;
    static double turnRadiusInches = Bot37523.turnRadiusInches;

    /** L153 measures them. */
    static double kSForwardVolts = Bot37523.kSForwardVolts;
    static double kVForwardVoltSecondsPerInch = Bot37523.kVForwardVoltSecondsPerInch;
    static double kSStrafeVolts = Bot37523.kSStrafeVolts;
    static double kVStrafeVoltSecondsPerInch = Bot37523.kVStrafeVoltSecondsPerInch;
    static double kSTurnVolts = Bot37523.kSTurnVolts;
    static double kVTurnVoltSecondsPerRadian = Bot37523.kVTurnVoltSecondsPerRadian;

    static double launcherTargetTicksPerSecond = Bot37523.launcherTargetTicksPerSecond;
    static double launcherFeedAboveTicksPerSecond = Bot37523.launcherFeedAboveTicksPerSecond;
    static double launcherVelocityP = Bot37523.launcherVelocityP;
    static double launcherVelocityI = Bot37523.launcherVelocityI;
    static double launcherVelocityD = Bot37523.launcherVelocityD;
    static double launcherStaticPower = Bot37523.launcherStaticPower;
    static double launcherPowerPerTickPerSecond = Bot37523.launcherPowerPerTickPerSecond;
}
