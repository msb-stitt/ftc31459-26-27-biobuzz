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
 * this file when it names {@code BOT_31459}, and {@link Constants} takes
 * them.
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
        c.xPodOffset.set(-3.002032182348056);
        c.yPodOffset.set(-7.232765137679934);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });

    // Foresight Tuner, 2026-11-03.
    public static ForesightConfig foresightConfig = new ForesightConfig(
            c -> {
                Controller primaryTranslationalForward = Controller.proportional(0.19280496789139748);
                Controller secondaryTranslationalForward = Controller.proportional(0.07123628379224645);
                Controller primaryTranslationalLateral = Controller.proportional(0.22996010977612444);
                Controller secondaryTranslationalLateral = Controller.proportional(0.08496411591497718);

                c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
                c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

                c.coast.set(Controller.proportionalFeedforward(0.018184711100380703));
                c.brake.set(Controller.proportionalFeedforward(0.015457004435323596));

                c.headingFeedback.set(Controller.proportional(3.697458365309655));
                c.headingBrakeCoefficients.set(Vector2D.cartesian(0.04749188247756955, 0.004654220237835112));

                c.linearBrakeCoefficients.set(Matrix.diag(0.06832967486923454, 0.05864048144358318));
                c.quadraticBrakeCoefficients.set(Matrix.diag(0.0011877659183408091, 0.0012705511233557533));

                c.maxAchievableForwardVelocity.set(60.04203560837476);
                c.maxAchievableStrafeVelocity.set(48.96052241434506);
                c.naturalForwardDeceleration.set(41.102346447557636);
                c.naturalStrafeDeceleration.set(59.60529985123621);
            }
    );
    // Choices, not measurements. AutoTune does not print them, so each is a line
    // of its own, and Constants sets it on the configs above; a new paste leaves
    // them alone.
    // Brake rather than coast when a driver releases the sticks.
    static boolean manualBrakeMode = true;
    // Pedro's own default, named here because our drivetrains cache motor writes
    // with it: a power that moves by less than this does not reach the hardware.
    static double powerThreshold = 0.01;
    static double translationalConstraint = 0.5;                   // hold tolerance in inches
    static double headingConstraint = Math.toRadians(1);           // hold tolerance degrees
    static double velocityConstraint = 0.1;                        // hold tolerance inches/sec
    static double holdPointTranslationalScaling = 0.45;            // default=0.45
    static double holdPointHeadingScaling = 0.35;                  // default=0.35

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

    /** The launcher motor's P, I and D. Measured on 2026-11-03 */
    static double launcher_P_ctrlUnitsPerTickPerS = 10;
    static double launcher_I = 0;
    static double launcher_D = 0;

    /**
     * The launcher's feedforward, as a fraction of full power.
     * Tested on 2026-11-03.
     */
    static double launcher_kS_powerFraction = 0.05;
    static double launcher_kV_powerFractionPerTickPerS =  0.00032636316;
}
