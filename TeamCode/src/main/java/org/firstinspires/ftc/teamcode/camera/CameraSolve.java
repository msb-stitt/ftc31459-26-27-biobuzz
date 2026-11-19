package org.firstinspires.ftc.teamcode.camera;

import com.pedropathing.math.Pose;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The camera's heading, pitch and roll, solved from the stops the calibration
 * auto made, with the camera's position held at its constants.
 *
 * <p>At each stop, the robot's pose, the camera's position and the tag table
 * say which way each tag is from the camera, in the robot's frame. The camera
 * saw the same tags, which gives the same directions in its own frame. The
 * rotation that best turns the camera's directions into the robot's is
 * Wahba's problem, solved here in closed form by Horn's quaternion method.
 *
 * <p>Each stop is one sample, since its frames share one robot pose. An
 * angle's σ is the jackknife's: the angles solved leaving out each stop in
 * turn, and their spread. Each stop is also solved by itself, so an angle
 * that changes steadily with the stop's distance shows that a position, the
 * camera's or a tag's, is wrong.
 */
// 'public' so the OpModes, in another package, can use it. 'public' allows access from any code.
public final class CameraSolve {

    private CameraSolve() {
    }

    /** One stop: where the robot was, and the mean direction to each tag it saw, camera frame. */
    public static final class Stop {
        public final Pose robot;
        public final Map<Integer, double[]> directions;

        public Stop(Pose robot, Map<Integer, double[]> directions) {
            this.robot = robot;
            this.directions = directions;
        }
    }

    /** The angles and their σ, degrees, and each stop's own angles, residual and distance. */
    public static final class Result {
        public double headingDeg;
        public double pitchDeg;
        public double rollDeg;
        public double headingSigmaDeg;
        public double pitchSigmaDeg;
        public double rollSigmaDeg;
        /** Each stop's angle between what it saw and what the answer predicts, its tags averaged. */
        public double[] stopResidualDeg;
        /** Each stop's distance from the camera to its tags, averaged, inches. */
        public double[] stopDistanceIn;
        public double[] stopHeadingDeg;
        public double[] stopPitchDeg;
        public double[] stopRollDeg;
    }

    /**
     * The angles from {@code stops}, the camera at {@code camera} in Pedro's
     * robot frame, inches, and the tags at {@code tags}. The σ needs three
     * stops or more, and is NaN with fewer.
     */
    public static Result solve(List<Stop> stops, double[] camera, FieldTags.Tag[] tags) {
        Result result = new Result();
        double[][] r = fit(stops, camera, tags);
        double[] angles = CameraAngles.anglesOf(r);
        result.headingDeg = angles[0];
        result.pitchDeg = angles[1];
        result.rollDeg = angles[2];

        double[] variance = new double[3];
        int n = stops.size();
        for (int left = 0; left < n; left++) {
            List<Stop> others = new ArrayList<>(stops);
            others.remove(left);
            double[] a = CameraAngles.anglesOf(fit(others, camera, tags));
            double[] off = {CameraAngles.wrapDegrees(a[0] - angles[0]), a[1] - angles[1],
                    CameraAngles.wrapDegrees(a[2] - angles[2])};
            for (int i = 0; i < 3; i++) variance[i] += off[i] * off[i] * (n - 1) / n;
        }
        boolean enough = n >= 3;
        result.headingSigmaDeg = enough ? Math.sqrt(variance[0]) : Double.NaN;
        result.pitchSigmaDeg = enough ? Math.sqrt(variance[1]) : Double.NaN;
        result.rollSigmaDeg = enough ? Math.sqrt(variance[2]) : Double.NaN;

        result.stopResidualDeg = new double[n];
        result.stopDistanceIn = new double[n];
        result.stopHeadingDeg = new double[n];
        result.stopPitchDeg = new double[n];
        result.stopRollDeg = new double[n];
        for (int s = 0; s < n; s++) {
            Stop stop = stops.get(s);
            double residual = 0;
            double distance = 0;
            for (Map.Entry<Integer, double[]> seen : stop.directions.entrySet()) {
                FieldTags.Tag tag = FieldTags.lookup(tags, seen.getKey());
                double[] predicted = towardTag(stop.robot, camera, tag);
                double[] measured = CameraAngles.apply(r, seen.getValue());
                double cos = Math.max(-1, Math.min(1, CameraAngles.dot(predicted, measured)));
                residual += Math.toDegrees(Math.acos(cos)) / stop.directions.size();
                distance += length(fromCamera(stop.robot, camera, tag)) / stop.directions.size();
            }
            double[] own = CameraAngles.anglesOf(fit(stops.subList(s, s + 1), camera, tags));
            result.stopResidualDeg[s] = residual;
            result.stopDistanceIn[s] = distance;
            result.stopHeadingDeg[s] = own[0];
            result.stopPitchDeg[s] = own[1];
            result.stopRollDeg[s] = own[2];
        }
        return result;
    }

    /** The unit vector from the camera to the tag's centre, in the robot's frame. */
    public static double[] towardTag(Pose robot, double[] camera, FieldTags.Tag tag) {
        double[] v = fromCamera(robot, camera, tag);
        double n = length(v);
        return new double[]{v[0] / n, v[1] / n, v[2] / n};
    }

    /** From the camera to the tag's centre, in the robot's frame, inches. */
    static double[] fromCamera(Pose robot, double[] camera, FieldTags.Tag tag) {
        double dx = tag.centre[0] - robot.x();
        double dy = tag.centre[1] - robot.y();
        double c = Math.cos(robot.heading());
        double s = Math.sin(robot.heading());
        return new double[]{c * dx + s * dy - camera[0], -s * dx + c * dy - camera[1],
                tag.centre[2] - camera[2]};
    }

    /** The rotation from the camera's frame to the robot's that best fits the stops. */
    static double[][] fit(List<Stop> stops, double[] camera, FieldTags.Tag[] tags) {
        // Horn's S: the sum over every tag seen of the camera's direction times the robot's.
        double[][] s = new double[3][3];
        for (Stop stop : stops) {
            for (Map.Entry<Integer, double[]> seen : stop.directions.entrySet()) {
                double[] a = seen.getValue();
                double[] b = towardTag(stop.robot, camera, FieldTags.lookup(tags, seen.getKey()));
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) s[i][j] += a[i] * b[j];
                }
            }
        }
        double[][] k = {
                {s[0][0] + s[1][1] + s[2][2], s[1][2] - s[2][1], s[2][0] - s[0][2], s[0][1] - s[1][0]},
                {s[1][2] - s[2][1], s[0][0] - s[1][1] - s[2][2], s[0][1] + s[1][0], s[2][0] + s[0][2]},
                {s[2][0] - s[0][2], s[0][1] + s[1][0], -s[0][0] + s[1][1] - s[2][2], s[1][2] + s[2][1]},
                {s[0][1] - s[1][0], s[2][0] + s[0][2], s[1][2] + s[2][1], -s[0][0] - s[1][1] + s[2][2]},
        };
        double[] q = largestEigenvector(k);
        double w = q[0], x = q[1], y = q[2], z = q[3];
        return new double[][]{
                {w * w + x * x - y * y - z * z, 2 * (x * y - w * z), 2 * (x * z + w * y)},
                {2 * (x * y + w * z), w * w - x * x + y * y - z * z, 2 * (y * z - w * x)},
                {2 * (x * z - w * y), 2 * (y * z + w * x), w * w - x * x - y * y + z * z},
        };
    }

    /** The unit eigenvector of a symmetric 4 × 4 matrix with the largest eigenvalue, by Jacobi. */
    static double[] largestEigenvector(double[][] matrix) {
        int n = 4;
        double[][] a = new double[n][];
        for (int i = 0; i < n; i++) a[i] = matrix[i].clone();
        double[][] v = new double[n][n];
        for (int i = 0; i < n; i++) v[i][i] = 1;
        for (int sweep = 0; sweep < 100; sweep++) {
            double off = 0;
            for (int p = 0; p < n; p++) {
                for (int q = p + 1; q < n; q++) off += a[p][q] * a[p][q];
            }
            if (off < 1e-30) break;
            for (int p = 0; p < n; p++) {
                for (int q = p + 1; q < n; q++) {
                    if (Math.abs(a[p][q]) < 1e-300) continue;
                    double theta = (a[q][q] - a[p][p]) / (2 * a[p][q]);
                    double t = Math.signum(theta) / (Math.abs(theta) + Math.sqrt(theta * theta + 1));
                    if (theta == 0) t = 1;
                    double c = 1 / Math.sqrt(t * t + 1);
                    double sn = t * c;
                    for (int k = 0; k < n; k++) {
                        double akp = a[k][p], akq = a[k][q];
                        a[k][p] = c * akp - sn * akq;
                        a[k][q] = sn * akp + c * akq;
                    }
                    for (int k = 0; k < n; k++) {
                        double apk = a[p][k], aqk = a[q][k];
                        a[p][k] = c * apk - sn * aqk;
                        a[q][k] = sn * apk + c * aqk;
                    }
                    for (int k = 0; k < n; k++) {
                        double vkp = v[k][p], vkq = v[k][q];
                        v[k][p] = c * vkp - sn * vkq;
                        v[k][q] = sn * vkp + c * vkq;
                    }
                }
            }
        }
        int best = 0;
        for (int i = 1; i < n; i++) {
            if (a[i][i] > a[best][best]) best = i;
        }
        double[] e = new double[n];
        double norm = 0;
        for (int i = 0; i < n; i++) {
            e[i] = v[i][best];
            norm += e[i] * e[i];
        }
        norm = Math.sqrt(norm);
        for (int i = 0; i < n; i++) e[i] /= norm;
        return e;
    }

    private static double length(double[] v) {
        return Math.sqrt(CameraAngles.dot(v, v));
    }
}
