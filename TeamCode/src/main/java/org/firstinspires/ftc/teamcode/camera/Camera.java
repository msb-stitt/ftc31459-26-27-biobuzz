package org.firstinspires.ftc.teamcode.camera;

import android.util.Size;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagLibrary;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.apriltag.AprilTagSingleDetection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * The USB camera, and the AprilTags it sees: each one's id and its centre in
 * the picture, in pixels.
 *
 * <p>{@link #from} builds the SDK's VisionPortal at 640 × 480 with an AprilTag
 * processor whose library holds {@link FieldTags#RED_UPRIGHT} as single tags.
 * The SDK then reports each one by itself, with its centre; with its own
 * BIOBUZZ library it would report the cluster as one, with no centre. The
 * library holds each tag's id and size and not its field place, since nothing
 * here reads the SDK's own robot pose. The lens numbers are {@link Lens}'s,
 * set on the processor, so the SDK and {@link CameraSolve} use the same four.
 *
 * <p>A test hands the constructor a source of its own in place of the camera.
 */
// 'public' so the OpModes, in another package, can use it. 'public' allows access from any code.
public class Camera {

    /** One tag seen in one frame: its id, its centre in pixels, and when the frame was taken. */
    public static final class Sighting {
        public final int id;
        public final double u;
        public final double v;
        public final long frameNanos;

        public Sighting(int id, double u, double v, long frameNanos) {
            this.id = id;
            this.u = u;
            this.v = v;
            this.frameNanos = frameNanos;
        }
    }

    /** The camera's picture size and lens numbers. */
    public static final Lens LENS = Lens.C270_640_BY_480;

    private final Supplier<List<Sighting>> source;
    private final Runnable closer;

    /** Sightings from {@code source}, which gives null or nothing when no new frame has come. */
    public Camera(Supplier<List<Sighting>> source, Runnable closer) {
        this.source = source;
        this.closer = closer;
    }

    /** The webcam named in {@link Constants}, posed as its constants say. */
    public static Camera from(HardwareMap map) {
        AprilTagLibrary.Builder library = new AprilTagLibrary.Builder();
        for (FieldTags.Tag tag : FieldTags.RED_UPRIGHT) {
            library.addTag(tag.id, "ID " + tag.id, FieldTags.TAG_SIZE_IN, DistanceUnit.INCH);
        }
        double[] at = CameraAngles.sdkPosition(Constants.cameraXInches, Constants.cameraYInches,
                Constants.cameraZInches);
        double[] looks = CameraAngles.sdkYawPitchRoll(Constants.cameraHeadingDegrees,
                Constants.cameraPitchDegrees, Constants.cameraRollDegrees);
        AprilTagProcessor tags = new AprilTagProcessor.Builder()
                .setTagLibrary(library.build())
                .setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11)
                .setLensIntrinsics(LENS.fx, LENS.fy, LENS.cx, LENS.cy)
                .setCameraPose(new Position(DistanceUnit.INCH, at[0], at[1], at[2], 0),
                        new YawPitchRollAngles(AngleUnit.DEGREES, looks[0], looks[1], looks[2], 0))
                .setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES)
                .build();
        VisionPortal portal = new VisionPortal.Builder()
                .setCamera(map.get(WebcamName.class, Constants.webcamName))
                .setCameraResolution(new Size(LENS.width, LENS.height))
                .addProcessor(tags)
                .build();
        return new Camera(() -> sightings(tags.getFreshDetections()), portal::close);
    }

    /** The single tags among the SDK's detections. */
    static List<Sighting> sightings(List<AprilTagDetection> detections) {
        List<Sighting> seen = new ArrayList<>();
        if (detections == null) return seen;
        for (AprilTagDetection detection : detections) {
            if (detection instanceof AprilTagSingleDetection) {
                AprilTagSingleDetection tag = (AprilTagSingleDetection) detection;
                seen.add(new Sighting(tag.id, tag.center.x, tag.center.y,
                        tag.frameAcquisitionNanoTime));
            }
        }
        return seen;
    }

    /** What the camera has seen since the last call: empty when no new frame has come. */
    public List<Sighting> fresh() {
        List<Sighting> seen = source.get();
        return seen == null ? Collections.emptyList() : seen;
    }

    /** Stops the camera. */
    public void close() {
        closer.run();
    }
}
