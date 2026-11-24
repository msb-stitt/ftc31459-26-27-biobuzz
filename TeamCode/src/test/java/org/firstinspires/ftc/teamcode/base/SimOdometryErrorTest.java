package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.pedropathing.math.Pose;

import org.junit.Test;

import java.util.Arrays;

/**
 * The simulated Pinpoint reads the robot's moves with a tenth of the error
 * waggle found: its pods' scale errors and one slip in the first dash, the
 * heading true.
 */
public class SimOdometryErrorTest {

    private static final double EPS = 1e-9;

    /** The robot followed from MuJoCo's motion, driven along +x, then turned to +y and driven on. */
    private static SimRobot.SimLocalizer drive(SimOdometryError error, double alongX, double alongY) {
        SimRobot.SimLocalizer l = new SimRobot(() -> 0L).localizer;
        l.odometryError(error);
        double[] at = new double[3]; // x, y, travel
        l.follow(() -> new SimRobot.Motion(at[0], at[1], at[1] > 0 ? Math.PI / 2 : 0, 0, 0, 0,
                at[1] > 0 ? Math.PI / 2 : 0, new double[4], at[2], 0, 0));
        for (double step = 0; step < alongX; step += 1) {
            at[0] += 1; at[2] += 1; l.update();
        }
        for (double step = 0; step < alongY; step += 1) {
            at[1] += 1; at[2] += 1; l.update();
        }
        return l;
    }

    @Test
    public void thePodsReadShortBySlipOverTheFirstDashThenTrue() {
        SimRobot.SimLocalizer l = drive(new SimOdometryError(0, 0, 0.4, 96), 120, 0);
        Pose p = l.state().pose();
        assertEquals(120, l.truePose().x(), EPS);
        assertEquals("0.4 in short over the first 96 in, none after", 119.6, p.x(), EPS);
        assertEquals(119.6, l.forwardInches(), EPS);
        assertEquals("the heading reads true", 0, p.heading(), EPS);
    }

    @Test
    public void eachPodReadsItsTravelScaledByItsErrorTurnedWithTheRobot() {
        SimRobot.SimLocalizer l = drive(new SimOdometryError(0.001, 0, 0, 96), 100, 50);
        Pose p = l.state().pose();
        assertEquals("forward along +x reads 0.1 % long", 100.1, p.x(), EPS);
        assertEquals("then forward along +y", 50.05, p.y(), EPS);
        assertEquals(Math.PI / 2, p.heading(), EPS);
    }

    @Test
    public void aPoseSetReadsAsSetAndTheErrorGrowsFromThere() {
        SimRobot.SimLocalizer l = drive(new SimOdometryError(0, 0, 0.4, 96), 48, 0);
        l.setPose(new Pose(10, 20, 0));
        assertEquals(10, l.state().pose().x(), EPS);
        assertEquals(20, l.state().pose().y(), EPS);
        assertEquals(0, l.state().pose().heading(), EPS);
    }

    @Test
    public void withNoErrorThePoseIsTheRobots() {
        SimRobot.SimLocalizer l = drive(null, 120, 30);
        assertEquals(l.truePose().x(), l.state().pose().x(), EPS);
        assertEquals(l.truePose().y(), l.state().pose().y(), EPS);
        assertEquals(150, l.forwardInches(), EPS);
    }

    @Test
    public void aRunsPodErrorsHaveAMedianSizeOfATenthOfWaggles() {
        double[] sizes = new double[4000];
        for (int seed = 0; seed < 2000; seed++) {
            SimOdometryError e = SimOdometryError.seeded(seed);
            sizes[2 * seed] = Math.abs(e.forwardScale);
            sizes[2 * seed + 1] = Math.abs(e.strafeScale);
        }
        Arrays.sort(sizes);
        assertEquals(0.000064, (sizes[1999] + sizes[2000]) / 2, 0.000004);
    }

    @Test
    public void simRunsErrorIsSeededAndExactTurnsItOff() {
        assertEquals(SimOdometryError.seeded(1).forwardScale,
                SimOdometryError.fromVariable(null).forwardScale, 0);
        assertEquals(SimOdometryError.seeded(7).strafeScale,
                SimOdometryError.fromVariable("7").strafeScale, 0);
        assertNull(SimOdometryError.fromVariable("exact"));
    }
}
