package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** The band and each shape, as L065's lesson has a student write them. */
public class StickShaperTest {

    private static final double EPS = 1e-9;

    @Test
    public void aNewShaperSquaresOutsideItsBand() {
        StickShaper shaper = new StickShaper(0.05);
        assertEquals(0.25, shaper.shaped(0.5), EPS);
        assertEquals("squaring keeps the sign", -0.25, shaper.shaped(-0.5), EPS);
        assertEquals("inside the band is zero", 0.0, shaper.shaped(-0.04), EPS);
        assertEquals("a full stick is full speed", 1.0, shaper.shaped(1.0), EPS);
    }

    @Test
    public void eachShapeShapesItsOwnWay() {
        StickShaper shaper = new StickShaper(0.05);
        shaper.setShape(StickShaper.Shape.LINEAR);
        assertEquals(0.5, shaper.shaped(0.5), EPS);
        shaper.setShape(StickShaper.Shape.CUBED);
        assertEquals(0.125, shaper.shaped(0.5), EPS);
        assertEquals("cubing keeps the sign", -0.125, shaper.shaped(-0.5), EPS);
    }

    @Test
    public void theBandCanBeChanged() {
        StickShaper shaper = new StickShaper(0.05);
        assertEquals(0.0064, shaper.shaped(0.08), EPS);
        shaper.setBand(0.1);
        assertEquals(0.0, shaper.shaped(0.08), EPS);
    }
}
