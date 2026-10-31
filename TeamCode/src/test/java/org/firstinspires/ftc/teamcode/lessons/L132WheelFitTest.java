package org.firstinspires.ftc.teamcode.lessons;

import static org.junit.Assert.assertEquals;

import org.firstinspires.ftc.teamcode.base.WheelTargets;
import org.junit.Test;

/** L132: do the sums give back the angle and the radius the wheels were rolled with? */
public class L132WheelFitTest {

    private static final double EPS = 1e-9;
    /** The wheels these tests roll: rollers at 40°, and a turn radius of 8.5 in. */
    private static final double COT_ALPHA = 1 / Math.tan(Math.toRadians(40));
    private static final double TURN_RADIUS_IN = 8.5;

    private final Object fit = MyTry.make("L132WheelFit", Object.class);

    /** One loop: the robot moves forward, slides left and turns, and each wheel rolls its share. */
    private void move(double forward_in, double strafe_in, double turn_rad) {
        double[] wheelSteps_in = WheelTargets.forMecanum(
                forward_in, strafe_in * COT_ALPHA, turn_rad, TURN_RADIUS_IN);
        MyTry.call(fit, "addWheels", (Object) wheelSteps_in);
        MyTry.call(fit, "addRobot", strafe_in, turn_rad);
    }

    private double cotAlpha() {
        return (double) MyTry.call(fit, "cotAlpha");
    }

    private double turnRadius() {
        return (double) MyTry.call(fit, "turnRadius_in");
    }

    @Test
    public void slidingGivesCotAlpha() {
        for (int i = 0; i < 10; i++) {
            move(0, 0.5, 0);
        }
        assertEquals("cot 40°", COT_ALPHA, cotAlpha(), EPS);
    }

    @Test
    public void slidingEitherWayGivesTheSameCotAlpha() {
        move(0, 3.0, 0);
        move(0, -1.0, 0);
        assertEquals(COT_ALPHA, cotAlpha(), EPS);
    }

    @Test
    public void drivingForwardDoesNotCountAsSliding() {
        move(0, 2.0, 0);
        move(5.0, 0, 0);
        assertEquals("forward rolls each wheel the same way, so it cancels", COT_ALPHA, cotAlpha(), EPS);
    }

    @Test
    public void turningGivesTheTurnRadius() {
        for (int i = 0; i < 10; i++) {
            move(0, 0, 0.02);
        }
        assertEquals(TURN_RADIUS_IN, turnRadius(), EPS);
    }

    @Test
    public void aMixedRunGivesBoth() {
        for (int i = 0; i < 10; i++) {
            move(0.4, 0.3, 0.02);
        }
        assertEquals("turning does not count as sliding", COT_ALPHA, cotAlpha(), EPS);
        assertEquals("sliding does not count as turning", TURN_RADIUS_IN, turnRadius(), EPS);
    }
}
