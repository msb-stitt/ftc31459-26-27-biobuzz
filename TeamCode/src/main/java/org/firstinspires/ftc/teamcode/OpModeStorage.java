package org.firstinspires.ftc.teamcode;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;

/**
 * What one OpMode leaves behind for the next one.
 *
 * <p>An autonomous writes where it finished, and that one has run, in
 * {@code CorbelsOpMode.stopAfter}, and a teleop starts from it, in
 * {@code CorbelsOpMode.initAfter}. Until an autonomous has run, a teleop
 * outside the lessons starts at {@link #defaultStartPose} instead. A lesson's
 * autonomous writes its alliance, from L140 on. Static
 * because the two are separate OpMode objects: the Driver Station builds a new
 * one each time PLAY is pressed, and only the class outlives that. It survives
 * Stop and Init, and does not survive restarting the Robot Controller app or
 * running Clear Auto Has Run, so the first teleop after either uses the
 * default below.
 *
 * <h2>Where (0, 0) is</h2>
 *
 * <p>Pedro puts the origin in a corner of the field and the middle at
 * (72, 72), in inches. Which corner point that is, the inside or the outside of
 * the perimeter frame, is not settled in the Pedro 3 community. This code takes
 * the reading that goes with a middle at (72, 72): (0, 0) is the outside
 * corner, and the frame stands between it and the tiles.
 *
 * <p>So a robot cannot be at (0, 0). The closest it can get is
 * {@link #fieldPerimeterWidthIn} in from each wall, and further still by half
 * its own length in whichever direction it faces. That is what
 * {@link #botStartYIn} adds up.
 *
 * <p>The other reading, that (0, 0) is the inside corner, puts the middle at
 * 70.75 in, because the tiles measure 141.5 in across rather than 144 in. A
 * robot mirrored for the other alliance under the wrong one of these lands
 * about 2.5 in out.
 */
public final class OpModeStorage {

    private static final PoseFactory POSES = PoseFactory.degrees();

    /** Nose to middle, so a robot against a wall is this far from it. */
    public static final double robotHalfLengthIn = 9.0;

    /** The frame's thickness, between the outside corner at (0, 0) and the tiles. */
    public static final double fieldPerimeterWidthIn = 1.5;

    /** How far up the field a robot's middle is when its back is against the wall. */
    public static final double botStartYIn = robotHalfLengthIn + fieldPerimeterWidthIn;

    /**
     * Where the last autonomous finished, and so where the next teleop starts.
     * The default is the middle of the near wall, facing up the field, for a
     * lesson's teleop run with no autonomous before it.
     */
    public static Pose autonomousEndPose;

    /**
     * Whether an autonomous has finished since the Robot Controller app
     * started, so {@link #autonomousEndPose} is where it ended rather than the
     * default. {@code CorbelsOpMode.initAfter} reads it, and so does a teleop
     * that starts where its own auto starts.
     */
    public static boolean autoHasRun;

    /**
     * Whether the last autonomous ran for the red alliance, so the next teleop
     * drives from that Alliance Area. True for a teleop run with no autonomous
     * before it.
     */
    public static boolean isRed;

    static {
        clear();
    }

    /**
     * Puts the fields above back as the Robot Controller app starts them, as
     * if no autonomous had run.
     */
    public static void clear() {
        autonomousEndPose = POSES.of(72, botStartYIn, 90);
        autoHasRun = false;
        isRed = true;
    }

    /**
     * Where an OpMode outside the lessons starts when nothing else says: against
     * the alliance's wall, 12 in to the left of its middle as its drivers see
     * it, facing them, so that driving field relative is the right way round.
     * Red's pose, turned a half-turn about the field's middle for blue.
     */
    public static Pose defaultStartPose(boolean red) {
        PoseFactory poses = POSES;
        if (!red) {
            poses = POSES.rotateAround(72.0, 72.0, 180.0);
        }
        Pose defaultStart = poses.of(60.0, botStartYIn, -90.0);
        return defaultStart;
    }

    private OpModeStorage() {
    }
}
