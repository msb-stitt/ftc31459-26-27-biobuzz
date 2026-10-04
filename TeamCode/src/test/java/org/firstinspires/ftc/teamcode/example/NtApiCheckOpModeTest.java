package org.firstinspires.ftc.teamcode.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import edu.wpi.first.util.datalog.DataLogReader;
import edu.wpi.first.util.datalog.DataLogRecord;

import org.firstinspires.ftc.teamcode.base.OpModeHarness;
import org.firstinspires.ftc.teamcode.example.NtApiCheckOpMode.Entry;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * The NetworkTables API check's OpMode, run in the simulator: it moves nothing,
 * turns the server on, and records every value it promises. What a client reads
 * back is {@code NtApiCheck}'s to check, in a JVM of its own. Binds the real
 * port, 5810, so it fails if a {@code simRun} is running at the same time.
 */
public class NtApiCheckOpModeTest {

    @Test
    public void itMovesNothingServesAndRecordsEveryValue() throws IOException {
        OpModeHarness h = new OpModeHarness(new NtApiCheckOpMode());
        File folder = java.nio.file.Files.createTempDirectory("ntapicheck").toFile();
        h.logTo(folder);
        h.init();
        h.start();
        h.loops(10, 0);
        // Its own server, not one a stray adb forward or simRun holds on 5810.
        assertTrue(h.driverStation.text(), h.driverStation.shows("NetworkTables on; step 10"));

        for (Map.Entry<String, OpModeHarness.FakeMotor> m : h.motors.entrySet()) {
            assertEquals(m.getKey() + " power", 0.0, m.getValue().power, 0.0);
        }
        try (Socket s = new Socket("127.0.0.1", 5810)) {
            s.getOutputStream().write("GET / HTTP/1.1\r\nHost: x\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            String status = new BufferedReader(new InputStreamReader(s.getInputStream(),
                    StandardCharsets.UTF_8)).readLine();
            assertEquals("HTTP/1.1 200 OK", status);
        }
        h.stop();

        Map<String, String> types = new HashMap<>();
        for (File log : h.logs()) {
            for (DataLogRecord r : new DataLogReader(log.getPath())) {
                if (r.isStart()) {
                    DataLogRecord.StartRecordData d = r.getStartData();
                    types.put(d.name, d.type.equals("int64") ? "int"
                            : d.type.equals("int64[]") ? "int[]" : d.type);
                }
            }
        }
        for (Entry e : NtApiCheckOpMode.FIXED) assertEquals(e.name, e.type, types.get(e.name));
        assertEquals("string", types.get(NtApiCheckOpMode.BEFORE_SERVE.name));
        assertEquals("int", types.get(NtApiCheckOpMode.STEP));
        assertTrue(types.toString(), types.containsKey(NtApiCheckOpMode.POSE));
    }
}
