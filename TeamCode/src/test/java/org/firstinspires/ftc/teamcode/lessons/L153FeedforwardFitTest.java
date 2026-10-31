package org.firstinspires.ftc.teamcode.lessons;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** L153: does the fit find the straight line through volts against speed? */
public class L153FeedforwardFitTest {

    private static final double EPS = 1e-9;

    private final Object fit = MyTry.make("L153FeedforwardFit", Object.class);

    private void add(double volts, double speed) {
        MyTry.call(fit, "add", volts, speed);
    }

    private double kS() {
        return (double) MyTry.call(fit, "kS");
    }

    private double kV() {
        return (double) MyTry.call(fit, "kV");
    }

    @Test
    public void twoPointsGiveTheLineThroughThem() {
        add(2.0, 10.0);
        add(4.0, 30.0);
        assertEquals("2 more volts for 20 more inches per second", 0.1, kV(), EPS);
        assertEquals("the line crosses zero speed at 1 volt", 1.0, kS(), EPS);
    }

    @Test
    public void fourPointsOnALineGiveThatLine() {
        double[] speeds = {10.0, 25.0, 40.0, 55.0};
        for (double speed : speeds) {
            add(0.6 + 0.177 * speed, speed);
        }
        assertEquals(0.177, kV(), EPS);
        assertEquals(0.6, kS(), EPS);
    }

    @Test
    public void theOrderOfThePointsDoesNotMatter() {
        add(4.0, 30.0);
        add(2.0, 10.0);
        assertEquals(0.1, kV(), EPS);
        assertEquals(1.0, kS(), EPS);
    }

    @Test
    public void aPointOffTheLinePullsTheLineAsLittleAsItCan() {
        add(1.0, 0.0);
        add(3.0, 1.0);
        add(5.0, 2.0);
        add(8.0, 3.0);
        assertEquals("not 2.0, the slope of the first three, nor 3.0, the last", 2.3, kV(), EPS);
        assertEquals(0.8, kS(), EPS);
    }
}
