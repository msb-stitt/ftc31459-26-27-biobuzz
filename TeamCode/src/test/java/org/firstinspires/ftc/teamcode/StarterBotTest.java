package org.firstinspires.ftc.teamcode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.base.OpModeHarness;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;
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
    }

    @Test
    public void theDirectionsAreGoBildas() {
        assertEquals(DcMotorSimple.Direction.REVERSE, rightServo.direction);
        assertEquals(null, leftServo.direction);
        assertEquals(DcMotorSimple.Direction.REVERSE, windmill.direction);
    }

    @Test
    public void theLauncherFeedsOnlyAboveItsSpeed() {
        Command fire = launcher.fire();
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

        Scheduler.cancel(fire);
        assertEquals("let go, the motor stops", 0, launcherMotor.targetVelocity, 0);
        assertEquals(0, windmill.power, 0);
        assertFalse(launcher.feeding());
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
    public void theTeleOpBindsTheTriggersAndTheBumper() {
        StarterBotTeleOp.intakes = map -> intake;
        StarterBotTeleOp.launchers = map -> launcher;
        OpModeHarness h = new OpModeHarness(new StarterBotTeleOp());
        h.init();
        h.start();

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
}
