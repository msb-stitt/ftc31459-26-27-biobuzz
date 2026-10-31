package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** The arithmetic the measuring OpModes rely on. */
public class CalibrationTest {

    @Test
    public void pushingStraightForwardShowsUpAsTheForwardPart() {
        assertEquals(100, Calibration.forwardPart(100, 100, 100, 100), 1e-9);
        assertEquals("turning nothing", 0, Calibration.turnPart(100, 100, 100, 100), 1e-9);
    }

    @Test
    public void spinningShowsUpAsTheTurnPart() {
        // Counter-clockwise: left wheels back, right wheels forward.
        assertEquals(50, Calibration.turnPart(-50, 50, -50, 50), 1e-9);
        assertEquals("going nowhere", 0, Calibration.forwardPart(-50, 50, -50, 50), 1e-9);
    }

    @Test
    public void ticksPerInchIsTicksOverInches() {
        assertEquals(45.0, Calibration.ticksPerInch(1800, 40), 1e-9);
        assertEquals("direction does not matter", 45.0, Calibration.ticksPerInch(-1800, 40), 1e-9);
        assertTrue("no answer from no movement", Double.isNaN(Calibration.ticksPerInch(1800, 0)));
    }

    @Test
    public void theRadiusIsWheelTravelPerRadian() {
        // Two full turns, a wheel 8 inches out, travels 8 * 4pi inches.
        assertEquals(8.0, Calibration.turnRadiusInches(8 * 4 * Math.PI, 4 * Math.PI), 1e-9);
        assertTrue(Double.isNaN(Calibration.turnRadiusInches(100, 0)));
    }

    @Test
    public void headingAddsUpAcrossTheWrap() {
        // Just past pi, which the pose reports as just past -pi.
        double before = Math.PI - 0.01;
        double after = -Math.PI + 0.01;
        assertEquals(0.02, Calibration.unwrap(before, after), 1e-9);
        assertEquals(-0.02, Calibration.unwrap(after, before), 1e-9);
    }

    @Test
    public void aWholeSpinAddsUpToTwoPi() {
        double total = 0;
        double previous = 0;
        for (int i = 1; i <= 360; i++) {
            double heading = Math.atan2(Math.sin(Math.toRadians(i)), Math.cos(Math.toRadians(i)));
            total += Calibration.unwrap(previous, heading);
            previous = heading;
        }
        assertEquals(2 * Math.PI, total, 1e-6);
    }

    @Test
    public void ticksPerInchFromGoBildasParts() {
        // goBILDA's 312 RPM motor counts 537.7 for each turn of its output
        // shaft, which turns the 104 mm wheel once through two 24-tooth gears.
        assertEquals(41.80, Calibration.ticksPerInchFromTheParts(537.7, 104), 0.01);
    }

    @Test
    public void theTurnRadiusIsHalfTheLengthPlusHalfTheWidth() {
        assertEquals("A + B, not the hypotenuse, 9.55",
                13.5, Calibration.turnRadiusFromTheWheels(6.5, 7.0), 1e-9);
    }
}
