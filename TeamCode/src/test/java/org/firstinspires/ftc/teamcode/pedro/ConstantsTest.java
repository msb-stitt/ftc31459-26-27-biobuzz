package org.firstinspires.ftc.teamcode.pedro;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.revhub.drivetrains.MecanumConfig;

import org.junit.Test;

/** Which robot's values Constants holds, and that each robot's file gives its own. */
public class ConstantsTest {

    @Test
    public void eachRobotControllerNameStartingWithOurNumberPicksThatRobot() {
        assertEquals(Constants.Robot.BOT_88888, Constants.robotNamedBy("88888-RC"));
        assertEquals(Constants.Robot.BOT_37523, Constants.robotNamedBy("37523-RC"));
        assertEquals(Constants.Robot.BOT_31459, Constants.robotNamedBy("31459-RC"));
        assertNull("not one of ours", Constants.robotNamedBy("12345-RC"));
        assertNull("not at the start", Constants.robotNamedBy("RC-88888"));
        assertNull("unreadable",
                Constants.robotNamedBy("(could not read: no Robot Controller name yet)"));
    }

    @Test
    public void theRobotControllerNamePicksThisRobotOverTheLineWrittenInConstants() {
        try {
            Constants.pickByTheRobotControllerName("88888-RC");
            assertEquals(Constants.Robot.BOT_88888, Constants.thisRobot);
            assertTrue(Constants.pickedByRobotControllerName);
        } finally {
            Constants.thisRobot = Constants.Robot.BOT_37523;
            Constants.pickedByRobotControllerName = false;
        }
    }

    @Test
    public void aRobotControllerNameOfNoRobotLeavesThisRobotAsWritten() {
        Constants.pickByTheRobotControllerName("12345-RC");
        assertEquals(Constants.Robot.BOT_37523, Constants.thisRobot);
        assertFalse(Constants.pickedByRobotControllerName);
    }

    @Test
    public void theDefaultRobotIs37523AndItsValuesAreTheOnesInUse() {
        assertEquals(Constants.Robot.BOT_37523, Constants.thisRobot);
        assertFalse("the tests have no Robot Controller name",
                Constants.pickedByRobotControllerName);
        assertSame(Bot37523.drivetrainConfig, Constants.drivetrainConfig);
        assertSame(Bot37523.localizerConfig, Constants.localizerConfig);
        assertSame(Bot37523.foresightConfig, Constants.foresightConfig);
        assertEquals(Bot37523.ticksPerInch, Constants.ticksPerInch, 0);
        assertEquals(Bot37523.turnRadiusInches, Constants.turnRadiusInches, 0);
        assertEquals(Bot37523.kSForwardVolts, Constants.kSForwardVolts, 0);
        assertEquals(Bot37523.kVForwardVoltSecondsPerInch, Constants.kVForwardVoltSecondsPerInch, 0);
        assertEquals(Bot37523.kSStrafeVolts, Constants.kSStrafeVolts, 0);
        assertEquals(Bot37523.kVStrafeVoltSecondsPerInch, Constants.kVStrafeVoltSecondsPerInch, 0);
        assertEquals(Bot37523.kSTurnVolts, Constants.kSTurnVolts, 0);
        assertEquals(Bot37523.kVTurnVoltSecondsPerRadian, Constants.kVTurnVoltSecondsPerRadian, 0);
        assertEquals(Bot37523.launcherTargetTicksPerSecond, Constants.launcherTargetTicksPerSecond, 0);
        assertEquals(Bot37523.launcherFeedAboveTicksPerSecond,
                Constants.launcherFeedAboveTicksPerSecond, 0);
        assertEquals(Bot37523.launcher_P_ctrlUnitsPerTickPerS, Constants.launcher_P_ctrlUnitsPerTickPerS, 0);
        assertEquals(Bot37523.launcher_I, Constants.launcher_I, 0);
        assertEquals(Bot37523.launcher_D, Constants.launcher_D, 0);
        assertEquals(Bot37523.launcher_kS_powerFraction, Constants.launcher_kS_powerFraction, 0);
        assertEquals(Bot37523.launcher_kV_powerFractionPerTickPerS,
                Constants.launcher_kV_powerFractionPerTickPerS, 0);
    }

    @Test
    public void robot88888And37523EachHaveTheirOwnConfigs() {
        assertNotSame(Bot37523.drivetrainConfig, Bot88888.drivetrainConfig);
        assertNotSame(Bot37523.localizerConfig, Bot88888.localizerConfig);
        assertNotSame(Bot37523.foresightConfig, Bot88888.foresightConfig);
    }

    @Test
    public void robot31459And37523EachHaveTheirOwnConfigs() {
        assertNotSame(Bot37523.drivetrainConfig, Bot31459.drivetrainConfig);
        assertNotSame(Bot37523.localizerConfig, Bot31459.localizerConfig);
        assertNotSame(Bot37523.foresightConfig, Bot31459.foresightConfig);
    }

    @Test
    public void thisRobotPicksWhichFileTheValuesComeFrom() {
        try {
            Constants.thisRobot = Constants.Robot.BOT_88888;
            Constants.takeThisRobotsValues();
            assertSame(Bot88888.drivetrainConfig, Constants.drivetrainConfig);
            assertSame(Bot88888.localizerConfig, Constants.localizerConfig);
            assertSame(Bot88888.foresightConfig, Constants.foresightConfig);

            Constants.thisRobot = Constants.Robot.BOT_31459;
            Constants.takeThisRobotsValues();
            assertSame(Bot31459.drivetrainConfig, Constants.drivetrainConfig);
            assertSame(Bot31459.localizerConfig, Constants.localizerConfig);
            assertSame(Bot31459.foresightConfig, Constants.foresightConfig);
            assertEquals(Bot31459.ticksPerInch, Constants.ticksPerInch, 0);
            assertEquals(Bot31459.turnRadiusInches, Constants.turnRadiusInches, 0);
            assertEquals(Bot31459.kSForwardVolts, Constants.kSForwardVolts, 0);
            assertEquals(Bot31459.kVForwardVoltSecondsPerInch,
                    Constants.kVForwardVoltSecondsPerInch, 0);
            assertEquals(Bot31459.kSStrafeVolts, Constants.kSStrafeVolts, 0);
            assertEquals(Bot31459.kVStrafeVoltSecondsPerInch,
                    Constants.kVStrafeVoltSecondsPerInch, 0);
            assertEquals(Bot31459.kSTurnVolts, Constants.kSTurnVolts, 0);
            assertEquals(Bot31459.kVTurnVoltSecondsPerRadian,
                    Constants.kVTurnVoltSecondsPerRadian, 0);
            assertEquals(Bot31459.launcherTargetTicksPerSecond,
                    Constants.launcherTargetTicksPerSecond, 0);
            assertEquals(Bot31459.launcherFeedAboveTicksPerSecond,
                    Constants.launcherFeedAboveTicksPerSecond, 0);
            assertEquals(Bot31459.launcher_P_ctrlUnitsPerTickPerS, Constants.launcher_P_ctrlUnitsPerTickPerS, 0);
            assertEquals(Bot31459.launcher_I, Constants.launcher_I, 0);
            assertEquals(Bot31459.launcher_D, Constants.launcher_D, 0);
            assertEquals(Bot31459.launcher_kS_powerFraction, Constants.launcher_kS_powerFraction, 0);
            assertEquals(Bot31459.launcher_kV_powerFractionPerTickPerS,
                    Constants.launcher_kV_powerFractionPerTickPerS, 0);
        } finally {
            Constants.thisRobot = Constants.Robot.BOT_37523;
            Constants.takeThisRobotsValues();
        }
    }

    @Test
    public void eachRobotsChoicesAreSetOnTopOfItsPaste() {
        try {
            Constants.thisRobot = Constants.Robot.BOT_88888;
            Constants.takeThisRobotsValues();
            MecanumConfig d = Constants.drivetrainConfig;
            ForesightConfig f = Constants.foresightConfig;
            assertEquals(Bot88888.manualBrakeMode, d.manualBrakeMode.get());
            assertEquals(Bot88888.powerThreshold, d.powerThreshold.get(), 0);
            assertEquals(Bot88888.translationalConstraint, f.translationalConstraint.get(), 0);
            assertEquals(Bot88888.headingConstraint, f.headingConstraint.get(), 0);
            assertEquals(Bot88888.velocityConstraint, f.velocityConstraint.get(), 0);
            assertEquals(Bot88888.holdPointTranslationalScaling,
                    f.holdPointTranslationalScaling.get(), 0);
            assertEquals(Bot88888.holdPointHeadingScaling, f.holdPointHeadingScaling.get(), 0);

            Constants.thisRobot = Constants.Robot.BOT_37523;
            Constants.takeThisRobotsValues();
            d = Constants.drivetrainConfig;
            f = Constants.foresightConfig;
            assertEquals(Bot37523.manualBrakeMode, d.manualBrakeMode.get());
            assertEquals(Bot37523.powerThreshold, d.powerThreshold.get(), 0);
            assertEquals(Bot37523.translationalConstraint, f.translationalConstraint.get(), 0);
            assertEquals(Bot37523.headingConstraint, f.headingConstraint.get(), 0);
            assertEquals(Bot37523.velocityConstraint, f.velocityConstraint.get(), 0);
            assertEquals(Bot37523.holdPointTranslationalScaling,
                    f.holdPointTranslationalScaling.get(), 0);
            assertEquals(Bot37523.holdPointHeadingScaling, f.holdPointHeadingScaling.get(), 0);

            Constants.thisRobot = Constants.Robot.BOT_31459;
            Constants.takeThisRobotsValues();
            d = Constants.drivetrainConfig;
            f = Constants.foresightConfig;
            assertEquals(Bot31459.manualBrakeMode, d.manualBrakeMode.get());
            assertEquals(Bot31459.powerThreshold, d.powerThreshold.get(), 0);
            assertEquals(Bot31459.translationalConstraint, f.translationalConstraint.get(), 0);
            assertEquals(Bot31459.headingConstraint, f.headingConstraint.get(), 0);
            assertEquals(Bot31459.velocityConstraint, f.velocityConstraint.get(), 0);
            assertEquals(Bot31459.holdPointTranslationalScaling,
                    f.holdPointTranslationalScaling.get(), 0);
            assertEquals(Bot31459.holdPointHeadingScaling, f.holdPointHeadingScaling.get(), 0);
        } finally {
            Constants.thisRobot = Constants.Robot.BOT_37523;
            Constants.takeThisRobotsValues();
        }
    }

    @Test
    public void theDriveNumbersAreReadFromTheRobotsForesightValues() {
        ForesightConfig f = Constants.foresightConfig;
        assertEquals(f.maxAchievableForwardVelocity.get(), Constants.maxForwardInchesPerSecond, 0);
        assertEquals(f.maxAchievableStrafeVelocity.get(), Constants.maxStrafeInchesPerSecond, 0);
        assertEquals("the coast feedforward", f.coast.get().calculate(1, 0),
                Constants.powerPerInchPerSecond, 1e-15);
    }
}
