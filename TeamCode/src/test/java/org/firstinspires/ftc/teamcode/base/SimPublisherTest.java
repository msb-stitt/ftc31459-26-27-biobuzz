package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import edu.wpi.first.networktables.BooleanSubscriber;
import edu.wpi.first.networktables.DoubleSubscriber;
import edu.wpi.first.networktables.IntegerSubscriber;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.RawSubscriber;
import edu.wpi.first.networktables.StringSubscriber;

import io.github.mikestitt.corbelsflightlog.FlightLog;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * The simulation's NetworkTables server: that WPILib's own NetworkTables 4
 * client connects to it, and that what it reads is what the simulated robot is
 * doing. The server is corbelsflightlog's {@code Nt4Server}, the one the robot
 * runs, so this is also WPILib's reading of that server's protocol.
 *
 * <p>One test rather than one per fact, because a second NetworkTables instance
 * in the same JVM aborts that JVM on macOS. Measured on macos-latest, Temurin
 * 17.0.20 on arm64: the first server logs
 * {@code NT: Listening on NT3 port 1800, NT4 port 5900} and 15 ms later the next
 * instance's startup gives {@code libc++abi: terminating due to uncaught
 * exception of type std::__1::system_error: mutex lock failed: Invalid argument}
 * and exit 134, with no test reported as failing. There is no
 * {@code hs_err_pid} file to read, because the abort is on a thread HotSpot does
 * not cover, and macOS writes no crash report for it on that runner either. It
 * failed three of the six runs made on 2026-09-30 and never once on this
 * laptop, which has sixteen cores against the runner's few: 15 runs of this
 * suite in one JVM, 600 sequential create-and-close cycles, and 600 more with
 * each close on a thread of its own so a teardown overlaps the next startup.
 *
 * <p>Standing one server up and asking it everything is also the simpler shape.
 * Binding a port is the one thing here that is not side-effect-free, and three
 * tests bound three of them to check facts that need no client at all.
 */
public class SimPublisherTest {

    /** Where a failure left its flight log. */
    @Rule
    public final SimLogs logs = new SimLogs();

    /** 5899 rather than the real 5810, so the test never fights a server
     *  someone left running. */
    private static final int NT4 = 5899;

    /** Waits up to 10 s for a condition the client's thread makes true. */
    private static void eventually(String what, BooleanSupplier condition) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() >= deadline) {
                fail("timed out waiting for " + what);
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("interrupted waiting for " + what);
            }
        }
    }

    /** The three little-endian doubles of a {@code struct:Pose2d}. */
    private static double[] unpack(byte[] raw) {
        ByteBuffer b = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
        return new double[]{b.getDouble(), b.getDouble(), b.getDouble()};
    }

    @Test
    public void wpilibsClientReadsWhatTheSimulatedRobotIsDoing() throws IOException {
        int savedTurns = FlightLog.fieldQuarterTurns;
        OpModeHarness h = new OpModeHarness(new SimOpModes.Tank());
        try {
            FlightLog.fieldQuarterTurns = 0;
            h.init();
            h.start();
            h.gamepad1.left_stick_y = -1.0f;
            h.gamepad1.right_stick_y = -1.0f;
            h.loops(200, 0);
            Tracker.publish("lesson/number", 2.5);
            Tracker.publish("lesson/flag", true);
            Tracker.publish("lesson/word", "pressed");
            Tracker.flightlog.recordOutput("lesson/only_in_the_log", 7L);
            NtNatives.load();
            NetworkTableInstance client = NetworkTableInstance.create();
            try (SimPublisher out = new SimPublisher(h, NT4)) {
                out.publish();
                client.startClient4("SimPublisherTest");
                client.setServer("127.0.0.1", NT4);

                String[][] expected = {
                        {"struct:Translation2d", "double x;double y"},
                        {"struct:Rotation2d", "double value"},
                        {"struct:Pose2d", "Translation2d translation;Rotation2d rotation"}};
                RawSubscriber[] schemas = new RawSubscriber[expected.length];
                for (int i = 0; i < expected.length; i++) {
                    schemas[i] = client.getRawTopic("/.schema/" + expected[i][0])
                            .subscribe("structschema", new byte[0]);
                }
                RawSubscriber pose = client.getRawTopic("sim/Pose")
                        .subscribe("struct:Pose2d", new byte[0]);
                DoubleSubscriber[] wheels = {
                        client.getDoubleTopic("sim/wheels/frontLeft").subscribe(0),
                        client.getDoubleTopic("sim/wheels/frontRight").subscribe(0),
                        client.getDoubleTopic("sim/wheels/backLeft").subscribe(0),
                        client.getDoubleTopic("sim/wheels/backRight").subscribe(0)};
                DoubleSubscriber leftY = client.getDoubleTopic("sim/stick/leftY").subscribe(0);
                DoubleSubscriber forward = client.getDoubleTopic("sim/vel/forward_ips").subscribe(0);
                DoubleSubscriber number = client.getDoubleTopic("/lesson/number").subscribe(0);
                BooleanSubscriber flag = client.getBooleanTopic("/lesson/flag").subscribe(false);
                StringSubscriber word = client.getStringTopic("/lesson/word").subscribe("");
                IntegerSubscriber onlyInLog =
                        client.getIntegerTopic("/lesson/only_in_the_log").subscribe(0);

                eventually("WPILib's client to connect", client::isConnected);
                eventually("every value to arrive", () -> pose.get().length > 0
                        && schemas[2].get().length > 0 && wheels[3].get() != 0
                        && !word.get().isEmpty() && flag.get() && onlyInLog.get() != 0);

                for (int i = 0; i < expected.length; i++) {
                    String key = "/.schema/" + expected[i][0];
                    assertEquals(key + " is published as a schema", "structschema",
                            client.getTopic(key).getTypeString());
                    assertEquals("what " + expected[i][0] + " is made of", expected[i][1],
                            new String(schemas[i].get(), StandardCharsets.UTF_8));
                }

                assertEquals("the pose topic says it is a struct", "struct:Pose2d",
                        client.getTopic("sim/Pose").getTypeString());
                byte[] raw = pose.get();
                assertEquals("a Pose2d is three doubles", 24, raw.length);
                double[] xyh = unpack(raw);
                double inches = h.robot.localizer.state().pose().x();
                assertEquals("x is metres from the centre of the field",
                        (inches - 72) * 0.0254, xyh[0], 1e-9);
                double acrossIn = h.robot.localizer.state().pose().y();
                assertEquals("y is metres from the centre of the field",
                        (acrossIn - 72) * 0.0254, xyh[1], 1e-9);

                double[] powers = new double[4];
                for (int i = 0; i < 4; i++) {
                    powers[i] = wheels[i].get();
                }
                assertArrayEquals("all four wheels at full power",
                        new double[]{1, 1, 1, 1}, powers, 1e-9);

                assertEquals("the stick the driver is holding", -1.0, leftY.get(), 1e-9);
                assertTrue("moving forward at a fair speed", forward.get() > 50);

                assertEquals("a number the OpMode published, under its flight log name", 2.5,
                        number.get(), 1e-9);
                assertTrue("a true or false it published", flag.get());
                assertEquals("and a word", "pressed", word.get());
                assertEquals("a value only the flight log recorded, mirrored, as an int", 7L,
                        onlyInLog.get());
            } finally {
                client.close();
            }
            h.stop();
        } finally {
            FlightLog.fieldQuarterTurns = savedTurns;
        }
    }
}
