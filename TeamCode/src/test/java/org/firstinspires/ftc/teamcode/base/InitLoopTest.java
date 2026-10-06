package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import org.firstinspires.ftc.teamcode.example.TeleOpWithPanelsOpMode;
import org.junit.Test;

import java.lang.reflect.Constructor;

/**
 * What a lesson shows between INIT and PLAY, where L115 reads the heading. It
 * binds the real port, 5810, as the robot does, so it fails if a
 * {@code simRun} is running at the same time.
 *
 * <p>Here and not in the lesson tests, which {@code :TeamCode:test} leaves
 * out: this is {@link CorbelsOpMode}'s behaviour, not a lesson's.
 */
public class InitLoopTest {

    @Test
    public void aLessonPublishesTheHeadingBeforePlay() throws Exception {
        CorbelsOpMode opMode = (CorbelsOpMode) lesson("L110FollowerOpMode");
        OpModeHarness h = new OpModeHarness(opMode);
        h.init();
        assertNull("nothing yet, before the first pass",
                Tracker.values().get("Localizer/pinPoint/heading_deg"));
        h.initLoop();
        // A TeleOp starts where the last auto ended, so the heading is the
        // follower's, not a fixed number.
        assertEquals(Math.toDegrees(opMode.follower.pose().heading()),
                (Double) Tracker.values().get("Localizer/pinPoint/heading_deg"), 1e-6);
        h.initLoop();
        assertEquals("a pass between INIT and PLAY is not a loop", 0L, Tracker.loopCount());
        h.start();
        // The SDK sends the Driver Station's lines from a thread of its own, from
        // INIT on; the harness sends them at the end of the first loop.
        h.loops(1, 0);
        String ds = h.driverStation.text();
        assertTrue(ds, h.driverStation.shows("AdvantageScope: connect to 192.168.43.1"));
        assertFalse(ds, h.driverStation.shows("NetworkTables off"));
        h.stop();
    }

    /** A starter from {@code lessons}, which is package-private there. */
    private static OpMode lesson(String name) throws Exception {
        Constructor<?> c = Class.forName("org.firstinspires.ftc.teamcode.lessons." + name)
                .getDeclaredConstructor();
        c.setAccessible(true);
        return (OpMode) c.newInstance();
    }

    @Test
    public void anOpModeOutsideTheLessonsKeepsToPanels() {
        OpModeHarness h = new OpModeHarness(new TeleOpWithPanelsOpMode());
        h.init();
        h.initLoop();
        h.start();
        h.loops(1, 0);
        String ds = h.driverStation.text();
        assertTrue(ds, h.driverStation.shows("Panels: http://192.168.43.1:8001"));
        assertFalse(ds, h.driverStation.shows("AdvantageScope"));
        h.stop();
    }
}
