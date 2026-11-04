package org.firstinspires.ftc.teamcode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.base.OpModeHarness;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Consumer;

/**
 * 88888's OpModes do what the solved L140 Auto and L140 Field Relative do, with
 * no mechanism.
 */
public class Bot88888Test {

    private static final PoseFactory POSES = PoseFactory.degrees();
    private static final double EPS = 1e-6;

    private Pose theAutosEndPose;
    private boolean anAutoHadRun;

    @Before
    public void setUp() {
        Scheduler.reset();
        theAutosEndPose = OpModeStorage.autonomousEndPose;
        anAutoHadRun = OpModeStorage.autoHasRun;
        OpModeStorage.autoHasRun = false;
    }

    @After
    public void tearDown() {
        OpModeStorage.autonomousEndPose = theAutosEndPose;
        OpModeStorage.autoHasRun = anAutoHadRun;
        OpModeStorage.isRed = true;
        OpModeHarness.restoreFactories();
        Scheduler.reset();
    }

    @Test
    public void theTeleOpDrivesTheFieldWhicheverWayTheRobotFacesAndLooksUpNoMechanism() {
        // The harness has no HardwareMap: looking up an intake or a launcher would throw.
        OpModeHarness h = new OpModeHarness(new Bot88888TeleOp());
        Follower follower = h.robot.follower;
        h.init();
        h.start();

        follower.setPose(POSES.of(0, 0, 0));
        h.gamepad1.left_stick_y = -1.0f;          // away from the driver
        h.loop();
        assertEquals(1.0, h.wheelsForward(), 1e-3);
        assertEquals(-1.0, (Double) Tracker.values().get("gamepad1/leftStickY"), EPS);
        assertEquals(1.0, (Double) Tracker.values().get("drivetrain/fieldXSpeed"), EPS);

        follower.setPose(POSES.of(0, 0, 90));     // robot now faces +y
        h.loop();
        assertEquals("same stick, still moves away from the driver",
                0.0, h.wheelsForward(), 1e-3);
        assertEquals(-1.0, h.wheelsStrafe(), 1e-3);

        h.gamepad1.left_stick_y = -0.5f;
        h.loop();
        assertEquals("half a stick is a quarter of full speed", 0.25,
                (Double) Tracker.values().get("drivetrain/fieldXSpeed"), EPS);
        h.gamepad1.left_stick_y = -0.04f;
        h.loop();
        assertEquals("inside the band is still", 0.0,
                (Double) Tracker.values().get("drivetrain/fieldXSpeed"), EPS);
        h.stop();
    }

    @Test
    public void fromTheBlueAllianceAreaTheTeleOpsStickMeansTheOtherWay() {
        OpModeStorage.isRed = false;
        OpModeHarness h = new OpModeHarness(new Bot88888TeleOp());
        Follower follower = h.robot.follower;
        h.init();
        h.initLoop();
        h.start();

        follower.setPose(POSES.of(72, 72, 0));    // robot faces +x
        h.gamepad1.left_stick_y = -1.0f;          // away from the blue drivers, toward -x
        h.loop();
        assertEquals("away from the blue drivers is -x, behind a robot facing +x",
                -1.0, h.wheelsForward(), 1e-3);

        h.gamepad1.left_stick_y = 0.0f;
        h.gamepad1.left_stick_x = -1.0f;          // to the blue drivers' left, toward -y
        h.loop();
        assertEquals("their left is -y, to the right of a robot facing +x",
                -1.0, h.wheelsStrafe(), 1e-3);
        h.stop();
    }

    @Test
    public void holdingTheLeftBumperTheTeleOpDrivesTheRobotNotTheField() {
        OpModeStorage.isRed = false;
        OpModeHarness h = new OpModeHarness(new Bot88888TeleOp());
        Follower follower = h.robot.follower;
        h.init();
        h.initLoop();
        h.start();

        follower.setPose(POSES.of(72, 72, 90));   // robot faces +y
        h.gamepad1.left_bumper = true;
        h.gamepad1.left_stick_y = -1.0f;          // away from the driver
        h.loop();
        assertEquals("the robot's front forward, whichever way it faces and on Blue",
                1.0, h.wheelsForward(), 1e-3);
        assertEquals(0.0, h.wheelsStrafe(), 1e-3);
        assertEquals(true, Tracker.values().get("gamepad1/leftBumper"));

        h.gamepad1.left_stick_y = 0.0f;
        h.gamepad1.left_stick_x = -0.5f;          // to the driver's left
        h.loop();
        assertEquals("to the robot's own left, half a stick a quarter of full speed",
                0.25, h.wheelsStrafe(), 1e-3);

        h.gamepad1.left_bumper = false;
        h.gamepad1.left_stick_x = 0.0f;
        h.gamepad1.left_stick_y = -1.0f;          // away from the blue drivers, toward -x
        h.loop();
        assertEquals("let go, the field again: -x is to the left of a robot facing +y",
                1.0, h.wheelsStrafe(), 1e-3);
        assertEquals(0.0, h.wheelsForward(), 1e-3);
        assertEquals(false, Tracker.values().get("gamepad1/leftBumper"));
        h.stop();
    }

    @Test
    public void theTeleOpMenuChoosesTheAllianceAndTheWall() {
        OpModeStorage.autonomousEndPose = POSES.of(30, 40, 0);
        OpModeStorage.autoHasRun = true;
        OpModeHarness h = new OpModeHarness(new Bot88888TeleOp());
        Follower follower = h.robot.follower;
        h.init();
        h.initLoop();
        assertTrue("starts at the alliance the auto handed over: " + h.driverStation.text(),
                h.driverStation.shows("> Alliance: <Red>"));
        assertEquals("and where the auto ended", 30.0, follower.pose().x(), EPS);

        pressDuringInit(h, pad -> pad.dpad_right = true);
        pressDuringInit(h, pad -> pad.a = true);
        pressDuringInit(h, pad -> pad.dpad_down = true);
        pressDuringInit(h, pad -> pad.dpad_right = true);
        pressDuringInit(h, pad -> pad.a = true);
        assertTrue(h.driverStation.text(), h.driverStation.shows("B-W"));
        h.start();

        // Red's wall start, (72, 10.5, 90), turned a half-turn about (72, 72).
        assertEquals("against blue's wall", 72.0, follower.pose().x(), EPS);
        assertEquals(133.5, follower.pose().y(), EPS);
        double heading_deg = Math.toDegrees(follower.pose().heading());
        assertEquals("facing -y", 270.0, (heading_deg % 360.0 + 360.0) % 360.0, 1e-3);
        h.stop();
    }

    @Test
    public void withNoAutoRunAutoEndIsWhereTheAutoStartsAndIsLoggedBeforePlay() {
        for (boolean red : new boolean[]{true, false}) {
            String what = red ? "red" : "blue";
            Pose autoStart = bot88888AutoStart(red);

            OpModeStorage.isRed = red;
            OpModeHarness h = new OpModeHarness(new Bot88888TeleOp());
            Follower follower = h.robot.follower;
            h.init();
            h.initLoop();                                       // Auto End, chosen already

            assertSamePose(what + ", before PLAY", autoStart, follower.pose());
            assertEquals(what + ": logged x", autoStart.x(),
                    (Double) Tracker.values().get("Localizer/pinPoint/x_in"), EPS);
            assertEquals(what + ": logged y", autoStart.y(),
                    (Double) Tracker.values().get("Localizer/pinPoint/y_in"), EPS);
            double loggedHeading = Math.toRadians(
                    (Double) Tracker.values().get("Localizer/pinPoint/heading_deg"));
            assertEquals(what + ": logged heading", 0.0,
                    angleBetween(autoStart.heading(), loggedHeading), EPS);
            h.start();
            assertSamePose(what + ", after PLAY", autoStart, follower.pose());
            h.stop();
            Scheduler.reset();
        }
    }

    @Test
    public void thePoseFollowsTheMenuAndAutoEndAfterAnAutoIsWhereItEnded() {
        Pose redWall = bot88888AutoStart(true);
        Pose blueWall = bot88888AutoStart(false);
        Pose autoEnd = POSES.of(30, 40, 0);
        OpModeStorage.autonomousEndPose = autoEnd;
        OpModeStorage.autoHasRun = true;
        OpModeStorage.isRed = true;

        OpModeHarness h = new OpModeHarness(new Bot88888TeleOp());
        Follower follower = h.robot.follower;
        h.init();
        h.initLoop();
        assertSamePose("Auto End, red", autoEnd, follower.pose());
        pressDuringInit(h, pad -> pad.dpad_down = true);        // the Start row
        pressDuringInit(h, pad -> pad.dpad_right = true);       // Wall
        pressDuringInit(h, pad -> pad.a = true);
        assertSamePose("Wall, red", redWall, follower.pose());
        pressDuringInit(h, pad -> pad.dpad_up = true);          // the Alliance row
        pressDuringInit(h, pad -> pad.dpad_right = true);       // blue
        pressDuringInit(h, pad -> pad.a = true);
        assertSamePose("Wall, blue", blueWall, follower.pose());
        pressDuringInit(h, pad -> pad.dpad_down = true);        // the Start row
        pressDuringInit(h, pad -> pad.dpad_left = true);        // Auto End
        pressDuringInit(h, pad -> pad.a = true);
        assertSamePose("back to Auto End, blue", autoEnd, follower.pose());
        h.stop();
    }

    @Test
    public void aPassWithNoChangeInTheMenuLeavesThePose() {
        OpModeHarness h = new OpModeHarness(new Bot88888TeleOp());
        Follower follower = h.robot.follower;
        h.init();
        h.initLoop();
        Pose movedTo = POSES.of(50, 60, 30);
        follower.setPose(movedTo);
        h.initLoop();
        assertSamePose("after a pass with no change", movedTo, follower.pose());
        h.stop();
    }

    /** Where 88888 Auto starts on an alliance, chosen in its menu. */
    private static Pose bot88888AutoStart(boolean red) {
        Bot88888Auto auto = new Bot88888Auto();
        OpModeHarness a = new OpModeHarness(auto);
        a.init();
        a.initLoop();
        if (!red) {
            pressDuringInit(a, pad -> pad.dpad_right = true);
            pressDuringInit(a, pad -> pad.a = true);
        }
        Pose autoStart = auto.startPose();
        return autoStart;
    }

    @Test
    public void theAutoMenuChoosesTheAllianceAndTheRoute() {
        OpModeHarness h = new OpModeHarness(new Bot88888Auto());
        Follower follower = h.robot.follower;
        h.init();
        h.initLoop();
        assertTrue("the menu is on the Driver Station during INIT: " + h.driverStation.text(),
                h.driverStation.shows("> Alliance: <Red>"));
        assertTrue(h.driverStation.text(), h.driverStation.shows("R-N"));

        pressDuringInit(h, pad -> pad.dpad_right = true);
        assertEquals("the summary first, then right offers Blue, not yet selected",
                "R-N\n> Alliance: Blue? A:OK B:Back\n  Auto: <Nothing>"
                        + "\nLive NetworkTables off\nPanels: http://192.168.43.1:8001",
                h.driverStation.text());
        pressDuringInit(h, pad -> pad.b = true);
        assertTrue("B clears the offer: " + h.driverStation.text(),
                h.driverStation.shows("> Alliance: <Red>"));

        pressDuringInit(h, pad -> pad.dpad_right = true);
        pressDuringInit(h, pad -> pad.a = true);
        pressDuringInit(h, pad -> pad.dpad_down = true);
        pressDuringInit(h, pad -> pad.dpad_right = true);
        pressDuringInit(h, pad -> pad.dpad_right = true);
        pressDuringInit(h, pad -> pad.a = true);
        assertTrue("the summary names what was selected: " + h.driverStation.text(),
                h.driverStation.shows("B-FS"));

        h.start();
        assertEquals("placed at red's start turned a half-turn", 133.5, follower.pose().y(), EPS);
        runUntilDone(h, follower, 6.0);
        h.stop();

        // Red's route ends at (96, 72); turned a half-turn about (72, 72), (48, 72).
        Pose end = follower.pose();
        assertEquals("forward then strafe, for blue", 48.0, end.x(), 2.0);
        assertEquals(72.0, end.y(), 2.0);
    }

    @Test
    public void theAutoDoesNothingByDefaultAndHandsItsAllianceToTheTeleOp() {
        assertEquals("the Driver Station picks 88888 TeleOp when the auto ends",
                Bot88888TeleOp.class.getAnnotation(TeleOp.class).name(),
                Bot88888Auto.class.getAnnotation(Autonomous.class).preselectTeleOp());

        OpModeHarness h = new OpModeHarness(new Bot88888Auto());
        Follower follower = h.robot.follower;
        h.init();
        h.initLoop();
        pressDuringInit(h, pad -> pad.dpad_right = true);
        pressDuringInit(h, pad -> pad.a = true);
        assertTrue("Nothing is the auto until another is chosen: " + h.driverStation.text(),
                h.driverStation.shows("B-N"));
        h.start();
        h.loops(20, 5);
        h.stop();

        assertEquals("blue, not red, handed to the teleop", false, OpModeStorage.isRed);
        assertEquals("Nothing does not move", 133.5, follower.pose().y(), 0.1);
    }

    private static void assertSamePose(String what, Pose expected, Pose pose) {
        assertEquals(what + ": x", expected.x(), pose.x(), EPS);
        assertEquals(what + ": y", expected.y(), pose.y(), EPS);
        assertEquals(what + ": heading", 0.0, angleBetween(expected.heading(), pose.heading()),
                EPS);
    }

    /** The turn from one heading to another, in radians, from -pi to pi. */
    private static double angleBetween(double from, double to) {
        return Math.atan2(Math.sin(to - from), Math.cos(to - from));
    }

    /** One press and release of gamepad 1's buttons between INIT and PLAY. */
    private static void pressDuringInit(OpModeHarness h, Consumer<Gamepad> buttons) {
        Gamepad held = new Gamepad();
        buttons.accept(held);
        h.gamepad1.copy(held);
        h.initLoop();
        h.gamepad1.copy(new Gamepad());
        h.initLoop();
    }

    /** Loops until the route has been followed and the follower has gone idle, or the time is up. */
    private static void runUntilDone(OpModeHarness h, Follower follower, double seconds) {
        long deadline = h.clock.nanos() + (long) (seconds * 1e9);
        boolean followed = false;
        int loopsIdle = 0;
        while (loopsIdle < 3 && h.clock.nanos() < deadline) {
            h.loop();
            if (follower.mode() == Follower.Mode.FOLLOW) followed = true;
            if (followed && follower.mode() != Follower.Mode.FOLLOW && follower.currentPath() == null) {
                loopsIdle++;
            } else {
                loopsIdle = 0;
            }
            OpModeHarness.sleep(5);
        }
        assertTrue("the robot never started following a path", followed);
    }
}
