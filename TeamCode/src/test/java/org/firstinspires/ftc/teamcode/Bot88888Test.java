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
import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.teamcode.pedro.RobotControllerName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 88888's OpModes do what the solved L140 Auto and L140 Field Relative do, with
 * no mechanism, and Robot Name Check shows what it found.
 */
public class Bot88888Test {

    private static final PoseFactory POSES = PoseFactory.degrees();
    private static final double EPS = 1e-6;

    private Supplier<String> theSdksReader;
    private Pose theAutosEndPose;

    @Before
    public void setUp() {
        Scheduler.reset();
        theSdksReader = RobotControllerName.reader;
        theAutosEndPose = OpModeStorage.autonomousEndPose;
    }

    @After
    public void tearDown() {
        RobotControllerName.reader = theSdksReader;
        OpModeStorage.autonomousEndPose = theAutosEndPose;
        OpModeStorage.alliance = "Red";
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
        OpModeStorage.alliance = "Blue";
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
        OpModeStorage.alliance = "Blue";
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
    public void theAutoMenuChoosesTheAllianceAndTheRoute() {
        OpModeHarness h = new OpModeHarness(new Bot88888Auto());
        Follower follower = h.robot.follower;
        h.init();
        h.initLoop();
        assertTrue("the menu is on the Driver Station during INIT: " + h.driverStation.text(),
                h.driverStation.shows("> Alliance: <Red>"));
        assertTrue(h.driverStation.text(), h.driverStation.shows("R-N"));

        pressDuringInit(h, pad -> pad.dpad_right = true);
        assertEquals("the summary first, then right shows Blue, not yet selected",
                "R-N\n> Alliance: Blue? A:OK B:Back\n  Auto: <N-Nothing>"
                        + "\nLive NetworkTables off\nPanels: http://192.168.43.1:8001",
                h.driverStation.text());
        pressDuringInit(h, pad -> pad.b = true);
        assertTrue("B goes back to Red: " + h.driverStation.text(),
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

        assertEquals("the alliance handed to the teleop", "Blue", OpModeStorage.alliance);
        assertEquals("Nothing does not move", 133.5, follower.pose().y(), 0.1);
    }

    @Test
    public void theNameCheckSaysWhetherConstantsHadRunAndWhatItFound() {
        List<String> notYet = RobotNameCheck.linesToShow(false, "a.B.init\nc.D.run\n",
                "(could not read: no Robot Controller name yet)", "37523-RC",
                Constants.Robot.BOT_37523, false);
        assertEquals("Constants had not run before this INIT; this INIT ran it.", notYet.get(0));
        assertEquals("What ran Constants, nearest first:", notYet.get(1));
        assertEquals("  a.B.init", notYet.get(2));
        assertEquals("  c.D.run", notYet.get(3));
        assertEquals("Robot Controller name when Constants ran: "
                + "(could not read: no Robot Controller name yet)", notYet.get(4));
        assertEquals("Robot Controller name now: 37523-RC", notYet.get(5));
        assertEquals("thisRobot picks: BOT_37523, as written in Constants", notYet.get(6));

        List<String> already = RobotNameCheck.linesToShow(true, "a.B.init\n", "88888-RC",
                "88888-RC", Constants.Robot.BOT_88888, true);
        assertEquals("Constants had already run before this INIT.", already.get(0));
        assertEquals("thisRobot picks: BOT_88888, by the Robot Controller name",
                already.get(already.size() - 1));
    }

    @Test
    public void theNameCheckShowsItOnTheDriverStationAtInit() {
        RobotControllerName.reader = () -> "37523-RC";
        OpModeHarness h = new OpModeHarness(new RobotNameCheck());
        h.init();
        // The SDK sends what init() printed after the first pass.
        h.initLoop();
        String shown = h.driverStation.text();
        assertTrue(shown, shown.contains("Constants had already run before this INIT."));
        assertTrue(shown, shown.contains("Robot Controller name now: 37523-RC"));
        assertTrue(shown, shown.contains("thisRobot picks: BOT_37523"));
        h.start();
        h.loops(1, 0);
        h.stop();
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
        long deadline = System.nanoTime() + (long) (seconds * 1e9);
        boolean followed = false;
        int loopsIdle = 0;
        while (loopsIdle < 3 && System.nanoTime() < deadline) {
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
