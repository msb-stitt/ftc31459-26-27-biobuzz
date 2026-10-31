package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pedropathing.revhub.localizers.PinpointConfig;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.base.odometry.PinpointSource;
import org.junit.Test;

/**
 * The simulated Pinpoint's pods count what the simulated robot does, as the
 * config says they are mounted.
 */
public class SimPinpointTest {

    private static final double TICKS_PER_INCH = 19.89436789 * 25.4;

    /** A forward pod 2 in left of the middle, counting backward; a strafe pod 7 in behind it. */
    private static PinpointConfig config() {
        return new PinpointConfig(c -> {
            c.name.set("pinpoint");
            c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
            c.xPodOffset.set(2.0);
            c.yPodOffset.set(-7.0);
            c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
            c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
            c.offsetUnits.set(DistanceUnit.INCH);
        });
    }

    private long now_ns = 1;

    /** The robot driven at forward, strafe and turn for a second, in steps of 10 ms. */
    private SimRobot drive(double forward, double strafe, double turn) {
        SimRobot robot = new SimRobot(() -> now_ns);
        robot.localizer.driveFrom(() -> new double[]{forward, strafe, turn});
        for (int i = 0; i <= 100; i++) {
            robot.localizer.update();
            now_ns += 10_000_000L;
        }
        return robot;
    }

    @Test
    public void theForwardPodCountsDriving() {
        SimRobot robot = drive(0.5, 0, 0);
        PinpointSource pinpoint = new SimPinpoint(robot.localizer, config());
        pinpoint.update();
        double forward_in = robot.localizer.forwardInches();
        assertTrue("it moved: " + forward_in, forward_in > 10);
        assertEquals("REVERSED counts down", -forward_in * TICKS_PER_INCH,
                pinpoint.getEncoderX(), 1);
        assertEquals("the strafe pod stays put", 0, pinpoint.getEncoderY());
        assertEquals(0, pinpoint.getHeadingRadians(), 1e-9);
    }

    @Test
    public void theStrafePodCountsSliding() {
        SimRobot robot = drive(0, 0.5, 0);
        PinpointSource pinpoint = new SimPinpoint(robot.localizer, config());
        pinpoint.update();
        double left_in = robot.localizer.strafeInches();
        assertTrue("it moved: " + left_in, left_in > 10);
        assertEquals(left_in * TICKS_PER_INCH, pinpoint.getEncoderY(), 1);
        assertEquals("the forward pod stays put", 0, pinpoint.getEncoderX());
    }

    @Test
    public void turningRollsEachPodByItsOffset() {
        SimRobot robot = drive(0, 0, 0.5);
        PinpointSource pinpoint = new SimPinpoint(robot.localizer, config());
        pinpoint.update();
        double turned_rad = robot.localizer.turnedRadians();
        assertTrue("it turned: " + turned_rad, turned_rad > 1);
        // 2 in to the left, turning counterclockwise, rolls backward; REVERSED counts that up.
        assertEquals(2.0 * turned_rad * TICKS_PER_INCH, pinpoint.getEncoderX(), 1);
        // 7 in behind, turning counterclockwise, rolls to the right.
        assertEquals(-7.0 * turned_rad * TICKS_PER_INCH, pinpoint.getEncoderY(), 1);
        assertEquals(turned_rad, pinpoint.getHeadingRadians(), 1e-9);
    }

    @Test
    public void theCountsHoldUntilTheNextUpdate() {
        SimRobot robot = drive(0.5, 0, 0);
        PinpointSource pinpoint = new SimPinpoint(robot.localizer, config());
        pinpoint.update();
        int before = pinpoint.getEncoderX();
        now_ns += 100_000_000L;
        robot.localizer.update();
        assertEquals(before, pinpoint.getEncoderX());
        pinpoint.update();
        assertTrue(pinpoint.getEncoderX() != before);
    }
}
