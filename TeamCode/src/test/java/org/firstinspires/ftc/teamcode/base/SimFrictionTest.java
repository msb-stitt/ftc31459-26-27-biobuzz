package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * The simulated robot has static friction, so a fit of volts against steady
 * speed, as L153's will be, finds a kS that is not zero.
 *
 * <p>Each run holds one axis at a fixed power, 0.2, 0.4, 0.6 and 0.8, as
 * L153's autos do, until the speed is steady, on a 12 V battery. A straight
 * line through the four points gives back the kS and kV the simulator was
 * given.
 */
public class SimFrictionTest {

    private static final double VOLTS = 12.0;
    private static final double[] POWERS = {0.2, 0.4, 0.6, 0.8};

    /** The robot's steady speed with one axis held at {@code power}, the others at 0. */
    private static double steadySpeed(int axis, double power) {
        long[] now = {1};
        SimRobot robot = new SimRobot(() -> now[0]);
        double[] command = new double[3];
        command[axis] = power;
        robot.localizer.driveFrom(() -> command);
        // 5 s to settle, then 1 s measured: how far it went in that second.
        run(robot, now, 500);
        double before = travelled(robot, axis);
        run(robot, now, 100);
        return travelled(robot, axis) - before;
    }

    /** {@code loops} updates of 10 ms each. */
    private static void run(SimRobot robot, long[] now, int loops) {
        for (int i = 0; i < loops; i++) {
            now[0] += 10_000_000L;
            robot.localizer.update();
        }
    }

    /** How far the robot has gone along one axis: inches forward or left, or radians turned. */
    private static double travelled(SimRobot robot, int axis) {
        if (axis == 0) return robot.localizer.forwardInches();
        if (axis == 1) return robot.localizer.strafeInches();
        return robot.localizer.turnedRadians();
    }

    /** kS and kV, the intercept and slope of volts against speed, by least squares. */
    private static double[] fit(int axis) {
        int n = POWERS.length;
        double sx = 0, sy = 0, sxx = 0, sxy = 0;
        for (double power : POWERS) {
            double speed = steadySpeed(axis, power);
            double volts = power * VOLTS;
            sx += speed;
            sy += volts;
            sxx += speed * speed;
            sxy += speed * volts;
        }
        double kV = (n * sxy - sx * sy) / (n * sxx - sx * sx);
        double kS = (sy - kV * sx) / n;
        return new double[]{kS, kV};
    }

    private static void assertRecovers(int axis, double kS, double max) {
        double[] found = fit(axis);
        assertEquals("kS, volts", kS * VOLTS, found[0], 1e-6);
        assertEquals("kV, volts per unit of speed", (1 - kS) * VOLTS / max, found[1], 1e-6);
    }

    @Test
    public void belowItsKsTheRobotDoesNotMove() {
        assertEquals(0.0, steadySpeed(0, SimRobot.KS_FORWARD), 1e-9);
        assertEquals(0.0, steadySpeed(1, -SimRobot.KS_STRAFE), 1e-9);
        assertEquals(0.0, steadySpeed(2, SimRobot.KS_TURN / 2), 1e-9);
    }

    @Test
    public void fullPowerIsStillFullSpeed() {
        assertEquals(SimRobot.MAX_FORWARD_IPS, steadySpeed(0, 1.0), 1e-6);
        assertEquals(-SimRobot.MAX_STRAFE_IPS, steadySpeed(1, -1.0), 1e-6);
    }

    @Test
    public void aFitOfForwardRunsFindsItsKsAndKv() {
        assertRecovers(0, SimRobot.KS_FORWARD, SimRobot.MAX_FORWARD_IPS);
    }

    @Test
    public void aFitOfStrafeRunsFindsItsKsAndKv() {
        assertRecovers(1, SimRobot.KS_STRAFE, SimRobot.MAX_STRAFE_IPS);
    }

    @Test
    public void aFitOfTurnRunsFindsItsKsAndKv() {
        assertRecovers(2, SimRobot.KS_TURN, SimRobot.MAX_TURN_RADPS);
    }
}
