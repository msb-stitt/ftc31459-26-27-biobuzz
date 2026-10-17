package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Two virtual gamepads, attached in this process beside whatever is plugged in,
 * run through {@link SimPads} the way a real one is: both take slots, Start and
 * A on the second moves it to {@code gamepad1}, a stick reaches its slot's
 * gamepad object, and the census and
 * {@code local.properties} name each by its device id, since a virtual gamepad
 * reports no serial number.
 *
 * <p>The one simulator test that starts SDL. A virtual gamepad lives only in the
 * process that attached it, so nothing here can reach another program.
 */
public final class VirtualPadsTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private static final String NAME = SimGamepad.VIRTUAL_NAME;

    /** Holds Start and one button on {@code pad} for one gesture pass, then lets go. */
    private static void gesture(SimPads pads, SimGamepad.Virtual pad, String button,
            Gamepad gamepad1, Gamepad gamepad2) {
        SimGamepad.setVirtual(pad, "start", 1f);
        SimGamepad.setVirtual(pad, button, 1f);
        pads.read(gamepad1, gamepad2);
        pads.gestures();
        SimGamepad.setVirtual(pad, "start", 0f);
        SimGamepad.setVirtual(pad, button, 0f);
        pads.read(gamepad1, gamepad2);
    }

    @Test
    public void startAndAMovesTheSecondVirtualGamepadToGamepad1ByItsDeviceId()
            throws Exception {
        Path store = folder.newFile("local.properties").toPath();
        ByteArrayOutputStream printed = new ByteArrayOutputStream();
        Gamepad gamepad1 = new Gamepad();
        Gamepad gamepad2 = new Gamepad();
        SimGamepad.Virtual first;
        SimGamepad.Virtual second;
        try (SimPads pads = new SimPads(SimGamepad.open(), store,
                new PrintStream(printed, true, "UTF-8"))) {
            first = SimGamepad.attachVirtual();
            second = SimGamepad.attachVirtual();
            pads.read(gamepad1, gamepad2);
            pads.rescan();
            // With nothing else plugged in, the two fill both slots in the order
            // found and this swaps them. With a gamepad plugged in and found first,
            // it held gamepad1 and the first virtual one gamepad2, and this displaces
            // it. Either way the two virtual gamepads end in both slots.
            gesture(pads, second, "a", gamepad1, gamepad2);

            SimGamepad.setVirtual(first, "left_stick_y", -1f);
            pads.read(gamepad1, gamepad2);
            assertEquals("the first virtual gamepad's stick, in gamepad2",
                    -1f, gamepad2.left_stick_y, 0f);
            assertEquals("and not in gamepad1", 0f, gamepad1.left_stick_y, 0f);
        }

        String census = new String(printed.toByteArray(), StandardCharsets.UTF_8);
        assertTrue("an empty file is reset to version 2 and says so: " + census,
                census.contains("  The gamepad lines in local.properties were reset, because"
                        + " it had no sim.gamepad.version."));
        assertTrue(census, census.contains("\"" + NAME + "\", no serial number, id "
                + second.id + " -- gamepad1, claimed this run"));
        assertTrue(census, census.contains("\"" + NAME + "\", no serial number, id "
                + first.id + " -- gamepad2, "));
        Map<String, String> file = SimPads.read(store);
        assertEquals(new SimPads.Key(SimPads.NO_SERIAL, second.id), SimPads.storedKey(file, 1));
        assertEquals(new SimPads.Key(SimPads.NO_SERIAL, first.id), SimPads.storedKey(file, 2));
        assertEquals("2", file.get(SimPads.VERSION));
    }
}
