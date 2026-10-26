package org.firstinspires.ftc.teamcode.panels;

import com.bylazar.field.FieldManager;
import com.bylazar.field.FieldPresets;
import com.bylazar.field.PanelsField;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.follower.Follower;


import java.util.ArrayDeque;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * Shared Panels logging for the Corbels OpModes.
 *
 * <p><b>How Panels 1.0 graphs.</b> There is no graph() method. The Graph panel
 * parses telemetry text: any line containing {@code name: number} becomes a
 * plottable series. {@code addData(k, v)} writes exactly that format, so every
 * addData value appears in the Panels text panel AND the graph picker. Names
 * may contain anything except a colon.
 *
 * <p>A series appears in the picker only after it has been sent once, in the
 * order it was first sent. The picker is a flat list; the {@code loop/},
 * {@code pose/} and {@code vel/} prefixes are for readability only.
 *
 * <p><b>Sampling.</b> The robot sends to Panels every 75 ms by default and
 * discards lines from the loops in between, so graphs are sampled, not
 * per-loop. With a ~5 ms loop, roughly 1 loop in 15 reaches the graph.
 * {@code loop/max_ms} exists to catch spikes that sampling misses.
 * {@code panels.setUpdateInterval(ms)} lowers the interval at the cost of
 * more Wi-Fi traffic.
 *
 * <p><b>Panels only.</b> Everything here goes to Panels and nowhere else.
 * ({@code TelemetryManager.update(telemetry)} would copy every Panels line to
 * the Driver Station, which crowds its screen.) The Driver Station belongs to
 * {@link Tracker#printToDs}, which also owns the loop count and the loop
 * timing.
 *
 * <p><b>Field view.</b> Draws on the Panels Field panel, in this order:
 * <ol>
 *   <li><b>Path</b> (orange) -- the segment being followed, if any.</li>
 *   <li><b>Trail</b> (grey) -- where the robot has been since start().</li>
 *   <li><b>Aim point</b> (green square) -- {@code closestPose()}: the nearest
 *       point on the path, at the heading the path wants there. The gap to the
 *       robot is the tracking error. Only drawn while following or holding.</li>
 *   <li><b>Robot</b> (blue square, 18 x 18 in) with a heading line.</li>
 * </ol>
 * This is the Pedro 3 equivalent of Pedro 2's {@code Drawing.drawDebug()},
 * which no longer exists. The PEDRO_PATHING preset maps Pedro coordinates
 * (0..144 in, origin in a field corner) onto the field image, so poses go in
 * unconverted.
 */
public class PanelsLogger {

    /** Panels' telemetry handle. Panels itself is already running inside the
     *  Robot Controller app; there is nothing to start. PanelsTelemetry is a
     *  Kotlin object, hence INSTANCE from Java. */
    private final TelemetryManager panels = PanelsTelemetry.INSTANCE.getTelemetry();

    /** Panels' field-drawing handle. Also a Kotlin object, hence INSTANCE. */
    private final FieldManager field = PanelsField.INSTANCE.getField();

    private static final double ROBOT_SIZE_IN = 18.0;
    private static final String ROBOT_COLOR = "#3F51B5";  // blue
    private static final String AIM_COLOR = "#4CAF50";    // green
    private static final String PATH_COLOR = "#FF9800";   // orange
    private static final String TRAIL_COLOR = "#9E9E9E";  // grey

    /** Points sampled along the current path segment when drawing it. */
    private static final int PATH_SAMPLES = 20;
    /** A trail point is added once the robot has moved this far. */
    private static final double TRAIL_SPACING_IN = 1.0;
    /** Oldest trail points are dropped past this many. */
    private static final int TRAIL_MAX_POINTS = 150;

    /** Panels sends field frames every 100 ms; building one every ~5 ms loop
     *  would discard 19 of 20. Build at most this often instead. */
    private static final long FIELD_DRAW_INTERVAL_NS = 50_000_000L;
    private long lastFieldDrawNs;

    private final ArrayDeque<double[]> trail = new ArrayDeque<>();

    /** Latest line Pedro pushed through its own logger, if wired up. */
    private String pedroLog = "";

    /** Call once from start(), so init-loop time is not counted. */
    public void start() {
        lastFieldDrawNs = 0L;
        trail.clear();
    }

    /**
     * Sink for Pedro's own follower log. {@code CorbelsOpMode} wires it as
     * {@code follower.withLogger(followerLog -> Tracker.logger.pedro(followerLog.toString()))}
     */
    public void pedro(String line) {
        pedroLog = line;
    }

    /** Call once per loop, after follower.update(). */
    public void update(Follower follower) {
        long now = System.nanoTime();

        if (follower != null) {
            // ---- robot position ----
            // Pedro 3 uses follower.pose(), NOT getPose(), and the accessors are
            // x()/y()/heading() -- methods, not fields. Heading is in radians.
            double x = follower.pose().x();
            double y = follower.pose().y();
            double headingDeg = Math.toDegrees(follower.pose().heading());

            panels.addData("pose/x_in", x);
            panels.addData("pose/y_in", y);
            panels.addData("pose/heading_deg", headingDeg);

            // ---- robot velocity ----
            // Three flavors, all useful for different questions:
            //   velocity()           world frame -- where on the field is it going
            //   twist()              body frame  -- forward / strafe from the robot's view
            //   tangentialVelocity() scalar      -- speed along the current path
            // Velocity and Twist expose PUBLIC FIELDS vx/vy/omega, not getters.
            double vx = follower.velocity().vx;
            double vy = follower.velocity().vy;
            double omega = follower.velocity().omega;
            double forwardXInPerS = follower.twist().vx;
            double strafeYInPerS = follower.twist().vy;
            // tangentialVelocity() is velocity . closestTangent(), and Pedro only
            // computes closestTangent while following or holding. In MANUAL it is
            // null until a path has run, and tangentialVelocity() throws a
            // NullPointerException -- which crashed TeleOp + Panels on its first
            // loop. NaN when undefined: Panels shows it as text, the graph skips it.
            double tangential = hasAim(follower) && follower.closestTangent() != null
                    ? follower.tangentialVelocity()
                    : Double.NaN;
            double speed = Math.hypot(vx, vy);

            panels.addData("vel/vx_ips", vx);
            panels.addData("vel/vy_ips", vy);
            panels.addData("vel/speed_ips", speed);
            panels.addData("vel/omega_radps", omega);
            panels.addData("vel/forward_ips", forwardXInPerS);
            panels.addData("vel/strafe_ips", strafeYInPerS);
            panels.addData("vel/tangential_ips", tangential);

            drawField(follower, x, y, follower.pose().heading(), now);
        }

        if (!pedroLog.isEmpty()) {
            // Text only -- unless FollowerLog.toString() contains "name: number"
            // pairs, in which case those become extra graph series as well.
            panels.debug("pedro  " + pedroLog);
        }

        panels.update();   // every series above, throttled to 75 ms
    }

    /** True while Pedro is following a path or holding a pose -- the only
     *  modes in which closestPose() and closestTangent() are current. In
     *  MANUAL (teleop) and IDLE they are null or left over from earlier. */
    private static boolean hasAim(Follower follower) {
        Follower.Mode mode = follower.mode();
        return mode == Follower.Mode.FOLLOW || mode == Follower.Mode.HOLD;
    }

    private void drawField(Follower follower, double x, double y, double headingRad, long now) {
        // Trail is recorded every loop, so it stays accurate even though
        // drawing below is rate limited.
        double[] last = trail.peekLast();
        if (last == null || Math.hypot(x - last[0], y - last[1]) >= TRAIL_SPACING_IN) {
            trail.addLast(new double[]{x, y});
            if (trail.size() > TRAIL_MAX_POINTS) {
                trail.removeFirst();
            }
        }

        if (lastFieldDrawNs != 0L && now - lastFieldDrawNs < FIELD_DRAW_INTERVAL_NS) {
            return;
        }
        lastFieldDrawNs = now;

        // The preset survives between frames (update() only clears drawn
        // items), but setting it each frame is cheap and self-contained.
        field.setOffsets(FieldPresets.INSTANCE.getPEDRO_PATHING());

        // 1. Path. poseAt() samples the CURRENT SEGMENT, so check that rather
        //    than currentPath(): on the last update of a path the segment queue
        //    empties one loop before the path clears, and poseAt() then throws
        //    on a null segment. With no path at all it quietly returns the
        //    robot's own pose, which would draw a fake one-point path.
        if (follower.currentSegment() != null) {
            field.setStyle(PanelsField.INSTANCE.getTRANSPARENT(), PATH_COLOR, 0.5);
            double px = follower.poseAt(0.0).x();
            double py = follower.poseAt(0.0).y();
            for (int i = 1; i <= PATH_SAMPLES; i++) {
                double t = (double) i / PATH_SAMPLES;
                double nx = follower.poseAt(t).x();
                double ny = follower.poseAt(t).y();
                segment(px, py, nx, ny);
                px = nx;
                py = ny;
            }
        }

        // 2. Trail.
        if (trail.size() > 1) {
            field.setStyle(PanelsField.INSTANCE.getTRANSPARENT(), TRAIL_COLOR, 0.5);
            double[] prev = null;
            for (double[] p : trail) {
                if (prev != null) {
                    segment(prev[0], prev[1], p[0], p[1]);
                }
                prev = p;
            }
        }

        // 3. Aim point: same square as the robot, in green.
        if (hasAim(follower) && follower.closestPose() != null) {
            drawSquare(follower.closestPose().x(), follower.closestPose().y(),
                    follower.closestPose().heading(), AIM_COLOR, 0.5);
        }

        // 4. Robot, drawn last so it sits on top.
        drawSquare(x, y, headingRad, ROBOT_COLOR, 0.75);

        // Sends the frame (if 100 ms have passed) and clears it either way.
        field.update();
    }

    /**
     * An 18 x 18 in square at a Pedro pose, turned to its heading, with a line
     * from the center to the middle of the front edge. Panels' rect() has no
     * rotation, so the square is drawn as four edges between rotated corners.
     */
    private void drawSquare(double x, double y, double headingRad, String color, double width) {
        field.setStyle(PanelsField.INSTANCE.getTRANSPARENT(), color, width);
        double half = ROBOT_SIZE_IN / 2.0;
        double c = Math.cos(headingRad);
        double s = Math.sin(headingRad);
        // Corners in the robot's own frame (+x forward, +y left), in order
        // around the square: front-left, front-right, back-right, back-left.
        double[][] corners = {{half, half}, {half, -half}, {-half, -half}, {-half, half}};
        double[] fx = new double[4];
        double[] fy = new double[4];
        for (int i = 0; i < 4; i++) {
            fx[i] = x + corners[i][0] * c - corners[i][1] * s;
            fy[i] = y + corners[i][0] * s + corners[i][1] * c;
        }
        for (int i = 0; i < 4; i++) {
            int next = (i + 1) % 4;
            segment(fx[i], fy[i], fx[next], fy[next]);
        }
        segment(x, y, x + half * c, y + half * s);  // heading
    }

    /** line() draws from the cursor and does NOT move it, so every segment
     *  needs its own moveCursor(); chaining line() calls would draw spokes. */
    private void segment(double x1, double y1, double x2, double y2) {
        field.moveCursor(x1, y1);
        field.line(x2, y2);
    }
}
