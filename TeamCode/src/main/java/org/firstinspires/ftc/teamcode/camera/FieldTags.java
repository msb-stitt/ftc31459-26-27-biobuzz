package org.firstinspires.ftc.teamcode.camera;

/**
 * Where the AprilTags the calibration uses are on the field, in Pedro's field
 * frame, inches: (0, 0) the outside corner, the middle at (72, 72), +z up from
 * the tiles.
 *
 * <p>Red's upright cluster in the pre-match state, IDs 34 to 37 under red's
 * audience CELL, as {@code tools/field/tag_poses.py} prints it from FIRST's
 * Onshape field document, read 2026-11-19. Each tag's facing is the direction
 * out of its printed face; its right is the decal's right seen from below.
 */
// 'public' so the OpModes, in another package, can use it. 'public' allows access from any code.
public final class FieldTags {

    /** A tag's black square, edge to edge, inches. */
    public static final double TAG_SIZE_IN = 3.25;

    /** One tag: its id, its centre, and which way it faces. */
    public static final class Tag {
        public final int id;
        public final double[] centre;
        public final double[] facing;
        public final double[] right;

        public Tag(int id, double x, double y, double z, double facingX, double facingY,
                   double facingZ, double rightX, double rightY, double rightZ) {
            this.id = id;
            this.centre = new double[]{x, y, z};
            this.facing = new double[]{facingX, facingY, facingZ};
            this.right = new double[]{rightX, rightY, rightZ};
        }
    }

    /** Red's upright cluster, IDs 34 to 37, left to right seen from below. */
    public static final Tag[] RED_UPRIGHT = {
            new Tag(34, 52.757, 58.767, 49.494, 0.0000, -0.4806, -0.8769, 1, 0, 0),
            new Tag(35, 56.507, 58.767, 49.494, 0.0000, -0.4806, -0.8769, 1, 0, 0),
            new Tag(36, 62.007, 58.767, 49.494, 0.0000, -0.4806, -0.8769, 1, 0, 0),
            new Tag(37, 65.757, 58.767, 49.494, 0.0000, -0.4806, -0.8769, 1, 0, 0),
    };

    private FieldTags() {
    }

    /** The tag with this id among {@code tags}, or null. */
    public static Tag lookup(Tag[] tags, int id) {
        for (Tag tag : tags) {
            if (tag.id == id) return tag;
        }
        return null;
    }
}
