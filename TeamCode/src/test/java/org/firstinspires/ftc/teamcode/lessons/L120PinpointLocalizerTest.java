package org.firstinspires.ftc.teamcode.lessons;

import static org.junit.Assert.assertEquals;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.localization.Localizer;
import com.pedropathing.math.Pose;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.base.odometry.PinpointSource;
import org.junit.Test;

/** L120: does the Pinpoint localizer's arithmetic work, on a laptop, with no robot? */
public class L120PinpointLocalizerTest {

    private static final PoseFactory POSES = PoseFactory.degrees();
    private static final double EPS = 0.01;
    private static final double TICKS_PER_INCH = 19.89436789 * 25.4;
    /** The forward pod 2 in left of the middle, counting backward; the strafe pod 7 in behind. */
    private static final double X_POD_OFFSET_IN = 2.0;
    private static final double Y_POD_OFFSET_IN = -7.0;

    private static PinpointConfig config() {
        return new PinpointConfig(c -> {
            c.name.set("pinpoint");
            c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
            c.xPodOffset.set(X_POD_OFFSET_IN);
            c.yPodOffset.set(Y_POD_OFFSET_IN);
            c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.REVERSED);
            c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
            c.offsetUnits.set(DistanceUnit.INCH);
        });
    }

    /** Pod counts and a heading the test sets by hand, with the pods mounted as config() says. */
    private static final class FakePinpoint implements PinpointSource {
        double forwardPod_in;
        double strafePod_in;
        double heading;

        /** Rolls the pods as if the robot moved forward and left, then turned counterclockwise. */
        void move(double forward, double left, double turn_rad) {
            forwardPod_in += forward - X_POD_OFFSET_IN * turn_rad;
            strafePod_in += left + Y_POD_OFFSET_IN * turn_rad;
            heading += turn_rad;
        }

        @Override
        public void update() {
        }

        @Override
        public int getEncoderX() {
            return (int) Math.round(-forwardPod_in * TICKS_PER_INCH);
        }

        @Override
        public int getEncoderY() {
            return (int) Math.round(strafePod_in * TICKS_PER_INCH);
        }

        @Override
        public double getHeadingRadians() {
            return heading;
        }
    }

    private final FakePinpoint pinpoint = new FakePinpoint();
    private final Localizer localizer =
            MyTry.make("L120PinpointLocalizer", Localizer.class, pinpoint, config());

    private void step() {
        localizer.update();
    }

    @Test
    public void startsAtTheOrigin() {
        step();
        assertEquals(0.0, localizer.pose().x(), EPS);
        assertEquals(0.0, localizer.pose().y(), EPS);
    }

    @Test
    public void countsStraightAheadWithTheReversedPod() {
        step();
        pinpoint.move(24, 0, 0);
        step();
        assertEquals("the forward pod counts down, and is REVERSED", 24.0, localizer.pose().x(), EPS);
        assertEquals(0.0, localizer.pose().y(), EPS);
    }

    @Test
    public void countsStrafingLeft() {
        step();
        pinpoint.move(0, 12, 0);
        step();
        assertEquals(0.0, localizer.pose().x(), EPS);
        assertEquals("left is +y when facing +x", 12.0, localizer.pose().y(), EPS);
    }

    @Test
    public void turningOnTheSpotDoesNotMoveIt() {
        step();
        pinpoint.move(0, 0, Math.toRadians(90));
        step();
        assertEquals("the forward pod rolled, but only from turning", 0.0, localizer.pose().x(), EPS);
        assertEquals("the strafe pod rolled, but only from turning", 0.0, localizer.pose().y(), EPS);
        assertEquals(Math.toRadians(90), localizer.pose().heading(), EPS);
    }

    @Test
    public void appliesTheHeadingSoForwardIsTheRobots() {
        pinpoint.heading = Math.toRadians(90);   // robot faces +y
        step();
        pinpoint.move(10, 0, 0);                 // 10 inches "forward"
        step();
        assertEquals(0.0, localizer.pose().x(), EPS);
        assertEquals(10.0, localizer.pose().y(), EPS);
    }

    @Test
    public void addsUpOverManyLoops() {
        step();
        for (int i = 0; i < 100; i++) {
            pinpoint.move(0.24, 0, 0);
            step();
        }
        assertEquals(24.0, localizer.pose().x(), EPS);
    }

    @Test
    public void setPoseMovesTheEstimateWithoutMovingTheRobot() {
        pinpoint.heading = Math.toRadians(45);
        step();
        localizer.setPose(POSES.of(72, 36, 45));
        Pose p = localizer.pose();
        assertEquals(72.0, p.x(), EPS);
        assertEquals(36.0, p.y(), EPS);
        assertEquals(Math.toRadians(45), p.heading(), EPS);
        pinpoint.move(10, 0, 0);
        step();
        assertEquals(72 + 10 * Math.cos(Math.toRadians(45)), localizer.pose().x(), EPS);
    }
}
