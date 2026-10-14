package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.lwjgl.sdl.SDLError.SDL_GetError;
import static org.lwjgl.sdl.SDLGUID.SDL_GUIDToString;
import static org.lwjgl.sdl.SDLGamepad.SDL_AddGamepadMapping;
import static org.lwjgl.sdl.SDLHints.SDL_HINT_JOYSTICK_MFI;
import static org.lwjgl.sdl.SDLHints.SDL_SetHint;
import static org.lwjgl.sdl.SDLInit.SDL_INIT_EVENTS;
import static org.lwjgl.sdl.SDLInit.SDL_INIT_GAMEPAD;
import static org.lwjgl.sdl.SDLInit.SDL_INIT_JOYSTICK;
import static org.lwjgl.sdl.SDLInit.SDL_Init;
import static org.lwjgl.sdl.SDLInit.SDL_Quit;
import static org.lwjgl.sdl.SDLJoystick.SDL_AttachVirtualJoystick;
import static org.lwjgl.sdl.SDLJoystick.SDL_GetJoystickGUIDForID;
import static org.lwjgl.sdl.SDLJoystick.SDL_JOYSTICK_TYPE_GAMEPAD;
import static org.lwjgl.sdl.SDLJoystick.SDL_OpenJoystick;
import static org.lwjgl.sdl.SDLJoystick.SDL_SetJoystickVirtualAxis;
import static org.lwjgl.sdl.SDLJoystick.SDL_SetJoystickVirtualButton;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.lwjgl.sdl.SDL_GUID;
import org.lwjgl.sdl.SDL_VirtualJoystickDesc;
import org.lwjgl.system.MemoryStack;

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

    private static final String NAME = "Virtual Xbox 360 Controller";

    /** SDL's button numbers for this test's mapping. */
    private static final int BUTTON_A = 0;

    private static final int BUTTON_START = 6;

    /** SDL's axis number for the left stick's y in this test's mapping. */
    private static final int AXIS_LEFT_Y = 1;

    /**
     * Attaches one virtual gamepad and gives it a mapping, which SDL needs before
     * it lists the device as a gamepad.
     *
     * <p>The triggers are mapped to half an axis, so a virtual axis at rest reads
     * as a trigger at rest.
     */
    private static int attach() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            SDL_VirtualJoystickDesc desc = SDL_VirtualJoystickDesc.calloc(stack)
                    .version(SDL_VirtualJoystickDesc.SIZEOF)
                    .type((short) SDL_JOYSTICK_TYPE_GAMEPAD)
                    .naxes((short) 6)
                    .nbuttons((short) 11)
                    .name(stack.UTF8(NAME));
            int id = SDL_AttachVirtualJoystick(desc);
            assertTrue("attach: " + SDL_GetError(), id != 0);
            SDL_GUID guid = SDL_GetJoystickGUIDForID(id, SDL_GUID.malloc(stack));
            ByteBuffer text = stack.malloc(33);
            SDL_GUIDToString(guid, text);
            String mapping = StandardCharsets.US_ASCII.decode(text).toString().trim()
                    .replace("\0", "")
                    + "," + NAME + ",a:b0,b:b1,x:b2,y:b3,back:b4,guide:b5,start:b6,"
                    + "leftstick:b7,rightstick:b8,leftshoulder:b9,rightshoulder:b10,"
                    + "leftx:a0,lefty:a1,rightx:a2,righty:a3,lefttrigger:+a4,righttrigger:+a5,";
            assertTrue("mapping: " + SDL_GetError(), SDL_AddGamepadMapping(mapping) >= 0);
            return id;
        }
    }

    /** Holds Start and one button on {@code joystick} for one gesture pass, then lets go. */
    private static void gesture(SimPads pads, long joystick, int button,
            Gamepad gamepad1, Gamepad gamepad2) {
        SDL_SetJoystickVirtualButton(joystick, BUTTON_START, true);
        SDL_SetJoystickVirtualButton(joystick, button, true);
        pads.read(gamepad1, gamepad2);
        pads.gestures();
        SDL_SetJoystickVirtualButton(joystick, BUTTON_START, false);
        SDL_SetJoystickVirtualButton(joystick, button, false);
        pads.read(gamepad1, gamepad2);
    }

    @Test
    public void startAndAMovesTheSecondVirtualGamepadToGamepad1ByItsDeviceId()
            throws Exception {
        SDL_SetHint(SDL_HINT_JOYSTICK_MFI, "0");
        assertTrue("SDL would not start: " + SDL_GetError(),
                SDL_Init(SDL_INIT_GAMEPAD | SDL_INIT_JOYSTICK | SDL_INIT_EVENTS));
        try {
            int first = attach();
            int second = attach();
            long joystick1 = SDL_OpenJoystick(first);
            long joystick2 = SDL_OpenJoystick(second);
            assertTrue("open: " + SDL_GetError(), joystick1 != 0L && joystick2 != 0L);

            Path store = folder.newFile("local.properties").toPath();
            ByteArrayOutputStream printed = new ByteArrayOutputStream();
            Gamepad gamepad1 = new Gamepad();
            Gamepad gamepad2 = new Gamepad();
            try (SimPads pads = new SimPads(SimGamepad.open(), store,
                    new PrintStream(printed, true, "UTF-8"))) {
                // With nothing else plugged in, the two fill both slots in the order
                // found and this swaps them. With a gamepad plugged in and found first,
                // it held gamepad1 and the first virtual one gamepad2, and this displaces
                // it. Either way the two virtual gamepads end in both slots.
                gesture(pads, joystick2, BUTTON_A, gamepad1, gamepad2);

                SDL_SetJoystickVirtualAxis(joystick1, AXIS_LEFT_Y, (short) -32768);
                pads.read(gamepad1, gamepad2);
                assertEquals("the first virtual gamepad's stick, in gamepad2",
                        -1f, gamepad2.left_stick_y, 0f);
                assertEquals("and not in gamepad1", 0f, gamepad1.left_stick_y, 0f);
            }

            String census = new String(printed.toByteArray(), StandardCharsets.UTF_8);
            assertTrue(census, census.contains("\"" + NAME + "\", no serial number, id " + second
                    + " -- gamepad1, claimed this run"));
            assertTrue(census, census.contains("\"" + NAME + "\", no serial number, id " + first
                    + " -- gamepad2, "));
            Map<String, String> file = SimPads.read(store);
            assertEquals(new SimPads.Key(SimPads.NO_SERIAL, second), SimPads.storedKey(file, 1));
            assertEquals(new SimPads.Key(SimPads.NO_SERIAL, first), SimPads.storedKey(file, 2));
        } finally {
            SDL_Quit();
        }
    }
}
