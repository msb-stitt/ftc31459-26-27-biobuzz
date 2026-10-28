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
 * Robot 37523's own values: a StarterBot. {@link Constants#thisRobot} picks
 * this file when it names {@code BOT_37523}, and {@link Constants} sets what
 * every robot shares on top.
 *
 * <p>Each of the three configs is pasted whole from the AutoTune procedure
 * named above it, in AutoTune, the page the robot serves at 192.168.43.1:10158.
 */
class Bot37523 {

    // Set on 2026-10-17, when 37523 first drove. MEASURE: Mecanum Tuner.
    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set("Front Left");
        c.frontRightName.set("Front Right");
        c.backLeftName.set("Back Left");
        c.backRightName.set("Back Right");
        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backLeftDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
    });

    // Pinpoint Tuner, 2026-10-27. MEASURE: swingarm pods set as 4-bar ones read
    // 16 in for a 24 in push. If its pods are swingarm, run the tuner again with
    // SWING_ARM.
    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set("pinpoint");
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        c.xPodOffset.set(-1.1594286490613082);
        c.yPodOffset.set(-3.765331778939315);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });

    // Foresight Tuner, 2026-10-27, with the Pinpoint above. MEASURE: again once
    // the pods are settled.
    public static ForesightConfig foresightConfig = new ForesightConfig(
            c -> {
                Controller primaryTranslationalForward = Controller.proportional(0.19534165825111682);
                Controller secondaryTranslationalForward = Controller.proportional(0.07217352309854799);
                Controller primaryTranslationalLateral = Controller.proportional(0.24710418199283002);
                Controller secondaryTranslationalLateral = Controller.proportional(0.09129839250100334);

                c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
                c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

                c.coast.set(Controller.proportionalFeedforward(0.016982498310964104));
                c.brake.set(Controller.proportionalFeedforward(0.014435123564319488));

                c.headingFeedback.set(Controller.proportional(3.140054534488619));
                c.headingBrakeCoefficients.set(Vector2D.cartesian(0.048915222154460536, 0.00409680844212561));

                c.linearBrakeCoefficients.set(Matrix.diag(0.06567645082861989, 1.2290908866223547));
                c.quadraticBrakeCoefficients.set(Matrix.diag(0.001197158665240995, -0.023572421661671415));

                c.maxAchievableForwardVelocity.set(60.37859937397769);
                c.maxAchievableStrafeVelocity.set(51.208204281004114);
                c.naturalForwardDeceleration.set(34.20732210878504);
                c.naturalStrafeDeceleration.set(57.455448354199014);
            }
    );

    /**
     * Ticks per inch of wheel travel, and how far a wheel sits from the middle.
     * MEASURE: run L195. These are the first robot's.
     */
    static double ticksPerInch = 45.0;
    static double turnRadiusInches = 8.0;

    /** goBILDA's launcher speeds: spin to the target, feed only above the second. */
    static double launcherTargetTicksPerSecond = 1250;
    static double launcherFeedAboveTicksPerSecond = 1200;

    /** The launcher motor's P, I and D, tuned on 37523 on 2026-10-21. goBILDA's are 40, 0, 0. */
    static double launcherVelocityP = 40;
    static double launcherVelocityI = 0;
    static double launcherVelocityD = 0;

    /**
     * The launcher's feedforward, as a fraction of full power: a static part,
     * to overcome friction, and a part for each tick per second. Together they
     * give 0.5245 at 1250 ticks/s, the F of 13.75 tuned on 37523 on 2026-10-21.
     * MEASURE: the static part is twice the bare motor's 0.027, as a start.
     */
    static double launcherStaticPower = 0.05;
    static double launcherPowerPerTickPerSecond = 0.00037963;
}
