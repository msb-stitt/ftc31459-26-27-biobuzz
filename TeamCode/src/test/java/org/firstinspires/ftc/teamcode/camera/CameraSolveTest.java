package org.firstinspires.ftc.teamcode.camera;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The camera's angles solved from stops made up from a known camera, before
 * any robot run.
 */
public class CameraSolveTest {

    private static final PoseFactory POSES = PoseFactory.degrees();
    private static final double[] CAMERA = {-7.0, 0.0, 10.0};
    private static final double[] TRUE_ANGLES = {178.7, 46.2, 1.3};

    /** The calibration auto's stops, by its plan. */
    private static List<Pose> grid() {
        List<Pose> poses = new ArrayList<>();
        double[] xs = {46, 59, 72};
        double[] ys = {14.76, 21, 29};
        double[] offsets = {-10, 0, 10};
        for (double y : ys) {
            for (int i = 0; i < xs.length; i++) {
                double heading = Math.toDegrees(Math.atan2(58.767 - y, 59.257 - xs[i])) - 180;
                poses.add(POSES.of(xs[i], y, heading + offsets[i]));
            }
        }
        return poses;
    }

    /**
     * What a camera at {@code camera} with {@code angles} sees from each pose, each stop's
     * directions turned by {@code noise} of its own, a random rotation of that many degrees
     * about each axis.
     */
    private static List<CameraSolve.Stop> stops(List<Pose> poses, double[] camera,
                                                 double[] angles, double noiseDeg, Random random) {
        double[][] r = CameraAngles.opticalToRobot(angles[0], angles[1], angles[2]);
        List<CameraSolve.Stop> stops = new ArrayList<>();
        for (Pose pose : poses) {
            double[][] shake = CameraAngles.multiply(CameraAngles.multiply(
                    CameraAngles.aboutZ(noiseDeg * random.nextGaussian()),
                    CameraAngles.aboutY(noiseDeg * random.nextGaussian())),
                    aboutX(noiseDeg * random.nextGaussian()));
            Map<Integer, double[]> seen = new HashMap<>();
            for (FieldTags.Tag tag : FieldTags.RED_UPRIGHT) {
                double[] inRobot = CameraSolve.towardTag(pose, camera, tag);
                double[] optical = CameraAngles.apply(CameraAngles.transpose(r), inRobot);
                seen.put(tag.id, CameraAngles.apply(shake, optical));
            }
            stops.add(new CameraSolve.Stop(pose, seen));
        }
        return stops;
    }

    private static double[][] aboutX(double deg) {
        double c = Math.cos(Math.toRadians(deg));
        double s = Math.sin(Math.toRadians(deg));
        return new double[][]{{1, 0, 0}, {0, c, -s}, {0, s, c}};
    }

    @Test
    public void theAnglesComeBackWithNoNoise() {
        List<CameraSolve.Stop> stops = stops(grid(), CAMERA, TRUE_ANGLES, 0, new Random(1));
        CameraSolve.Result result = CameraSolve.solve(stops, CAMERA, FieldTags.RED_UPRIGHT);
        assertEquals("heading", 0, CameraAngles.wrapDegrees(result.headingDeg - TRUE_ANGLES[0]), 0.05);
        assertEquals("pitch", TRUE_ANGLES[1], result.pitchDeg, 0.05);
        assertEquals("roll", TRUE_ANGLES[2], result.rollDeg, 0.05);
        for (double residual : result.stopResidualDeg) {
            assertEquals("each stop's residual", 0, residual, 1e-6);
        }
    }

    @Test
    public void theSigmasMatchTheSpreadTheNoiseMakes() {
        Random random = new Random(2);
        int trials = 200;
        double[] squared = new double[3];
        double[] sigmas = new double[3];
        for (int t = 0; t < trials; t++) {
            List<CameraSolve.Stop> stops = stops(grid(), CAMERA, TRUE_ANGLES, 0.1, random);
            CameraSolve.Result r = CameraSolve.solve(stops, CAMERA, FieldTags.RED_UPRIGHT);
            double[] off = {CameraAngles.wrapDegrees(r.headingDeg - TRUE_ANGLES[0]),
                    r.pitchDeg - TRUE_ANGLES[1], r.rollDeg - TRUE_ANGLES[2]};
            double[] sigma = {r.headingSigmaDeg, r.pitchSigmaDeg, r.rollSigmaDeg};
            for (int i = 0; i < 3; i++) {
                squared[i] += off[i] * off[i] / trials;
                sigmas[i] += sigma[i] / trials;
            }
        }
        String[] names = {"heading", "pitch", "roll"};
        for (int i = 0; i < 3; i++) {
            double spread = Math.sqrt(squared[i]);
            double ratio = sigmas[i] / spread;
            assertTrue(names[i] + ": sigma " + sigmas[i] + " against a spread of " + spread,
                    ratio > 1 / 1.5 && ratio < 1.5);
        }
    }

    @Test
    public void aCameraPositionHalfAnInchOutShowsAsAnAngleThatTrendsWithDistance() {
        double[] truth = {-7.0, 0.5, 10.0};
        List<CameraSolve.Stop> stops = stops(grid(), truth, TRUE_ANGLES, 0, new Random(3));
        CameraSolve.Result r = CameraSolve.solve(stops, CAMERA, FieldTags.RED_UPRIGHT);
        double[] inverse = new double[r.stopDistanceIn.length];
        for (int i = 0; i < inverse.length; i++) inverse[i] = 1 / r.stopDistanceIn[i];
        double best = Math.max(Math.abs(correlation(inverse, r.stopHeadingDeg)),
                Math.max(Math.abs(correlation(inverse, r.stopPitchDeg)),
                        Math.abs(correlation(inverse, r.stopRollDeg))));
        assertTrue("a stop's own angle follows one over its distance: " + best, best > 0.9);

        List<CameraSolve.Stop> right = stops(grid(), CAMERA, TRUE_ANGLES, 0, new Random(3));
        CameraSolve.Result matched = CameraSolve.solve(right, CAMERA, FieldTags.RED_UPRIGHT);
        for (double heading : matched.stopHeadingDeg) {
            assertEquals("with the position right, each stop's own heading is the same", 0,
                    CameraAngles.wrapDegrees(heading - TRUE_ANGLES[0]), 1e-6);
        }
    }

    private static double correlation(double[] a, double[] b) {
        double ma = 0, mb = 0;
        for (int i = 0; i < a.length; i++) {
            ma += a[i] / a.length;
            mb += b[i] / b.length;
        }
        double ab = 0, aa = 0, bb = 0;
        for (int i = 0; i < a.length; i++) {
            ab += (a[i] - ma) * (b[i] - mb);
            aa += (a[i] - ma) * (a[i] - ma);
            bb += (b[i] - mb) * (b[i] - mb);
        }
        return ab / Math.sqrt(aa * bb);
    }
}
