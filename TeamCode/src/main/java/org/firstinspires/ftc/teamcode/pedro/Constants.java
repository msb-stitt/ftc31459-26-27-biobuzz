package org.firstinspires.ftc.teamcode.pedro;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.hardwareMap;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.controllers.Controller;
import com.pedropathing.drivetrain.Drivetrain;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;
import com.pedropathing.revhub.drivetrains.MecanumConfig;

import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.RobotHardware;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.pedropathing.tuning.autotune.Procedure;
import com.pedropathing.tuning.autotune.Tuner;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.pedro.procedures.ForesightTuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.PinpointTuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.Tests;


public class Constants {

    /** The robots this code is built for. Each has its own file: Bot88888 and so on. */
    enum Robot { BOT_88888, BOT_37523, BOT_31459 }

    /**
     * The robot this code is going onto. Change this line when you switch
     * robots. What differs between robots is in that robot's own file; what
     * they share is in this one.
     */
    static Robot thisRobot = Robot.BOT_37523;

    // ---- Device names. These must match the Robot Controller configuration,
    // ---- and nothing outside this file should name a device. The drivetrain's
    // ---- four names are in drivetrainConfig below. --------------------------

    public static String imuName = "imu";

    // goBILDA's StarterBot names for its mechanisms, as its example code has them.
    public static String intakeName = "intake";
    public static String leftIntakeServoName = "left_intake_servo";
    public static String rightIntakeServoName = "right_intake_servo";
    public static String launcherName = "launcher";
    public static String windmillServoName = "windmillServo";

    // ---- Every number below marked MEASURE was measured on an earlier robot,
    // ---- and is a starting point until it is measured on each starter bot with
    // ---- the tuner it names. The Pedro tuners are in Tuning, under AutoTune. --

    /**
     * Ticks per inch of wheel travel, and the fastest a wheel actually goes.
     * MEASURE BOTH on your robot: push it a known distance for the first, and
     * drive flat out and read the velocity for the second.
     */
    public static double ticksPerInch = 45.0;          // MEASURE: run L195

    /** How far a wheel sits from the middle, inches. MEASURE: run L195. */
    public static double turnRadiusInches = 8.0;

    /**
     * The fastest the robot goes, inches per second, and the power it takes to
     * ask for one inch per second. Both come from AutoTune:
     * maxAchievableForwardVelocity, maxAchievableStrafeVelocity, and the coast
     * feedforward. Strafing is slower than driving because mecanum rollers
     * waste some of it sideways.
     */
    // MEASURE: Foresight Tuner, all three.
    public static double maxForwardInchesPerSecond = 64.43298446564829;
    public static double maxStrafeInchesPerSecond = 43.595132915165195;
    public static double powerPerInchPerSecond = 0.016695978563625216;

    /**
     * The drivetrain: what its four motors are called, which way each one spins,
     * and whether it brakes when a driver lets go. One holder, read live.
     *
     * <p>{@link RobotHardware} is handed this and looks its motors up from it,
     * and our drivetrains read it back through {@code hardware.mecanumConfig},
     * so a value set here is read by everything that uses one. It is also the
     * type Pedro's own {@code Mecanum} takes and the type AutoTune's Mecanum
     * procedure generates, so what the tuner works out can be pasted in whole.
     *
     * <p>Each field is a {@code ConfigVar}: {@code get()} reads it, {@code set()}
     * writes it, and there are no plain-static copies of any of it anywhere.
     */
    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set("Front Left");
        c.frontRightName.set("Front Right");
        c.backLeftName.set("Back Left");
        c.backRightName.set("Back Right");
        // Which way each motor is mounted differs between robots, so it is in
        // the file of the robot thisRobot names.
        if (thisRobot == Robot.BOT_88888) {
            Bot88888.drivetrain(c);
        } else if (thisRobot == Robot.BOT_37523) {
            Bot37523.drivetrain(c);
        } else {
            Bot31459.drivetrain(c);
        }

        // Brake rather than coast when a driver releases the sticks.
        c.manualBrakeMode.set(true);

        // Pedro's own default, left alone and named here because our drivetrains
        // cache motor writes with it: a power that moves by less than this does
        // not reach the hardware.
        c.powerThreshold.set(0.01);
    });

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
     * For code that has no RobotHardware of its own -- the standalone examples.
     * Prefer the two-argument form, which shares the already-resolved devices.
     */
    public static Follower create(HardwareMap h) {
        return create(h, new CorbelsMecanum(new RobotHardware(h, drivetrainConfig)));
    }

    public static Follower create(HardwareMap h, Drivetrain drivetrain) {
        return new Follower(
                new PinpointLocalizer(h, localizerConfig),   // Your Localizer object
                drivetrain,                                  // Your Drivetrain object
                new Foresight(foresightConfig)               // Your Foresight algorithm object
        );
    }


}

