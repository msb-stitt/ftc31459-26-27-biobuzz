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
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.Intake;
import org.firstinspires.ftc.teamcode.Launcher;
import org.firstinspires.ftc.teamcode.OpModeStorage;
import org.firstinspires.ftc.teamcode.base.OpModeHarness;
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
 * field and runs the intake and launcher from gamepad 1, and Shoot Then Intake
 * does its steps in order, for as long as each is meant to take, on the
 * simulator's clock.
 */
public class ExampleTest {

    /** A motor or servo that records what was written, and reports a velocity a test sets. */
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
        Scheduler.reset();
        intake = new Intake(intakeMotor.as(DcMotor.class), leftServo.as(CRServo.class),
                rightServo.as(CRServo.class));
        launcher = new Launcher(launcherMotor.as(DcMotorEx.class), windmill.as(CRServo.class));
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
        OpModeStorage.isRed = true;
        OpModeHarness.restoreFactories();
        Scheduler.reset();
    }

    @Test
    public void theAutoHandsOverToExampleTeleOp() {
        assertEquals(ExampleTeleOp.class.getAnnotation(TeleOp.class).name(),
                ExampleAuto.class.getAnnotation(Autonomous.class).preselectTeleOp());
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
        launcherMotor.velocity = 1210;
        h.loop();
        assertEquals(1250, launcherMotor.targetVelocity, 0);
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
        assertAt("shoots from 6 in forward", 72.0, 16.5, shootingStarts.pose);
        for (Sample s : between(run, shootingStarts, shootingEnds)) {
            assertAt("held while shooting, at " + s.seconds + " s", 72.0, 16.5, s.pose);
            assertEquals("fed, once fast enough", 1, s.windmill, 0);
            assertEquals("the intake boosted while it feeds", Launcher.INTAKE_BOOST,
                    s.intakeSpeed, EPS);
        }
        assertEquals("the windmill stops with the launcher", 0, shootingEnds.windmill, 0);
        assertEquals("and the intake", 0, shootingEnds.intakeSpeed, 0);

        Sample strafeStarts = firstAfter(run, shootingEnds, s -> s.following);
        assertEquals("waits 3 s", 3.0, strafeStarts.seconds - shootingEnds.seconds, 0.05);
        for (Sample s : between(run, shootingEnds, strafeStarts)) {
            assertAt("held while waiting, at " + s.seconds + " s", 72.0, 16.5, s.pose);
            assertEquals("nothing runs while it waits", 0, s.intakeSpeed, 0);
            assertEquals(0, s.launcherTarget, 0);
        }

        Sample intakeStarts = firstAfter(run, strafeStarts, s -> s.intakeSpeed > 0);
        Sample intakeEnds = firstAfter(run, intakeStarts, s -> s.intakeSpeed == 0);
        assertAt("strafed 24 in to the robot's left, -x", 48.0, 16.5, intakeStarts.pose);
        assertEquals("intakes for 3 s", 3.0, intakeEnds.seconds - intakeStarts.seconds, 0.05);
        for (Sample s : between(run, intakeStarts, intakeEnds)) {
            assertEquals("full speed in", Intake.FULL_SPEED_IN, s.intakeSpeed, EPS);
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
        assertAt("shoots from 6 in forward of blue's wall", 72.0, 127.5, shootingStarts.pose);
        Sample intakeStarts = firstAfter(run, shootingStarts,
                s -> s.intakeSpeed == Intake.FULL_SPEED_IN);
        assertAt("strafed to the robot's left, +x", 96.0, 127.5, intakeStarts.pose);
        assertEquals("blue, handed to the teleop", false, OpModeStorage.isRed);
    }

    /**
     * Example Auto with Shoot Then Intake chosen, run on the simulator's clock
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
        pressDuringInit(h, pad -> pad.dpad_left = true);   // wraps to the last, Shoot Then Intake
        pressDuringInit(h, pad -> pad.a = true);
        String summary = (red ? "R" : "B") + "-SI";
        assertTrue(h.driverStation.text(), h.driverStation.shows(summary));

        launcherMotor.velocity = 1210;
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
