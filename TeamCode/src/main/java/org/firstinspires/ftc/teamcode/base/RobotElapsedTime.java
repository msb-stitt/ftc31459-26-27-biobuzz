package org.firstinspires.ftc.teamcode.base;

import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * The SDK's {@link ElapsedTime}, read from {@link RobotClock}: the wall clock
 * on the robot, and the simulated clock in a sim run.
 */
public class RobotElapsedTime extends ElapsedTime {
    @Override
    protected long nsNow() {
        return RobotClock.nanoTime();
    }
}
