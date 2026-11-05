package org.firstinspires.ftc.teamcode.example;

import static org.junit.Assert.assertEquals;

import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.OpModeStorage;
import org.firstinspires.ftc.teamcode.base.OpModeHarness;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Auto: Drive 24in starts at red's default start and drives 24 in up the field. */
public class DriveForward24OpModeTest {

    @Before
    public void setUp() {
        OpModeStorage.clear();
        Scheduler.reset();
    }

    @After
    public void tearDown() {
        OpModeHarness.restoreFactories();
        Scheduler.reset();
    }

    @Test
    public void itStartsAtTheDefaultAndDrivesForward24In() {
        OpModeHarness h = new OpModeHarness(new DriveForward24OpMode());
        Follower follower = h.robot.follower;
        h.init();
        Pose start = OpModeStorage.defaultStartPose(true);
        assertEquals("at the default start during INIT: x", start.x(), follower.pose().x(), 1e-6);
        assertEquals("y", start.y(), follower.pose().y(), 1e-6);
        double turnFromStart = follower.pose().heading() - start.heading();
        assertEquals("heading", 0.0, Math.atan2(Math.sin(turnFromStart), Math.cos(turnFromStart)),
                1e-6);

        h.start();
        long deadline = h.clock.nanos() + 4_000_000_000L;
        while (h.clock.nanos() < deadline) {
            h.loop();
            OpModeHarness.sleep(5);
        }
        h.stop();
        Pose end = follower.pose();
        assertEquals("forward 24 in, to +y", 34.5, end.y(), 2.0);
        assertEquals("and straight", 60.0, end.x(), 2.0);
    }
}
