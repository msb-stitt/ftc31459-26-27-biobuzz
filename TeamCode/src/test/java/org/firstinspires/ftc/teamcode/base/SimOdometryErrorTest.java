package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.pedropathing.math.Pose;

import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

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

    private final long[] now_ns = {1};

    /** A still robot followed from MuJoCo, then {@code seconds} later driven {@code alongX} along +x. */
    private SimRobot.SimLocalizer waitThenDrive(SimOdometryError error, double seconds, double alongX) {
        SimRobot.SimLocalizer l = new SimRobot(() -> now_ns[0]).localizer;
        l.odometryError(error);
        double[] at = new double[1];
        l.follow(() -> new SimRobot.Motion(at[0], 0, 0, 0, 0, 0, 0, new double[4], at[0], 0, 0));
        l.update();
        now_ns[0] += (long) (seconds * 1e9);
        l.update();
        for (int step = 0; step < alongX; step++) {
            at[0] += 1; l.update();
        }
        return l;
    }

    private static SimOdometryError headingOnly(double rate, double noise) {
        return new SimOdometryError(0, 0, 0, 96, 0, new SimHeadingError(rate, noise, new Random(3)), null);
    }

    @Test
    public void theHeadingDriftsFromTrueWhereItsPoseWasSet() {
        double rate = Math.toRadians(0.01);
        SimRobot.SimLocalizer l = waitThenDrive(headingOnly(rate, 0), 100, 0);
        assertEquals(Math.toRadians(1), l.state().pose().heading(), EPS);
        assertEquals("the Pinpoint's own heading reads it too", Math.toRadians(1), l.pinpointTurnedRadians(), EPS);
        l.setPose(new Pose(10, 20, 0.5));
        assertEquals("a pose set reads as set", 0.5, l.state().pose().heading(), EPS);
    }

    @Test
    public void aHeadingOffTurnsEachMoveMadeAfterIt() {
        SimRobot.SimLocalizer l = waitThenDrive(headingOnly(Math.toRadians(0.01), 0), 100, 100);
        assertEquals(100 * Math.sin(Math.toRadians(1)), l.state().pose().y(), 1e-6);
        assertEquals(0, l.truePose().y(), EPS);
    }

    @Test
    public void eachReadingsNoiseHasItsSpread() {
        SimOdometryError e = new SimOdometryError(0, 0, 0, 96, 0.3,
                new SimHeadingError(0, Math.toRadians(0.1), new Random(4)), new Random(5));
        SimRobot.SimLocalizer l = new SimRobot(() -> 0L).localizer;
        l.odometryError(e);
        l.follow(() -> new SimRobot.Motion(0, 0, 0, 0, 0, 0, 0, new double[4], 0, 0, 0));
        int n = 20000;
        double xx = 0, hh = 0;
        for (int i = 0; i < n; i++) {
            l.update();
            xx += l.state().pose().x() * l.state().pose().x();
            double h = Math.IEEEremainder(l.state().pose().heading(), 2 * Math.PI); // Pedro wraps to 0..2π
            hh += h * h;
        }
        assertEquals(0.3, Math.sqrt(xx / n), 0.01);
        assertEquals(Math.toRadians(0.1), Math.sqrt(hh / n), Math.toRadians(0.005));
    }

    @Test
    public void theImuIsThreeTimesAsNoisyAndDriftyAsThePinpoint() {
        double[] pinpoint = new double[2000], imu = new double[2000];
        for (int seed = 0; seed < 2000; seed++) {
            pinpoint[seed] = Math.abs(SimOdometryError.seeded(seed).heading.rate);
            imu[seed] = Math.abs(SimOdometryError.imu(seed).rate);
        }
        Arrays.sort(pinpoint);
        Arrays.sort(imu);
        double median = SimOdometryError.HEADING_RATE_MEDIAN;
        assertEquals(median, (pinpoint[999] + pinpoint[1000]) / 2, median * 0.08);
        assertEquals(3 * median, (imu[999] + imu[1000]) / 2, 3 * median * 0.08);
        assertEquals(3 * SimOdometryError.seeded(1).heading.noise, SimOdometryError.imu(1).noise, EPS);
    }

    @Test
    public void withNoImuErrorTheImuReadsTheTurn() {
        SimRobot.SimLocalizer l = drive(null, 10, 10);
        assertEquals(l.turnedRadians(), l.imuTurnedRadians(), EPS);
        l.imuError(new SimHeadingError(0.001, 0, null));
        assertEquals(l.turnedRadians() + 0.001 * l.seconds(), l.imuTurnedRadians(), EPS);
    }

    @Test
    public void simRunsErrorIsSeededAndExactTurnsItOff() {
        assertEquals(SimOdometryError.seeded(1).forwardScale,
                SimOdometryError.fromVariable(null).forwardScale, 0);
        assertEquals(SimOdometryError.seeded(7).strafeScale,
                SimOdometryError.fromVariable("7").strafeScale, 0);
        assertNull(SimOdometryError.fromVariable("exact"));
        assertNull(SimOdometryError.imuFromVariable("exact"));
    }
}
