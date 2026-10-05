package org.firstinspires.ftc.teamcode.lessons;

import static org.junit.Assert.assertEquals;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.localization.Localizer;
import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.base.odometry.WheelSource;
import org.junit.Test;

/** L120: does the encoder localizer's arithmetic work, on a laptop, with no robot? */
public class L120EncoderLocalizerTest {

    private static final PoseFactory POSES = PoseFactory.degrees();
    private static final double EPS = 1e-6;

    /** Wheel travel and heading the test sets by hand. */
    private static final class FakeWheels implements WheelSource {
        double[] wheels = {0, 0, 0, 0};
        double heading;

        @Override
        public double[] wheelInches() {
            return wheels.clone();
        }

        @Override
        public double headingRadians() {
            return heading;
        }

        /** Rolls the wheels as if the robot moved forward and left. */
        void move(double forward, double left) {
            wheels[0] += forward - left;   // front left
            wheels[1] += forward + left;   // front right
            wheels[2] += forward + left;   // back left
            wheels[3] += forward - left;   // back right
        }
    }

    private final FakeWheels wheels = new FakeWheels();
    private final Localizer localizer = MyTry.make("L120EncoderLocalizer", Localizer.class, wheels);

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
    public void countsStraightAhead() {
        step();
        wheels.move(24, 0);
        step();
        assertEquals(24.0, localizer.pose().x(), EPS);
        assertEquals(0.0, localizer.pose().y(), EPS);
    }

    @Test
    public void countsStrafingLeft() {
        step();
        wheels.move(0, 12);
        step();
        assertEquals(0.0, localizer.pose().x(), EPS);
        assertEquals("left is +y when facing +x", 12.0, localizer.pose().y(), EPS);
    }

    @Test
    public void appliesTheHeadingSoSidewaysIsRelativeToTheRobot() {
        wheels.heading = Math.toRadians(90);     // robot faces +y
        step();
        wheels.move(10, 0);                      // 10 inches "forward"
        step();
        assertEquals(0.0, localizer.pose().x(), 1e-6);
        assertEquals(10.0, localizer.pose().y(), 1e-6);
    }

    @Test
    public void readsHeadingStraightFromTheImu() {
        wheels.heading = Math.toRadians(30);
        step();
        assertEquals(Math.toRadians(30), localizer.pose().heading(), EPS);
    }

    @Test
    public void addsUpOverManyLoops() {
        step();
        for (int i = 0; i < 100; i++) {
            wheels.move(0.24, 0);
            step();
        }
        assertEquals(24.0, localizer.pose().x(), 1e-6);
    }

    @Test
    public void setPoseMovesTheEstimateWithoutMovingTheRobot() {
        wheels.heading = Math.toRadians(45);
        step();
        localizer.setPose(POSES.of(72, 36, 45));
        Pose p = localizer.pose();
        assertEquals(72.0, p.x(), EPS);
        assertEquals(36.0, p.y(), EPS);
        assertEquals(Math.toRadians(45), p.heading(), EPS);
        wheels.move(10, 0);
        step();
        assertEquals(72 + 10 * Math.cos(Math.toRadians(45)), localizer.pose().x(), 1e-6);
    }

    @Test
    public void aWheelThatSlipsIsSimplyBelieved() {
        // The lesson's point: dead reckoning has no way to know.
        step();
        wheels.wheels[0] += 24;                  // one wheel spins on the spot
        step();
        assertEquals("it reports movement that never happened", 6.0, localizer.pose().x(), EPS);
    }
}
