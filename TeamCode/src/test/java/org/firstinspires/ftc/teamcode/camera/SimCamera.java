package org.firstinspires.ftc.teamcode.camera;

import com.pedropathing.math.Pose;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * A stand-in for the camera: a frame every 1/30 s of the clock, holding each
 * of {@link FieldTags#RED_UPRIGHT} that a camera at a known place and angles
 * on the robot would see, at the pixel its centre projects to.
 *
 * <p>A tag is seen when it is ahead of the camera, its printed face turned
 * toward it, and its centre at least {@link #MARGIN_PX} inside the picture.
 *
 * <p>Passes when: CameraCalibrationTest.
 */
public final class SimCamera {

    /** How far inside the picture's edge a tag's centre has to be to be seen, pixels. */
    public static final double MARGIN_PX = 40;
    static final long FRAME_NANOS = 1_000_000_000L / 30;

    private SimCamera() {
    }

    /** What a camera at {@code camera}, inches, with {@code angles}, degrees, sees in one frame. */
    public static List<Camera.Sighting> frame(Pose robot, double[] camera, double[] angles,
                                              long nanos) {
        double[][] toOptical = CameraAngles.transpose(
                CameraAngles.opticalToRobot(angles[0], angles[1], angles[2]));
        double c = Math.cos(robot.heading());
        double s = Math.sin(robot.heading());
        double[] cameraOnField = {robot.x() + c * camera[0] - s * camera[1],
                robot.y() + s * camera[0] + c * camera[1], camera[2]};
        Lens lens = Camera.LENS;
        List<Camera.Sighting> seen = new ArrayList<>();
        for (FieldTags.Tag tag : FieldTags.RED_UPRIGHT) {
            double[] toCamera = {cameraOnField[0] - tag.centre[0], cameraOnField[1] - tag.centre[1],
                    cameraOnField[2] - tag.centre[2]};
            if (CameraAngles.dot(tag.facing, toCamera) <= 0) continue;
            double[] optical = CameraAngles.apply(toOptical,
                    CameraSolve.towardTag(robot, camera, tag));
            if (optical[2] <= 0) continue;
            double[] px = lens.pixel(optical);
            if (px[0] < MARGIN_PX || px[0] > lens.width - MARGIN_PX
                    || px[1] < MARGIN_PX || px[1] > lens.height - MARGIN_PX) continue;
            seen.add(new Camera.Sighting(tag.id, px[0], px[1], nanos));
        }
        return seen;
    }

    /** A camera that sees from {@code robot}'s pose, a new frame each 1/30 s of {@code clock}. */
    public static Camera on(Supplier<Pose> robot, LongSupplier clock, double[] camera,
                            double[] angles) {
        long[] last = {Long.MIN_VALUE};
        return new Camera(() -> {
            long now = clock.getAsLong();
            if (last[0] != Long.MIN_VALUE && now - last[0] < FRAME_NANOS) return null;
            last[0] = now;
            return frame(robot.get(), camera, angles, now);
        }, () -> { });
    }
}
