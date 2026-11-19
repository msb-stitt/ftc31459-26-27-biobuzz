package org.firstinspires.ftc.teamcode.camera;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.base.OpModeHarness;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.junit.After;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Camera Calibration, run in the simulator against a camera turned from its
 * constants by a known amount.
 */
public class CameraCalibrationTest {

    @After
    public void putTheCameraBack() {
        CameraCalibration.cameras = Camera::from;
        CameraCalibration.clock = System::nanoTime;
    }

    @Test
    public void itFindsTheCamerasAnglesWithinAFifthOfADegreeFromStopsThatEachSeeEveryTag() {
        OpModeHarness h = new OpModeHarness(new CameraCalibration());
        CameraCalibration auto = (CameraCalibration) h.opMode();
        // Pitched 52°, where every stop's tags sit inside the picture: below about 48° the
        // far row's sit above it.
        double[] truth = {177.0, 52.0, 1.0};
        double[] at = {Constants.cameraXInches, Constants.cameraYInches, Constants.cameraZInches};
        CameraCalibration.clock = h.clock::nanos;
        CameraCalibration.cameras = map -> SimCamera.on(h.robot.follower::pose, h.clock::nanos,
                at, truth);
        h.init();
        h.initLoop();
        h.start();
        while (!auto.finished && h.clock.nanos() < 120_000_000_000L) {
            h.loop(10);
        }
        h.loop(10);   // the answer reaches the Driver Station on the loop after it is solved
        h.stop();

        assertTrue("finished within 120 s", auto.finished);
        assertEquals("every stop kept", CameraCalibration.stopPoses().size(), auto.stops.size());
        Set<Integer> all = new HashSet<>(Arrays.asList(34, 35, 36, 37));
        for (CameraSolve.Stop stop : auto.stops) {
            assertEquals("each stop sees IDs 34 to 37", all, stop.directions.keySet());
        }
        CameraSolve.Result r = auto.result;
        assertEquals("heading", 0, CameraAngles.wrapDegrees(r.headingDeg - truth[0]), 0.2);
        assertEquals("pitch", truth[1], r.pitchDeg, 0.2);
        assertEquals("roll", truth[2], r.rollDeg, 0.2);
        assertEquals("published", r.headingDeg, (Double) Tracker.values().get("camera/heading_deg"),
                1e-9);
        assertTrue(h.driverStation.text(), h.driverStation.shows("Camera heading"));
    }
}
