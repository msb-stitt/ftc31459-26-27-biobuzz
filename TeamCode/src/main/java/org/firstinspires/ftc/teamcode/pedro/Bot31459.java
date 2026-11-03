package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.controllers.Controller;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * Robot 31459's own values: a StarterBot. {@link Constants#thisRobot} picks
 * this file when it names {@code BOT_31459}, and {@link Constants} sets what
 * every robot shares on top.
 *
 * <p>Each value but {@code drivetrainConfig} is a copy of 37523's, written
 * here, as a start, so a change to Bot37523 does not reach this robot. MEASURE
 * each: paste over a config below what the AutoTune procedure named above it
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

    // Pinpoint Tuner. 37523's, of 2026-10-28, pasted whole. MEASURE: paste over it
    // when 31459 is autotuned.
    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set("pinpoint");
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        c.xPodOffset.set(2.7927852240134414);
        c.yPodOffset.set(-6.942704568697712);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });

    // Foresight Tuner. 37523's, of 2026-10-28, pasted whole. MEASURE: paste over it
    // when 31459 is autotuned.
    public static ForesightConfig foresightConfig = new ForesightConfig(
            c -> {
                Controller primaryTranslationalForward = Controller.proportional(0.19309146376508124);
                Controller secondaryTranslationalForward = Controller.proportional(0.07134213636226185);
                Controller primaryTranslationalLateral = Controller.proportional(0.2698163685547349);
                Controller secondaryTranslationalLateral = Controller.proportional(0.09968993855482516);

                c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
                c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

                c.coast.set(Controller.proportionalFeedforward(0.016302572392860027));
                c.brake.set(Controller.proportionalFeedforward(0.013857186533931023));

                c.headingFeedback.set(Controller.proportional(3.68805492162697));
                c.headingBrakeCoefficients.set(Vector2D.cartesian(0.05047235349262692, 0.0037170236365034882));

                c.linearBrakeCoefficients.set(Matrix.diag(0.05374753488442498, 0.08038796880925157));
                c.quadraticBrakeCoefficients.set(Matrix.diag(0.0014742548482148384, 6.671759553987834E-4));

                c.maxAchievableForwardVelocity.set(61.530995739393326);
                c.maxAchievableStrafeVelocity.set(50.713485841346866);
                c.naturalForwardDeceleration.set(31.26271531515227);
                c.naturalStrafeDeceleration.set(61.213577857447575);
            }
    );

    /** 37523's. MEASURE: L113 works both out, and L131 measures them. */
    static double ticksPerInch = 45.0;
    static double turnRadiusInches = 8.0;

    /** 37523's, which are the simulator's. MEASURE: L153 measures them. */
    static double kSForwardVolts = 0.6;
    static double kVForwardVoltSecondsPerInch = 0.177;
    static double kSStrafeVolts = 0.96;
    static double kVStrafeVoltSecondsPerInch = 0.204;
    static double kSTurnVolts = 0.72;
    static double kVTurnVoltSecondsPerRadian = 2.82;

    /** goBILDA's launcher speeds: spin to the target, feed only above the second. */
    static double launcherTargetTicksPerSecond = 1250;
    static double launcherFeedAboveTicksPerSecond = 1200;

    /** The launcher motor's P, I and D, 37523's. MEASURE. */
    static double launcherVelocityP = 40;
    static double launcherVelocityI = 0;
    static double launcherVelocityD = 0;

    /**
     * The launcher's feedforward, as a fraction of full power: 37523's static
     * part, and 10/16 of its part for each tick per second. MEASURE.
     */
    static double launcherStaticPower = 0.05;
    static double launcherPowerPerTickPerSecond =  0.00037963 * 1000.0/1600.0;
}
