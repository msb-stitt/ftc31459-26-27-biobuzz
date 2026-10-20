package org.firstinspires.ftc.teamcode.pedro;

import static org.junit.Assert.assertEquals;

import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction;

import org.junit.Test;

/** Which robot's values Constants holds, and that each robot's file gives its own. */
public class ConstantsTest {

    @Test
    public void theDefaultRobotIs37523AndItsDirectionsAreTheOnesInUse() {
        assertEquals(Constants.Robot.BOT_37523, Constants.thisRobot);
        MecanumConfig c = Constants.drivetrainConfig;
        assertEquals(Direction.REVERSE, c.frontLeftDirection.get());
        assertEquals(Direction.FORWARD, c.frontRightDirection.get());
        assertEquals(Direction.FORWARD, c.backLeftDirection.get());
        assertEquals(Direction.FORWARD, c.backRightDirection.get());
    }

    @Test
    public void robot88888ReversesItsBackLeftMotorWhere37523DoesNot() {
        MecanumConfig c = new MecanumConfig(config -> { });
        Bot88888.drivetrain(c);
        assertEquals(Direction.REVERSE, c.backLeftDirection.get());
        Bot37523.drivetrain(c);
        assertEquals(Direction.FORWARD, c.backLeftDirection.get());
    }
}
