package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.pedropathing.math.Pose;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.firstinspires.ftc.teamcode.example.TeleOpWithPanelsOpMode;
import org.junit.Test;

/**
 * simRun's side of Scrimmage's lockstep, against {@link FakeHalsimServer}
 * playing the server as the fork's {@code MultiServer} does.
 */
public final class SimScrimmageTest {

    private static final double IN = 0.0254;
    private static final double EPS = 1e-9;

    @Test
    public void theVariableNamesTheServerAndTheRobot() {
        assertNull(SimScrimmage.open(null));
        SimScrimmage s = SimScrimmage.open("localhost:3300/37523");
        assertEquals("localhost", s.host);
        assertEquals(3300, s.port);
        assertEquals("37523", s.name);
    }

    @Test(expected = IllegalArgumentException.class)
    public void aVariableWithNoRobotIsRefused() {
        SimScrimmage.open("localhost:3300");
    }

    @Test(expected = IllegalArgumentException.class)
    public void aVariableWithNoPortIsRefused() {
        SimScrimmage.open("localhost/37523");
    }

    @Test
    public void aMotorIsNamedForItsHardwareNameItsSpacesAsUnderscores() {
        assertEquals("Front_Left", SimScrimmage.device("Front Left"));
    }

    @Test
    public void theDrivebasesMetresAreTheMotionsInches() {
        Map<String, Object> d = new HashMap<>();
        d.put(">x", 1.0);
        d.put(">y", -0.5);
        d.put(">heading", 0.25);
        d.put(">vx", 0.0254);
        d.put(">rolled2", 0.254);
        d.put(">travel_y", 2.54);
        d.put(">place_seq", 3L);
        SimRobot.Motion m = SimScrimmage.motion(d);
        assertEquals(1.0 / IN, m.x, EPS);
        assertEquals(-0.5 / IN, m.y, EPS);
        assertEquals(0.25, m.heading, EPS);
        assertEquals(1.0, m.vx, EPS);
        assertEquals(10.0, m.rolled[2], EPS);
        assertEquals(100.0, m.strafe, EPS);
        assertEquals(3, m.seq);
    }

    @Test
    public void aSyncIsAnsweredWithThePowersThePlacingAndTheNextPassesTime() throws Exception {
        OpModeHarness h = new OpModeHarness(new TeleOpWithPanelsOpMode());
        try (FakeHalsimServer server = new FakeHalsimServer()) {
            SimScrimmage s = new SimScrimmage("localhost", server.port(), "37523");
            Thread accept = new Thread(() -> {
                try {
                    server.accept();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            accept.start();
            s.connect(h.robot, new PrintStream(new ByteArrayOutputStream()));
            accept.join();
            assertEquals("/wpilibws/37523", server.path);
            SimHalsimWs.Message joined = server.receive();
            assertEquals("ScrimmageLockstep", joined.device);
            assertEquals(false, joined.data.get("<engaged"));

            server.send("SimDevice", "ScrimmageLockstep", of(">step_us", 0L, ">engage", true));
            Map<String, Object> drive = of(">x", 1.0, ">y", 0.5, ">heading", 0.0);
            drive.put(">place_seq", 0L);
            server.send("ScrimmageDrivebase", "drive", drive);
            server.send("ScrimmageMotor", SimScrimmage.device(OpModeHarness.FRONT_LEFT), of(">vbus", 12.5));
            server.send("SimDevice", "ScrimmageLockstep", of(">step_seq", 21_000L));
            assertEquals(21_000L, s.awaitSync());
            assertEquals(true, server.receive().data.get("<engaged"));
            h.robot.localizer.update();
            assertEquals(1.0 / IN, h.robot.localizer.state().pose().x(), 1e-6);

            h.motors.get(OpModeHarness.FRONT_LEFT).speed = 0.5;
            Pose start = SimRobot.POSES.of(10, 20, 90);
            h.robot.localizer.setPose(start);
            s.answer(h, 21_000L, 31_000L);
            Map<String, Map<String, Object>> sent = new LinkedHashMap<>();
            while (!sent.containsKey("ScrimmageLockstep")) {
                SimHalsimWs.Message m = server.receive();
                sent.put(m.device, m.data);
            }
            assertEquals(6.25, (Double) sent.get(SimScrimmage.device(OpModeHarness.FRONT_LEFT))
                    .get("<motorVoltage"), EPS);
            assertEquals(0.0, (Double) sent.get(SimScrimmage.device(OpModeHarness.BACK_RIGHT))
                    .get("<motorVoltage"), EPS);
            Map<String, Object> place = sent.get("drive");
            assertEquals(10 * IN, (Double) place.get("<place_x"), EPS);
            assertEquals(20 * IN, (Double) place.get("<place_y"), EPS);
            assertEquals(start.heading(), (Double) place.get("<place_heading"), EPS);
            assertEquals(1L, place.get("<place_seq"));
            Map<String, Object> done = sent.get("ScrimmageLockstep");
            assertEquals(31_000L, done.get("<next_us"));
            assertEquals(21_000L, done.get("<done_seq"));
            assertTrue("next_us before done_seq", done.keySet().toArray()[0].equals("<next_us"));
            s.close();
        }
    }

    private static Map<String, Object> of(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put((String) kv[i], kv[i + 1]);
        }
        return m;
    }
}
