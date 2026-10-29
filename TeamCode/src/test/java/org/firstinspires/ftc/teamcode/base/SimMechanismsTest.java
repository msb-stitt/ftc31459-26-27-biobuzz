package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pedropathing.ivy.Scheduler;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.Bot88888Auto;
import org.firstinspires.ftc.teamcode.StarterBotTeleOp;
import org.firstinspires.ftc.teamcode.example.ExampleAuto;
import org.firstinspires.ftc.teamcode.example.ExampleTeleOp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/** simRun's stand-in intake and launcher reach the OpModes that take them. */
public class SimMechanismsTest {

    private static final Class<?>[] TAKERS = {
            ExampleAuto.class, ExampleTeleOp.class, StarterBotTeleOp.class};

    /** Each factory as it was before a test, to put back after it. */
    private final Map<Field, Object> before = new HashMap<>();

    @Before
    public void setUp() throws IllegalAccessException {
        Scheduler.reset();
        for (Class<?> taker : TAKERS) {
            for (Field field : taker.getDeclaredFields()) {
                if (field.getName().equals("intakes") || field.getName().equals("launchers")) {
                    field.setAccessible(true);
                    before.put(field, field.get(null));
                }
            }
        }
    }

    @After
    public void tearDown() throws IllegalAccessException {
        for (Map.Entry<Field, Object> each : before.entrySet()) {
            each.getKey().set(null, each.getValue());
        }
        OpModeHarness.restoreFactories();
        Scheduler.reset();
    }

    @Test
    public void anOpModeThatTakesAnIntakeAndALauncherIsGivenBoth() throws IllegalAccessException {
        assertEquals(2, SimMechanisms.install(ExampleAuto.class));
        assertEquals(2, SimMechanisms.install(ExampleTeleOp.class));
        assertEquals(2, SimMechanisms.install(StarterBotTeleOp.class));
        assertEquals("88888 Auto takes neither", 0, SimMechanisms.install(Bot88888Auto.class));
    }

    @Test
    public void exampleTeleOpRunsAndItsLauncherFeedsInTheSimulator() throws IllegalAccessException {
        // Without the stand-ins, INIT looks them up in a HardwareMap the simulator has not got.
        SimMechanisms.install(ExampleTeleOp.class);
        OpModeHarness h = new OpModeHarness(new ExampleTeleOp());
        h.init();
        h.initLoop();
        h.start();
        h.gamepad1.right_bumper = true;
        h.loop();
        h.loop();
        assertEquals(1250.0, (Double) Tracker.values().get("launcher/target_tps"), 0);
        assertEquals("the stand-in reports the speed it was given", 1250.0,
                (Double) Tracker.values().get("launcher/measured_tps"), 0);
        assertEquals("so the windmill feeds", true, Tracker.values().get("launcher/feeding"));
        // The intake's command runs before the launcher's, so it sees the feeding a loop later.
        h.loop();
        assertEquals("and the intake gets its boost", 0.5,
                (Double) Tracker.values().get("intake/speed"), 1e-9);
        h.gamepad1.copy(new Gamepad());
        h.loop();
        assertEquals(0.0, (Double) Tracker.values().get("launcher/target_tps"), 0);
        assertTrue(h.driverStation.text(), h.driverStation.shows("R-A"));
        h.stop();
    }
}
