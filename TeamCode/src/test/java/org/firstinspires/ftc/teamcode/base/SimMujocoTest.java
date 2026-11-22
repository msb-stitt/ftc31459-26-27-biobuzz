package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.pedropathing.math.Pose;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * The link to MuJoCo: the lines it sends and reads, and the localizer that
 * reports MuJoCo's motion in place of its own.
 *
 * <p>The socket test connects the way {@code mujocobiobuzz.link} does and
 * answers as it does, so a pass goes the whole way into the localizer.
 */
public final class SimMujocoTest {

    private static final double EPS = 1e-9;

    private static SimRobot.Motion motion(double x, double y, double heading, long seq) {
        return new SimRobot.Motion(x, y, heading, 10, 2, 0.5, heading, new double[]{1, 2, 3, 4},
                5, 6, seq);
    }

    @Test
    public void aRequestCarriesTheTimeAndTheValuesAndAPoseOnlyWhenOneIsPending() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("sim/wheels/frontLeft", 0.5);
        values.put("/launcher/feeding", true);
        assertEquals("{\"t\": 1.25, \"values\": {\"sim/wheels/frontLeft\": 0.5,"
                        + " \"/launcher/feeding\": true}}\n",
                SimMujoco.request(1.25, values, null, 0));
        Pose p = SimRobot.POSES.of(10, 20, 90);
        String line = SimMujoco.request(0, new LinkedHashMap<>(), p, 3);
        assertTrue(line, line.contains("\"pose\": [10.0, 20.0, " + p.heading() + ", 3]"));
    }

    @Test
    public void aNumberThatIsNotFiniteGoesAsZero() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("a", Double.NaN);
        assertTrue(SimMujoco.request(0, values, null, 0).contains("\"a\": 0.0"));
    }

    @Test
    public void anAnswerIsFifteenNumbersInMotionsOrder() {
        SimRobot.Motion m = SimMujoco.parse(
                "1.5 3.0 10.0 20.0 0.5 1.0 2.0 0.1 0.25 4.0 5.0 6.0 7.0 8.0 9.0\n");
        assertEquals(3, m.seq);
        assertEquals(10, m.x, EPS);
        assertEquals(20, m.y, EPS);
        assertEquals(0.5, m.heading, EPS);
        assertEquals(1, m.vx, EPS);
        assertEquals(2, m.vy, EPS);
        assertEquals(0.1, m.omega, EPS);
        assertEquals(0.25, m.turned, EPS);
        assertEquals(7, m.rolled[3], EPS);
        assertEquals(8, m.forward, EPS);
        assertEquals(9, m.strafe, EPS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void anAnswerOfTheWrongLengthIsRefused() {
        SimMujoco.parse("1 2 3");
    }

    @Test(expected = IllegalArgumentException.class)
    public void aValueThatIsNotAPortIsRefused() throws Exception {
        SimMujoco.open("fifty");
    }

    @Test
    public void noVariableIsNoLink() throws Exception {
        assertNull(SimMujoco.open(null));
    }

    @Test
    public void theLocalizerHoldsAPoseItWasGivenUntilMujocoTakesIt() {
        SimRobot robot = new SimRobot(() -> 0L);
        SimRobot.Motion[] latest = {null};
        robot.localizer.follow(() -> latest[0]);
        Pose start = SimRobot.POSES.of(30, 40, 0);
        robot.localizer.setPose(start);
        assertSame(start, robot.localizer.posePending());
        assertEquals(1, robot.localizer.poseSeq());

        latest[0] = motion(99, 99, 0, 0);
        robot.localizer.update();
        assertEquals(30, robot.localizer.state().pose().x(), EPS);
        assertEquals(0, robot.localizer.state().twist().vx, EPS);

        latest[0] = motion(31, 41, 0.2, 1);
        robot.localizer.update();
        assertNull(robot.localizer.posePending());
        assertEquals(31, robot.localizer.state().pose().x(), EPS);
        assertEquals(41, robot.localizer.state().pose().y(), EPS);
        assertEquals(10, robot.localizer.state().twist().vx, EPS);
    }

    @Test
    public void theEncodersImuAndPodsReadMujocosRobot() {
        SimRobot robot = new SimRobot(() -> 0L);
        robot.localizer.follow(() -> motion(0, 0, 0.3, 0));
        robot.localizer.update();
        assertEquals(0.3, robot.localizer.turnedRadians(), EPS);
        assertEquals(3, robot.localizer.rolledInches(2), EPS);
        assertEquals(5, robot.localizer.forwardInches(), EPS);
        assertEquals(6, robot.localizer.strafeInches(), EPS);
    }

    @Test
    public void aPassSendsOneLineAndTheLocalizerReportsTheAnswer() throws Exception {
        OpModeHarness h = new OpModeHarness(new SimOpModes.Tank());
        List<String> seen = new ArrayList<>();
        try (SimMujoco link = SimMujoco.open("0")) {
            Thread mujoco = new Thread(() -> {
                try (Socket s = new Socket(InetAddress.getLoopbackAddress(), link.port());
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
                        OutputStream out = s.getOutputStream()) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        seen.add(line);
                        out.write("0.01 0 12.0 34.0 0.0 1.0 0.0 0.0 0.0 1.0 1.0 1.0 1.0 2.0 0.0\n"
                                .getBytes(StandardCharsets.UTF_8));
                        out.flush();
                    }
                } catch (Exception e) {
                    seen.add("error " + e);
                }
            });
            mujoco.setDaemon(true);
            mujoco.start();
            link.connect(h.robot, new PrintStream(new ByteArrayOutputStream()));
            link.step(h, 0.01);
            h.robot.localizer.update();
            assertEquals(12, h.robot.localizer.state().pose().x(), EPS);
            assertEquals(34, h.robot.localizer.state().pose().y(), EPS);
        }
        assertEquals(1, seen.size());
        assertTrue(seen.get(0), seen.get(0).startsWith("{\"t\": 0.01, \"values\": {"));
        assertTrue(seen.get(0), seen.get(0).contains("\"sim/wheels/backRight\": "));
    }
}
