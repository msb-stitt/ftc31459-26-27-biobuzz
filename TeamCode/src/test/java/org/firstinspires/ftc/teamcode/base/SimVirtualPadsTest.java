package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * The port a script drives virtual gamepads through, and the lines it takes.
 *
 * <p>The socket tests start SDL in this process and connect to the port the
 * way {@code tools/vpad.py} does, so a command goes the whole way into a
 * gamepad object.
 */
public final class SimVirtualPadsTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    // --- the lines ----------------------------------------------------------

    private static String refusal(String line) {
        Object parsed = SimVirtualPads.parse(line);
        assertTrue("\"" + line + "\" should be refused", parsed instanceof String);
        return (String) parsed;
    }

    private static SimVirtualPads.Command command(String line) {
        Object parsed = SimVirtualPads.parse(line);
        assertTrue("\"" + line + "\" should be taken: " + parsed,
                parsed instanceof SimVirtualPads.Command);
        return (SimVirtualPads.Command) parsed;
    }

    @Test
    public void aCommandNamesAGamepadAControlAndAValue() {
        SimVirtualPads.Command c = command("pad 1 left_stick_y -0.73");
        assertEquals(1, c.pad);
        assertEquals("left_stick_y", c.control);
        assertEquals(-0.73f, c.value, 0f);
        assertEquals(12, command("  pad 12   start 1 \r").pad);
    }

    @Test
    public void everyControlTheControlOptionsNameIsTaken() {
        for (String axis : SimArgs.AXES) {
            command("pad 1 " + axis + " 0");
        }
        for (String button : SimArgs.BUTTONS) {
            command("pad 1 " + button + " 1");
        }
        assertEquals(SimArgs.AXES.size(), SimGamepad.VIRTUAL_AXES.size());
        assertEquals(SimArgs.BUTTONS.size(), SimGamepad.VIRTUAL_BUTTONS.size());
        assertTrue(SimGamepad.VIRTUAL_AXES.containsAll(SimArgs.AXES));
        assertTrue(SimGamepad.VIRTUAL_BUTTONS.containsAll(SimArgs.BUTTONS));
    }

    @Test
    public void aLineThatIsNotACommandIsRefusedAndSaysWhy() {
        assertTrue(refusal("hello").startsWith("a command is: pad <number> <control> <value>"));
        assertTrue(refusal("pad 1 a").startsWith("a command is:"));
        assertTrue(refusal("pad 0 a 1").contains("from 1"));
        assertTrue(refusal("pad one a 1").contains("from 1"));
        assertEquals("no control is called jump", refusal("pad 1 jump 1"));
    }

    @Test
    public void eachKindOfControlTakesItsOwnRange() {
        command("pad 1 left_stick_x -1");
        command("pad 1 left_stick_x 1");
        assertEquals("left_stick_x takes -1 to 1, not 1.01", refusal("pad 1 left_stick_x 1.01"));
        assertEquals("left_stick_x takes -1 to 1, not nan", refusal("pad 1 left_stick_x nan"));
        command("pad 1 right_trigger 0");
        command("pad 1 right_trigger 1");
        assertEquals("right_trigger takes 0 to 1, not -0.1",
                refusal("pad 1 right_trigger -0.1"));
        assertEquals("a takes 0 or 1, not 0.5", refusal("pad 1 a 0.5"));
        assertEquals("a takes 0 or 1, not 2", refusal("pad 1 a 2"));
    }

    @Test
    public void aValueGoesThroughTheRawReadingAndComesBackWithin0point0001() {
        for (float v = -1f; v <= 1f; v += 0.01f) {
            assertEquals(v, SimGamepad.stickScale(SimGamepad.rawAxis(v)), 0.0001f);
        }
        for (float v = 0f; v <= 1f; v += 0.01f) {
            assertEquals(v, SimGamepad.triggerScale(SimGamepad.rawAxis(v)), 0.0001f);
        }
        assertEquals(-1f, SimGamepad.stickScale(SimGamepad.rawAxis(-1f)), 0f);
        assertEquals(1f, SimGamepad.stickScale(SimGamepad.rawAxis(1f)), 0f);
    }

    // --- the switch ---------------------------------------------------------

    private static String said(java.util.function.Function<PrintStream, SimVirtualPads> open)
            throws Exception {
        ByteArrayOutputStream printed = new ByteArrayOutputStream();
        SimVirtualPads virtual = open.apply(new PrintStream(printed, true, "UTF-8"));
        assertNull(virtual);
        return new String(printed.toByteArray(), StandardCharsets.UTF_8);
    }

    @Test
    public void withTheVariableUnsetNoPortIsOpenedAndNothingIsSaid() throws Exception {
        assertEquals("", said(out -> SimVirtualPads.open(null, true, out)));
    }

    @Test
    public void withoutPadOrPadCheckTheVariableIsIgnoredInOneLine() throws Exception {
        String said = said(out -> SimVirtualPads.open("5812", false, out));
        assertEquals("  SIMRUN_VIRTUAL_PADS is set, but only --pad and --pad-check take virtual"
                + " gamepads, so it is ignored.", said.replaceAll("\\R$", ""));
    }

    @Test
    public void aValueThatIsNotAPortIsSaidInOneLine() throws Exception {
        assertTrue(said(out -> SimVirtualPads.open("pad", true, out))
                .contains("SIMRUN_VIRTUAL_PADS=pad is not a port number"));
        assertTrue(said(out -> SimVirtualPads.open("70000", true, out))
                .contains("SIMRUN_VIRTUAL_PADS=70000 is not a port number"));
    }

    @Test
    public void aPortInUseIsSaidInOneLineAndTheRunGoesOn() throws Exception {
        try (ServerSocket taken = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            String said = said(out -> SimVirtualPads.open(
                    Integer.toString(taken.getLocalPort()), true, out));
            assertTrue(said, said.contains("Port " + taken.getLocalPort()
                    + " could not be opened"));
            assertEquals(said, 1, said.trim().split("\\R").length);
        }
    }

    // --- the socket ---------------------------------------------------------

    /** A script's end of the port: one connection, a line out and its answer back. */
    private static final class Script implements AutoCloseable {
        final Socket socket;
        final OutputStream out;
        final BufferedReader in;
        final SimVirtualPads virtual;

        Script(SimVirtualPads virtual) throws Exception {
            this.virtual = virtual;
            socket = new Socket(InetAddress.getLoopbackAddress(), virtual.port());
            out = socket.getOutputStream();
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                    StandardCharsets.UTF_8));
        }

        /** Sends one line and passes the loop until its answer comes back, as simRun would. */
        String send(String line) throws Exception {
            out.write((line + "\n").getBytes(StandardCharsets.UTF_8));
            out.flush();
            long until = System.currentTimeMillis() + 2000;
            while (System.currentTimeMillis() < until) {
                virtual.poll();
                if (in.ready()) {
                    return in.readLine();
                }
                Thread.sleep(1);
            }
            throw new AssertionError("no answer to \"" + line + "\" in 2 s");
        }

        @Override
        public void close() throws Exception {
            socket.close();
        }
    }

    @Test
    public void commandsThroughThePortAttachGamepadsThatHoldEachValue() throws Exception {
        Path store = folder.newFile("local.properties").toPath();
        ByteArrayOutputStream printed = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(printed, true, "UTF-8");
        Gamepad gamepad1 = new Gamepad();
        Gamepad gamepad2 = new Gamepad();
        List<SimGamepad.Pad> virtuals = new ArrayList<>();
        SimGamepad sdl = SimGamepad.open();
        try (SimPads pads = new SimPads(sdl, store, out);
                SimVirtualPads virtual = SimVirtualPads.open("0", true, out);
                Script script = new Script(virtual)) {
            int before = sdl.pads().size();

            assertEquals("ok", script.send("pad 1 left_stick_y -0.73"));
            assertEquals("ok", script.send("pad 2 right_trigger 0.25"));
            assertEquals("ok", script.send("pad 3 a 1"));
            assertTrue(script.send("pad 1 left_stick_y 2").startsWith("error "));
            assertTrue(script.send("pad 1 jump 1").startsWith("error "));
            pads.rescan();

            List<SimGamepad.Pad> all = sdl.pads();
            assertEquals("three attached", before + 3, all.size());
            virtuals.addAll(all.subList(before, all.size()));
            for (int i = 0; i < 3; i++) {
                SimGamepad.Pad pad = virtuals.get(i);
                assertEquals(SimGamepad.VIRTUAL_NAME, pad.name);
                assertNull("no serial number", pad.serial);
                assertTrue("virtual", pad.virtual);
                if (i > 0) {
                    assertTrue("ids follow in the order attached",
                            pad.id > virtuals.get(i - 1).id);
                }
            }

            // Pad 3 is a third gamepad, unclaimed while the first two hold the slots.
            for (int pass = 0; pass < 3; pass++) {
                pads.read(gamepad1, gamepad2);
            }
            boolean nothingElse = before == 0;
            if (nothingElse) {
                assertEquals("pad 1's stick in gamepad1, and it holds",
                        -0.73f, gamepad1.left_stick_y, 0.0001f);
                assertEquals("pad 2's trigger in gamepad2, and it holds",
                        0.25f, gamepad2.right_trigger, 0.0001f);
                assertTrue("pad 3's a reaches neither slot", !gamepad1.a && !gamepad2.a);
            }

            // Start and A from pad 3 claims gamepad1.
            assertEquals("ok", script.send("pad 3 start 1"));
            pads.read(gamepad1, gamepad2);
            pads.gestures();
            assertEquals("ok", script.send("pad 3 start 0"));
            assertEquals("ok", script.send("pad 3 a 0"));
            assertEquals("ok", script.send("pad 3 left_stick_x 0.5"));
            pads.read(gamepad1, gamepad2);
            assertEquals("pad 3 now drives gamepad1", 0.5f, gamepad1.left_stick_x, 0.0001f);
        }

        String census = new String(printed.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(census, census.contains("virtual \"" + SimGamepad.VIRTUAL_NAME
                + "\", no serial number, id " + virtuals.get(2).id
                + " -- gamepad1, claimed this run"));
    }

    /**
     * Gamepad 1 attached as SDL starts is in the first census, as a gamepad
     * plugged in before the run is, and the port says nothing of itself. SDL
     * quits before the port closes, as in simRun, and the close still runs.
     */
    @Test
    public void gamepad1AttachedAtStartIsInTheFirstCensusAndCommandsReachIt() throws Exception {
        Path store = folder.newFile("local.properties").toPath();
        ByteArrayOutputStream printed = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(printed, true, "UTF-8");
        int attached;
        try (SimVirtualPads virtual = SimVirtualPads.open("0", true, out);
                SimGamepad sdl = SimGamepad.open(virtual::attachFirst);
                SimPads pads = new SimPads(sdl, store, out);
                Script script = new Script(virtual)) {
            List<SimGamepad.Pad> all = sdl.pads();
            attached = all.size();
            assertTrue("gamepad 1 is found at start", all.stream().anyMatch(p -> p.virtual));
            assertEquals("ok", script.send("pad 1 left_stick_y -0.73"));
            pads.rescan();
            assertEquals("pad 1 is the gamepad already attached", attached,
                    sdl.pads().size());
        }
        String census = new String(printed.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(census, census.contains("virtual \"" + SimGamepad.VIRTUAL_NAME
                + "\", no serial number, id "));
        assertTrue(census, !census.contains("No gamepad is plugged in"));
        assertTrue(census, !census.contains("new gamepad:"));
        assertTrue(census, !census.contains("127.0.0.1"));
    }

    @Test
    public void aConnectionThatSendsHalfALineIsAnsweredWhenTheRestArrives() throws Exception {
        Path store = folder.newFile("local.properties").toPath();
        PrintStream out = new PrintStream(new ByteArrayOutputStream(), true, "UTF-8");
        try (SimPads pads = new SimPads(SimGamepad.open(), store, out);
                SimVirtualPads virtual = SimVirtualPads.open("0", true, out);
                Script script = new Script(virtual)) {
            script.out.write("pad 1 left_".getBytes(StandardCharsets.UTF_8));
            script.out.flush();
            for (int pass = 0; pass < 20; pass++) {
                virtual.poll();
                Thread.sleep(1);
            }
            assertTrue("no answer to half a line", !script.in.ready());
            assertEquals("ok", script.send("stick_x 1"));
        }
    }
}
