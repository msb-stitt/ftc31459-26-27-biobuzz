package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.junit.Test;

/** A menu says its choice changed on the first pass and when a choice is selected with A. */
public class DriverStationMenuTest {

    private final Gamepad gamepad = new Gamepad();
    private final DriverStationMenu menu = new DriverStationMenu(twoRows());

    @Test
    public void theChoiceChangesOnTheFirstPassAndWhenAnOfferIsSelected() {
        menu.loop(gamepad);
        assertTrue("the first pass", menu.choiceChanged());

        menu.loop(gamepad);
        assertFalse("a pass with no press", menu.choiceChanged());

        pass("right");
        pass(null);
        assertFalse("an offer not yet selected", menu.choiceChanged());

        pass("A");
        assertTrue("the offer selected", menu.choiceChanged());
        pass(null);
        assertFalse("and the pass after", menu.choiceChanged());
    }

    /** One init_loop pass with the button held, or none, as the robot sees it. */
    private void pass(String button) {
        Gamepad held = new Gamepad();
        if ("right".equals(button)) {
            held.dpad_right = true;
        }
        if ("A".equals(button)) {
            held.a = true;
        }
        gamepad.copy(held);
        menu.loop(gamepad);
    }

    private static OptionMenu twoRows() {
        MenuRow alliance = new MenuRow("Alliance");
        alliance.add(new MenuItem("R", "Red"));
        alliance.add(new MenuItem("B", "Blue"));
        MenuRow start = new MenuRow("Start");
        start.add(new MenuItem("A", "Auto End"));
        start.add(new MenuItem("W", "Wall"));
        OptionMenu optionMenu = new OptionMenu();
        optionMenu.add(alliance);
        optionMenu.add(start);
        return optionMenu;
    }
}
