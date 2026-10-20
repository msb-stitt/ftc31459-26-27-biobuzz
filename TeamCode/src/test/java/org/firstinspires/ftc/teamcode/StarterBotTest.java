package org.firstinspires.ftc.teamcode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.base.OpModeHarness;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.function.Function;

/** goBILDA's StarterBot behavior, kept: the launcher feeds only when fast enough. */
public class StarterBotTest {

    /** A motor or servo that records what was written, and reports a velocity a test sets. */
    static final class Fake {
        double power;
        double targetVelocity;
        double velocity;
        DcMotorSimple.Direction direction;

        <T> T as(Class<T> type) {
            return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                    (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "setPower": power = (Double) args[0]; return null;
                            case "setVelocity": targetVelocity = (Double) args[0]; return null;
                            case "getVelocity": return velocity;
                            case "setDirection":
                                direction = (DcMotorSimple.Direction) args[0];
                                return null;
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
    }

    @After
    public void tearDown() {
        StarterBotTeleOp.intakes = Intake::from;
        StarterBotTeleOp.launchers = Launcher::from;
        LauncherCheck.launchers = Launcher::from;
    }

    @Test
    public void theDirectionsAreGoBildas() {
        assertEquals(DcMotorSimple.Direction.REVERSE, rightServo.direction);
        assertEquals(null, leftServo.direction);
        assertEquals(DcMotorSimple.Direction.REVERSE, windmill.direction);
    }

    @Test
    public void theLauncherFeedsOnlyAboveItsSpeedWhileTheButtonIsHeld() {
        boolean[] held = {true};
        Command fire = launcher.fireWhile(() -> held[0]);
        Scheduler.schedule(fire);

        launcherMotor.velocity = 1100;
        Scheduler.execute();
        assertEquals(1250, launcherMotor.targetVelocity, 0);
        assertEquals("too slow to feed", 0, windmill.power, 0);
        assertFalse(launcher.feeding());

        launcherMotor.velocity = 1210;
        Scheduler.execute();
        assertEquals("fast enough", 1, windmill.power, 0);
        assertTrue(launcher.feeding());

        held[0] = false;
        Scheduler.execute();
        assertEquals("let go, the motor stops", 0, launcherMotor.targetVelocity, 0);
        assertEquals(0, windmill.power, 0);
        assertFalse(launcher.feeding());
        assertTrue("and the command keeps running", Scheduler.isRunning(fire));
    }

    @Test
    public void theIntakeRunsAllThreeAtOnePowerAndStopsWhenCancelled() {
        double[] asked = {0.4};
        Command run = intake.run(() -> asked[0]);
        Scheduler.schedule(run);
        Scheduler.execute();
        assertEquals(0.4, intakeMotor.power, 1e-9);
        assertEquals(0.4, leftServo.power, 1e-9);
        assertEquals(0.4, rightServo.power, 1e-9);

        asked[0] = 1.5;
        Scheduler.execute();
        assertEquals("clipped", 1, intakeMotor.power, 0);

        Scheduler.cancel(run);
        assertEquals(0, intakeMotor.power, 0);
        assertEquals(0, rightServo.power, 0);
    }

    @Test
    public void theTeleOpRunsTheSticksTheTriggersAndTheBumper() {
        StarterBotTeleOp.intakes = map -> intake;
        StarterBotTeleOp.launchers = map -> launcher;
        OpModeHarness h = new OpModeHarness(new StarterBotTeleOp());
        h.init();
        h.start();

        h.gamepad1.left_stick_y = -1.0f;
        h.loop();
        assertEquals("pushed away, forward at full power", 1.0, h.wheelsForward(), 1e-3);

        h.gamepad1.left_stick_y = 0.0f;
        h.gamepad1.right_trigger = 0.75f;
        h.gamepad1.left_trigger = 0.25f;
        h.loop();
        assertEquals("right less left", 0.5, intakeMotor.power, 1e-6);

        h.gamepad1.right_bumper = true;
        launcherMotor.velocity = 1210;
        h.loop();
        assertEquals(1250, launcherMotor.targetVelocity, 0);
        h.loop();
        assertEquals("feeding adds 0.5", 1.0, intakeMotor.power, 1e-6);

        h.gamepad1.right_bumper = false;
        h.loop();
        assertEquals("let go, the launcher stops", 0, launcherMotor.targetVelocity, 0);
        assertEquals(0, windmill.power, 0);

        h.stop();
    }

    @Test
    public void aCommandThatNeedsTheDrivetrainSetsTheSticksAsideUntilItEnds() {
        StarterBotTeleOp.intakes = map -> intake;
        StarterBotTeleOp.launchers = map -> launcher;
        StarterBotTeleOp teleOp = new StarterBotTeleOp();
        OpModeHarness h = new OpModeHarness(teleOp);
        h.init();
        h.start();
        h.gamepad1.left_stick_y = -1.0f;
        h.loop();
        assertTrue(Scheduler.isRunning(teleOp.driveFromTheSticks));

        int[] loopsRun = {0};
        Command strafeForTwoLoops = Command.build()
                .setExecute(() -> {
                    loopsRun[0]++;
                    teleOp.drivetrain.sticks(0.0, 1.0, 0.0);
                })
                .setDone(() -> loopsRun[0] >= 2)
                .requiring(teleOp.drivetrain);
        Scheduler.schedule(strafeForTwoLoops);
        assertFalse("set aside", Scheduler.isRunning(teleOp.driveFromTheSticks));
        h.loop();
        assertEquals("the replacing command drives", 1.0, h.wheelsStrafe(), 1e-3);
        assertEquals(0.0, h.wheelsForward(), 1e-3);
        assertTrue("the intake and launcher keep running",
                Scheduler.isRunning(teleOp.runTheIntakeFromTheTriggers)
                        && Scheduler.isRunning(teleOp.fireTheLauncherFromTheBumper));

        h.loop();
        h.loop();
        assertTrue("back once it ends", Scheduler.isRunning(teleOp.driveFromTheSticks));
        assertEquals("and the sticks drive again", 1.0, h.wheelsForward(), 1e-3);
        h.stop();
    }

    @Test
    public void theTeleOpLogsEveryInputAndOutputOnce() {
        StarterBotTeleOp.intakes = map -> intake;
        StarterBotTeleOp.launchers = map -> launcher;
        OpModeHarness h = new OpModeHarness(new StarterBotTeleOp());
        h.init();
        h.start();
        h.gamepad1.left_stick_x = 0.25f;
        h.gamepad1.left_stick_y = -0.5f;
        h.gamepad1.right_stick_x = 0.125f;
        h.gamepad1.right_trigger = 0.75f;
        h.gamepad1.right_bumper = true;
        h.loop();

        Map<String, Object> logged = Tracker.values();
        assertEquals(0.25, (Double) logged.get("stick/leftX"), 1e-6);
        assertEquals(-0.5, (Double) logged.get("stick/leftY"), 1e-6);
        assertEquals(0.125, (Double) logged.get("stick/rightX"), 1e-6);
        assertEquals(0.75, (Double) logged.get("trigger/right"), 1e-6);
        assertEquals(0.0, (Double) logged.get("trigger/left"), 1e-6);
        assertEquals(true, logged.get("button/rightBumper"));
        assertEquals(0.5, (Double) logged.get("drive/forward"), 1e-6);
        assertEquals(-0.25, (Double) logged.get("drive/strafeLeft"), 1e-6);
        assertEquals(-0.125, (Double) logged.get("drive/turnCounterclockwise"), 1e-6);
        assertEquals(0.75, (Double) logged.get("intake/power"), 1e-6);
        assertEquals(1250.0, (Double) logged.get("launcher/target_tps"), 0);
        String[] wheels = {"frontLeft", "frontRight", "backLeft", "backRight"};
        String[] motors = {OpModeHarness.FRONT_LEFT, OpModeHarness.FRONT_RIGHT,
                OpModeHarness.BACK_LEFT, OpModeHarness.BACK_RIGHT};
        for (int i = 0; i < wheels.length; i++) {
            double logPower = (Double) logged.get("wheel/" + wheels[i] + "/power");
            assertEquals(wheels[i], h.motors.get(motors[i]).power, logPower, 1e-6);
        }
        for (String key : logged.keySet()) {
            assertFalse("Pedro's pose is logged by Tracker.endLoop alone: " + key,
                    key.toLowerCase().contains("pose"));
        }
        h.stop();
    }

    @Test
    public void theLauncherCheckStartsAtAFifthAndStepsByATenthFromATenthToAll() {
        LauncherCheck.launchers = map -> launcher;
        LauncherCheck check = new LauncherCheck();
        OpModeHarness h = new OpModeHarness(check);
        h.init();
        h.start();
        assertEquals(0.2, check.fractionOfTarget(), 1e-9);

        h.gamepad1.dpad_up = true;
        h.loop();
        h.loop();
        h.gamepad1.dpad_up = false;
        h.loop();
        assertEquals("one press, held for two loops, one step", 0.3, check.fractionOfTarget(), 1e-9);

        for (int i = 0; i < 12; i++) {
            h.gamepad1.dpad_up = true;
            h.loop();
            h.gamepad1.dpad_up = false;
            h.loop();
        }
        assertEquals("no further than all", 1.0, check.fractionOfTarget(), 1e-9);

        for (int i = 0; i < 12; i++) {
            h.gamepad1.dpad_down = true;
            h.loop();
            h.gamepad1.dpad_down = false;
            h.loop();
        }
        assertEquals("no slower than a tenth", 0.1, check.fractionOfTarget(), 1e-9);
        assertEquals(false, Tracker.values().get("button/dpadDown"));
        h.stop();
    }

    @Test
    public void theLauncherCheckSpinsAtItsFractionAndNeverFeeds() {
        LauncherCheck.launchers = map -> launcher;
        OpModeHarness h = new OpModeHarness(new LauncherCheck());
        h.init();
        h.start();

        h.gamepad1.right_bumper = true;
        launcherMotor.velocity = 1300;
        h.loop();
        assertEquals("a fifth of 1250", 250, launcherMotor.targetVelocity, 1e-9);
        assertEquals("fast, and still not feeding", 0, windmill.power, 0);
        assertFalse(launcher.feeding());
        assertEquals(true, Tracker.values().get("button/rightBumper"));

        h.gamepad1.right_bumper = false;
        h.loop();
        assertEquals("let go, the motor stops", 0, launcherMotor.targetVelocity, 0);
        h.stop();
    }

    @Test
    public void theAutoDrivesForwardThenStrafesAsL130Does() {
        OpModeHarness h = new OpModeHarness(new StarterBotAuto());
        Follower follower = h.robot.follower;
        h.init();
        h.start();
        double drift = 0;
        long deadline = System.nanoTime() + 3_000_000_000L;
        while (follower.pose().y() < 66 && System.nanoTime() < deadline) {
            h.loop();
            drift = Math.max(drift, Math.abs(follower.pose().x() - 72));
            OpModeHarness.sleep(5);
        }
        // Between two legs the follower holds for one loop with no path, so the
        // routine is done only once it has stayed that way for a few loops.
        boolean followed = false;
        int loopsIdle = 0;
        deadline = System.nanoTime() + 6_000_000_000L;
        while (loopsIdle < 3 && System.nanoTime() < deadline) {
            h.loop();
            boolean following = follower.mode() == Follower.Mode.FOLLOW;
            boolean hasAPath = follower.currentPath() != null;
            if (following) {
                followed = true;
            }
            if (followed && !following && !hasAPath) {
                loopsIdle++;
            } else {
                loopsIdle = 0;
            }
            OpModeHarness.sleep(5);
        }
        assertTrue("the robot never started following a path", followed);
        assertTrue(Tracker.values().containsKey("wheel/frontLeft/power"));
        h.stop();
        assertEquals("drove straight up to the corner first", 0.0, drift, 2.0);
        Pose end = follower.pose();
        assertEquals("strafed 24 inches in x", 96.0, end.x(), 2.0);
        assertEquals("and stayed on the line it drove up", 72.0, end.y(), 2.0);
        assertEquals("never turned", 90.0, Math.toDegrees(end.heading()), 15.0);
    }
}
