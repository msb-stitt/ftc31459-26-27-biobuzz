package org.firstinspires.ftc.teamcode.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.teamcode.Intake;
import org.firstinspires.ftc.teamcode.Launcher;
import org.firstinspires.ftc.teamcode.OpModeStorage;
import org.firstinspires.ftc.teamcode.base.OpModeHarness;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Example Auto and Example TeleOp, on fake mechanisms: the TeleOp drives the
 * field and runs the intake and launcher from gamepad 1, and Move Shot
 * Strafe In does its steps in order, for as long as each is meant to take, on the
 * simulator's clock.
 */
public class ExampleTest {

    /**
     * A motor or servo that records what was written, and reports a velocity a
     * test sets, or a battery that reports the nominal volts.
     */
    static final class Fake {
        double speed;
        double targetVelocity;
        double velocity;

        <T> T as(Class<T> type) {
            return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                    (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "setPower": speed = (Double) args[0]; return null;
                            case "setVelocity": targetVelocity = (Double) args[0]; return null;
                            case "getVelocity": return velocity;
                            case "getVoltage": return Constants.NOMINAL_BATTERY_VOLTAGE;
                            default:
                                Class<?> r = method.getReturnType();
                                if (r == boolean.class) return false;
                                if (r == double.class) return 0.0;
                                if (r == int.class) return 0;
                                return null;
                        }
                    }));
        }
    }

    /** What one loop of the auto left behind. */
    static final class Sample {
        final double seconds;
        final double launcherTarget;
        final double windmill;
        final double intakeSpeed;
        final boolean following;
        final Pose pose;

        Sample(double seconds, double launcherTarget, double windmill, double intakeSpeed,
               boolean following, Pose pose) {
            this.seconds = seconds;
            this.launcherTarget = launcherTarget;
            this.windmill = windmill;
            this.intakeSpeed = intakeSpeed;
            this.following = following;
            this.pose = pose;
        }
    }

    private static final double EPS = 1e-6;
    /** How far the simulated robot may be from a pose it holds, in inches. */
    private static final double HELD_IN = 1.0;

    private final Fake intakeMotor = new Fake();
    private final Fake leftServo = new Fake();
    private final Fake rightServo = new Fake();
    private final Fake launcherMotor = new Fake();
    private final Fake windmill = new Fake();
    private Intake intake;
    private Launcher launcher;

    @Before
    public void setUp() {
        OpModeStorage.clear();
        Scheduler.reset();
        intake = new Intake(intakeMotor.as(DcMotorEx.class), leftServo.as(CRServo.class),
                rightServo.as(CRServo.class));
        launcher = new Launcher(launcherMotor.as(DcMotorEx.class), windmill.as(CRServo.class),
                new Fake().as(VoltageSensor.class));
        ExampleAuto.intakes = map -> intake;
        ExampleAuto.launchers = map -> launcher;
        ExampleTeleOp.intakes = map -> intake;
        ExampleTeleOp.launchers = map -> launcher;
    }

    @After
    public void tearDown() {
        ExampleAuto.intakes = Intake::from;
        ExampleAuto.launchers = Launcher::from;
        ExampleAuto.clock = System::nanoTime;
        ExampleTeleOp.intakes = Intake::from;
        ExampleTeleOp.launchers = Launcher::from;
        OpModeHarness.restoreFactories();
        Scheduler.reset();
    }

    @Test
    public void theAutoHandsOverToExampleTeleOp() {
        assertEquals(ExampleTeleOp.class.getAnnotation(TeleOp.class).name(),
                ExampleAuto.class.getAnnotation(Autonomous.class).preselectTeleOp());
    }

    @Test
    public void theTeleOpsWallStartIsWhereTheAutoStartsOnEachAlliance() {
        for (boolean handedRed : new boolean[]{true, false}) {
            for (boolean chosenRed : new boolean[]{true, false}) {
                String what = (handedRed ? "red" : "blue") + " handed over, "
                        + (chosenRed ? "red" : "blue") + " chosen";
                Pose autoStart = exampleAutoStart(chosenRed);

                OpModeStorage.isRed = handedRed;
                OpModeHarness h = new OpModeHarness(new ExampleTeleOp());
                Follower follower = h.robot.follower;
                h.init();
                h.initLoop();
                if (chosenRed != handedRed) {
                    pressDuringInit(h, pad -> pad.dpad_right = true);   // the other alliance
                    pressDuringInit(h, pad -> pad.a = true);
                }
                pressDuringInit(h, pad -> pad.dpad_down = true);        // the Start row
                pressDuringInit(h, pad -> pad.dpad_right = true);       // Wall
                pressDuringInit(h, pad -> pad.a = true);
                h.start();

                Pose wallStart = follower.pose();
                assertAt(what, autoStart.x(), autoStart.y(), wallStart);
                assertEquals(what + ": heading", autoStart.heading(), wallStart.heading(), 1e-9);
                assertEquals(what, chosenRed, OpModeStorage.isRed);

                // Pushed away from the driver: +x for red, -x for blue.
                h.gamepad1.left_stick_y = -1.0f;
                h.loop();
                double heading = follower.pose().heading();
                double fieldXSpeed = h.wheelsForward() * Math.cos(heading)
                        - h.wheelsStrafe() * Math.sin(heading);
                double awayFromTheDriver = chosenRed ? 1.0 : -1.0;
                assertEquals(what + ": away from the driver", awayFromTheDriver, fieldXSpeed, 1e-3);
                h.stop();
                Scheduler.reset();
            }
        }
    }

    @Test
    public void withNoAutoRunAutoEndIsWhereTheAutoStartsAndIsLoggedBeforePlay() {
        for (boolean red : new boolean[]{true, false}) {
            String what = red ? "red" : "blue";
            Pose autoStart = exampleAutoStart(red);

            OpModeStorage.isRed = red;
            OpModeHarness h = new OpModeHarness(new ExampleTeleOp());
            Follower follower = h.robot.follower;
            h.init();
            h.initLoop();                                       // Auto End, chosen already

            assertSamePose(what + ", before PLAY", autoStart, follower.pose());
            assertEquals(what + ": logged x", autoStart.x(),
                    (Double) Tracker.values().get("Localizer/pinPoint/x_in"), HELD_IN);
            assertEquals(what + ": logged y", autoStart.y(),
                    (Double) Tracker.values().get("Localizer/pinPoint/y_in"), HELD_IN);
            double loggedHeading = Math.toRadians(
                    (Double) Tracker.values().get("Localizer/pinPoint/heading_deg"));
            assertEquals(what + ": logged heading", 0.0,
                    angleBetween(autoStart.heading(), loggedHeading), 1e-6);
            h.start();
            assertSamePose(what + ", after PLAY", autoStart, follower.pose());
            h.stop();
            Scheduler.reset();
        }
    }

    @Test
    public void thePoseFollowsTheMenuAndAutoEndAfterAnAutoIsWhereItEnded() {
        Pose redWall = exampleAutoStart(true);
        Pose blueWall = exampleAutoStart(false);
        Pose autoEnd = PoseFactory.degrees().of(30, 40, 0);
        OpModeStorage.autonomousEndPose = autoEnd;
        OpModeStorage.autoHasRun = true;
        OpModeStorage.isRed = true;

        OpModeHarness h = new OpModeHarness(new ExampleTeleOp());
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
        OpModeHarness h = new OpModeHarness(new ExampleTeleOp());
        Follower follower = h.robot.follower;
        h.init();
        h.initLoop();
        Pose movedTo = PoseFactory.degrees().of(50, 60, 30);
        follower.setPose(movedTo);
        h.initLoop();
        assertSamePose("after a pass with no change", movedTo, follower.pose());
        h.stop();
    }

    /** Where Example Auto starts on an alliance, chosen in its menu. */
    private static Pose exampleAutoStart(boolean red) {
        ExampleAuto auto = new ExampleAuto();
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
    public void eachAutosStartIsSetDuringInitAndFollowsAChangeOfAutoOrAlliance() {
        ExampleAuto auto = new ExampleAuto();
        OpModeHarness h = new OpModeHarness(auto);
        Follower follower = h.robot.follower;
        PoseFactory redPoses = PoseFactory.degrees();
        PoseFactory bluePoses = redPoses.rotateAround(72.0, 72.0, 180.0);
        h.init();
        h.initLoop();
        assertSamePose("Nothing, on red", OpModeStorage.defaultStartPose(true), follower.pose());

        pressDuringInit(h, pad -> pad.dpad_down = true);            // the Auto row
        String[] autos = {"Forward 24", "Forward Then Strafe", "Move Shot Strafe In",
                "Shoot And Park"};
        for (String name : autos) {
            follower.setPose(redPoses.of(50, 60, 30));
            pressDuringInit(h, pad -> pad.dpad_right = true);
            pressDuringInit(h, pad -> pad.a = true);
            assertEquals(name, auto.autoRow.getSelected().getFullName());
            assertSamePose(name + ", on red", redPoses.of(60.0, 10.5, -90.0), follower.pose());
        }

        pressDuringInit(h, pad -> pad.dpad_up = true);              // the Alliance row
        pressDuringInit(h, pad -> pad.dpad_right = true);           // blue
        pressDuringInit(h, pad -> pad.a = true);
        assertSamePose("Shoot And Park, on blue", bluePoses.of(60.0, 10.5, -90.0),
                follower.pose());

        pressDuringInit(h, pad -> pad.dpad_down = true);            // the Auto row
        pressDuringInit(h, pad -> pad.dpad_right = true);           // wraps to Nothing
        pressDuringInit(h, pad -> pad.a = true);
        assertSamePose("Nothing, on blue", OpModeStorage.defaultStartPose(false),
                follower.pose());

        Pose movedTo = redPoses.of(50, 60, 30);
        follower.setPose(movedTo);
        h.initLoop();
        assertSamePose("a pass with no change leaves it", movedTo, follower.pose());
        h.stop();
    }

    @Test
    public void forwardThenStrafeGoesForward24InThenLeft24In() {
        OpModeHarness h = new OpModeHarness(new ExampleAuto());
        h.init();
        h.initLoop();
        pressDuringInit(h, pad -> pad.dpad_down = true);            // the Auto row
        pressDuringInit(h, pad -> pad.dpad_right = true);           // Forward 24
        pressDuringInit(h, pad -> pad.dpad_right = true);           // Forward Then Strafe
        pressDuringInit(h, pad -> pad.a = true);
        assertTrue(h.driverStation.text(), h.driverStation.shows("R-FS"));
        h.start();
        h.loops(1000, 10);
        assertAt("forward 24 in, then left 24 in, on red's side", 36.0, 34.5,
                h.robot.follower.pose());
        h.stop();
    }

    @Test
    public void theTeleOpDrivesTheFieldAndRunsTheIntakeAndLauncher() {
        OpModeHarness h = new OpModeHarness(new ExampleTeleOp());
        Follower follower = h.robot.follower;
        h.init();
        h.initLoop();
        h.start();

        follower.setPose(PoseFactory.degrees().of(0, 0, 90));   // robot faces +y
        h.gamepad1.left_stick_y = -1.0f;                         // away from the red driver, +x
        h.loop();
        assertEquals("away from the driver, to the right of a robot facing +y",
                -1.0, h.wheelsStrafe(), 1e-3);
        assertEquals(0.0, h.wheelsForward(), 1e-3);

        h.gamepad1.left_stick_y = 0.0f;
        h.gamepad1.right_trigger = 0.75f;
        h.gamepad1.left_trigger = 0.25f;
        h.loop();
        assertEquals("right less left", 0.5, intakeMotor.speed, EPS);

        h.gamepad1.right_bumper = true;
        launcherMotor.velocity = Constants.launcherFeedAboveTicksPerSecond + 10;
        h.loop();
        assertEquals(Constants.launcherTargetTicksPerSecond, launcherMotor.targetVelocity, 0);
        h.loop();
        assertEquals("fast enough, it feeds", 1, windmill.speed, 0);
        assertEquals("feeding adds 0.5", 1.0, intakeMotor.speed, EPS);

        h.gamepad1.right_bumper = false;
        h.loop();
        assertEquals("let go, the launcher stops", 0, launcherMotor.targetVelocity, 0);
        assertEquals(0, windmill.speed, 0);
        h.stop();
    }

    @Test
    public void shootThenIntakeDoesEachStepInOrderForItsTime() {
        List<Sample> run = runShootThenIntake(true);

        // Forward 6 inches from red's start, (72, 10.5), then shoot.
        Sample shootingStarts = first(run, s -> s.launcherTarget > 0);
        Sample shootingEnds = firstAfter(run, shootingStarts, s -> s.launcherTarget == 0);
        assertEquals("shoots for 10 s", 10.0, shootingEnds.seconds - shootingStarts.seconds, 0.05);
        assertAt("shoots from 6 in forward", 60.0, 16.5, shootingStarts.pose);
        for (Sample s : between(run, shootingStarts, shootingEnds)) {
            assertAt("held while shooting, at " + s.seconds + " s", 60.0, 16.5, s.pose);
            assertEquals("fed, once fast enough", 1, s.windmill, 0);
            assertEquals("the intake boosted while it feeds", Launcher.INTAKE_BOOST_SPEED_FRACTION,
                    s.intakeSpeed, EPS);
        }
        assertEquals("the windmill stops with the launcher", 0, shootingEnds.windmill, 0);
        assertEquals("and the intake", 0, shootingEnds.intakeSpeed, 0);

        Sample strafeStarts = firstAfter(run, shootingEnds, s -> s.following);
        assertEquals("waits 3 s", 3.0, strafeStarts.seconds - shootingEnds.seconds, 0.05);
        for (Sample s : between(run, shootingEnds, strafeStarts)) {
            assertAt("held while waiting, at " + s.seconds + " s", 60.0, 16.5, s.pose);
            assertEquals("nothing runs while it waits", 0, s.intakeSpeed, 0);
            assertEquals(0, s.launcherTarget, 0);
        }

        Sample intakeStarts = firstAfter(run, strafeStarts, s -> s.intakeSpeed > 0);
        Sample intakeEnds = firstAfter(run, intakeStarts, s -> s.intakeSpeed == 0);
        assertAt("strafed 12 in to the robot's left, -x", 48.0, 16.5, intakeStarts.pose);
        assertEquals("intakes for 3 s", 3.0, intakeEnds.seconds - intakeStarts.seconds, 0.05);
        for (Sample s : between(run, intakeStarts, intakeEnds)) {
            assertEquals("full speed in", Intake.FULL_IN_SPEED_FRACTION, s.intakeSpeed, EPS);
            assertAt("held while intaking, at " + s.seconds + " s", 48.0, 16.5, s.pose);
            assertEquals("the launcher stays stopped", 0, s.launcherTarget, 0);
        }
        Sample last = run.get(run.size() - 1);
        assertEquals("and stays stopped", 0, last.intakeSpeed, 0);
    }

    @Test
    public void shootThenIntakeForBlueIsRedsTurnedAboutTheMiddle() {
        List<Sample> run = runShootThenIntake(false);

        // Red's (72, 16.5) and (48, 16.5), turned a half-turn about (72, 72).
        Sample shootingStarts = first(run, s -> s.launcherTarget > 0);
        assertAt("shoots from 6 in forward of blue's wall", 84.0, 127.5, shootingStarts.pose);
        Sample intakeStarts = firstAfter(run, shootingStarts,
                s -> s.intakeSpeed == Intake.FULL_IN_SPEED_FRACTION);
        assertAt("strafed to the robot's left, +x", 96.0, 127.5, intakeStarts.pose);
        assertEquals("blue, handed to the teleop", false, OpModeStorage.isRed);
    }

    /**
     * Example Auto with Move Shot Strafe In chosen, run on the simulator's clock
     * for 25 s, 10 ms a loop, the launcher fast enough to feed, and each loop
     * sampled.
     */
    private List<Sample> runShootThenIntake(boolean red) {
        OpModeHarness h = new OpModeHarness(new ExampleAuto());
        ExampleAuto.clock = h.clock::nanos;
        Follower follower = h.robot.follower;
        h.init();
        h.initLoop();
        if (!red) {
            pressDuringInit(h, pad -> pad.dpad_right = true);
            pressDuringInit(h, pad -> pad.a = true);
        }
        pressDuringInit(h, pad -> pad.dpad_down = true);
        pressDuringInit(h, pad -> pad.dpad_left = true);   // wraps to the last, Shoot And Park
        pressDuringInit(h, pad -> pad.dpad_left = true);   // Move Shot Strafe In
        pressDuringInit(h, pad -> pad.a = true);
        String summary = (red ? "R" : "B") + "-MVSI";
        assertTrue(h.driverStation.text(), h.driverStation.shows(summary));

        launcherMotor.velocity = Constants.launcherFeedAboveTicksPerSecond + 10;
        h.start();
        List<Sample> run = new ArrayList<>();
        while (h.clock.nanos() < 25_000_000_000L) {
            h.loop(10);
            run.add(new Sample(h.clock.nanos() / 1e9, launcherMotor.targetVelocity, windmill.speed,
                    intakeMotor.speed, follower.mode() == Follower.Mode.FOLLOW, follower.pose()));
        }
        h.stop();
        return run;
    }

    private static Sample first(List<Sample> run, Predicate<Sample> test) {
        for (Sample s : run) {
            if (test.test(s)) return s;
        }
        throw new AssertionError("never happened");
    }

    private static Sample firstAfter(List<Sample> run, Sample after,
                                     Predicate<Sample> test) {
        return first(run.subList(run.indexOf(after) + 1, run.size()), test);
    }

    /** The samples after {@code from} and before {@code to}. */
    private static List<Sample> between(List<Sample> run, Sample from, Sample to) {
        return run.subList(run.indexOf(from) + 1, run.indexOf(to));
    }

    private static void assertAt(String what, double x, double y, Pose pose) {
        assertEquals(what + ": x", x, pose.x(), HELD_IN);
        assertEquals(what + ": y", y, pose.y(), HELD_IN);
    }

    private static void assertSamePose(String what, Pose expected, Pose pose) {
        assertAt(what, expected.x(), expected.y(), pose);
        assertEquals(what + ": heading", 0.0, angleBetween(expected.heading(), pose.heading()),
                1e-6);
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
}
