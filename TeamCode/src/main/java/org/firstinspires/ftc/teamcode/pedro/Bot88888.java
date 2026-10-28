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
 * Robot 88888's own values: a Pinpoint and a drivetrain, with no intake,
 * launcher or windmill. {@link Constants#thisRobot} picks this file when it
 * names {@code BOT_88888}, and {@link Constants} sets what every robot shares
 * on top.
 *
 * <p>Each of the three configs is pasted whole from the AutoTune procedure
 * named above it, in AutoTune, the page the robot serves at 192.168.43.1:10158.
 */
class Bot88888 {

    // Mecanum Tuner. The directions are from 2026-10-22, when 88888 first drove.
    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set("Front Left");
        c.frontRightName.set("Front Right");
        c.backLeftName.set("Back Left");
        c.backRightName.set("Back Right");
        c.frontLeftDirection.set(DcMotorSimple.Direction.FORWARD);
        c.frontRightDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backLeftDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backRightDirection.set(DcMotorSimple.Direction.REVERSE);
    });

    // Pinpoint Tuner, 2026-09-20.
    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set("pinpoint");
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        c.xPodOffset.set(1.0581908639021746);
        c.yPodOffset.set(3.359022666150191);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });

    // Foresight Tuner, 2026-09-20.
    public static ForesightConfig foresightConfig = new ForesightConfig(
            c -> {
                Controller primaryTranslationalForward = Controller.proportional(0.368944499316663);
                Controller secondaryTranslationalForward = Controller.proportional(0.13631513411892088);
                Controller primaryTranslationalLateral = Controller.proportional(0.28643896916380074);
                Controller secondaryTranslationalLateral = Controller.proportional(0.10583154531580646);

                c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
                c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

                c.coast.set(Controller.proportionalFeedforward(0.016695978563625216));
                c.brake.set(Controller.proportionalFeedforward(0.014191581779081433));

                c.headingFeedback.set(Controller.proportional(4.443284774829611));
                c.headingBrakeCoefficients.set(Vector2D.cartesian(0.20442551239984175, -0.004974765042140602));

                c.linearBrakeCoefficients.set(Matrix.diag(0.04993877265618792, 0.08334033043867983));
                c.quadraticBrakeCoefficients.set(Matrix.diag(0.0011536551239685066, 6.430819946483606E-4));

                c.maxAchievableForwardVelocity.set(64.43298446564829);
                c.maxAchievableStrafeVelocity.set(43.595132915165195);
                c.naturalForwardDeceleration.set(46.50217822427653);
                c.naturalStrafeDeceleration.set(62.02176070971554);
            }
    );

    /** Ticks per inch of wheel travel, and how far a wheel sits from the middle. L195 measures both. */
    static double ticksPerInch = 45.0;
    static double turnRadiusInches = 8.0;

    // 88888 has no launcher. These are dev's values, so Constants reads the
    // same names on every robot; nothing on 88888 uses them.
    static double launcherTargetTicksPerSecond = 1250;
    static double launcherFeedAboveTicksPerSecond = 1200;
    static double launcherVelocityP = 40;
    static double launcherVelocityI = 0;
    static double launcherVelocityD = 0;
    static double launcherStaticPower = 0.05;
    static double launcherPowerPerTickPerSecond = 0.00037963;
}
