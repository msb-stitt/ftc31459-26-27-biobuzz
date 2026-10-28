package org.firstinspires.ftc.teamcode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.base.OpModeHarness;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.teamcode.pedro.RobotControllerName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.function.Supplier;

/** 88888's OpModes drive with no mechanism, and Robot Name Check shows what it found. */
public class Bot88888Test {

    private Supplier<String> theSdksReader;

    @Before
    public void setUp() {
        Scheduler.reset();
        theSdksReader = RobotControllerName.reader;
    }

    @After
    public void tearDown() {
        RobotControllerName.reader = theSdksReader;
    }

    @Test
    public void theTeleOpDrivesFromTheSticksAndLooksUpNoMechanism() {
        // The harness has no HardwareMap: looking up an intake or a launcher would throw.
        OpModeHarness h = new OpModeHarness(new Bot88888TeleOp());
        h.init();
        h.start();
        h.gamepad1.left_stick_y = -1.0f;
        h.loop();
        assertEquals("pushed away, forward at full speed", 1.0, h.wheelsForward(), 1e-3);
        assertEquals(-1.0, (Double) Tracker.values().get("gamepad1/leftStickY"), 1e-6);
        assertEquals(1.0, (Double) Tracker.values().get("drivetrain/forwardXSpeed"), 1e-6);
        h.gamepad1.left_stick_y = 0.0f;
        h.gamepad1.left_stick_x = 1.0f;
        h.loop();
        assertEquals("pushed right, strafes right", -1.0, h.wheelsStrafe(), 1e-3);
        h.stop();
    }

    @Test
    public void theAutoDrivesStarterBotAutosRoute() {
        OpModeHarness h = new OpModeHarness(new Bot88888Auto());
        Follower follower = h.robot.follower;
        h.init();
        h.start();
        long deadline = System.nanoTime() + 9_000_000_000L;
        boolean followed = false;
        int loopsIdle = 0;
        while (loopsIdle < 3 && System.nanoTime() < deadline) {
            h.loop();
            boolean following = follower.mode() == Follower.Mode.FOLLOW;
            boolean hasAPath = follower.currentPath() != null;
            if (following) {
                followed = true;
            }
            boolean pastTheCorner = follower.pose().x() > 90;
            if (followed && pastTheCorner && !following && !hasAPath) {
                loopsIdle++;
            } else {
                loopsIdle = 0;
            }
            OpModeHarness.sleep(5);
        }
        h.stop();
        assertTrue("the robot never started following a path", followed);
        Pose end = follower.pose();
        assertEquals("strafed 24 inches in x", 96.0, end.x(), 2.0);
        assertEquals(72.0, end.y(), 2.0);
    }

    @Test
    public void theNameCheckSaysWhetherConstantsHadRunAndWhatItFound() {
        List<String> notYet = RobotNameCheck.linesToShow(false, "a.B.init\nc.D.run\n",
                "(could not read: no name yet)", "37523-RC", Constants.Robot.BOT_37523);
        assertEquals("Constants had not run before this INIT; this INIT ran it.", notYet.get(0));
        assertEquals("What ran Constants, nearest first:", notYet.get(1));
        assertEquals("  a.B.init", notYet.get(2));
        assertEquals("  c.D.run", notYet.get(3));
        assertEquals("Name when Constants ran: (could not read: no name yet)", notYet.get(4));
        assertEquals("Name now: 37523-RC", notYet.get(5));
        assertEquals("thisRobot picks: BOT_37523", notYet.get(6));

        List<String> already = RobotNameCheck.linesToShow(true, "a.B.init\n", "37523-RC",
                "37523-RC", Constants.Robot.BOT_37523);
        assertEquals("Constants had already run before this INIT.", already.get(0));
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
        assertTrue(shown, shown.contains("Name now: 37523-RC"));
        assertTrue(shown, shown.contains("thisRobot picks: BOT_37523"));
        h.start();
        h.loops(1, 0);
        h.stop();
    }
}
