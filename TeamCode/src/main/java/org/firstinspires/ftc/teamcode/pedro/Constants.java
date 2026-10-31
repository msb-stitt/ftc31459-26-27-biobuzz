package org.firstinspires.ftc.teamcode.pedro;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.hardwareMap;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.controllers.Controller;
import com.pedropathing.drivetrain.Drivetrain;
import com.pedropathing.follower.Follower;
import com.pedropathing.revhub.drivetrains.MecanumConfig;

import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.RobotHardware;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.pedropathing.tuning.autotune.Procedure;
import com.pedropathing.tuning.autotune.Tuner;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.pedro.procedures.ForesightTuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.PinpointTuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.Tests;


public class Constants {

    /** The robots this code is built for. Each has its own file: Bot88888 and so on. */
    public enum Robot { BOT_88888, BOT_37523, BOT_31459 }

    /**
     * The robot this code is going onto. On a robot, the Robot Controller name
     * picks it, when that name starts with one of our robots' numbers, such as
     * 88888-RC; then this line is replaced. Otherwise, and in the simulator and
     * the tests, which have no Robot Controller name, this line picks it: change
     * it when you switch robots. What differs between robots is in that robot's
     * own file; what they share is in this one.
     */
    public static Robot thisRobot = Robot.BOT_37523;

    /** True when the Robot Controller name picked thisRobot, false when the line above did. */
    public static boolean pickedByRobotControllerName = false;

    // ---- Device names. These must match the Robot Controller configuration,
    // ---- and nothing outside this file and the robots' own files should name
    // ---- a device. The drivetrain's four names and the Pinpoint's are in each
    // ---- robot's drivetrainConfig and localizerConfig. -----------------------

    public static String imuName = "imu";

    // goBILDA's StarterBot names for its mechanisms, as its example code has them.
    public static String intakeName = "intake";
    public static String leftIntakeServoName = "left_intake_servo";
    public static String rightIntakeServoName = "right_intake_servo";
    public static String launcherName = "launcher";
    public static String windmillServoName = "windmillServo";

    // ---- Each robot's own values come from its file, Bot88888 and so on: the
    // ---- three configs its AutoTune procedures printed, pasted whole, and its
    // ---- other numbers. A value marked MEASURE there is a starting point until
    // ---- it is measured on that robot. The Pedro tuners are in Tuning, under
    // ---- AutoTune. ---------------------------------------------------------

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
    public static MecanumConfig drivetrainConfig;
    public static PinpointConfig localizerConfig;
    public static ForesightConfig foresightConfig;

    /** Ticks per inch of wheel travel. L113 works it out from the parts, and L131 measures it. */
    public static double ticksPerInch;

    /**
     * How far a wheel's rim rolls for each radian the robot turns, inches: A + B
     * for 45-degree mecanum wheels. L113 works it out from where the wheels sit,
     * and L131 measures it.
     */
    public static double turnRadiusInches;

    /**
     * The fastest the robot goes, inches per second, and the power it takes to
     * ask for one inch per second. Each is read from the robot's Foresight
     * values: maxAchievableForwardVelocity, maxAchievableStrafeVelocity, and
     * the coast feedforward. Strafing is slower than driving because mecanum
     * rollers waste some of it sideways.
     */
    public static double maxForwardInchesPerSecond;
    public static double maxStrafeInchesPerSecond;
    public static double powerPerInchPerSecond;

    /** The launcher's target speed, and the speed above which the windmill feeds it, ticks/s. */
    public static double launcherTargetTicksPerSecond;
    public static double launcherFeedAboveTicksPerSecond;

    /** The launcher motor's P, I and D, in the hub's own units. */
    public static double launcherVelocityP;
    public static double launcherVelocityI;
    public static double launcherVelocityD;

    /**
     * The launcher's feedforward, as a fraction of full power: the static part,
     * and the part for each tick per second of its target.
     */
    public static double launcherStaticPower;
    public static double launcherPowerPerTickPerSecond;

    static {
        RobotControllerName.recordConstantsRunning();
        pickByTheRobotControllerName(RobotControllerName.nameWhenConstantsRan());
        takeThisRobotsValues();
        setWhatEveryRobotShares();
        readTheDriveNumbersFromForesight();
    }

    /**
     * thisRobot from the Robot Controller name, such as BOT_88888 from 88888-RC,
     * when that name starts with one of our robots' numbers. Otherwise thisRobot
     * stays as written.
     */
    static void pickByTheRobotControllerName(String robotControllerName) {
        Robot named = robotNamedBy(robotControllerName);
        if (named != null) {
            thisRobot = named;
            pickedByRobotControllerName = true;
        }
    }

    /** The robot whose number the Robot Controller name starts with, or null if none. */
    static Robot robotNamedBy(String robotControllerName) {
        Robot named = null;
        if (robotControllerName.startsWith("88888")) {
            named = Robot.BOT_88888;
        } else if (robotControllerName.startsWith("37523")) {
            named = Robot.BOT_37523;
        } else if (robotControllerName.startsWith("31459")) {
            named = Robot.BOT_31459;
        }
        return named;
    }

    /** The values in the file of the robot thisRobot names. */
    static void takeThisRobotsValues() {
        if (thisRobot == Robot.BOT_88888) {
            takeBot88888sValues();
        } else if (thisRobot == Robot.BOT_37523) {
            takeBot37523sValues();
        } else {
            takeBot31459sValues();
        }
    }

    static void takeBot88888sValues() {
        drivetrainConfig = Bot88888.drivetrainConfig;
        localizerConfig = Bot88888.localizerConfig;
        foresightConfig = Bot88888.foresightConfig;
        ticksPerInch = Bot88888.ticksPerInch;
        turnRadiusInches = Bot88888.turnRadiusInches;
        launcherTargetTicksPerSecond = Bot88888.launcherTargetTicksPerSecond;
        launcherFeedAboveTicksPerSecond = Bot88888.launcherFeedAboveTicksPerSecond;
        launcherVelocityP = Bot88888.launcherVelocityP;
        launcherVelocityI = Bot88888.launcherVelocityI;
        launcherVelocityD = Bot88888.launcherVelocityD;
        launcherStaticPower = Bot88888.launcherStaticPower;
        launcherPowerPerTickPerSecond = Bot88888.launcherPowerPerTickPerSecond;
    }

    static void takeBot37523sValues() {
        drivetrainConfig = Bot37523.drivetrainConfig;
        localizerConfig = Bot37523.localizerConfig;
        foresightConfig = Bot37523.foresightConfig;
        ticksPerInch = Bot37523.ticksPerInch;
        turnRadiusInches = Bot37523.turnRadiusInches;
        launcherTargetTicksPerSecond = Bot37523.launcherTargetTicksPerSecond;
        launcherFeedAboveTicksPerSecond = Bot37523.launcherFeedAboveTicksPerSecond;
        launcherVelocityP = Bot37523.launcherVelocityP;
        launcherVelocityI = Bot37523.launcherVelocityI;
        launcherVelocityD = Bot37523.launcherVelocityD;
        launcherStaticPower = Bot37523.launcherStaticPower;
        launcherPowerPerTickPerSecond = Bot37523.launcherPowerPerTickPerSecond;
    }

    static void takeBot31459sValues() {
        drivetrainConfig = Bot31459.drivetrainConfig;
        localizerConfig = Bot31459.localizerConfig;
        foresightConfig = Bot31459.foresightConfig;
        ticksPerInch = Bot31459.ticksPerInch;
        turnRadiusInches = Bot31459.turnRadiusInches;
        launcherTargetTicksPerSecond = Bot31459.launcherTargetTicksPerSecond;
        launcherFeedAboveTicksPerSecond = Bot31459.launcherFeedAboveTicksPerSecond;
        launcherVelocityP = Bot31459.launcherVelocityP;
        launcherVelocityI = Bot31459.launcherVelocityI;
        launcherVelocityD = Bot31459.launcherVelocityD;
        launcherStaticPower = Bot31459.launcherStaticPower;
        launcherPowerPerTickPerSecond = Bot31459.launcherPowerPerTickPerSecond;
    }

    /**
     * Choices rather than measurements, the same on every robot, and set on
     * top of its pasted configs because the tuners do not print them.
     */
    static void setWhatEveryRobotShares() {
        // Brake rather than coast when a driver releases the sticks.
        drivetrainConfig.manualBrakeMode.set(true);

        // Pedro's own default, left alone and named here because our drivetrains
        // cache motor writes with it: a power that moves by less than this does
        // not reach the hardware.
        drivetrainConfig.powerThreshold.set(0.01);

        foresightConfig.translationalConstraint.set(0.5);              // hold tolerance in inches
        foresightConfig.headingConstraint.set(Math.toRadians(1));      // hold tolerance degrees
        foresightConfig.velocityConstraint.set(0.1);                   // hold tolerance inches/sec
        foresightConfig.holdPointTranslationalScaling.set(0.45);       // default=0.45
        foresightConfig.holdPointHeadingScaling.set(0.35);             // default=0.35
    }

    /** The three drive numbers, read from the Foresight values rather than written twice. */
    static void readTheDriveNumbersFromForesight() {
        maxForwardInchesPerSecond = foresightConfig.maxAchievableForwardVelocity.get();
        maxStrafeInchesPerSecond = foresightConfig.maxAchievableStrafeVelocity.get();
        Controller coastFeedforward = foresightConfig.coast.get();
        double oneInchPerSecond = 1;
        double measuredInchesPerSecond = 0;
        powerPerInchPerSecond = coastFeedforward.calculate(oneInchPerSecond, measuredInchesPerSecond);
    }

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

