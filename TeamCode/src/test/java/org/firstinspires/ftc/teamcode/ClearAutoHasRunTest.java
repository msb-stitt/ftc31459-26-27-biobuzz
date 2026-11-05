package org.firstinspires.ftc.teamcode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.base.OpModeHarness;
import org.junit.Before;
import org.junit.Test;

/** Clear Auto Has Run puts back what an auto leaves, as restarting the app does. */
public class ClearAutoHasRunTest {

    private static final PoseFactory POSES = PoseFactory.degrees();

    @Before
    public void startAsTheAppDoes() {
        OpModeStorage.clear();
    }

    @Test
    public void startClearsWhatAnAutoLeftAndInitSaysWhetherOneHadRun() {
        OpModeStorage.autonomousEndPose = POSES.of(30, 40, 0);
        OpModeStorage.autoHasRun = true;
        OpModeStorage.isRed = false;

        OpModeHarness first = new OpModeHarness(new ClearAutoHasRun());
        first.init();
        first.initLoop();
        assertTrue(first.driverStation.text(),
                first.driverStation.shows("An auto has run. Press Start to clear it."));
        first.start();
        first.loop();
        assertTrue(first.driverStation.text(),
                first.driverStation.shows("Cleared: no auto has run. Press Stop."));
        first.stop();
        assertFalse("no auto has run", OpModeStorage.autoHasRun);
        assertTrue("red, as the app starts", OpModeStorage.isRed);
        Pose appStart = POSES.of(72, OpModeStorage.botStartYIn, 90);
        Pose endPose = OpModeStorage.autonomousEndPose;
        assertEquals("the lessons' default: x", appStart.x(), endPose.x(), 1e-9);
        assertEquals("y", appStart.y(), endPose.y(), 1e-9);
        assertEquals("heading", appStart.heading(), endPose.heading(), 1e-9);

        OpModeHarness second = new OpModeHarness(new ClearAutoHasRun());
        second.init();
        second.initLoop();
        assertTrue(second.driverStation.text(),
                second.driverStation.shows("An auto has not run. Press Start to clear it."));
        second.stop();
    }
}
