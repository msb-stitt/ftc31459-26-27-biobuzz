package org.firstinspires.ftc.teamcode.base;

import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import io.github.mikestitt.corbelsflightlog.FlightLog;
import io.github.mikestitt.corbelsflightlog.ftc.FtcFlightLog;
import io.github.mikestitt.corbelsflightlog.nt.Nt4Server;
import io.github.mikestitt.corbelsflightlog.pedro.PedroFlightLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.panels.PanelsLogger;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Where everything the robot has to say goes: a graph in Panels, a line on the
 * Driver Station, a channel in the WPILOG file.
 *
 * <p>Every method is static, so a class that is not an OpMode -- an intake, a
 * flywheel, a localizer -- logs exactly the way a lesson does:
 *
 * <pre>
 * Tracker.publish("flywheel/rpm", rpm);                      // Panels and the file
 * Tracker.flightlog.recordOutput("flywheel/samples", raw);   // the file only
 * Tracker.printToDs("Flywheel ready");                       // the driver's screen
 * </pre>
 *
 * <p><b>Three destinations, three doors.</b> {@link #publish} is for numbers a
 * graph or a log viewer will read. {@link #flightlog} is the same file with all
 * 11 of its {@code recordOutput} types, for an array or a struct that
 * {@code publish} does not cover. {@link #printToDs} is text for the one screen
 * the driver can see during a match, and it takes what {@code println} takes.
 *
 * <p><b>Space on the Driver Station is scarce, so nothing appears there by
 * itself.</b> An OpMode that wants the pose, the speed and the loop calls
 * {@link #printPoseSpeedLoopToDs}.
 *
 * <p><b>Never call {@code telemetry} yourself.</b> FTC's {@code Telemetry}
 * clears its buffer every time it is flushed, and this class flushes once at the
 * end of each loop; a second flush inside the loop drops whatever the first one
 * sent. A build check enforces it.
 *
 * <p><b>Panels can be switched off</b> with {@link #enablePanels}, for a match
 * where Wi-Fi is not wanted. Publishing still reaches the file and
 * {@code printToDs} still reaches the driver.
 *
 * <p>The fields below are never null. Before {@link #begin} they are objects
 * that record nothing, so a class that logs during construction is safe.
 */
public final class Tracker {

    /** The run's WPILOG file. Records nothing until {@link #begin}. */
    public static FlightLog flightlog = FlightLog.disabled("not started");

    /** Pedro's pose, mode, path and velocity, into {@link #flightlog}. */
    public static PedroFlightLog pedro = new PedroFlightLog(flightlog, "Robot");

    /** Panels' graphs, text and field drawing. Null until {@link #startLogging}. */
    public static PanelsLogger logger;

    private static TelemetryManager panels;
    private static Telemetry ds;
    private static boolean panelsEnabled = true;

    private static final Map<String, Object> values = new LinkedHashMap<>();

    private static long loopCount;
    private static long lastLoopNs;
    private static long startNs;
    private static double loopPeriodMs;
    private static double maxLoopPeriodMs;

    private Tracker() {
    }

    // ------------------------------------------------------------ lifecycle

    /**
     * Opens the file and forgets the last run. Called from
     * {@code CorbelsOpMode.initBefore()}, before anything is logged.
     *
     * <p>The Robot Controller process outlives an OpMode, so everything static
     * here is cleared rather than assumed empty.
     */
    public static void begin(OpMode opMode) {
        values.clear();
        loopCount = 0;
        lastLoopNs = 0L;
        startNs = System.nanoTime();
        loopPeriodMs = 0.0;
        maxLoopPeriodMs = 0.0;
        panels = null;
        logger = null;
        panelsEnabled = true;
        ds = opMode == null ? null : opMode.telemetry;
        flightlog = FtcFlightLog.open(opMode);
        pedro = new PedroFlightLog(flightlog, "Robot");
    }

    /**
     * Sends everything the file records live to AdvantageScope as well, over
     * NetworkTables 4 on port 5810: on the robot, connect AdvantageScope to
     * {@code 192.168.43.1}. {@code initBefore()} calls this for a lesson and
     * a copy in {@code mytry}; any other OpMode calls it itself, after
     * {@code initBefore()}. {@link #begin} switches it off again.
     *
     * <p>For practice only: FTC rule R704 forbids third-party telemetry over
     * Wi-Fi at competitions. Returns false, with the reason on the Driver
     * Station, if the server cannot start.
     */
    public static boolean serveLive() {
        try {
            flightlog.mirrorTo(Nt4Server.shared());
            return true;
        } catch (IOException | RuntimeException e) {
            printToDs("NetworkTables off: " + e.getMessage());
            return false;
        }
    }

    /** Panels and its logger. Called from {@code CorbelsOpMode.startBefore()}. */
    public static void startLogging() {
        panels = PanelsTelemetry.INSTANCE.getTelemetry();
        logger = new PanelsLogger();
        logger.start();
        startNs = System.nanoTime();
        lastLoopNs = startNs;
    }

    /**
     * Counts the loop, times it, records Pedro, closes the file's record for
     * this loop, then flushes Panels and the Driver Station. Called from
     * {@code CorbelsOpMode.loopAfter()}, after everything else.
     */
    public static void endLoop(Follower follower) {
        long now = System.nanoTime();
        if (lastLoopNs != 0L) {
            loopPeriodMs = (now - lastLoopNs) / 1_000_000.0;
        }
        lastLoopNs = now;
        loopCount++;
        if (loopPeriodMs > maxLoopPeriodMs) {
            maxLoopPeriodMs = loopPeriodMs;
        }

        publish("loop/count", loopCount);
        publish("loop/period_ms", loopPeriodMs);
        publish("loop/max_period_ms", maxLoopPeriodMs);
        publish("loop/rate_hz", loopRateHz());
        publish("loop/uptime_s", (now - startNs) / 1_000_000_000.0);

        pedro.record(follower);
        // The file's record boundary: everything published since the last one
        // belongs to this loop, so nothing may be published after it.
        flightlog.endLoop();

        if (panelsEnabled && logger != null) {
            logger.update(follower);
        }
        if (ds != null) {
            ds.update();
        }
    }

    /**
     * Closes the file's record for one pass between INIT and PLAY, without
     * counting it as a loop. Called from {@code CorbelsOpMode.init_loop()}.
     */
    public static void endInitLoop() {
        flightlog.endLoop();
    }

    /** Closes the file. Called from {@code CorbelsOpMode.stopAfter()}. */
    public static void close() {
        flightlog.close();
    }

    // ------------------------------------------------------------ publishing

    /** A number, to Panels and to the file. */
    public static void publish(String key, double value) {
        if (panelsEnabled && panels != null) panels.addData(key, value);
        values.put(key, value);
        flightlog.recordOutput(key, value);
    }

    public static void publish(String key, boolean value) {
        if (panelsEnabled && panels != null) panels.addData(key, value);
        values.put(key, value);
        flightlog.recordOutput(key, value);
    }

    public static void publish(String key, String value) {
        if (panelsEnabled && panels != null) panels.addData(key, value);
        values.put(key, value);
        flightlog.recordOutput(key, value);
    }

    /**
     * A pose, as a struct AdvantageScope can draw on the field and as three
     * graphable numbers: {@code key/x_in}, {@code key/y_in},
     * {@code key/heading_deg}.
     */
    public static void publish(String key, Pose pose) {
        if (pose == null) return;
        PedroFlightLog.recordOutput(flightlog, key, pose);
        publish(key + "/x_in", pose.x());
        publish(key + "/y_in", pose.y());
        publish(key + "/heading_deg", Math.toDegrees(pose.heading()));
    }

    /** The last value published under each key, in the order the keys first appeared. */
    public static Map<String, Object> values() {
        return values;
    }

    // ------------------------------------------------------------ the driver's screen

    /** One line of text. */
    public static void printToDs(String line) {
        if (ds != null) ds.addLine(line);
    }

    /**
     * One line, formatted. Chosen over {@link #printToDs(String)} only when
     * arguments follow, so {@code printToDs("Remaining: %.1f in", inches)} and
     * {@code printToDs("Ready")} both do what they look like.
     */
    public static void printToDs(String format, Object... args) {
        printToDs(String.format(format, args));
    }

    public static void printToDs(double value) {
        printToDs(String.valueOf(value));
    }

    public static void printToDs(long value) {
        printToDs(String.valueOf(value));
    }

    public static void printToDs(boolean value) {
        printToDs(String.valueOf(value));
    }

    public static void printToDs(Object value) {
        printToDs(String.valueOf(value));
    }

    /** A blank line. */
    public static void printToDs() {
        printToDs("");
    }

    /**
     * Where the robot is, how fast it is going, and how the loop is doing, in
     * three lines. For an OpMode that wants them; nothing prints them by
     * default.
     */
    public static void printPoseSpeedLoopToDs(Follower follower) {
        if (follower != null) {
            Pose pose = follower.pose();
            printToDs("Pose  x %.1f  y %.1f  h %.0f deg",
                    pose.x(), pose.y(), Math.toDegrees(pose.heading()));
            printToDs("Speed  %.1f in/s",
                    Math.hypot(follower.velocity().vx, follower.velocity().vy));
        }
        printToDs("Loop  #%d  %.1f ms (max %.1f)", loopCount, loopPeriodMs, maxLoopPeriodMs);
    }

    // ------------------------------------------------------------ the loop and Panels

    /** Loops that have finished. 0 inside the first loop, 1 once it ends. */
    public static long loopCount() {
        return loopCount;
    }

    /** How long the last loop took. */
    public static double loopPeriodMs() {
        return loopPeriodMs;
    }

    /** The longest loop so far. */
    public static double maxLoopPeriodMs() {
        return maxLoopPeriodMs;
    }

    /** Loops per second, from the last loop's period. 0 before the first one ends. */
    public static double loopRateHz() {
        return loopPeriodMs > 0.0 ? 1000.0 / loopPeriodMs : 0.0;
    }

    /**
     * Whether anything goes to Panels. Off, publishing still reaches the file
     * and {@link #printToDs} still reaches the driver. Call it after
     * {@code initBefore()}; {@link #begin} switches Panels back on.
     */
    public static void enablePanels(boolean enabled) {
        panelsEnabled = enabled;
    }

    public static boolean panelsEnabled() {
        return panelsEnabled;
    }
}
