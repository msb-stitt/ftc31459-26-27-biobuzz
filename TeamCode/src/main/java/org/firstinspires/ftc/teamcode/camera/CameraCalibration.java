package org.firstinspires.ftc.teamcode.camera;

import static com.pedropathing.api.Paths.line;
import static com.pedropathing.ivy.commands.Commands.infinite;
import static com.pedropathing.ivy.commands.Commands.instant;
import static com.pedropathing.ivy.commands.Commands.lazy;
import static com.pedropathing.ivy.commands.Commands.waitUntil;
import static com.pedropathing.ivy.groups.Groups.deadline;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;
import static com.pedropathing.ivy.pedro.PedroCommands.hold;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.OpModeStorage;
import org.firstinspires.ftc.teamcode.base.AutoRoutine;
import org.firstinspires.ftc.teamcode.base.CorbelsAuto;
import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.LongSupplier;

/**
 * Finds which way the camera looks: its heading, pitch and roll, with their
 * σ, from where it sees red's upright AprilTags, IDs 34 to 37.
 *
 * <p>Red only, with the HIVES in the pre-match state. The robot starts with
 * its intake against the audience wall at x 60 in, facing it, drives straight
 * out to the first row, and then stops on a grid of x 46 in, 59 in and 72 in
 * by y 14.76 in, 21 in and 29 in. At each stop it turns so the camera, at its
 * constant heading, looks at the cluster's middle, and then by -10°, 0° or
 * +10° in turn, so the tags land across the picture. It holds 0.5 s to
 * settle, then 2.0 s taking frames. A tag with fewer than 10 frames at a stop
 * is left out of that stop.
 *
 * <p>The nine stops take longer than the Driver Station's 30 s autonomous
 * timer, so the driver turns that timer off before running it.
 *
 * <p>The camera's position is held at its constants and its angles are
 * solved by {@link CameraSolve}. Under {@code camera/} the flight log gets
 * each frame's tags and the robot's pose, each stop's residual, its own
 * angles and its distance, and at the end the angles and their σ, which the
 * Driver Station shows.
 */
@Autonomous(name = "Camera Calibration", group = "1 Experiments")
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class CameraCalibration extends CorbelsAuto {

    /** The camera, from the hardware. Tests and the simulator hand in their own. */
    static Function<HardwareMap, Camera> cameras = Camera::from;
    /** The clock the waits read, in nanoseconds. Tests hand in the simulator's. */
    static LongSupplier clock = System::nanoTime;

    static final double SETTLE_SECONDS = 0.5;
    static final double LOOK_SECONDS = 2.0;
    static final int FEWEST_FRAMES = 10;
    static final double[] COLUMNS_X_IN = {46, 59, 72};
    static final double[] ROWS_Y_IN = {14.76, 21, 29};
    static final double[] AIM_OFFSETS_DEG = {-10, 0, 10};
    static final FieldTags.Tag[] TAGS = FieldTags.RED_UPRIGHT;

    private static final PoseFactory POSES = PoseFactory.degrees();

    CorbelsMecanum drivetrain;
    Camera camera;
    /** The stops seen so far, each with the tags it saw enough of. */
    final List<CameraSolve.Stop> stops = new ArrayList<>();
    /** The stop being looked from, or -1 while driving. */
    int stop = -1;
    final Map<Integer, double[]> sums = new HashMap<>();
    final Map<Integer, Integer> frames = new HashMap<>();
    final double[] robotSums = new double[4];
    int robotFrames;
    CameraSolve.Result result;
    boolean finished;

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init() {
        initBefore();
        // Live in AdvantageScope from INIT, at 192.168.43.1, only when the
        // Live NetworkTables switch OpMode has turned it on. For practice only:
        // FTC rule R704 forbids it at competitions.
        Tracker.serveLive();
        drivetrain = new CorbelsMecanum(hardware);
        initAfter(drivetrain);
        camera = cameras.apply(hardwareMap);
        select(new AutoRoutine(OpModeStorage.defaultStartPose(true), route()));
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void start() {
        startBefore();
        Scheduler.schedule(selectedAuto.command());
        startAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();
        Tracker.publish("camera/stop", stop);
        if (result != null) {
            Tracker.printToDs("Camera heading %.2f° ± %.2f°", result.headingDeg, result.headingSigmaDeg);
            Tracker.printToDs("Camera pitch %.2f° ± %.2f°", result.pitchDeg, result.pitchSigmaDeg);
            Tracker.printToDs("Camera roll %.2f° ± %.2f°", result.rollDeg, result.rollSigmaDeg);
            Tracker.printToDs("From %d stops", stops.size());
        } else if (finished) {
            Tracker.printToDs("No stop saw " + FEWEST_FRAMES + " frames of IDs 34 to 37.");
        } else if (stop >= 0) {
            Tracker.printToDs("Stop %d of %d", stop + 1, stopPoses().size());
        } else {
            Tracker.printToDs("Driving to the next stop");
        }
        Tracker.printPoseSpeedLoopToDs(follower);
        loopAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void stop() {
        if (camera != null) camera.close();
        stopAfter();
    }

    /** The stops, in the order the robot visits them: across each row, back and forth. */
    static List<Pose> stopPoses() {
        double[] aim = clusterMiddle();
        List<Pose> poses = new ArrayList<>();
        int k = 0;
        for (int row = 0; row < ROWS_Y_IN.length; row++) {
            for (int i = 0; i < COLUMNS_X_IN.length; i++) {
                double x = COLUMNS_X_IN[row % 2 == 0 ? i : COLUMNS_X_IN.length - 1 - i];
                double y = ROWS_Y_IN[row];
                double towardTheTags = Math.toDegrees(Math.atan2(aim[1] - y, aim[0] - x));
                double heading = towardTheTags - Constants.cameraHeadingDegrees
                        + AIM_OFFSETS_DEG[k % AIM_OFFSETS_DEG.length];
                poses.add(POSES.of(x, y, heading));
                k++;
            }
        }
        return poses;
    }

    /** The middle of the tags' centres, on the field, inches. */
    static double[] clusterMiddle() {
        double[] middle = new double[2];
        for (FieldTags.Tag tag : TAGS) {
            middle[0] += tag.centre[0] / TAGS.length;
            middle[1] += tag.centre[1] / TAGS.length;
        }
        return middle;
    }

    /**
     * Straight out from the wall to the first row, so the robot clears the
     * wall before it turns; then to each stop, turning on the way, and the
     * settle, the look and the stop's sums at each; then the answer.
     */
    Command route() {
        Pose start = OpModeStorage.defaultStartPose(true);
        Pose offTheWall = POSES.of(start.x(), ROWS_Y_IN[0], Math.toDegrees(start.heading()));
        List<Command> steps = new ArrayList<>();
        steps.add(follow(follower, line(start, offTheWall).constant(start)));
        Pose from = offTheWall;
        List<Pose> poses = stopPoses();
        for (int k = 0; k < poses.size(); k++) {
            int index = k;
            Pose to = poses.get(k);
            steps.add(instant(() -> stop = -1));
            steps.add(follow(follower, line(from, to).linear(from, to)));
            steps.add(deadline(waitSeconds(SETTLE_SECONDS), hold(follower, to)));
            steps.add(instant(() -> beginStop(index)));
            steps.add(deadline(waitSeconds(LOOK_SECONDS), hold(follower, to), infinite(this::look)));
            steps.add(instant(this::endStop));
            from = to;
        }
        steps.add(instant(this::finish));
        steps.add(hold(follower, from));
        return sequential(steps.toArray(new Command[0]));
    }

    /** The frames from here on count for stop {@code index}; what came before is dropped. */
    void beginStop(int index) {
        camera.fresh();
        stop = index;
        sums.clear();
        frames.clear();
        java.util.Arrays.fill(robotSums, 0);
        robotFrames = 0;
    }

    /** Each fresh frame: its tags added to the stop's sums, and logged. */
    void look() {
        List<Camera.Sighting> seen = camera.fresh();
        if (seen.isEmpty()) return;
        Pose robot = follower.pose();
        long[] ids = new long[seen.size()];
        double[] px = new double[2 * seen.size()];
        for (int i = 0; i < seen.size(); i++) {
            Camera.Sighting tag = seen.get(i);
            ids[i] = tag.id;
            px[2 * i] = tag.u;
            px[2 * i + 1] = tag.v;
            if (FieldTags.lookup(TAGS, tag.id) == null) continue;
            double[] d = Camera.LENS.direction(tag.u, tag.v);
            double[] sum = sums.computeIfAbsent(tag.id, id -> new double[3]);
            for (int j = 0; j < 3; j++) sum[j] += d[j];
            frames.merge(tag.id, 1, Integer::sum);
        }
        robotSums[0] += robot.x();
        robotSums[1] += robot.y();
        robotSums[2] += Math.cos(robot.heading());
        robotSums[3] += Math.sin(robot.heading());
        robotFrames++;
        Tracker.flightlog.recordOutput("camera/tag_ids", ids);
        Tracker.flightlog.recordOutput("camera/tag_px", px);
        Tracker.flightlog.recordOutput("camera/frame_ns", seen.get(0).frameNanos);
        Tracker.publish("camera/robot", robot);
    }

    /** The stop's mean directions kept, and the answer so far solved, its residual logged. */
    void endStop() {
        Map<Integer, double[]> directions = new HashMap<>();
        for (Map.Entry<Integer, double[]> sum : sums.entrySet()) {
            if (frames.get(sum.getKey()) < FEWEST_FRAMES) continue;
            double[] s = sum.getValue();
            double n = Math.sqrt(CameraAngles.dot(s, s));
            directions.put(sum.getKey(), new double[]{s[0] / n, s[1] / n, s[2] / n});
        }
        stop = -1;
        if (directions.isEmpty()) return;
        Pose robot = POSES.of(robotSums[0] / robotFrames, robotSums[1] / robotFrames,
                Math.toDegrees(Math.atan2(robotSums[3], robotSums[2])));
        stops.add(new CameraSolve.Stop(robot, directions));
        CameraSolve.Result soFar = CameraSolve.solve(stops, cameraPosition(), TAGS);
        int last = stops.size() - 1;
        Tracker.publish("camera/stop_residual_deg", soFar.stopResidualDeg[last]);
        Tracker.publish("camera/stop_distance_in", soFar.stopDistanceIn[last]);
        Tracker.publish("camera/stop_heading_deg", soFar.stopHeadingDeg[last]);
        Tracker.publish("camera/stop_pitch_deg", soFar.stopPitchDeg[last]);
        Tracker.publish("camera/stop_roll_deg", soFar.stopRollDeg[last]);
    }

    /** The answer from every stop kept, logged and shown. */
    void finish() {
        finished = true;
        if (stops.isEmpty()) return;
        result = CameraSolve.solve(stops, cameraPosition(), TAGS);
        Tracker.publish("camera/heading_deg", result.headingDeg);
        Tracker.publish("camera/pitch_deg", result.pitchDeg);
        Tracker.publish("camera/roll_deg", result.rollDeg);
        Tracker.publish("camera/heading_sigma_deg", result.headingSigmaDeg);
        Tracker.publish("camera/pitch_sigma_deg", result.pitchSigmaDeg);
        Tracker.publish("camera/roll_sigma_deg", result.rollSigmaDeg);
    }

    /** The camera's position from its constants, inches, in Pedro's robot frame. */
    static double[] cameraPosition() {
        return new double[]{Constants.cameraXInches, Constants.cameraYInches,
                Constants.cameraZInches};
    }

    /**
     * Done once {@code seconds} have passed on {@link #clock}, counted from
     * when it starts. Ivy's own waitMs reads the wall clock, which a test's
     * simulated clock does not move.
     */
    Command waitSeconds(double seconds) {
        long waitNanos = (long) (seconds * 1e9);
        return lazy(() -> {
            long doneAt = clock.getAsLong() + waitNanos;
            return waitUntil(() -> clock.getAsLong() >= doneAt);
        });
    }
}
