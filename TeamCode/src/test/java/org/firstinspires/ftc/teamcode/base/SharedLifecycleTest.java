package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.commands.Commands;
import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.OpModeStorage;
import org.firstinspires.ftc.teamcode.lessons.L040TankOpMode;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * TeleOps and autos now share one parent. These check that both halves still
 * get what they used to: hardware at init, the scheduler, the shadow
 * localizers, Panels, and a follower that is stopped at the end.
 */
public class SharedLifecycleTest {

    private static final PoseFactory POSES = PoseFactory.degrees();

    /** A minimal teleop: records when each hook ran. */
    public static class SampleTeleOp extends CorbelsTeleOp {
        public final List<String> calls = new ArrayList<>();

        @Override public void shadowLocalizers() { calls.add("shadowLocalizers"); }

        @Override public void init() { initBefore(); initAfter(); }

        @Override public void loop() {
            loopBefore();
            calls.add("drive");
            Tracker.publish("teleop/loops", Tracker.loopCount());
            loopAfter();
        }
    }

    /** A minimal auto. */
    public static class SampleAutoOpMode extends CorbelsAuto {
        public final List<String> calls = new ArrayList<>();
        public boolean ran;

        @Override public Pose startPose() {
            calls.add("startPose");
            return POSES.of(24, 24, 45);
        }

        Command routine() {
            calls.add("routine");
            return Commands.instant(() -> ran = true);
        }

        @Override public void start() {
            startBefore();
            Scheduler.schedule(routine());
            startAfter();
        }

        @Override public void shadowLocalizers() { calls.add("shadowLocalizers"); }

        @Override public void init() { initBefore(); initAfter(); }

        @Override public void loop() { loopBefore(); loopAfter(); }
    }

    /** An auto whose start() schedules nothing. */
    public static class AutoThatSchedulesNothing extends CorbelsAuto {
        public boolean ran;

        @Override public Pose startPose() { return POSES.of(24, 24, 45); }

        Command routine() { return Commands.instant(() -> ran = true); }

        @Override public void init() { initBefore(); initAfter(); }

        @Override public void loop() { loopBefore(); loopAfter(); }
    }

    /** An auto that runs whatever it was last given to select, as a menu auto does. */
    public static class AutoThatSelects extends CorbelsAuto {
        @Override public void init() { initBefore(); initAfter(); }

        @Override public void start() {
            startBefore();
            Scheduler.schedule(selectedAuto.command());
            startAfter();
        }

        @Override public void loop() { loopBefore(); loopAfter(); }
    }

    @Before
    public void startAsTheAppDoes() {
        OpModeStorage.clear();
    }

    @Test
    public void anAutoStartsAtTheDefaultDoingNothingUntilItSelectsARoutine() {
        for (boolean red : new boolean[]{true, false}) {
            OpModeStorage.isRed = red;
            AutoThatSelects opMode = new AutoThatSelects();
            OpModeHarness h = new OpModeHarness(opMode);
            h.init();
            assertSamePose("the default, before a routine is selected",
                    OpModeStorage.defaultStartPose(red), h.robot.follower.pose());
            assertSame(Command.NOOP, opMode.selectedAuto.command());
            h.stop();
            Scheduler.reset();
        }
    }

    @Test
    public void selectingARoutineMovesTheRobotToItsStartAndPlayRunsItsCommand() {
        AutoThatSelects opMode = new AutoThatSelects();
        OpModeHarness h = new OpModeHarness(opMode);
        h.init();
        h.initLoop();
        boolean[] ran = {false};
        Pose start = POSES.of(30, 40, 0);
        AutoRoutine routine = new AutoRoutine(start, Commands.instant(() -> ran[0] = true));
        opMode.select(routine);
        assertSamePose("moved during INIT", start, h.robot.follower.pose());

        h.start();
        assertSamePose("and still there at PLAY", start, h.robot.follower.pose());
        h.loop();
        assertTrue("the selected routine's command ran", ran[0]);
        h.stop();
    }

    @Test
    public void aTeleOpOutsideTheLessonsStartsAtTheDefaultUntilAnAutoHasRun() {
        for (boolean red : new boolean[]{true, false}) {
            OpModeStorage.isRed = red;
            OpModeHarness h = new OpModeHarness(new SampleTeleOp());
            h.init();
            assertSamePose("no auto run, " + (red ? "red" : "blue"),
                    OpModeStorage.defaultStartPose(red), h.robot.follower.pose());
            h.stop();
        }

        Pose autoEnd = POSES.of(30, 40, 0);
        OpModeStorage.autonomousEndPose = autoEnd;
        OpModeStorage.autoHasRun = true;
        OpModeHarness h = new OpModeHarness(new SampleTeleOp());
        h.init();
        assertSamePose("after an auto, where it ended", autoEnd, h.robot.follower.pose());
        h.stop();
    }

    @Test
    public void aLessonsTeleOpStartsAtTheLessonsDefaultWithNoAutoRun() {
        Pose lessonsDefault = POSES.of(72, OpModeStorage.botStartYIn, 90);
        for (boolean red : new boolean[]{true, false}) {
            OpModeStorage.isRed = red;
            OpModeHarness h = new OpModeHarness(new L040TankOpMode());
            h.init();
            assertSamePose("no auto run, " + (red ? "red" : "blue"),
                    lessonsDefault, h.robot.follower.pose());
            h.stop();
        }
    }

    private static void assertSamePose(String what, Pose expected, Pose pose) {
        assertEquals(what + ": x", expected.x(), pose.x(), 1e-6);
        assertEquals(what + ": y", expected.y(), pose.y(), 1e-6);
        double turn = pose.heading() - expected.heading();
        assertEquals(what + ": heading", 0.0, Math.atan2(Math.sin(turn), Math.cos(turn)), 1e-6);
    }

    @Test
    public void anAutoRunsOnlyWhatItsOwnStartSchedules() {
        AutoThatSchedulesNothing opMode = new AutoThatSchedulesNothing();
        OpModeHarness h = new OpModeHarness(opMode);
        h.init();
        h.start();
        h.loop();
        assertFalse("nothing ran that start() did not schedule", opMode.ran);
        h.stop();
    }

    @Test
    public void aTeleOpGetsHardwareAtInitAndItsHooksInOrder() {
        SampleTeleOp opMode = new SampleTeleOp();
        OpModeHarness h = new OpModeHarness(opMode);

        h.init();
        assertNotNull("hardware resolved at init", opMode.hardware);
        assertEquals("no hooks yet", 0, opMode.calls.size());

        h.start();
        assertEquals("the shadow localizers at start",
                "[shadowLocalizers]", opMode.calls.toString());

        h.loop();
        assertTrue(opMode.calls.contains("drive"));
        assertEquals("one loop finished", 1, Tracker.loopCount());
        assertEquals("and none had finished while its body ran",
                0.0, (Double) Tracker.values().get("teleop/loops"), 1e-9);

        h.stop();
        assertEquals("stopped", 0.0, h.forward(), 1e-9);
    }

    @Test
    public void anAutoGetsTheSameTreatmentPlusItsStartPoseAndRunsWhatItsStartSchedules() {
        SampleAutoOpMode opMode = new SampleAutoOpMode();
        OpModeHarness h = new OpModeHarness(opMode);

        h.init();
        assertNotNull("hardware resolved at init, for autos too", opMode.hardware);
        assertTrue("the start pose is set during init", opMode.calls.contains("startPose"));
        assertEquals("placed where it said", 24.0, h.robot.follower.pose().x(), 1e-6);

        h.start();
        assertTrue(opMode.calls.contains("routine"));
        assertTrue(opMode.calls.contains("shadowLocalizers"));

        h.loop();
        assertTrue("the scheduler ran the routine", opMode.ran);
        assertEquals(1, Tracker.loopCount());

        h.stop();
        assertEquals(0.0, h.forward(), 1e-9);
    }

    @Test
    public void onlyAnAutoThatStopsSaysAnAutoHasRun() {
        OpModeHarness teleop = new OpModeHarness(new SampleTeleOp());
        teleop.init();
        teleop.start();
        teleop.stop();
        assertFalse("a teleop does not", OpModeStorage.autoHasRun);

        OpModeHarness auto = new OpModeHarness(new SampleAutoOpMode());
        auto.init();
        auto.start();
        assertFalse("an auto still running does not", OpModeStorage.autoHasRun);
        auto.stop();
        assertTrue("an auto stopped does", OpModeStorage.autoHasRun);
    }

    @Test
    public void bothResolveTheHardwareExactlyOnce() {
        OpModeHarness teleop = new OpModeHarness(new SampleTeleOp());
        teleop.init();
        teleop.start();
        teleop.loops(5, 0);
        assertEquals("once, at init", 1, teleop.lookups);

        OpModeHarness auto = new OpModeHarness(new SampleAutoOpMode());
        auto.init();
        auto.start();
        auto.loops(5, 0);
        assertEquals("and the same for an auto", 1, auto.lookups);
    }
}
