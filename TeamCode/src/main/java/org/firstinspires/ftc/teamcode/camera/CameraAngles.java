package org.firstinspires.ftc.teamcode.camera;

/**
 * The camera's heading, pitch and roll, as {@code Constants} holds them, and
 * the rotation they make.
 *
 * <p>The camera's own frame is OpenCV's: +x right in the picture, +y down,
 * +z the way it looks. Pedro's robot frame is +x forward, +y left, +z up.
 * Heading turns where the camera looks about +z, 0 forward and 90 left; pitch
 * lifts it up from level; roll turns the camera about where it looks,
 * clockwise as the camera sees, 0 when the picture's +x is level.
 *
 * <p>The SDK's {@code setCameraPose} takes its own robot frame, +x right and
 * +y forward, and angles applied as intrinsic Z, X, Z with all zero looking
 * straight up. Its yaw is the heading, its pitch is the pitch less 90°, and
 * its roll is the roll. CameraAnglesTest checks that against the SDK's own
 * {@code Orientation}.
 *
 * <p>A rotation is a 3 × 3 array, rows then columns; its columns are the
 * camera's axes in the robot's frame.
 */
// 'public' so the OpModes, in another package, can use it. 'public' allows access from any code.
public final class CameraAngles {

    private CameraAngles() {
    }

    /** The rotation from the camera's frame to Pedro's robot frame. */
    public static double[][] opticalToRobot(double headingDeg, double pitchDeg, double rollDeg) {
        // Heading, pitch and roll all 0: the camera looks forward, +x, with its right edge to the
        // robot's right, -y, and its bottom edge down, -z.
        double[][] level = {{0, 0, 1}, {-1, 0, 0}, {0, -1, 0}};
        double[][] r = multiply(aboutZ(headingDeg), aboutY(-pitchDeg));
        return multiply(multiply(r, level), aboutZ(rollDeg));
    }

    /** {heading, pitch, roll} in degrees, back from {@link #opticalToRobot}. */
    public static double[] anglesOf(double[][] r) {
        double heading = Math.toDegrees(Math.atan2(r[1][2], r[0][2]));
        double pitch = Math.toDegrees(Math.asin(Math.max(-1, Math.min(1, r[2][2]))));
        // The picture's +x turns from level, -y turned by the heading and pitch, toward the
        // picture's down with no roll.
        double[][] turned = multiply(aboutZ(heading), aboutY(-pitch));
        double[] level = apply(turned, new double[]{0, -1, 0});
        double[] down = apply(turned, new double[]{0, 0, -1});
        double[] x = {r[0][0], r[1][0], r[2][0]};
        double roll = Math.toDegrees(Math.atan2(dot(x, down), dot(x, level)));
        return new double[]{heading, pitch, roll};
    }

    /** The SDK's {yaw, pitch, roll} for {@code setCameraPose}, in degrees. */
    public static double[] sdkYawPitchRoll(double headingDeg, double pitchDeg, double rollDeg) {
        return new double[]{headingDeg, pitchDeg - 90, rollDeg};
    }

    /** The SDK's {x, y, z} for {@code setCameraPose}, +x right and +y forward, from Pedro's. */
    public static double[] sdkPosition(double xIn, double yIn, double zIn) {
        return new double[]{-yIn, xIn, zIn};
    }

    /** An angle in degrees, from -180 to 180. */
    public static double wrapDegrees(double deg) {
        return Math.toDegrees(Math.atan2(Math.sin(Math.toRadians(deg)), Math.cos(Math.toRadians(deg))));
    }

    static double[][] aboutZ(double deg) {
        double c = Math.cos(Math.toRadians(deg));
        double s = Math.sin(Math.toRadians(deg));
        return new double[][]{{c, -s, 0}, {s, c, 0}, {0, 0, 1}};
    }

    static double[][] aboutY(double deg) {
        double c = Math.cos(Math.toRadians(deg));
        double s = Math.sin(Math.toRadians(deg));
        return new double[][]{{c, 0, s}, {0, 1, 0}, {-s, 0, c}};
    }

    static double[][] multiply(double[][] a, double[][] b) {
        double[][] m = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                m[i][j] = a[i][0] * b[0][j] + a[i][1] * b[1][j] + a[i][2] * b[2][j];
            }
        }
        return m;
    }

    static double[][] transpose(double[][] a) {
        double[][] t = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                t[i][j] = a[j][i];
            }
        }
        return t;
    }

    static double[] apply(double[][] a, double[] v) {
        return new double[]{dot(a[0], v), dot(a[1], v), dot(a[2], v)};
    }

    static double dot(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }
}
