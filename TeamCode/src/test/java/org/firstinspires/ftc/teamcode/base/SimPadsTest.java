package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * The parts of {@link SimPads} that can go wrong without a pad plugged in:
 * whether an empty slot reads untouched, what a gamepad is known by, whether a
 * stored key names a gamepad, and whether writing the assignment leaves the rest
 * of somebody's {@code local.properties} alone.
 *
 * <p>The gesture itself needs a hand on a pad and is not here.
 */
public final class SimPadsTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private static SimGamepad.Pad pad(int id, String serial) {
        return new SimGamepad.Pad(id, 0L, "pad " + id, serial);
    }

    /** A key named by its serial number alone. */
    private static SimPads.Key k(String serial) {
        return new SimPads.Key(serial, null);
    }

    /** A key named by its serial number and device id. */
    private static SimPads.Key k(String serial, int id) {
        return new SimPads.Key(serial, id);
    }

    /** How {@code local.properties} writes a missing serial number and an empty one. */
    private static final String NONE = "(no serial number)";

    private static final String EMPTY = "\"\"";

    // --- an empty slot reads untouched ------------------------------------

    /**
     * {@code reset()} is what an empty slot gets, in place of a list of fields
     * written out by hand. This compares every field the SDK declares, so a
     * field added to {@code Gamepad} later cannot slip past by being missing
     * from a list nobody updated.
     */
    @Test
    public void anEmptySlotReadsLikeAFreshGamepad() throws Exception {
        Gamepad held = new Gamepad();
        held.left_stick_y = -1f;
        held.right_stick_x = 0.5f;
        held.left_trigger = 1f;
        held.right_trigger = 1f;
        held.a = true;
        held.start = true;
        held.dpad_left = true;
        held.left_bumper = true;
        held.right_stick_button = true;

        held.reset();

        Gamepad fresh = new Gamepad();
        List<String> differing = new ArrayList<>();
        for (Field f : Gamepad.class.getFields()) {
            if (Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            if (!String.valueOf(f.get(held)).equals(String.valueOf(f.get(fresh)))) {
                differing.add(f.getName());
            }
        }
        assertEquals("fields a fresh pad and a reset one disagree on",
                Collections.emptyList(), differing);
    }

    // --- what a gamepad is known by ---------------------------------------

    @Test
    public void aSerialNumberOfItsOwnIsTheWholeKey() {
        SimGamepad.Pad xbox = pad(1, "1DD5F3D");
        List<SimGamepad.Pad> pads = Arrays.asList(xbox, pad(2, "BBB"));
        assertEquals(k("1DD5F3D"), SimPads.key(xbox, pads));
        SimGamepad.Pad one = pad(3, "X");
        assertEquals("one character is a serial number",
                k("X"), SimPads.key(one, Collections.singletonList(one)));
    }

    @Test
    public void noSerialNumberAndAnEmptyOneEachTakeTheDeviceId() {
        SimGamepad.Pad none = pad(2, null);
        SimGamepad.Pad empty = pad(5, "");
        List<SimGamepad.Pad> pads = Arrays.asList(none, empty);
        assertEquals(k(NONE, 2), SimPads.key(none, pads));
        assertEquals(k(EMPTY, 5), SimPads.key(empty, pads));
    }

    @Test
    public void aMissingSerialNumberAnEmptyOneAndTextNeverMatchOneAnother() {
        SimGamepad.Pad none = pad(2, null);
        SimGamepad.Pad empty = pad(2, "");
        assertFalse("the same device id does not make them one key",
                SimPads.key(none, Collections.singletonList(none))
                        .equals(SimPads.key(empty, Collections.singletonList(empty))));
        assertFalse(k(NONE, 2).equals(k("X", 2)));
        assertFalse(k(EMPTY, 2).equals(k("X", 2)));
    }

    @Test
    public void aTwinAddsTheDeviceIdToBothKeysAndTakesItAwayWhenItGoes() {
        SimGamepad.Pad first = pad(1, "SAME");
        SimGamepad.Pad twin = pad(2, "SAME");
        List<SimGamepad.Pad> both = Arrays.asList(first, twin);
        assertEquals(k("SAME", 1), SimPads.key(first, both));
        assertEquals(k("SAME", 2), SimPads.key(twin, both));
        assertEquals("the twin is unplugged",
                k("SAME"), SimPads.key(first, Collections.singletonList(first)));
    }

    // --- what fills a slot before any gesture -----------------------------

    private static Map<String, String> storedAs(String... keysAndValues) {
        Map<String, String> out = new HashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            out.put(keysAndValues[i], keysAndValues[i + 1]);
        }
        return out;
    }

    private static SimPads.Opening open(List<SimGamepad.Pad> pads, Map<String, String> stored) {
        return SimPads.opening(pads, stored, null, null);
    }

    /** The state with no file at all, for the cases the file cannot change. */
    private static SimPads.State stateOf(SimGamepad.Pad pad,
            SimGamepad.Pad player1, SimGamepad.Pad player2) {
        return SimPads.state(pad, player1, player2, storedAs());
    }

    /** The census with no file at all, for the cases the file cannot change. */
    private static String censusOf(List<SimGamepad.Pad> pads, SimGamepad.Pad player1,
            SimGamepad.Pad player2, Map<SimGamepad.Pad, SimPads.Source> source) {
        return SimPads.censusText(pads, player1, player2, source, storedAs());
    }

    @Test
    public void aStoredSecondSlotBeatsAFreeFirstSlot() {
        List<SimGamepad.Pad> pads = Collections.singletonList(pad(1, "AAA"));
        SimPads.Opening got = open(pads, storedAs(SimPads.KEY2, "AAA"));
        assertNull("gamepad1 stays empty, so a robot can be driven from gamepad2", got.pad1);
        assertSame("the gamepad the file names is in the slot it names", pads.get(0), got.pad2);
        assertEquals(SimPads.Source.STORED, got.from2);
    }

    @Test
    public void aGamepadTheFileDoesNotNameTakesTheFirstFreeSlot() {
        List<SimGamepad.Pad> pads = Collections.singletonList(pad(1, "AAA"));
        SimPads.Opening got = open(pads, storedAs());
        assertSame(pads.get(0), got.pad1);
        assertNull(got.pad2);
        assertEquals(SimPads.Source.FREE_SLOT, got.from1);
    }

    @Test
    public void anEntryNamingAnAbsentGamepadHoldsItsSlotAndPushesTheOtherGamepadOn() {
        List<SimGamepad.Pad> pads = Collections.singletonList(pad(1, "AAA"));
        SimPads.Opening got = open(pads, storedAs(SimPads.KEY1, "BBB"));
        assertNull("gamepad1 is held for BBB, which is not plugged in", got.pad1);
        assertSame("so the gamepad that is takes the slot the file leaves free",
                pads.get(0), got.pad2);
        assertEquals(SimPads.Source.FREE_SLOT, got.from2);
    }

    @Test
    public void bothEntriesNamingAbsentGamepadsLeaveEveryGamepadUnclaimed() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(1, "AAA"), pad(2, "BBB"));
        SimPads.Opening got = open(pads, storedAs(SimPads.KEY1, "CCC", SimPads.KEY2, "DDD"));
        assertNull("no slot is free, so the robot sits still until a gesture", got.pad1);
        assertNull(got.pad2);
    }

    @Test
    public void oneSerialNumberInBothSlotsFillsTheFirstAndLeavesTheSecondEmpty() {
        List<SimGamepad.Pad> pads = Collections.singletonList(pad(1, "AAA"));
        SimPads.Opening got = open(pads, storedAs(SimPads.KEY1, "AAA", SimPads.KEY2, "AAA"));
        assertSame("the gamepad the file names still drives", pads.get(0), got.pad1);
        assertNull("one gamepad cannot fill two slots", got.pad2);
        assertEquals("the file named it, so that is where the slot came from",
                SimPads.Source.STORED, got.from1);
    }

    @Test
    public void oneSerialNumberInBothSlotsFillsTheFirstWithOtherGamepadsPluggedIn() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(1, "AAA"), pad(2, "BBB"));
        SimPads.Opening got = open(pads, storedAs(SimPads.KEY1, "AAA", SimPads.KEY2, "AAA"));
        assertSame("the gamepad the file names takes gamepad1", pads.get(0), got.pad1);
        assertNull("the second entry still names AAA, so the slot is not free this pass",
                got.pad2);
        assertEquals(SimPads.Source.STORED, got.from1);
    }

    @Test
    public void twoGamepadsAndAnEmptyFileFillBothSlotsInTheOrderTheyAppear() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(1, "AAA"), pad(2, "BBB"));
        SimPads.Opening got = open(pads, storedAs());
        assertSame("gamepad1 first", pads.get(0), got.pad1);
        assertSame(pads.get(1), got.pad2);
        assertEquals(SimPads.Source.FREE_SLOT, got.from1);
        assertEquals(SimPads.Source.FREE_SLOT, got.from2);
    }

    @Test
    public void aThirdGamepadFindsNoFreeSlotOnceTheFileNamesBoth() {
        List<SimGamepad.Pad> pads =
                Arrays.asList(pad(1, "AAA"), pad(2, "BBB"), pad(3, "CCC"));
        SimPads.Opening got = SimPads.opening(pads,
                storedAs(SimPads.KEY1, "AAA", SimPads.KEY2, "BBB"), pads.get(0), pads.get(1));
        assertSame(pads.get(0), got.pad1);
        assertSame(pads.get(1), got.pad2);
        assertNull("nothing was filled this pass, so nothing is written", got.from1);
        assertNull(got.from2);
    }

    @Test
    public void aGamepadHoldingASlotKeepsItWhenTheRulesRunAgain() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(1, "AAA"), pad(2, "BBB"));
        SimPads.Opening got = SimPads.opening(pads,
                storedAs(SimPads.KEY1, "BBB"), pads.get(0), null);
        assertSame("the file names BBB for gamepad1, but AAA is holding it", pads.get(0), got.pad1);
        assertNull("and BBB does not fall into gamepad2, because the file names it for gamepad1",
                got.pad2);
    }

    @Test
    public void bothStoredSlotsAreFilledWhenBothGamepadsAreThere() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(1, "AAA"), pad(2, "BBB"));
        SimPads.Opening got = open(pads, storedAs(SimPads.KEY1, "BBB", SimPads.KEY2, "AAA"));
        assertSame(pads.get(1), got.pad1);
        assertSame(pads.get(0), got.pad2);
        assertEquals(SimPads.Source.STORED, got.from1);
        assertEquals(SimPads.Source.STORED, got.from2);
    }

    @Test
    public void aBlankEntryIsNoEntryAtAllAndLeavesTheSlotFree() {
        List<SimGamepad.Pad> pads = Collections.singletonList(pad(1, "AAA"));
        assertTrue("no entry", SimPads.freeInFile(storedAs(), SimPads.KEY1));
        assertTrue("a blank value names nobody", SimPads.freeInFile(storedAs(SimPads.KEY1, " "),
                SimPads.KEY1));
        assertFalse("an entry naming somebody holds the slot",
                SimPads.freeInFile(storedAs(SimPads.KEY1, "BBB"), SimPads.KEY1));
        SimPads.Opening got = open(pads, storedAs(SimPads.KEY1, ""));
        assertSame("so the gamepad takes gamepad1", pads.get(0), got.pad1);
    }

    @Test
    public void anEmptySlotKeepsAnEntryHoldingItForAGamepadThatIsUnplugged() {
        List<SimGamepad.Pad> pads = Collections.singletonList(pad(1, "AAA"));
        assertEquals("the reservation survives a write for the other slot",
                k("GONE"), SimPads.slotLine(null, k("GONE"), pads));
        assertNull("nothing held it and nothing was reserved",
                SimPads.slotLine(null, null, pads));
        assertEquals("a gamepad in the slot writes its own key",
                k("AAA"), SimPads.slotLine(pads.get(0), k("GONE"), pads));
        assertNull("an entry naming a gamepad that is here is stale, so it goes",
                SimPads.slotLine(null, k("AAA"), pads));
    }

    @Test
    public void aPadWithNoSerialNumberHoldingASlotWritesItsKey() {
        SimGamepad.Pad nameless = pad(1, null);
        List<SimGamepad.Pad> pads = Collections.singletonList(nameless);
        assertEquals("so the slot is remembered between runs",
                k(NONE, 1), SimPads.slotLine(nameless, null, pads));
        assertEquals("a gesture can put it in a slot an absent gamepad had reserved,"
                        + " and then that reservation goes",
                k(NONE, 1), SimPads.slotLine(nameless, k("GONE"), pads));
    }

    @Test
    public void aTwinComingAndGoingRewritesTheEntryWithTheKeyAsItIsNow() {
        SimGamepad.Pad first = pad(1, "SAME");
        List<SimGamepad.Pad> both = Arrays.asList(first, pad(2, "SAME"));
        assertEquals("the twin is plugged in, so the entry gains the device id",
                k("SAME", 1), SimPads.slotLine(first, k("SAME"), both));
        assertEquals("the twin is unplugged, so the entry loses it",
                k("SAME"), SimPads.slotLine(first, k("SAME", 1),
                        Collections.singletonList(first)));
        assertEquals("an empty slot held by the twin that left keeps its entry",
                k("SAME", 2), SimPads.slotLine(null, k("SAME", 2),
                        Collections.singletonList(first)));
    }

    // --- whether a stored key names a gamepad -----------------------------

    @Test
    public void oneKeyNamingOnePadIsTheAssignment() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(1, "AAA"), pad(2, "BBB"));
        assertSame(pads.get(1), SimPads.named(pads, k("BBB")));
    }

    @Test
    public void aKeyNoPadHasIsNotTrusted() {
        List<SimGamepad.Pad> pads = Collections.singletonList(pad(1, "AAA"));
        assertNull("the pad it named is unplugged", SimPads.named(pads, k("BBB")));
        assertNull("nothing stored", SimPads.named(pads, null));
    }

    @Test
    public void aStoredMissingSerialNumberDoesNotNameAnEmptyOneNorTheOtherWayRound() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(2, null), pad(5, ""));
        assertSame(pads.get(0), SimPads.named(pads, k(NONE, 2)));
        assertSame(pads.get(1), SimPads.named(pads, k(EMPTY, 5)));
        assertNull(SimPads.named(pads, k(EMPTY, 2)));
        assertNull(SimPads.named(pads, k(NONE, 5)));
    }

    @Test
    public void aStoredKeyWithAnotherDeviceIdNamesNobody() {
        List<SimGamepad.Pad> pads = Collections.singletonList(pad(3, null));
        assertNull("plugged back in, so a new id", SimPads.named(pads, k(NONE, 2)));
        assertNull("an entry with no id line names no gamepad without a serial number",
                SimPads.named(pads, k(NONE)));
    }

    @Test
    public void aStoredSerialNumberAloneNamesNeitherTwin() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(3, "SAME"), pad(7, "SAME"));
        assertNull(SimPads.named(pads, k("SAME")));
        assertSame(pads.get(0), SimPads.named(pads, k("SAME", 3)));
        assertSame(pads.get(1), SimPads.named(pads, k("SAME", 7)));
    }

    // --- gamepads known by their device id --------------------------------

    @Test
    public void twinsEachFillASlotRatherThanOneBeingPassedOver() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(7, "SAME"), pad(3, "SAME"));
        SimPads.Opening got = open(pads, storedAs());
        assertSame("in the order found, not by device id", pads.get(0), got.pad1);
        assertSame(pads.get(1), got.pad2);
    }

    @Test
    public void twoGamepadsWithNoSerialNumberBothFillSlots() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(1, null), pad(2, null));
        SimPads.Opening got = open(pads, storedAs());
        assertSame(pads.get(0), got.pad1);
        assertSame(pads.get(1), got.pad2);
    }

    @Test
    public void twinsFindAnEntryForTheSerialNumberAloneMatchingNeither() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(1, "SAME"), pad(2, "SAME"));
        SimPads.Opening got = open(pads, storedAs(SimPads.KEY1, "SAME"));
        assertNull("gamepad1 is held for SAME on its own, which neither twin is", got.pad1);
        assertSame("so the first twin found takes the slot the file leaves free",
                pads.get(0), got.pad2);
    }

    @Test
    public void oneSerialNumberWithTwoDeviceIdsIsTwoGamepadsInTwoSlots() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(1, "SAME"), pad(2, "SAME"));
        SimPads.Opening got = open(pads, storedAs(SimPads.KEY1, "SAME", SimPads.ID1, "2",
                SimPads.KEY2, "SAME", SimPads.ID2, "1"));
        assertSame(pads.get(1), got.pad1);
        assertSame(pads.get(0), got.pad2);
        assertEquals(SimPads.Source.STORED, got.from1);
        assertEquals(SimPads.Source.STORED, got.from2);
    }

    @Test
    public void oneKeyWithADeviceIdInBothSlotsFillsTheFirstOnly() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(1, null), pad(2, null));
        SimPads.Opening got = open(pads, storedAs(SimPads.KEY1, NONE, SimPads.ID1, "1",
                SimPads.KEY2, NONE, SimPads.ID2, "1"));
        assertSame(pads.get(0), got.pad1);
        assertNull("the second entry still names it, so the slot is not free this pass",
                got.pad2);
    }

    @Test
    public void aGamepadWithNoSerialNumberIsRememberedByItsKey() {
        SimGamepad.Pad xbox = pad(1, "1DD5F3D");
        SimGamepad.Pad logitech = pad(2, null);
        SimPads.Opening got = open(Arrays.asList(xbox, logitech),
                storedAs(SimPads.KEY1, "1DD5F3D", SimPads.KEY2, NONE, SimPads.ID2, "2"));
        assertSame(xbox, got.pad1);
        assertSame(logitech, got.pad2);
        assertEquals(SimPads.Source.STORED, got.from2);
    }

    @Test
    public void aGamepadKnownByItsIdPluggedBackInWaitsWhileItsOldEntryHoldsTheSlot() {
        SimGamepad.Pad xbox = pad(1, "1DD5F3D");
        SimGamepad.Pad logitech = pad(3, null);
        Map<String, String> stored =
                storedAs(SimPads.KEY1, "1DD5F3D", SimPads.KEY2, NONE, SimPads.ID2, "2");
        SimPads.Opening got = SimPads.opening(Arrays.asList(xbox, logitech), stored, xbox, null);
        assertNull("gamepad2 is held for id 2, and it came back as id 3", got.pad2);
        assertEquals(SimPads.State.UNCLAIMED_FULL,
                SimPads.state(logitech, xbox, null, stored));
    }

    @Test
    public void aGamepadWithASerialNumberOfItsOwnPluggedBackInGoesBackToItsSlot() {
        SimGamepad.Pad logitech = pad(2, null);
        SimGamepad.Pad xbox = pad(4, "1DD5F3D");
        SimPads.Opening got = SimPads.opening(Arrays.asList(logitech, xbox),
                storedAs(SimPads.KEY1, "1DD5F3D", SimPads.KEY2, NONE, SimPads.ID2, "2"),
                null, logitech);
        assertSame("its device id went from 1 to 4, and its key did not change", xbox, got.pad1);
        assertEquals(SimPads.Source.STORED, got.from1);
    }

    // --- which of the three states a gamepad is in ------------------------

    @Test
    public void theStatesAreActiveAndTwoKindsOfUnclaimed() {
        assertEquals(Arrays.asList(SimPads.State.ACTIVE, SimPads.State.UNCLAIMED_FREE,
                        SimPads.State.UNCLAIMED_FULL),
                Arrays.asList(SimPads.State.values()));
    }

    @Test
    public void aGamepadInASlotIsActive() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(1, "AAA"), pad(2, "BBB"));
        assertEquals(SimPads.State.ACTIVE,
                stateOf(pads.get(0), pads.get(0), null));
        assertEquals(SimPads.State.ACTIVE,
                stateOf(pads.get(1), null, pads.get(1)));
    }

    @Test
    public void aGamepadWithNoSerialNumberWaitsForASlotRatherThanBeingRefused() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(1, null), pad(2, ""));
        assertEquals(SimPads.State.UNCLAIMED_FREE,
                stateOf(pads.get(0), null, null));
        assertEquals("an empty string either",
                SimPads.State.UNCLAIMED_FREE, stateOf(pads.get(1), null, null));
    }

    @Test
    public void anUnclaimedGamepadSaysWhetherAGestureHasAnywhereToPutIt() {
        List<SimGamepad.Pad> pads = Arrays.asList(pad(1, "AAA"), pad(2, "BBB"),
                pad(3, "CCC"));
        assertEquals("gamepad2 has no entry, so a gesture has somewhere to put it",
                SimPads.State.UNCLAIMED_FREE,
                SimPads.state(pads.get(2), pads.get(0), null,
                        storedAs(SimPads.KEY1, "AAA")));
        assertEquals("both entries are taken, so a gesture has to displace somebody",
                SimPads.State.UNCLAIMED_FULL,
                SimPads.state(pads.get(2), pads.get(0), pads.get(1),
                        storedAs(SimPads.KEY1, "AAA", SimPads.KEY2, "BBB")));
    }

    @Test
    public void bothEntriesTakenLeavesNoFreeSlotEvenThoughNoGamepadIsDriving() {
        List<SimGamepad.Pad> pads = Collections.singletonList(pad(1, "AAA"));
        assertEquals("the file holds both slots for gamepads that are not plugged in,"
                        + " so a gesture has to displace one of them",
                SimPads.State.UNCLAIMED_FULL,
                SimPads.state(pads.get(0), null, null,
                        storedAs(SimPads.KEY1, "GONE1", SimPads.KEY2, "GONE2")));
    }

    // --- what the census says ---------------------------------------------

    private static Map<SimGamepad.Pad, SimPads.Source> from(SimGamepad.Pad pad,
            SimPads.Source source) {
        Map<SimGamepad.Pad, SimPads.Source> map = new LinkedHashMap<>();
        map.put(pad, source);
        return map;
    }

    @Test
    public void theCensusOfNoGamepadsSaysSoRatherThanSayingNothing() {
        assertEquals("No gamepad is plugged in. A gamepad object with no gamepad in its"
                        + " slot reads as untouched.\n",
                censusOf(Collections.<SimGamepad.Pad>emptyList(), null, null,
                        Collections.<SimGamepad.Pad, SimPads.Source>emptyMap()));
    }

    @Test
    public void everyGamepadGetsALineSayingWhatItIsDoing() {
        SimGamepad.Pad driving = pad(1, "AAA");
        SimGamepad.Pad nameless = pad(2, null);
        SimGamepad.Pad waiting = pad(3, "CCC");
        List<SimGamepad.Pad> pads = Arrays.asList(driving, nameless, waiting);
        assertEquals("  \"pad 1\", serial AAA, id 1 -- gamepad1, a slot local.properties left free\n"
                        + "  \"pad 2\", no serial number, id 2 -- not claimed yet\n"
                        + "  \"pad 3\", serial CCC, id 3 -- not claimed yet\n"
                        + "Hold Start and press A to drive as gamepad1,"
                        + " or Start and B for gamepad2.\n",
                censusOf(pads, driving, null, from(driving, SimPads.Source.FREE_SLOT)));
    }

    @Test
    public void theSameGamepadsAndTheSameSlotsGiveTheSameCensus() {
        SimGamepad.Pad one = pad(1, "AAA");
        List<SimGamepad.Pad> pads = Collections.singletonList(one);
        Map<SimGamepad.Pad, SimPads.Source> source = from(one, SimPads.Source.GESTURE);
        assertEquals("nothing changed, so there is nothing to print",
                censusOf(pads, one, null, source),
                censusOf(pads, one, null, source));
    }

    @Test
    public void everySlotSourceHasItsOwnWords() {
        Set<String> words = new LinkedHashSet<>();
        for (SimPads.Source source : SimPads.Source.values()) {
            words.add(SimPads.because(source, false));
        }
        assertEquals("a source with no words of its own falls through to another's: " + words,
                SimPads.Source.values().length, words.size());
    }

    @Test
    public void aGamepadInASlotSaysWhereItsSlotCameFrom() {
        SimGamepad.Pad one = pad(1, "AAA");
        List<SimGamepad.Pad> pads = Collections.singletonList(one);
        assertEquals("  \"pad 1\", serial AAA, id 1 -- gamepad1,"
                        + " remembered in local.properties by serial number\n",
                censusOf(pads, one, null, from(one, SimPads.Source.STORED)));
        assertTrue(censusOf(pads, one, null, from(one, SimPads.Source.GESTURE))
                .contains("gamepad1, claimed this run"));
    }

    @Test
    public void aGamepadRememberedByItsDeviceIdSaysSo() {
        SimGamepad.Pad nameless = pad(2, null);
        assertTrue(censusOf(Collections.singletonList(nameless), null, nameless,
                        from(nameless, SimPads.Source.STORED))
                .contains("gamepad2, remembered in local.properties by serial number and"
                        + " device id"));
    }

    @Test
    public void theCensusTellsAMissingSerialNumberFromAnEmptyOne() {
        SimGamepad.Pad none = pad(1, null);
        SimGamepad.Pad empty = pad(2, "");
        String census = censusOf(Arrays.asList(none, empty), null, null,
                Collections.<SimGamepad.Pad, SimPads.Source>emptyMap());
        assertTrue(census, census.contains("\"pad 1\", no serial number, id 1"));
        assertTrue(census, census.contains("\"pad 2\", serial \"\", id 2"));
    }

    @Test
    public void bothSlotsAreNamedGamepad1AndGamepad2AndNeitherIsANumber() {
        SimGamepad.Pad one = pad(1, "AAA");
        SimGamepad.Pad two = pad(2, "BBB");
        Map<SimGamepad.Pad, SimPads.Source> source = from(one, SimPads.Source.GESTURE);
        source.put(two, SimPads.Source.GESTURE);
        assertEquals("  \"pad 1\", serial AAA, id 1 -- gamepad1, claimed this run\n"
                        + "  \"pad 2\", serial BBB, id 2 -- gamepad2, claimed this run\n",
                censusOf(Arrays.asList(one, two), one, two, source));
    }

    @Test
    public void aCensusWithEveryGamepadDrivingDoesNotAskForAGesture() {
        SimGamepad.Pad one = pad(1, "AAA");
        SimGamepad.Pad two = pad(2, "BBB");
        String full = censusOf(Arrays.asList(one, two), one, two,
                from(one, SimPads.Source.STORED));
        assertFalse("nothing is waiting for a slot: " + full, full.contains("Hold Start"));
    }

    // --- which controls the report names -----------------------------------

    @Test
    public void anUntouchedGamepadHasNothingToReport() {
        assertEquals(Collections.emptyList(), SimPads.offRest(new Gamepad()));
    }

    @Test
    public void aStickThatWasPushedAndLetGoIsNotReported() {
        Gamepad state = new Gamepad();
        state.left_stick_y = -0.00003f;
        state.right_stick_x = 0.04f;
        assertEquals("0.00003 of full scale is where a released stick sits",
                Collections.emptyList(), SimPads.offRest(state));
    }

    @Test
    public void aStickAndAButtonAreBothNamedWithTheirValues() {
        Gamepad state = new Gamepad();
        state.left_stick_y = -0.7344055f;
        state.a = true;
        assertEquals(Arrays.asList("left_stick_y=-0.73", "a=true"), SimPads.offRest(state));
    }

    @Test
    public void aTriggerIsReportedOnceItIsPastTheThreshold() {
        Gamepad state = new Gamepad();
        state.right_trigger = SimPads.OFF_REST;
        assertEquals("exactly at the threshold is still at rest",
                Collections.emptyList(), SimPads.offRest(state));
        state.right_trigger = 0.06f;
        assertEquals(Collections.singletonList("right_trigger=0.06"), SimPads.offRest(state));
    }

    @Test
    public void everyOneOfTheTwentyOneControlsCanBeReported() throws Exception {
        Gamepad state = new Gamepad();
        for (String name : SimArgs.AXES) {
            Gamepad.class.getField(name).setFloat(state, 1f);
        }
        for (String name : SimArgs.BUTTONS) {
            Gamepad.class.getField(name).setBoolean(state, true);
        }
        List<String> named = new ArrayList<>();
        for (String control : SimPads.offRest(state)) {
            named.add(control.substring(0, control.indexOf('=')));
        }
        assertEquals(21, named.size());
        assertTrue("every control the options can set: " + named,
                named.containsAll(SimArgs.CONTROLS));
    }

    // --- the claiming gesture ---------------------------------------------

    private static Gamepad pressing(boolean start, boolean a, boolean b) {
        Gamepad state = new Gamepad();
        state.start = start;
        state.a = a;
        state.b = b;
        return state;
    }

    @Test
    public void startAndAClaimsPlayerOneAndStartAndBClaimsPlayerTwo() {
        assertEquals(1, SimPads.claimedPlayer(pressing(true, true, false)));
        assertEquals(2, SimPads.claimedPlayer(pressing(true, false, true)));
    }

    @Test
    public void aButtonWithoutStartClaimsNothing() {
        assertEquals("A on its own", 0, SimPads.claimedPlayer(pressing(false, true, false)));
        assertEquals("B on its own", 0, SimPads.claimedPlayer(pressing(false, false, true)));
        assertEquals("Start on its own", 0, SimPads.claimedPlayer(pressing(true, false, false)));
    }

    @Test
    public void bothButtonsAtOnceClaimsPlayerOne() {
        assertEquals(1, SimPads.claimedPlayer(pressing(true, true, true)));
    }

    // --- what a gesture does to the two slots -----------------------------

    @Test
    public void aGestureFillsAnEmptySlotAndLeavesTheOtherAlone() {
        SimGamepad.Pad one = pad(1, "AAA");
        SimGamepad.Pad two = pad(2, "BBB");
        assertArrayEquals(new SimGamepad.Pad[] {one, two},
                SimPads.displace(one, 1, null, two));
        assertArrayEquals(new SimGamepad.Pad[] {one, two},
                SimPads.displace(two, 2, one, null));
    }

    @Test
    public void theGamepadDisplacedMovesToTheOtherSlotWhenItIsFree() {
        SimGamepad.Pad held = pad(1, "AAA");
        SimGamepad.Pad arriving = pad(2, "BBB");
        assertArrayEquals("held moves out of gamepad1 and into gamepad2",
                new SimGamepad.Pad[] {arriving, held},
                SimPads.displace(arriving, 1, held, null));
    }

    @Test
    public void aThirdGamepadLeavesTheDisplacedOneWithNoSlot() {
        SimGamepad.Pad one = pad(1, "AAA");
        SimGamepad.Pad two = pad(2, "BBB");
        SimGamepad.Pad three = pad(3, "CCC");
        assertArrayEquals("one is displaced and both slots are full",
                new SimGamepad.Pad[] {three, two},
                SimPads.displace(three, 1, one, two));
    }

    @Test
    public void aGamepadClaimingTheOtherSlotSwapsWithWhoeverIsThere() {
        SimGamepad.Pad one = pad(1, "AAA");
        SimGamepad.Pad two = pad(2, "BBB");
        assertArrayEquals(new SimGamepad.Pad[] {two, one},
                SimPads.displace(one, 2, one, two));
        assertArrayEquals("and it leaves gamepad1 empty when nobody is in gamepad2",
                new SimGamepad.Pad[] {null, one},
                SimPads.displace(one, 2, one, null));
    }

    @Test
    public void claimingTheSlotAGamepadAlreadyHasChangesNothing() {
        SimGamepad.Pad one = pad(1, "AAA");
        SimGamepad.Pad two = pad(2, "BBB");
        assertArrayEquals("a held Start and A is not a new gesture every 50 ms",
                new SimGamepad.Pad[] {one, two},
                SimPads.displace(one, 1, one, two));
        assertArrayEquals("and it does not put one gamepad in both slots",
                new SimGamepad.Pad[] {one, null},
                SimPads.displace(one, 1, one, null));
    }

    // --- writing the assignment down --------------------------------------

    private Path store(String... lines) throws IOException {
        Path file = folder.newFile("local.properties").toPath();
        Files.write(file, Arrays.asList(lines), StandardCharsets.UTF_8);
        return file;
    }

    @Test
    public void writingTheAssignmentLeavesEveryOtherLineAlone() throws IOException {
        Path file = store("# do not check this in", "sdk.dir=/Users/somebody/sdk");
        SimPads.write(file, k("AAA"), k("BBB"));
        assertEquals(Arrays.asList("# do not check this in",
                        "sdk.dir=/Users/somebody/sdk",
                        SimPads.KEY1 + "=AAA",
                        SimPads.KEY2 + "=BBB"),
                Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    @Test
    public void writingTwiceReplacesTheLineRatherThanAddingASecond() throws IOException {
        Path file = store("sdk.dir=/sdk");
        SimPads.write(file, k("AAA"), k("BBB"));
        SimPads.write(file, k("CCC"), k("DDD"));
        assertEquals(Arrays.asList("sdk.dir=/sdk",
                        SimPads.KEY1 + "=CCC",
                        SimPads.KEY2 + "=DDD"),
                Files.readAllLines(file, StandardCharsets.UTF_8));
        assertEquals("CCC", SimPads.read(file).get(SimPads.KEY1));
    }

    @Test
    public void anUnclaimedPlayerLeavesNoLine() throws IOException {
        Path file = store("sdk.dir=/sdk", SimPads.KEY1 + "=AAA", SimPads.KEY2 + "=BBB");
        SimPads.write(file, k("AAA"), null);
        assertEquals(Arrays.asList("sdk.dir=/sdk", SimPads.KEY1 + "=AAA"),
                Files.readAllLines(file, StandardCharsets.UTF_8));
        assertNull(SimPads.read(file).get(SimPads.KEY2));
    }

    @Test
    public void aCommentedOutAssignmentIsNotReadAndIsNotEaten() throws IOException {
        Path file = store("#" + SimPads.KEY1 + "=OLD", "sdk.dir=/sdk");
        assertNull("a commented line is not a value", SimPads.read(file).get(SimPads.KEY1));
        SimPads.write(file, k("AAA"), null);
        assertEquals(Arrays.asList("#" + SimPads.KEY1 + "=OLD",
                        "sdk.dir=/sdk",
                        SimPads.KEY1 + "=AAA"),
                Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    @Test
    public void aKeyWithADeviceIdIsWrittenAsTwoLinesAndReadBack() throws IOException {
        Path file = store("sdk.dir=/sdk");
        SimPads.write(file, k(NONE, 2), k(EMPTY, 5));
        assertEquals(Arrays.asList("sdk.dir=/sdk",
                        SimPads.KEY1 + "=(no serial number)",
                        SimPads.ID1 + "=2",
                        SimPads.KEY2 + "=\"\"",
                        SimPads.ID2 + "=5"),
                Files.readAllLines(file, StandardCharsets.UTF_8));
        Map<String, String> back = SimPads.read(file);
        assertEquals(k(NONE, 2), SimPads.storedKey(back, 1));
        assertEquals(k(EMPTY, 5), SimPads.storedKey(back, 2));
    }

    @Test
    public void aKeyThatLosesItsDeviceIdLosesItsIdLine() throws IOException {
        Path file = store("sdk.dir=/sdk", SimPads.KEY1 + "=SAME", SimPads.ID1 + "=1");
        SimPads.write(file, k("SAME"), null);
        assertEquals(Arrays.asList("sdk.dir=/sdk", SimPads.KEY1 + "=SAME"),
                Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    @Test
    public void aSlotIsReadWithItsDeviceIdOnlyWhenItHasAnIdLine() {
        assertEquals(k("SAME"), SimPads.storedKey(storedAs(SimPads.KEY1, "SAME"), 1));
        assertEquals(k("SAME", 4), SimPads.storedKey(
                storedAs(SimPads.KEY1, "SAME", SimPads.ID1, "4"), 1));
        assertNull("an id line with no serial line leaves the slot free",
                SimPads.storedKey(storedAs(SimPads.ID1, "4"), 1));
        assertTrue(SimPads.freeInFile(storedAs(SimPads.ID1, "4"), SimPads.KEY1));
    }

    // --- what a run lasting a practice session holds on to -----------------

    /**
     * Used heap after asking for a collection, which is what a leak shows up in.
     *
     * <p>Garbage the loop makes and drops does not: a collection takes it away
     * again. What this sees is what the loop holds on to, which is the thing a
     * run lasting a practice session cannot afford.
     */
    private static long settledHeap() {
        Runtime runtime = Runtime.getRuntime();
        for (int i = 0; i < 3; i++) {
            System.gc();
        }
        return runtime.totalMemory() - runtime.freeMemory();
    }

    @Test
    public void millionsOfLoopsThroughTheCopyPathHoldOnToNothing() {
        Gamepad staging = new Gamepad();
        Gamepad slot = new Gamepad();
        for (int warm = 0; warm < 200_000; warm++) {
            slot.copy(staging);
            slot.reset();
        }
        long before = settledHeap();
        for (int loop = 0; loop < 4_000_000; loop++) {
            staging.left_stick_y = loop % 2 == 0 ? -1f : 1f;
            staging.a = loop % 3 == 0;
            slot.copy(staging);
        }
        slot.reset();
        long grew = settledHeap() - before;
        assertTrue("4 million loops held on to " + grew / 1024 + " KB",
                grew < 4L * 1024 * 1024);
    }

    @Test
    public void aMissingFileIsNotAnError() {
        Path missing = folder.getRoot().toPath().resolve("nothing-here.properties");
        assertEquals(Collections.emptyMap(), SimPads.read(missing));
    }
}
