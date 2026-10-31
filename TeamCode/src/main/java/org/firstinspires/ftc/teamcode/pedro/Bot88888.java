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

    // Pinpoint Tuner, 2026-10-29.
    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set("pinpoint");
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        c.xPodOffset.set(-2.1917228999100336);
        c.yPodOffset.set(5.164966132697158);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });

    // Foresight Tuner, 2026-10-29.
    public static ForesightConfig foresightConfig = new ForesightConfig(
            c -> {
                Controller primaryTranslationalForward = Controller.proportional(0.1499508235462681);
                Controller secondaryTranslationalForward = Controller.proportional(0.055402822540547354);
                Controller primaryTranslationalLateral = Controller.proportional(0.2368577816880911);
                Controller secondaryTranslationalLateral = Controller.proportional(0.0875126213772609);

                c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
                c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

                c.coast.set(Controller.proportionalFeedforward(0.015486430439577797));
                c.brake.set(Controller.proportionalFeedforward(0.013163465873641127));

                c.headingFeedback.set(Controller.proportional(3.4130791236022087));
                c.headingBrakeCoefficients.set(Vector2D.cartesian(0.04228931997926617, 0.009992772964451706));

                c.linearBrakeCoefficients.set(Matrix.diag(0.06017869700690798, 0.051215357243745235));
                c.quadraticBrakeCoefficients.set(Matrix.diag(0.001102317520420922, 0.0015073370350015386));

                c.maxAchievableForwardVelocity.set(66.41368997893703);
                c.maxAchievableStrafeVelocity.set(57.7946128048502);
                c.naturalForwardDeceleration.set(37.28517692895563);
                c.naturalStrafeDeceleration.set(49.20290889885054);
            }
    );

    /**
     * Ticks per inch of wheel travel, and how far a wheel's rim rolls for each
     * radian the robot turns. L113 works both out, and L131 measures them.
     */
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
