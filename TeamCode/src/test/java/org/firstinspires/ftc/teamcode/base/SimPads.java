package org.firstinspires.ftc.teamcode.base;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Which pad is {@code gamepad1} and which is {@code gamepad2}.
 *
 * <p>{@link SimGamepad} reads pads; this decides whose readings go where, the
 * way the Driver Station does: hold Start and press A to be {@code gamepad1},
 * Start and B to be {@code gamepad2}.
 *
 * <p>One pad skips the gesture and becomes {@code gamepad1}. Two pads and no
 * assignment means the lesson reads a resting {@code Gamepad} and the robot
 * sits still until somebody claims one; the run is not refused. An assignment is
 * remembered in {@code local.properties}, which is per-machine and gitignored
 * already, keyed on each pad's {@link Key}.
 *
 * <p>Passes when: SimPadsTest.
 */
public final class SimPads implements AutoCloseable {

    static final String KEY1 = "sim.gamepad1.serial";

    static final String KEY2 = "sim.gamepad2.serial";

    static final String ID1 = "sim.gamepad1.id";

    static final String ID2 = "sim.gamepad2.id";

    static final String VERSION = "sim.gamepad.version";

    /** The only version of the {@code sim.gamepad} lines this code reads. */
    static final int FILE_VERSION = 2;

    /** How {@code local.properties} writes a missing serial number. */
    static final String NO_SERIAL = "(no serial number)";

    /** How {@code local.properties} writes an empty serial number. */
    static final String EMPTY_SERIAL = "\"\"";

    /**
     * What a gamepad is known by, in the slots, in {@code local.properties}, in
     * the census and to the gestures.
     *
     * <p>{@code serial} is spelled as the file spells it, so a missing serial
     * number and an empty one are each a value of their own. {@code id} is null
     * when the serial number alone names the gamepad.
     */
    static final class Key {
        final String serial;
        final Integer id;

        Key(String serial, Integer id) {
            this.serial = serial;
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Key)) {
                return false;
            }
            Key other = (Key) o;
            return serial.equals(other.serial) && Objects.equals(id, other.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(serial, id);
        }

        @Override
        public String toString() {
            return id == null ? serial : serial + " " + id;
        }
    }

    /**
     * A gamepad's key: its serial number alone, or with its device id when it
     * reports none, reports {@code ""}, or shares it with another gamepad in
     * {@code all}.
     *
     * <p>Device ids come from one count for every gamepad in a run, so no two
     * plugged-in gamepads share a key.
     */
    static Key key(SimGamepad.Pad pad, List<SimGamepad.Pad> all) {
        if (pad.serial == null) {
            return new Key(NO_SERIAL, pad.id);
        }
        if (pad.serial.isEmpty()) {
            return new Key(EMPTY_SERIAL, pad.id);
        }
        for (SimGamepad.Pad other : all) {
            if (other != pad && pad.serial.equals(other.serial)) {
                return new Key(pad.serial, pad.id);
            }
        }
        return new Key(pad.serial, null);
    }

    /** The gamepad in {@code all} whose key is {@code key}, or null. */
    static SimGamepad.Pad named(List<SimGamepad.Pad> all, Key key) {
        if (key == null) {
            return null;
        }
        for (SimGamepad.Pad pad : all) {
            if (key.equals(key(pad, all))) {
                return pad;
            }
        }
        return null;
    }

    /**
     * How a pad in a slot got there. Remembered, because it cannot be derived.
     */
    enum Source {
        /** Put in a slot {@code local.properties} left free, with no gesture. */
        FREE_SLOT,
        /** Read back from {@code local.properties} by its key. */
        STORED,
        /** Claimed with Start and A, or Start and B, during this run. */
        GESTURE
    }

    /**
     * What a gamepad the library is listing is doing, which is what its census
     * line says.
     *
     * <p>Derived rather than remembered: {@link #state} is a pure function of the
     * gamepads plugged in and the two slots, so a gamepad's state can change
     * because another gamepad moved.
     */
    enum State {
        /** Filling {@code gamepad1} or {@code gamepad2}. */
        ACTIVE,
        /** No slot yet, and one is free for a gesture to fill. */
        UNCLAIMED_FREE,
        /** No slot, and both are taken by other gamepads. */
        UNCLAIMED_FULL
    }

    /** Which of the three states a gamepad is in. Pure. */
    static State state(SimGamepad.Pad pad, SimGamepad.Pad player1, SimGamepad.Pad player2,
            Map<String, String> stored) {
        if (pad == player1 || pad == player2) {
            return State.ACTIVE;
        }
        return freeInFile(stored, KEY1) || freeInFile(stored, KEY2)
                ? State.UNCLAIMED_FREE
                : State.UNCLAIMED_FULL;
    }

    private final SimGamepad sdl;

    private final Path store;

    private final PrintStream out;

    /** Scratch, so a pad can be read for its gesture without reaching a lesson. */
    private final Gamepad probe = new Gamepad();

    /**
     * Where a pad's reading is held before it is copied, one per slot.
     *
     * <p>A reading goes into one of these and is then handed to the slot's own
     * {@code Gamepad.copy}, which is how the robot fills a gamepad: the nine
     * derived fields and every {@code WasPressed} method come out of that call
     * rather than out of this code. Held rather than made each loop, so a pass
     * allocates nothing.
     */
    private final Gamepad staging1 = new Gamepad();

    private final Gamepad staging2 = new Gamepad();

    private final Map<SimGamepad.Pad, Source> source = new LinkedHashMap<>();

    private SimGamepad.Pad player1;

    private SimGamepad.Pad player2;

    /**
     * The off-rest report printed last, so a stick held still prints once.
     *
     * <p>Empty before the first one, which is why a run where nothing has been
     * touched yet does not say so.
     */
    private String lastReport = "";

    /**
     * The census text printed last, so an unchanged census prints nothing.
     *
     * <p>Empty before the first one, which is why a run with nothing plugged in
     * still says so.
     */
    private String lastCensus = "";

    /**
     * False once {@code local.properties} could not be read or written, after
     * which the run goes on with nothing remembered and leaves the file alone.
     */
    private boolean remembering = true;

    SimPads(SimGamepad sdl, Path store, PrintStream out) {
        this.sdl = sdl;
        this.store = store;
        this.out = out;
        assign();
        census();
    }

    /** Opens SDL, loads any stored assignment, and says what it found. */
    public static SimPads open() {
        return open(null);
    }

    /** {@link #open()}, with {@code started} run as {@link SimGamepad#open(Runnable)} runs it. */
    public static SimPads open(Runnable started) {
        return new SimPads(SimGamepad.open(started), localProperties(), System.out);
    }

    /**
     * Reads every pad and hands each player's reading to its {@code Gamepad}.
     *
     * <p>Called once per instant on {@link SimTicker}'s grid, before anything
     * else that instant, so every job that runs sees the same reading. A
     * {@code Gamepad} with no pad behind it reads as untouched.
     */
    public void read(Gamepad gamepad1, Gamepad gamepad2) {
        sdl.update();
        fill(player1, staging1, gamepad1);
        fill(player2, staging2, gamepad2);
    }

    /**
     * Watches every gamepad without a slot for Start and A, or Start and B.
     *
     * <p>Every fifth instant. Faster buys nothing: the gesture is held rather
     * than a press, so 50 ms cannot miss one.
     *
     * <p>A full pair of slots is not a reason to stop watching: a gesture takes
     * an occupied slot and moves whoever was there out of the way. A gamepad
     * already in a slot is watched too, so one gamepad on its own can move
     * itself to the other slot; a gesture that changes nothing is ignored, which
     * is what a held Start and A on the gamepad that already has that slot is.
     */
    public void gestures() {
        List<SimGamepad.Pad> all = sdl.pads();
        for (SimGamepad.Pad pad : all) {
            sdl.read(pad, probe);
            int player = claimedPlayer(probe);
            if (player != 0) {
                take(pad, player);
            }
        }
    }

    /**
     * Fills one slot's {@code Gamepad} the way the robot fills it.
     *
     * <p>An empty slot is reset instead, every loop rather than once when the
     * pad went away, so a robot being driven forward stops when the cable comes
     * out and stays stopped.
     */
    private void fill(SimGamepad.Pad pad, Gamepad staging, Gamepad target) {
        if (pad == null) {
            target.reset();
            return;
        }
        sdl.read(pad, staging);
        target.copy(staging);
    }

    @Override
    public void close() {
        sdl.close();
    }

    /**
     * Picks up a pad plugged in since the last look, and lets go of one
     * unplugged.
     *
     * <p>Every twenty-fifth instant, which is four times a second: a rescan
     * costs about 1 us, measured, so the period is politeness to the outlier
     * rather than to the mean, and it is far faster than a hand with a cable.
     *
     * <p>A pad that goes away releases its player, and {@link #read} resets that
     * player's {@code Gamepad} on the next instant. Each event prints its own
     * line and then {@link #census()} says where every gamepad now stands, so a
     * gamepad whose state changed because another one moved shows up too.
     */
    public void rescan() {
        List<SimGamepad.Pad> gone = sdl.departed();
        for (SimGamepad.Pad pad : gone) {
            source.remove(pad);
            String left = "was not claimed";
            if (pad == player1) {
                player1 = null;
                left = "gamepad1 is back to rest";
            } else if (pad == player2) {
                player2 = null;
                left = "gamepad2 is back to rest";
            }
            out.println("  gone: " + pad + " -- " + left);
        }
        List<SimGamepad.Pad> came = sdl.arrived();
        if (!came.isEmpty()) {
            assign();
            for (SimGamepad.Pad pad : came) {
                out.println("  new gamepad: " + pad);
            }
        }
        if (!gone.isEmpty() || !came.isEmpty()) {
            // A twin coming or going adds or drops a device id from a key.
            store();
        }
        census();
    }

    // --- the assignment ---------------------------------------------------

    /**
     * The assignment before any gesture: the only pad, or what the store says.
     *
     * <p>A stored key is used only when a pad plugged in has it. A stale entry
     * asks for the gesture again rather than silently driving the wrong robot.
     *
     * <p>Called again after a pad arrives, and then it does nothing unless both
     * players are free: a pad arriving beside one that is already driving asks
     * for the gesture rather than reading the store. The gesture is always
     * available, which is what makes the simple rule enough.
     */
    private void assign() {
        Opening open = opening(sdl.pads(), stored(), player1, player2);
        if (open.pad1 == player1 && open.pad2 == player2) {
            return;
        }
        player1 = open.pad1;
        player2 = open.pad2;
        if (open.from1 != null) {
            source.put(player1, open.from1);
        }
        if (open.from2 != null) {
            source.put(player2, open.from2);
        }
        store();
    }

    /** Which gamepad each slot holds, and why, after one pass of the rules. */
    static final class Opening {
        final SimGamepad.Pad pad1;
        final SimGamepad.Pad pad2;
        /** Why {@code pad1} has the slot, or null when this pass did not fill it. */
        final Source from1;
        /** Why {@code pad2} has the slot, or null when this pass did not fill it. */
        final Source from2;

        Opening(SimGamepad.Pad pad1, SimGamepad.Pad pad2, Source from1, Source from2) {
            this.pad1 = pad1;
            this.pad2 = pad2;
            this.from1 = from1;
            this.from2 = from2;
        }
    }

    /**
     * Whether {@code local.properties} leaves this slot free.
     *
     * <p>An entry naming a gamepad that is not plugged in is not free: it holds
     * the slot for that gamepad, which is how two gamepads can both be unclaimed
     * with the robot sitting still. A blank value names nobody and so is no
     * entry at all.
     */
    static boolean freeInFile(Map<String, String> stored, String key) {
        String serial = stored.get(key);
        return serial == null || serial.trim().isEmpty();
    }

    /**
     * The key {@code local.properties} holds for slot 1 or 2, or null when it
     * leaves the slot free.
     *
     * <p>The key has a device id exactly when the slot has an {@code id} line.
     * An id that is not a number is a hand edit, which is not guarded against;
     * it reads as 0, which SDL never hands out, so it names nobody.
     */
    static Key storedKey(Map<String, String> stored, int slot) {
        String serialName = slot == 1 ? KEY1 : KEY2;
        if (freeInFile(stored, serialName)) {
            return null;
        }
        String serial = stored.get(serialName);
        String id = stored.get(slot == 1 ? ID1 : ID2);
        if (id == null) {
            return new Key(serial, null);
        }
        try {
            return new Key(serial, Integer.valueOf(id.trim()));
        } catch (NumberFormatException e) {
            return new Key(serial, 0);
        }
    }

    /**
     * Which slots are filled before any gesture, and why.
     *
     * <p>A slot named in {@code local.properties} wins over the one-gamepad
     * rule, because a gamepad whose key is stored fills the slot named
     * there and only a gamepad not named there falls into a free one. So a
     * gesture that put the only gamepad on {@code gamepad2} still has it there
     * next run, which is how a robot is driven from the second slot with one
     * gamepad in hand.
     *
     * <p>A gamepad the file does not name takes a slot the file leaves free,
     * {@code gamepad1} first, in the order the library lists them, and the
     * caller writes that assignment back so the file goes on being the slot
     * table. A gamepad with no free slot is left for a gesture.
     *
     * <p>A gamepad already holding a slot keeps it: the two held slots go in as
     * arguments and come back untouched, which is the rule that a slot is kept
     * until the cable comes out or a gesture moves it.
     *
     * <p>A key written to both slots names one gamepad for two slots,
     * which one gamepad cannot fill. The first slot wins and the second entry
     * names nobody, so the write that follows clears it and the second slot is
     * free on the next pass rather than on this one.
     */
    static Opening opening(List<SimGamepad.Pad> all, Map<String, String> stored,
            SimGamepad.Pad held1, SimGamepad.Pad held2) {
        SimGamepad.Pad slot1 = held1;
        SimGamepad.Pad slot2 = held2;
        Source from1 = null;
        Source from2 = null;
        SimGamepad.Pad named1 = named(all, storedKey(stored, 1));
        SimGamepad.Pad named2 = named(all, storedKey(stored, 2));
        if (named1 != null && named1 == named2) {
            named2 = null;
        }
        if (slot1 == null && named1 != null && named1 != slot2) {
            slot1 = named1;
            from1 = Source.STORED;
        }
        if (slot2 == null && named2 != null && named2 != slot1) {
            slot2 = named2;
            from2 = Source.STORED;
        }
        for (SimGamepad.Pad pad : all) {
            if (pad == slot1 || pad == slot2 || pad == named1 || pad == named2) {
                continue;
            }
            if (slot1 == null && freeInFile(stored, KEY1)) {
                slot1 = pad;
                from1 = Source.FREE_SLOT;
            } else if (slot2 == null && freeInFile(stored, KEY2)) {
                slot2 = pad;
                from2 = Source.FREE_SLOT;
            }
        }
        return new Opening(slot1, slot2, from1, from2);
    }

    /**
     * The slot a gamepad's current state claims, or 0 for none.
     *
     * <p>Held, not toggled: the gesture is Start down and A down at the same
     * moment, the way the Driver Station's is, so pressing A on its own does
     * nothing. Whether the slot is free is not asked, because a gesture takes an
     * occupied slot.
     */
    static int claimedPlayer(Gamepad state) {
        if (!state.start) {
            return 0;
        }
        if (state.a) {
            return 1;
        }
        if (state.b) {
            return 2;
        }
        return 0;
    }

    /**
     * The two slots after a gesture: {@code gamepad1} at 0, {@code gamepad2} at
     * 1, either of them null for empty.
     *
     * <p>Pure, and it is the whole slot rule for a gesture. The gamepad making
     * the gesture takes the slot it asked for, whoever was in it. The gamepad
     * displaced moves to the other slot when that one is free, and is left with
     * none when it is not, which takes three gamepads to happen. A gamepad holds
     * one slot, so one claiming the other slot lets go of the one it had, and the
     * two swap.
     */
    static SimGamepad.Pad[] displace(SimGamepad.Pad claimant, int player,
            SimGamepad.Pad player1, SimGamepad.Pad player2) {
        SimGamepad.Pad wanted = player == 1 ? player1 : player2;
        SimGamepad.Pad other = player == 1 ? player2 : player1;
        if (wanted == claimant) {
            return new SimGamepad.Pad[] {player1, player2};
        }
        if (other == claimant) {
            other = null;
        }
        if (other == null) {
            other = wanted;
        }
        return player == 1
                ? new SimGamepad.Pad[] {claimant, other}
                : new SimGamepad.Pad[] {other, claimant};
    }

    private void take(SimGamepad.Pad pad, int player) {
        SimGamepad.Pad[] slots = displace(pad, player, player1, player2);
        if (slots[0] == player1 && slots[1] == player2) {
            return;
        }
        SimGamepad.Pad was1 = player1;
        SimGamepad.Pad was2 = player2;
        player1 = slots[0];
        player2 = slots[1];
        // Only a gamepad whose slot changed had it decided by this gesture.
        if (player1 != null && player1 != was1) {
            source.put(player1, Source.GESTURE);
        }
        if (player2 != null && player2 != was2) {
            source.put(player2, Source.GESTURE);
        }
        out.println("  claimed: " + pad + " -- gamepad" + player);
        store();
        census();
    }

    // --- the store --------------------------------------------------------

    /** The repository's own {@code local.properties}, found by walking up. */
    static Path localProperties() {
        File dir = new File(".").getAbsoluteFile();
        while (dir != null) {
            if (new File(dir, "settings.gradle").isFile()) {
                return dir.toPath().resolve("local.properties");
            }
            dir = dir.getParentFile();
        }
        return new File("local.properties").getAbsoluteFile().toPath();
    }

    /**
     * Every {@code key=value} line, in order, with comments and blanks dropped.
     *
     * <p>Hand-parsed rather than {@code Properties.load}, because writing it
     * back has to leave every other line of somebody's {@code local.properties}
     * alone, {@code sdk.dir} and its header comment included.
     */
    static Map<String, String> read(Path file) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String line : lines(file)) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("!")) {
                continue;
            }
            int equals = trimmed.indexOf('=');
            if (equals > 0) {
                values.put(trimmed.substring(0, equals).trim(),
                        trimmed.substring(equals + 1).trim());
            }
        }
        return values;
    }

    private static List<String> lines(Path file) {
        try {
            return Files.exists(file)
                    ? Files.readAllLines(file, StandardCharsets.UTF_8)
                    : new ArrayList<String>();
        } catch (IOException e) {
            throw new IllegalStateException("cannot read " + file, e);
        }
    }

    /**
     * The file's values, read through {@link #load}, or nothing once the file
     * has been found unreadable or unwritable.
     */
    private Map<String, String> stored() {
        if (!remembering) {
            return new LinkedHashMap<>();
        }
        Loaded loaded = load(store);
        if (loaded.said != null) {
            out.println(loaded.said);
        }
        if (loaded.stored == null) {
            remembering = false;
            return new LinkedHashMap<>();
        }
        return loaded.stored;
    }

    private void store() {
        Map<String, String> stored = stored();
        if (!remembering) {
            return;
        }
        List<SimGamepad.Pad> all = sdl.pads();
        String said = save(store, slotLine(player1, storedKey(stored, 1), all),
                slotLine(player2, storedKey(stored, 2), all));
        if (said != null) {
            remembering = false;
            out.println(said);
        }
    }

    /**
     * Why the file's {@code sim.gamepad} lines are not version 2, or null when
     * they are.
     */
    static String versionProblem(Map<String, String> stored) {
        String version = stored.get(VERSION);
        if (version == null) {
            return "it had no " + VERSION;
        }
        try {
            if (Integer.parseInt(version) == FILE_VERSION) {
                return null;
            }
            return VERSION + "=" + version + " is not " + FILE_VERSION;
        } catch (NumberFormatException e) {
            return VERSION + "=" + version + " is not an integer";
        }
    }

    /**
     * What a run reads from the file: its values, or null when nothing is to
     * be remembered, and the line to print about it, or null.
     */
    static final class Loaded {
        final Map<String, String> stored;
        final String said;

        Loaded(Map<String, String> stored, String said) {
            this.stored = stored;
            this.said = said;
        }
    }

    /**
     * Reads the file, and resets its {@code sim.gamepad} lines to an empty
     * version 2 set when they are any other version.
     *
     * <p>Reading and parsing sit in one exception block. A file that throws on
     * reading is left as it is, since its other lines, {@code sdk.dir} among
     * them, cannot be kept without being read.
     */
    static Loaded load(Path file) {
        Map<String, String> stored;
        String problem;
        try {
            stored = read(file);
            problem = versionProblem(stored);
        } catch (RuntimeException e) {
            return new Loaded(null, "  local.properties could not be read (" + reason(e)
                    + "), so no gamepad is remembered this run.");
        }
        if (problem == null) {
            return new Loaded(stored, null);
        }
        String said = "  The gamepad lines in local.properties were reset, because "
                + problem + ".";
        String failed = save(file, null, null);
        if (failed != null) {
            return new Loaded(null, said + "\n" + failed);
        }
        Map<String, String> fresh = new LinkedHashMap<>();
        fresh.put(VERSION, Integer.toString(FILE_VERSION));
        return new Loaded(fresh, said);
    }

    /** Writes the slots, or returns the line saying why it could not. */
    static String save(Path file, Key key1, Key key2) {
        try {
            write(file, key1, key2);
            return null;
        } catch (RuntimeException e) {
            return "  local.properties could not be saved (" + reason(e)
                    + "), so no gamepad is remembered from now on.";
        }
    }

    private static String reason(RuntimeException e) {
        return String.valueOf(e.getCause() != null ? e.getCause() : e);
    }

    /**
     * What the file should say about one slot, given what holds it.
     *
     * <p>A gamepad in the slot writes its own key, as it is now, so an entry
     * gains or loses its device id as a twin comes and goes. An empty slot keeps
     * the entry it had when no gamepad present answers to it, because that entry
     * is holding the slot for a gamepad that is unplugged and only a gesture
     * changes it. An empty slot whose entry names a gamepad that is plugged in
     * loses it: that gamepad is somewhere else, or the entry is the second half
     * of one key written to both slots.
     */
    static Key slotLine(SimGamepad.Pad held, Key stored, List<SimGamepad.Pad> all) {
        if (held != null) {
            return key(held, all);
        }
        return named(all, stored) == null ? stored : null;
    }

    /**
     * Rewrites only the {@code sim.gamepad} lines, in place where they exist and
     * appended where they do not, the version line first.
     */
    static void write(Path file, Key key1, Key key2) {
        List<String> out = new ArrayList<>();
        boolean wroteVersion = false;
        boolean wrote1 = false;
        boolean wrote2 = false;
        for (String line : lines(file)) {
            String name = line.trim().startsWith("#") ? "" : name(line);
            boolean ours = VERSION.equals(name) || KEY1.equals(name) || ID1.equals(name)
                    || KEY2.equals(name) || ID2.equals(name);
            if (ours && !wroteVersion) {
                out.add(VERSION + "=" + FILE_VERSION);
                wroteVersion = true;
            }
            if (VERSION.equals(name)) {
                continue;
            } else if (KEY1.equals(name) || ID1.equals(name)) {
                if (!wrote1) {
                    add(out, KEY1, ID1, key1);
                    wrote1 = true;
                }
            } else if (KEY2.equals(name) || ID2.equals(name)) {
                if (!wrote2) {
                    add(out, KEY2, ID2, key2);
                    wrote2 = true;
                }
            } else {
                out.add(line);
            }
        }
        if (!wroteVersion) {
            out.add(VERSION + "=" + FILE_VERSION);
        }
        if (!wrote1) {
            add(out, KEY1, ID1, key1);
        }
        if (!wrote2) {
            add(out, KEY2, ID2, key2);
        }
        try {
            Files.write(file, out, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("cannot write " + file, e);
        }
    }

    private static void add(List<String> out, String serialName, String idName, Key key) {
        if (key == null) {
            return;
        }
        out.add(serialName + "=" + key.serial);
        if (key.id != null) {
            out.add(idName + "=" + key.id);
        }
    }

    private static String name(String line) {
        int equals = line.indexOf('=');
        return equals > 0 ? line.substring(0, equals).trim() : "";
    }

    // --- the display ------------------------------------------------------

    /**
     * One line per gamepad the library is listing, each saying what that gamepad
     * is doing, and nothing at all when it all reads the same as last time.
     *
     * <p>Startup is the first census rather than a shape of its own, so the
     * lines a student sees before a run are the lines a cable coming out
     * produces. The event lines that name what happened are printed by whoever
     * saw it happen; this says where everything stands afterwards.
     */
    private void census() {
        String body = censusText(sdl.pads(), player1, player2, source, stored());
        if (body.equals(lastCensus)) {
            return;
        }
        lastCensus = body;
        out.print(body);
    }

    /**
     * The whole census as text: one line per gamepad, and the line asking for
     * the gesture when some gamepad has no slot.
     *
     * <p>Pure, so that what the census says can be read in a test where two
     * gamepads and a shared serial number are a list rather than a bench. The
     * same gamepads and the same slots give the same text, which is how
     * {@link #census()} knows nothing changed.
     */
    static String censusText(List<SimGamepad.Pad> pads, SimGamepad.Pad player1,
            SimGamepad.Pad player2, Map<SimGamepad.Pad, Source> source,
            Map<String, String> stored) {
        if (pads.isEmpty()) {
            return "No gamepad is plugged in. A gamepad object with no gamepad in its"
                    + " slot reads as untouched.\n";
        }
        StringBuilder text = new StringBuilder();
        boolean settled = true;
        for (SimGamepad.Pad pad : pads) {
            State s = state(pad, player1, player2, stored);
            if (s != State.ACTIVE) {
                settled = false;
            }
            text.append("  ").append(pad).append(" -- ")
                    .append(explain(pad, s, player1, source, key(pad, pads).id != null))
                    .append('\n');
        }
        if (!settled) {
            text.append("Hold Start and press A to drive as gamepad1,"
                    + " or Start and B for gamepad2.\n");
        }
        return text.toString();
    }

    /** What one gamepad's census line says after the two dashes. */
    private static String explain(SimGamepad.Pad pad, State s, SimGamepad.Pad player1,
            Map<SimGamepad.Pad, Source> source, boolean byId) {
        switch (s) {
            case ACTIVE:
                return (pad == player1 ? "gamepad1, " : "gamepad2, ")
                        + because(source.get(pad), byId);
            case UNCLAIMED_FULL:
                return "not claimed; both slots are taken, and a gesture takes one anyway";
            default:
                return "not claimed yet";
        }
    }

    /**
     * Why the gamepad in a slot is the one in it; {@code byId} when its key holds
     * its device id.
     */
    static String because(Source from, boolean byId) {
        if (from == Source.FREE_SLOT) {
            return "a slot local.properties left free";
        }
        if (from == Source.STORED) {
            return byId
                    ? "remembered in local.properties by serial number and device id"
                    : "remembered in local.properties by serial number";
        }
        return "claimed this run";
    }

    // --- the off-rest report ----------------------------------------------

    /** How far a stick or trigger moves before the report names it. */
    static final float OFF_REST = 0.05f;

    /**
     * Every control of one gamepad that is away from its untouched value, as
     * {@code name=value}.
     *
     * <p>Pure, and the threshold is what needs saying: a stick that has been
     * pushed and let go can sit a count or two off centre, about 0.00003 of full
     * scale, so without it a run would name that stick for ever. A button is
     * away when it is pressed.
     *
     * <p>The names come from the sets {@link SimArgs} holds and the values off
     * {@code Gamepad}'s own fields, so this and the control options cannot come
     * to disagree about what a control is called.
     */
    static List<String> offRest(Gamepad state) {
        List<String> touched = new ArrayList<>();
        for (String name : SimArgs.AXES) {
            float value = axis(state, name);
            if (Math.abs(value) > OFF_REST) {
                touched.add(name + "=" + String.format(Locale.US, "%.2f", value));
            }
        }
        for (String name : SimArgs.BUTTONS) {
            if (button(state, name)) {
                touched.add(name + "=true");
            }
        }
        return touched;
    }

    private static float axis(Gamepad state, String name) {
        try {
            return Gamepad.class.getField(name).getFloat(state);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("no gamepad field called \"" + name + "\"", e);
        }
    }

    private static boolean button(Gamepad state, String name) {
        try {
            return Gamepad.class.getField(name).getBoolean(state);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("no gamepad field called \"" + name + "\"", e);
        }
    }

    /**
     * What every gamepad being read is having done to it, and one line when
     * everything has been let go.
     *
     * <p>Every twenty-fifth instant, and only when it reads differently from
     * last time, so a stick held still says so once rather than four times a
     * second. The reading is the one {@link #read} already took this instant for
     * a gamepad in a slot, and a fresh one for a gamepad that is only being
     * watched for the gesture.
     */
    public void report() {
        List<SimGamepad.Pad> all = sdl.pads();
        Map<String, String> stored = stored();
        StringBuilder text = new StringBuilder();
        for (SimGamepad.Pad pad : all) {
            State s = state(pad, player1, player2, stored);
            List<String> touched = offRest(reading(pad, s));
            if (touched.isEmpty()) {
                continue;
            }
            String slot = s != State.ACTIVE ? "no slot"
                    : pad == player1 ? "gamepad1" : "gamepad2";
            text.append("  ").append(pad).append(" -- ").append(slot).append(':');
            for (String control : touched) {
                text.append(' ').append(control);
            }
            text.append('\n');
        }
        String body = text.toString();
        if (body.equals(lastReport)) {
            return;
        }
        lastReport = body;
        out.print(body.isEmpty() ? "  Nothing is touched.\n" : body);
    }

    private Gamepad reading(SimGamepad.Pad pad, State s) {
        if (s == State.ACTIVE) {
            return pad == player1 ? staging1 : staging2;
        }
        sdl.read(pad, probe);
        return probe;
    }
}
