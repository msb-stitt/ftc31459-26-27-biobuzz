package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.github.mikestitt.corbelsflightlog.nt.Nt4Server;
import org.firstinspires.ftc.teamcode.example.TeleOpWithPanelsOpMode;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The robot code reads {@link RobotClock}, which a sim run sets to its
 * simulated clock, so a run faster than real time, or held still, reads the
 * simulation's time.
 */
public class RobotClockTest {

    @Test
    public void trackerTimesLoopsByTheSimulatedClock() {
        OpModeHarness h = new OpModeHarness(new TeleOpWithPanelsOpMode());
        h.step_ms = 37;
        h.init();
        h.start();
        h.loops(3, 0);
        assertEquals("37 ms of simulated time, however long the pass took", 37.0, Tracker.loopPeriodMs(), 0.0);
        h.loop(0);
        assertEquals("a clock held still reads no time", 0.0, Tracker.loopPeriodMs(), 0.0);
        h.stop();
    }

    @Test
    public void aServerStartedInASimRunStampsSimulatedTime() throws IOException {
        OpModeHarness h = new OpModeHarness(new TeleOpWithPanelsOpMode());
        Nt4Server server = Nt4Server.start(0);
        try {
            long before = server.nowMicros();
            h.clock.advance(37);
            assertEquals("37 ms of simulated time", 37_000L, server.nowMicros() - before);
        } finally {
            server.close();
            Nt4Server.useWallClock();
        }
    }

    @Test
    public void pedroTimesItsTimersByTheSimulatedClock() {
        OpModeHarness h = new OpModeHarness(new TeleOpWithPanelsOpMode());
        com.pedropathing.utils.Timer timer = new com.pedropathing.utils.Timer();
        h.clock.advance(37);
        assertEquals("37 ms of simulated time", 37.0, timer.milliseconds(), 0.0);
    }

    @Test
    public void theWallClockIsTheRobotsOnTheRobot() {
        RobotClock.useWallClock();
        long before = System.nanoTime();
        long now = RobotClock.nanoTime();
        assertTrue(now >= before && now - before < 1_000_000_000L);
    }

    /**
     * Where robot code still reads the wall clock, by file and count. A run ID
     * is a date, as a file name is, and VoltageResponseOpMode times what a call
     * costs; everything else reads {@link RobotClock}.
     */
    private static final Map<String, Long> WALL_CLOCK_ALLOWED = new TreeMap<>(Map.of(
            "base/RobotClock.java", 1L,
            "example/NtApiCheckOpMode.java", 1L,
            "logcheck/LogCheck9NtApiOpMode.java", 1L,
            "sysid/VoltageResponseOpMode.java", 2L));

    @Test
    public void robotCodeReadsTheWallClockOnlyWhereAllowed() throws IOException {
        Path root = Paths.get("src/main/java/org/firstinspires/ftc/teamcode");
        Map<String, Long> found = new TreeMap<>();
        try (Stream<Path> files = Files.walk(root)) {
            for (Path f : files.filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList())) {
                List<String> lines = Files.readAllLines(f);
                long n = lines.stream()
                        .filter(l -> l.contains("System.nanoTime") || l.contains("System.currentTimeMillis"))
                        .count();
                if (n > 0) found.put(root.relativize(f).toString().replace('\\', '/'), n);
            }
        }
        assertEquals(WALL_CLOCK_ALLOWED, found);
    }
}
