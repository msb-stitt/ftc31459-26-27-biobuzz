package org.firstinspires.ftc.teamcode.base;

import static org.lwjgl.sdl.SDLError.SDL_GetError;
import static org.lwjgl.sdl.SDLGUID.SDL_GUIDToString;
import static org.lwjgl.sdl.SDLGamepad.SDL_AddGamepadMapping;
import static org.lwjgl.sdl.SDLEvents.SDL_PumpEvents;
import static org.lwjgl.sdl.SDLGamepad.SDL_CloseGamepad;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_AXIS_LEFTX;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_AXIS_LEFTY;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_AXIS_LEFT_TRIGGER;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_AXIS_RIGHTX;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_AXIS_RIGHTY;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_AXIS_RIGHT_TRIGGER;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_BACK;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_DOWN;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_LEFT;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_RIGHT;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_UP;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_EAST;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_GUIDE;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_SHOULDER;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_STICK;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_NORTH;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_STICK;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_SOUTH;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_START;
import static org.lwjgl.sdl.SDLGamepad.SDL_GAMEPAD_BUTTON_WEST;
import static org.lwjgl.sdl.SDLGamepad.SDL_GetGamepadAxis;
import static org.lwjgl.sdl.SDLGamepad.SDL_GetGamepadButton;
import static org.lwjgl.sdl.SDLGamepad.SDL_GetGamepadName;
import static org.lwjgl.sdl.SDLGamepad.SDL_GetGamepadSerial;
import static org.lwjgl.sdl.SDLGamepad.SDL_GetGamepads;
import static org.lwjgl.sdl.SDLGamepad.SDL_OpenGamepad;
import static org.lwjgl.sdl.SDLGamepad.SDL_UpdateGamepads;
import static org.lwjgl.sdl.SDLHints.SDL_HINT_JOYSTICK_MFI;
import static org.lwjgl.sdl.SDLHints.SDL_SetHint;
import static org.lwjgl.sdl.SDLInit.SDL_INIT_EVENTS;
import static org.lwjgl.sdl.SDLInit.SDL_INIT_GAMEPAD;
import static org.lwjgl.sdl.SDLInit.SDL_INIT_JOYSTICK;
import static org.lwjgl.sdl.SDLInit.SDL_Init;
import static org.lwjgl.sdl.SDLInit.SDL_Quit;
import static org.lwjgl.sdl.SDLInit.SDL_WasInit;
import static org.lwjgl.sdl.SDLJoystick.SDL_AttachVirtualJoystick;
import static org.lwjgl.sdl.SDLJoystick.SDL_CloseJoystick;
import static org.lwjgl.sdl.SDLJoystick.SDL_DetachVirtualJoystick;
import static org.lwjgl.sdl.SDLJoystick.SDL_GetJoystickGUIDForID;
import static org.lwjgl.sdl.SDLJoystick.SDL_IsJoystickVirtual;
import static org.lwjgl.sdl.SDLJoystick.SDL_JOYSTICK_TYPE_GAMEPAD;
import static org.lwjgl.sdl.SDLJoystick.SDL_OpenJoystick;
import static org.lwjgl.sdl.SDLJoystick.SDL_SetJoystickVirtualAxis;
import static org.lwjgl.sdl.SDLJoystick.SDL_SetJoystickVirtualButton;
import static org.lwjgl.sdl.SDLStdinc.SDL_free;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.lwjgl.sdl.SDL_GUID;
import org.lwjgl.sdl.SDL_VirtualJoystickDesc;
import org.lwjgl.system.MemoryStack;

/**
 * The gamepads plugged into the laptop, as {@link Gamepad} fields.
 *
 * <p>This class owns SDL and nothing else: opening it, closing it, and turning
 * one pad's readings into one {@code Gamepad}. Which pad is {@code gamepad1} is
 * somebody else's question.
 *
 * <p>SDL3 is the same library WPILib's own simulator reads a gamepad with, and
 * on macOS it offers two backends that disagree about a pad. Apple's
 * GameController framework inverts the left stick on the pad this was written
 * against, so {@code SDL_JOYSTICK_MFI} is turned off and SDL reads IOKit
 * instead. Measured, both ways: {@code sim.sticks.input} in
 * {@code .docs/open-work.md}.
 *
 * <p>Passes when: SimGamepadTest, which runs with no pad plugged in.
 */
public final class SimGamepad implements AutoCloseable {

    /** How long {@link #open} waits for the first pad to appear. */
    private static final long DISCOVERY_MS = 2000;

    private static final long DISCOVERY_STEP_MS = 100;

    /** Full scale one way. A stick reads -32768 to 32767, so the two differ. */
    private static final float FULL_NEGATIVE = 32768f;

    private static final float FULL_POSITIVE = 32767f;

    /** One pad SDL has open, and what it calls itself. */
    public static final class Pad {

        /** SDL's id for this pad, which changes between runs. */
        public final int id;

        /** What the pad calls itself, for a line a reader can recognise. */
        public final String name;

        /**
         * The pad's own serial, or null where the pad or the backend has none.
         * {@link SimPads#key} makes what an assignment is stored against from
         * it, and from the id where this is null, empty or shared.
         */
        public final String serial;

        /** True for a gamepad {@link #attachVirtual} made, which a script drives. */
        public final boolean virtual;

        private final long handle;

        /**
         * Package-private, not private, so a test can make a pad with no SDL
         * behind it. A handle of 0 reads nothing, which is all
         * {@code SimPadsTest} needs.
         */
        Pad(int id, long handle, String name, String serial) {
            this(id, handle, name, serial, false);
        }

        Pad(int id, long handle, String name, String serial, boolean virtual) {
            this.id = id;
            this.handle = handle;
            this.name = name;
            this.serial = serial;
            this.virtual = virtual;
        }

        /**
         * The name, then the serial number, then the device id.
         *
         * <p>An empty serial number is printed as {@code serial ""}, so it is
         * told apart from a missing one. The device id comes last, so it cannot
         * be read as a slot. Leading
         * with it put a number next to a slot name with nothing to tell them
         * apart: {@code gamepad1 is now id 1 "Xbox 360 Controller"}. A virtual
         * gamepad says so first, outside the quotes, since its name is the one
         * a real gamepad has.
         */
        @Override
        public String toString() {
            return (virtual ? "virtual " : "") + "\"" + name + "\", "
                    + (serial == null ? "no serial number"
                            : serial.isEmpty() ? "serial \"\"" : "serial " + serial)
                    + ", id " + id;
        }
    }

    private final List<Pad> pads;

    private boolean closed;

    private SimGamepad(List<Pad> pads) {
        this.pads = pads;
    }

    /**
     * Starts SDL and opens every pad it can see, waiting up to
     * {@value #DISCOVERY_MS} ms for the first one.
     *
     * <p>The wait is not politeness. Measured on 2026-09-28:
     * {@code SDL_GetGamepads()} saw none on the first pass and one about 100 ms
     * later, so enumerating once finds nothing.
     *
     * @throws IllegalStateException if SDL will not start at all
     */
    public static SimGamepad open() {
        return open(null);
    }

    /**
     * {@link #open()}, running {@code started} once SDL has started and before
     * it looks for pads, so a virtual gamepad attached there is found with the
     * rest. Null runs nothing.
     */
    public static SimGamepad open(Runnable started) {
        SDL_SetHint(SDL_HINT_JOYSTICK_MFI, "0");
        if (!SDL_Init(SDL_INIT_GAMEPAD | SDL_INIT_JOYSTICK | SDL_INIT_EVENTS)) {
            throw new IllegalStateException("SDL would not start: " + SDL_GetError());
        }
        List<Pad> pads = new ArrayList<>();
        try {
            if (started != null) {
                started.run();
            }
            int[] ids = new int[0];
            for (long waited = 0; waited <= DISCOVERY_MS; waited += DISCOVERY_STEP_MS) {
                SDL_PumpEvents();
                SDL_UpdateGamepads();
                ids = list();
                if (ids.length > 0) {
                    break;
                }
                OpModeHarness.sleep(DISCOVERY_STEP_MS);
            }
            for (int id : ids) {
                long handle = SDL_OpenGamepad(id);
                if (handle == 0L) {
                    continue;
                }
                pads.add(new Pad(id, handle, SDL_GetGamepadName(handle),
                        SDL_GetGamepadSerial(handle), SDL_IsJoystickVirtual(id)));
            }
        } catch (RuntimeException e) {
            for (Pad pad : pads) {
                SDL_CloseGamepad(pad.handle);
            }
            SDL_Quit();
            throw e;
        }
        return new SimGamepad(pads);
    }

    /**
     * The ids SDL is listing, copied out of SDL's own memory.
     *
     * <p>{@code SDL_GetGamepads} mallocs the array it returns and the caller
     * owns it. Measured on 2026-09-28: 8 million calls without the free grew the
     * process by 26 MB/s and with it by nothing, and two calls in a row return
     * different addresses rather than one cached block.
     */
    private static int[] list() {
        IntBuffer ids = SDL_GetGamepads();
        if (ids == null) {
            return new int[0];
        }
        try {
            int[] out = new int[ids.remaining()];
            for (int i = 0; i < out.length; i++) {
                out[i] = ids.get(ids.position() + i);
            }
            return out;
        } finally {
            SDL_free(ids);
        }
    }

    /**
     * Pads SDL is listing that were not held, now opened and added to
     * {@link #pads()}.
     *
     * <p>How often this is called is {@link SimPads}'s business, which owns the
     * policy. A rescan costs about 1 us against the grid's 10 ms step, and
     * {@link #departed()} lists again rather than sharing the result, because
     * 1 us twice is not worth a shape that has to hand an array around.
     */
    public List<Pad> arrived() {
        List<Pad> added = new ArrayList<>();
        for (int id : addedIds(list(), pads)) {
            long handle = SDL_OpenGamepad(id);
            if (handle == 0L) {
                continue;
            }
            Pad pad = new Pad(id, handle, SDL_GetGamepadName(handle),
                    SDL_GetGamepadSerial(handle), SDL_IsJoystickVirtual(id));
            pads.add(pad);
            added.add(pad);
        }
        return added;
    }

    /**
     * Pads SDL has stopped listing, now removed from {@link #pads()} and closed.
     *
     * <p>A pad SDL stops listing is a pad that was unplugged, and that is the
     * whole test: what the driver reports for a disconnected pad is never asked.
     * An id SDL reuses for a different pad inside one run would be taken for the
     * pad that left, which is untested and has not been seen.
     */
    public List<Pad> departed() {
        List<Pad> gone = goneFrom(list(), pads);
        for (Pad pad : gone) {
            pads.remove(pad);
            SDL_CloseGamepad(pad.handle);
        }
        return gone;
    }

    /** Ids in {@code listed} that no pad in {@code held} has. */
    static List<Integer> addedIds(int[] listed, List<Pad> held) {
        List<Integer> out = new ArrayList<>();
        for (int id : listed) {
            if (withId(held, id) == null) {
                out.add(id);
            }
        }
        return out;
    }

    /** Pads in {@code held} whose id is not in {@code listed}. */
    static List<Pad> goneFrom(int[] listed, List<Pad> held) {
        List<Pad> out = new ArrayList<>();
        for (Pad pad : held) {
            boolean still = false;
            for (int id : listed) {
                if (pad.id == id) {
                    still = true;
                    break;
                }
            }
            if (!still) {
                out.add(pad);
            }
        }
        return out;
    }

    private static Pad withId(List<Pad> pads, int id) {
        for (Pad pad : pads) {
            if (pad.id == id) {
                return pad;
            }
        }
        return null;
    }

    /** The pads SDL has open, in the order it listed them. Possibly none. */
    public List<Pad> pads() {
        return Collections.unmodifiableList(pads);
    }

    /**
     * Reads every pad's current state from the driver.
     *
     * <p>Once a loop is enough: {@link #read} only copies what this call
     * fetched.
     *
     * <p>It pumps the event queue as well, the way {@link #open}'s discovery
     * loop does, so that {@link #arrived()} is listing against the same state
     * discovery lists against. Whether {@code SDL_UpdateGamepads} alone would
     * notice a pad plugged in is unmeasured; pumping costs 0.6 us, measured, so
     * the question was not worth leaving open.
     */
    public void update() {
        SDL_PumpEvents();
        SDL_UpdateGamepads();
    }

    /**
     * Writes one pad's sticks, triggers and buttons into {@code into}.
     *
     * <p>Every value is a pass-through. FTC's y is negative forward and its x
     * positive to the right, and SDL's readings carry the same two signs, so
     * nothing here flips one. The scaling is WPILib's own: a negative reading
     * over 32768 and a positive one over 32767, so a stick at its stop is
     * exactly 1.0 either way, and a trigger is its reading over 32767 for FTC's
     * 0.0 to 1.0.
     *
     * <p>This writes the 21 controls a pad has and nothing else. The nine
     * fields the SDK derives from them, the PS4 aliases and both
     * {@code _trigger_pressed} among them, and the 50 {@code WasPressed} and
     * {@code WasReleased} methods, come from handing this reading to
     * {@code Gamepad.copy}, which is {@link SimPads}'s job. So {@code into} is
     * a staging pad rather than the one an OpMode reads.
     */
    public void read(Pad pad, Gamepad into) {
        into.left_stick_x = stick(pad, SDL_GAMEPAD_AXIS_LEFTX);
        into.left_stick_y = stick(pad, SDL_GAMEPAD_AXIS_LEFTY);
        into.right_stick_x = stick(pad, SDL_GAMEPAD_AXIS_RIGHTX);
        into.right_stick_y = stick(pad, SDL_GAMEPAD_AXIS_RIGHTY);
        into.left_trigger = trigger(pad, SDL_GAMEPAD_AXIS_LEFT_TRIGGER);
        into.right_trigger = trigger(pad, SDL_GAMEPAD_AXIS_RIGHT_TRIGGER);

        into.a = SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_SOUTH);
        into.b = SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_EAST);
        into.x = SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_WEST);
        into.y = SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_NORTH);
        into.back = SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_BACK);
        into.guide = SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_GUIDE);
        into.start = SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_START);
        into.left_stick_button =
                SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_LEFT_STICK);
        into.right_stick_button =
                SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_RIGHT_STICK);
        into.left_bumper =
                SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_LEFT_SHOULDER);
        into.right_bumper =
                SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER);
        into.dpad_up = SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_DPAD_UP);
        into.dpad_down = SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_DPAD_DOWN);
        into.dpad_left = SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_DPAD_LEFT);
        into.dpad_right = SDL_GetGamepadButton(pad.handle, SDL_GAMEPAD_BUTTON_DPAD_RIGHT);
    }

    private static float stick(Pad pad, int axis) {
        return stickScale(SDL_GetGamepadAxis(pad.handle, axis));
    }

    private static float trigger(Pad pad, int axis) {
        return triggerScale(SDL_GetGamepadAxis(pad.handle, axis));
    }

    /**
     * One stick reading as FTC's -1.0 to 1.0.
     *
     * <p>The two ends divide by different numbers because the range is not
     * symmetric: dividing everything by 32767 would make a stick at its stop
     * read 1.00003 one way.
     */
    static float stickScale(short raw) {
        return raw < 0 ? raw / FULL_NEGATIVE : raw / FULL_POSITIVE;
    }

    /** One trigger reading as FTC's 0.0 to 1.0. A resting trigger reads 0. */
    static float triggerScale(short raw) {
        return raw <= 0 ? 0f : raw / FULL_POSITIVE;
    }

    // --- virtual gamepads --------------------------------------------------

    /** What a virtual gamepad calls itself. */
    static final String VIRTUAL_NAME = "Xbox 360 Controller";

    /** A virtual gamepad's axes, in the order its mapping numbers them. */
    static final List<String> VIRTUAL_AXES = Collections.unmodifiableList(Arrays.asList(
            "left_stick_x", "left_stick_y", "right_stick_x", "right_stick_y",
            "left_trigger", "right_trigger"));

    /** Its buttons, in the order its mapping numbers them. */
    static final List<String> VIRTUAL_BUTTONS = Collections.unmodifiableList(Arrays.asList(
            "a", "b", "x", "y", "back", "guide", "start",
            "left_stick_button", "right_stick_button", "left_bumper", "right_bumper",
            "dpad_up", "dpad_down", "dpad_left", "dpad_right"));

    /** SDL's names for the same axes, in the same order. */
    private static final String[] MAPPED_AXES = {
        "leftx", "lefty", "rightx", "righty", "lefttrigger", "righttrigger"};

    /** SDL's names for the same buttons, in the same order. */
    private static final String[] MAPPED_BUTTONS = {
        "a", "b", "x", "y", "back", "guide", "start", "leftstick", "rightstick",
        "leftshoulder", "rightshoulder", "dpup", "dpdown", "dpleft", "dpright"};

    /** One virtual gamepad: its device id, and the joystick its controls are set through. */
    static final class Virtual {
        final int id;
        final long joystick;

        Virtual(int id, long joystick) {
            this.id = id;
            this.joystick = joystick;
        }
    }

    /**
     * Attaches one virtual gamepad, with no serial number, and gives it a
     * mapping, which SDL needs before it lists the device as a gamepad.
     *
     * <p>SDL must already be started, by {@link #open}. The gamepad is found
     * like any other, by {@link #arrived}. The triggers are mapped to half an
     * axis, so an axis at rest reads as a trigger at rest.
     */
    static Virtual attachVirtual() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            SDL_VirtualJoystickDesc desc = SDL_VirtualJoystickDesc.calloc(stack)
                    .version(SDL_VirtualJoystickDesc.SIZEOF)
                    .type((short) SDL_JOYSTICK_TYPE_GAMEPAD)
                    .naxes((short) VIRTUAL_AXES.size())
                    .nbuttons((short) VIRTUAL_BUTTONS.size())
                    .name(stack.UTF8(VIRTUAL_NAME));
            int id = SDL_AttachVirtualJoystick(desc);
            if (id == 0) {
                throw new IllegalStateException("virtual gamepad: " + SDL_GetError());
            }
            SDL_GUID guid = SDL_GetJoystickGUIDForID(id, SDL_GUID.malloc(stack));
            ByteBuffer text = stack.malloc(33);
            SDL_GUIDToString(guid, text);
            StringBuilder mapping = new StringBuilder(
                    StandardCharsets.US_ASCII.decode(text).toString().replace("\0", "").trim())
                    .append(',').append(VIRTUAL_NAME).append(',');
            for (int i = 0; i < MAPPED_BUTTONS.length; i++) {
                mapping.append(MAPPED_BUTTONS[i]).append(":b").append(i).append(',');
            }
            for (int i = 0; i < MAPPED_AXES.length; i++) {
                boolean trigger = MAPPED_AXES[i].endsWith("trigger");
                mapping.append(MAPPED_AXES[i]).append(trigger ? ":+a" : ":a").append(i).append(',');
            }
            if (SDL_AddGamepadMapping(mapping) < 0) {
                throw new IllegalStateException("virtual gamepad mapping: " + SDL_GetError());
            }
            long joystick = SDL_OpenJoystick(id);
            if (joystick == 0L) {
                throw new IllegalStateException("virtual gamepad open: " + SDL_GetError());
            }
            return new Virtual(id, joystick);
        }
    }

    /**
     * Sets one control of a virtual gamepad, which holds until it is set again.
     *
     * <p>{@code control} is one of {@link #VIRTUAL_AXES} or
     * {@link #VIRTUAL_BUTTONS}; a button is pressed for any value but 0. It is
     * read at the next {@link #update}.
     */
    static void setVirtual(Virtual pad, String control, float value) {
        int axis = VIRTUAL_AXES.indexOf(control);
        if (axis >= 0) {
            SDL_SetJoystickVirtualAxis(pad.joystick, axis, rawAxis(value));
        } else {
            SDL_SetJoystickVirtualButton(pad.joystick, VIRTUAL_BUTTONS.indexOf(control),
                    value != 0f);
        }
    }

    /**
     * Closes and detaches a virtual gamepad, which then departs like a cable
     * coming out. Once SDL has quit it has detached them all itself, and this
     * does nothing.
     */
    static void detachVirtual(Virtual pad) {
        if (SDL_WasInit(SDL_INIT_JOYSTICK) == 0) {
            return;
        }
        SDL_CloseJoystick(pad.joystick);
        SDL_DetachVirtualJoystick(pad.id);
    }

    /**
     * The raw reading that {@link #stickScale} or {@link #triggerScale} turns
     * back into {@code value}, the same two divisors run the other way.
     */
    static short rawAxis(float value) {
        long raw = Math.round(value < 0 ? value * FULL_NEGATIVE : value * FULL_POSITIVE);
        return (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, raw));
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        for (Pad pad : pads) {
            SDL_CloseGamepad(pad.handle);
        }
        SDL_Quit();
    }
}
