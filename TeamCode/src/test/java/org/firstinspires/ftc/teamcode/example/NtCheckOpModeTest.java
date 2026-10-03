package org.firstinspires.ftc.teamcode.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.teamcode.base.OpModeHarness;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * The robot's NetworkTables check, run in the simulator. It binds the real
 * port, 5810, as it does on the robot, so it fails if a {@code simRun} is
 * running at the same time.
 */
public class NtCheckOpModeTest {

    @Test
    public void itTurnsTheServerOnAndShowsTheDriverWhatAdvantageScopeShouldRead() throws IOException {
        OpModeHarness h = new OpModeHarness(new NtCheckOpMode());
        h.init();
        h.start();
        h.gamepad1.left_stick_y = -0.5f;
        h.gamepad1.a = true;
        h.loops(5, 0);

        String ds = h.driverStation.text();
        assertTrue(ds, h.driverStation.shows("NetworkTables on, port 5810"));
        assertTrue(ds, h.driverStation.shows("leftY 0.50   a true"));
        assertEquals("A held", Tracker.values().get("ntcheck/word"));

        try (Socket s = new Socket("127.0.0.1", 5810)) {
            s.getOutputStream().write("GET / HTTP/1.1\r\nHost: x\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            String status = new BufferedReader(new InputStreamReader(s.getInputStream(),
                    StandardCharsets.UTF_8)).readLine();
            assertEquals("the server answers AdvantageScope's first request", "HTTP/1.1 200 OK", status);
        }
        h.stop();
    }
}
