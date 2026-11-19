package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.pedropathing.ivy.Scheduler;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.Bot88888Auto;
import org.firstinspires.ftc.teamcode.OpModeStorage;
import org.firstinspires.ftc.teamcode.StarterBotTeleOp;
import org.firstinspires.ftc.teamcode.camera.CameraCalibration;
import org.firstinspires.ftc.teamcode.example.ExampleAuto;
import org.firstinspires.ftc.teamcode.example.ExampleTeleOp;
import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.LongSupplier;

/** simRun's stand-in intake and launcher reach the OpModes that take them. */
public class SimMechanismsTest {

    private static final Class<?>[] TAKERS = {
            ExampleAuto.class, ExampleTeleOp.class, StarterBotTeleOp.class,
            CameraCalibration.class};

    /** Each factory as it was before a test, to put back after it. */
    private final Map<Field, Object> before = new HashMap<>();

    @Before
    public void setUp() throws IllegalAccessException {
        OpModeStorage.clear();
        Scheduler.reset();
        for (Class<?> taker : TAKERS) {
            for (Field field : taker.getDeclaredFields()) {
                if (field.getName().equals("intakes") || field.getName().equals("launchers")
                        || field.getName().equals("cameras") || field.getName().equals("clock")) {
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
    public void cameraCalibrationIsGivenACameraThatSeesFromTheSimulatedRobot()
            throws ReflectiveOperationException {
        OpModeHarness h = new OpModeHarness(new CameraCalibration());
        assertEquals(1, SimMechanisms.installCamera(h));
        // Its waits on the harness's clock, which here runs faster than the wall clock.
        Field clock = CameraCalibration.class.getDeclaredField("clock");
        clock.setAccessible(true);
        clock.set(null, (LongSupplier) h.clock::nanos);
        h.init();
        h.initLoop();
        h.start();
        // Out from the wall and to the first stop, then into its look.
        for (int i = 0; i < 1000 && stop() < 0; i++) {
            h.loop(10);
        }
        h.loops(20, 0);
        assertTrue("the tags are seen at the first stop",
                Tracker.values().containsKey("camera/robot/x_in"));
        h.stop();
        assertEquals("88888 Auto takes none", 0,
                SimMechanisms.installCamera(new OpModeHarness(new Bot88888Auto())));
    }

    private static double stop() {
        return (Double) Tracker.values().getOrDefault("camera/stop", -1.0);
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
        assertEquals(Constants.launcherTargetTicksPerSecond,
                (Double) Tracker.values().get("launcher/target_tps"), 0);
        assertEquals("the stand-in reports the speed it was given",
                Constants.launcherTargetTicksPerSecond,
                (Double) Tracker.values().get("launcher/measured_tps"), 0);
        assertEquals("so the windmill feeds", true, Tracker.values().get("launcher/feeding"));
        assertEquals("the stand-in battery", Constants.NOMINAL_BATTERY_VOLTAGE,
                (Double) Tracker.values().get("launcher/battery_volts"), 0);
        assertEquals("the spin-up's F, from that reading", Constants.NOMINAL_BATTERY_VOLTAGE,
                (Double) Tracker.values().get("launcher/f_battery_volts"), 0);
        assertTrue(Tracker.values().containsKey("launcher/f_ctrl_units"));
        // The intake's command runs before the launcher's, so it sees the feeding a loop later.
        h.loop();
        assertEquals("and the intake gets its boost", 0.5,
                (Double) Tracker.values().get("intake/speed"), 1e-9);
        assertEquals("the stand-in intake motor is given a power, not a velocity", 0.0,
                (Double) Tracker.values().get("intake/measured_tps"), 0);
        assertEquals(0.0, (Double) Tracker.values().get("intake/current_amps"), 0);
        h.gamepad1.copy(new Gamepad());
        h.loop();
        assertEquals(0.0, (Double) Tracker.values().get("launcher/target_tps"), 0);
        assertTrue(h.driverStation.text(), h.driverStation.shows("R-A"));
        h.stop();
    }
}
