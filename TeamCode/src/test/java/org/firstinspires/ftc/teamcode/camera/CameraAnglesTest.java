package org.firstinspires.ftc.teamcode.camera;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.junit.Test;

/** The camera's heading, pitch and roll, as a rotation and as the SDK's angles. */
public class CameraAnglesTest {

    private static final double EPS = 1e-9;
    private static final Lens LENS = Lens.C270_640_BY_480;

    /** Where a point in the robot's frame lands in the picture, for a camera at the origin. */
    private static double[] pixelOf(double[][] opticalToRobot, double[] point) {
        double[] optical = CameraAngles.apply(CameraAngles.transpose(opticalToRobot), point);
        return LENS.pixel(optical);
    }

    @Test
    public void aCameraFacingBackAndUpSeesAPointBehindAndAboveAtThePicturesCentre() {
        double[][] r = CameraAngles.opticalToRobot(180, 45, 0);
        double s = Math.sqrt(0.5);
        double[] centre = pixelOf(r, new double[]{-10 * s, 0, 10 * s});
        assertEquals("u", LENS.cx, centre[0], 1e-6);
        assertEquals("v", LENS.cy, centre[1], 1e-6);

        // The camera faces back, so the robot's right, -y, is the camera's left.
        double[] right = pixelOf(r, new double[]{-10 * s, -1, 10 * s});
        assertTrue("the robot's right is left in the picture: " + right[0], right[0] < LENS.cx);
        double[] higher = pixelOf(r, new double[]{-10 * s, 0, 10 * s + 1});
        assertTrue("higher is up in the picture: " + higher[1], higher[1] < LENS.cy);
    }

    @Test
    public void rollTurnsThePictureClockwiseAsTheCameraSees() {
        // Facing forward and level, a point to the robot's left is left of centre. Rolled 90°
        // clockwise, the camera's right edge points down and the picture turns anticlockwise,
        // so that point is below the centre.
        double[] left = {10, 1, 0};
        double[] level = pixelOf(CameraAngles.opticalToRobot(0, 0, 0), left);
        assertTrue("left of centre: " + level[0], level[0] < LENS.cx);
        double[] rolled = pixelOf(CameraAngles.opticalToRobot(0, 0, 90), left);
        assertEquals("rolled: on the centre line", LENS.cx, rolled[0], 1e-6);
        assertTrue("rolled: below the centre: " + rolled[1], rolled[1] > LENS.cy);
    }

    @Test
    public void theAnglesComeBackFromTheRotation() {
        double[][] cases = {{180, 45, 0}, {170, 40, 2}, {-35, -10, -5}, {90, 60, 15}};
        for (double[] c : cases) {
            double[] back = CameraAngles.anglesOf(CameraAngles.opticalToRobot(c[0], c[1], c[2]));
            assertEquals("heading", 0, CameraAngles.wrapDegrees(back[0] - c[0]), 1e-9);
            assertEquals("pitch", c[1], back[1], 1e-9);
            assertEquals("roll", c[2], back[2], 1e-9);
        }
    }

    @Test
    public void theSdksAnglesGiveTheSameRotation() {
        double[][] cases = {{180, 45, 0}, {170, 40, 2}, {-35, -10, -5}, {90, 60, 15}};
        for (double[] c : cases) {
            double[] ypr = CameraAngles.sdkYawPitchRoll(c[0], c[1], c[2]);
            OpenGLMatrix sdk = new Orientation(AxesReference.INTRINSIC, AxesOrder.ZXZ,
                    AngleUnit.DEGREES, (float) ypr[0], (float) ypr[1], (float) ypr[2], 0)
                    .getRotationMatrix();
            double[][] ours = CameraAngles.opticalToRobot(c[0], c[1], c[2]);
            // The SDK's robot frame is +x right, +y forward: Pedro's x is its y, Pedro's y its -x.
            for (int col = 0; col < 3; col++) {
                assertEquals("x of column " + col, sdk.get(1, col), ours[0][col], 1e-6);
                assertEquals("y of column " + col, -sdk.get(0, col), ours[1][col], 1e-6);
                assertEquals("z of column " + col, sdk.get(2, col), ours[2][col], 1e-6);
            }
        }
    }

    @Test
    public void theSdksPositionIsPedrosTurnedAQuarter() {
        double[] p = CameraAngles.sdkPosition(-7, 2, 10);
        assertEquals("right", -2, p[0], EPS);
        assertEquals("forward", -7, p[1], EPS);
        assertEquals("up", 10, p[2], EPS);
    }
}
