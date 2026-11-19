package org.firstinspires.ftc.teamcode.camera;

/**
 * A camera's picture size and its four lens numbers, in pixels: the focal
 * lengths fx and fy and the centre cx, cy. Lens distortion is left out, as the
 * SDK leaves it out.
 */
// 'public' so the OpModes, in another package, can use it. 'public' allows access from any code.
public final class Lens {

    /** The Logitech C270 at 640 × 480, by the SDK 12.0's built-in calibration for it. */
    public static final Lens C270_640_BY_480 = new Lens(640, 480, 822.317, 822.317, 319.495, 242.502);

    public final int width;
    public final int height;
    public final double fx;
    public final double fy;
    public final double cx;
    public final double cy;

    public Lens(int width, int height, double fx, double fy, double cx, double cy) {
        this.width = width;
        this.height = height;
        this.fx = fx;
        this.fy = fy;
        this.cx = cx;
        this.cy = cy;
    }

    /** The unit vector, in the camera's frame, toward what is at pixel (u, v). */
    public double[] direction(double u, double v) {
        double x = (u - cx) / fx;
        double y = (v - cy) / fy;
        double n = Math.sqrt(x * x + y * y + 1);
        return new double[]{x / n, y / n, 1 / n};
    }

    /** The pixel {u, v} a direction in the camera's frame lands on; it must point ahead, z > 0. */
    public double[] pixel(double[] optical) {
        return new double[]{cx + fx * optical[0] / optical[2], cy + fy * optical[1] / optical[2]};
    }
}
