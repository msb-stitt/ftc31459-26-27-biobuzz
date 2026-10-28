package org.firstinspires.ftc.teamcode.pedro;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver.EncoderDirection;
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction;

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
        MecanumConfig c = Constants.drivetrainConfig;
        assertEquals(Direction.REVERSE, c.frontLeftDirection.get());
        assertEquals(Direction.FORWARD, c.frontRightDirection.get());
        assertEquals(Direction.FORWARD, c.backLeftDirection.get());
        assertEquals(Direction.FORWARD, c.backRightDirection.get());
        assertEquals(45.0, Constants.ticksPerInch, 0);
        assertEquals(8.0, Constants.turnRadiusInches, 0);
        assertEquals(1250, Constants.launcherTargetTicksPerSecond, 0);
        assertEquals(1200, Constants.launcherFeedAboveTicksPerSecond, 0);
        assertEquals(40, Constants.launcherVelocityP, 0);
        assertEquals(0.05, Constants.launcherStaticPower, 0);
        assertEquals(0.00037963, Constants.launcherPowerPerTickPerSecond, 0);
    }

    @Test
    public void robot88888KeepsDevsValuesAnd37523HasItsOwn() {
        MecanumConfig drive88888 = Bot88888.drivetrainConfig;
        assertEquals(Direction.FORWARD, drive88888.frontLeftDirection.get());
        assertEquals(Direction.REVERSE, drive88888.frontRightDirection.get());
        assertEquals(Direction.FORWARD, drive88888.backLeftDirection.get());
        assertEquals(Direction.REVERSE, drive88888.backRightDirection.get());
        assertEquals(1.0581908639021746, Bot88888.localizerConfig.xPodOffset.get(), 0);
        assertEquals(EncoderDirection.REVERSED, Bot88888.localizerConfig.xPodDirection.get());
        assertEquals(64.43298446564829, Bot88888.foresightConfig.maxAchievableForwardVelocity.get(), 0);

        assertEquals(-1.1594286490613082, Bot37523.localizerConfig.xPodOffset.get(), 0);
        assertEquals(EncoderDirection.FORWARD, Bot37523.localizerConfig.xPodDirection.get());
        assertEquals(60.37859937397769, Bot37523.foresightConfig.maxAchievableForwardVelocity.get(), 0);
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
        assertTrue(Constants.drivetrainConfig.manualBrakeMode.get());
        assertEquals(0.01, Constants.drivetrainConfig.powerThreshold.get(), 0);
        ForesightConfig f = Constants.foresightConfig;
        assertEquals(0.5, f.translationalConstraint.get(), 0);
        assertEquals(Math.toRadians(1), f.headingConstraint.get(), 0);
        assertEquals(0.1, f.velocityConstraint.get(), 0);
        assertEquals(0.45, f.holdPointTranslationalScaling.get(), 0);
        assertEquals(0.35, f.holdPointHeadingScaling.get(), 0);
    }

    @Test
    public void theDriveNumbersAreReadFromTheRobotsForesightValues() {
        assertEquals(60.37859937397769, Constants.maxForwardInchesPerSecond, 0);
        assertEquals(51.208204281004114, Constants.maxStrafeInchesPerSecond, 0);
        assertEquals("the coast feedforward", 0.016982498310964104, Constants.powerPerInchPerSecond, 1e-15);
    }
}
