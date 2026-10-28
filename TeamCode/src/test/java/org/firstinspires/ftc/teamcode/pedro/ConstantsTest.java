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
        assertEquals(Bot37523.launcherTargetTicksPerSecond, Constants.launcherTargetTicksPerSecond, 0);
        assertEquals(Bot37523.launcherFeedAboveTicksPerSecond,
                Constants.launcherFeedAboveTicksPerSecond, 0);
        assertEquals(Bot37523.launcherVelocityP, Constants.launcherVelocityP, 0);
        assertEquals(Bot37523.launcherVelocityI, Constants.launcherVelocityI, 0);
        assertEquals(Bot37523.launcherVelocityD, Constants.launcherVelocityD, 0);
        assertEquals(Bot37523.launcherStaticPower, Constants.launcherStaticPower, 0);
        assertEquals(Bot37523.launcherPowerPerTickPerSecond,
                Constants.launcherPowerPerTickPerSecond, 0);
    }

    @Test
    public void robot88888And37523EachHaveTheirOwnConfigs() {
        assertNotSame(Bot37523.drivetrainConfig, Bot88888.drivetrainConfig);
        assertNotSame(Bot37523.localizerConfig, Bot88888.localizerConfig);
        assertNotSame(Bot37523.foresightConfig, Bot88888.foresightConfig);
    }

    @Test
    public void robot31459Uses37523sValuesUntilItsOwnAreMeasured() {
        assertSame(Bot37523.drivetrainConfig, Bot31459.drivetrainConfig);
        assertSame(Bot37523.localizerConfig, Bot31459.localizerConfig);
        assertSame(Bot37523.foresightConfig, Bot31459.foresightConfig);
        assertEquals(Bot37523.launcherStaticPower, Bot31459.launcherStaticPower, 0);
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
            assertSame(Bot31459.localizerConfig, Constants.localizerConfig);
        } finally {
            Constants.thisRobot = Constants.Robot.BOT_37523;
            Constants.takeThisRobotsValues();
        }
    }

    @Test
    public void theChoicesEveryRobotSharesAreSetOnTopOfItsPaste() {
        MecanumConfig d37523 = Bot37523.drivetrainConfig;
        ForesightConfig f37523 = Bot37523.foresightConfig;
        try {
            Constants.thisRobot = Constants.Robot.BOT_88888;
            Constants.takeThisRobotsValues();
            Constants.setWhatEveryRobotShares();
            MecanumConfig d = Constants.drivetrainConfig;
            ForesightConfig f = Constants.foresightConfig;
            assertEquals(d37523.manualBrakeMode.get(), d.manualBrakeMode.get());
            assertEquals(d37523.powerThreshold.get(), d.powerThreshold.get(), 0);
            assertEquals(f37523.translationalConstraint.get(), f.translationalConstraint.get(), 0);
            assertEquals(f37523.headingConstraint.get(), f.headingConstraint.get(), 0);
            assertEquals(f37523.velocityConstraint.get(), f.velocityConstraint.get(), 0);
            assertEquals(f37523.holdPointTranslationalScaling.get(),
                    f.holdPointTranslationalScaling.get(), 0);
            assertEquals(f37523.holdPointHeadingScaling.get(), f.holdPointHeadingScaling.get(), 0);
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
