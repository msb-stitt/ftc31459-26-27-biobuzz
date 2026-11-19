package org.firstinspires.ftc.teamcode;

import static org.junit.Assert.assertEquals;

import com.pedropathing.math.Pose;

import org.junit.Test;

/** The default start pose for the OpModes outside the lessons, on each alliance. */
public class OpModeStorageTest {

    private static final double EPS = 1e-9;

    @Test
    public void theDefaultStartIsAgainstTheAlliancesWallFacingIt() {
        Pose red = OpModeStorage.defaultStartPose(true);
        assertEquals("red: x", 60.0, red.x(), EPS);
        assertEquals("red: against the near wall", OpModeStorage.botStartYIn, red.y(), EPS);
        assertEquals("red: facing -y, the near wall", 0.0,
                turnTo(-Math.PI / 2, red.heading()), EPS);

        Pose blue = OpModeStorage.defaultStartPose(false);
        assertEquals("blue: red's turned about (72, 72)", 84.0, blue.x(), EPS);
        assertEquals("blue: against the far wall", 144.0 - OpModeStorage.botStartYIn,
                blue.y(), EPS);
        assertEquals("blue: facing +y, the far wall", 0.0,
                turnTo(Math.PI / 2, blue.heading()), EPS);
    }

    @Test
    public void a37523StartsWithItsIntakeAgainstTheWall() {
        // The frame's 1.5 in, and the intake's end 9.76 in ahead of the wheels' centre.
        assertEquals("red: y", 11.26, OpModeStorage.defaultStartPose(true).y(), EPS);
        assertEquals("blue: y", 144.0 - 11.26, OpModeStorage.defaultStartPose(false).y(), EPS);
    }

    /** The turn from one heading to another, in radians, from -pi to pi. */
    private static double turnTo(double from, double to) {
        return Math.atan2(Math.sin(to - from), Math.cos(to - from));
    }
}
