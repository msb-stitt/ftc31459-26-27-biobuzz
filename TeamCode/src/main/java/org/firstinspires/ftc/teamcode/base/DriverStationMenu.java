package org.firstinspires.ftc.teamcode.base;

import com.qualcomm.robotcore.hardware.Gamepad;

/**
 * A menu on the Driver Station, worked with a gamepad between INIT and PLAY,
 * for an OpMode to make as a field and call from init_loop(). The menu and its
 * rows are {@link OptionMenu}'s; this class reads the buttons and shows it.
 *
 * <p>Uses {@link OptionMenu}, {@link MenuButtons}, which it makes, and
 * {@link Tracker}, which shows the menu on the Driver Station and logs its
 * summary as menu/summary.
 */
// 'public' so OpModes outside base can make one. 'public' allows access from any code.
public class DriverStationMenu {

    OptionMenu optionMenu;
    MenuButtons menuButtons;

    // 'public' so OpModes outside base can make one. 'public' allows access from any code.
    public DriverStationMenu(OptionMenu optionMenu) {
        this.optionMenu = optionMenu;
        menuButtons = new MenuButtons(optionMenu);
    }

    /** Called from init_loop(): reads the buttons, then shows the menu and logs its summary. */
    // 'public' so OpModes outside base can call it. 'public' allows access from any code.
    public void loop(Gamepad gamepad) {
        menuButtons.loop(gamepad);
        String summary = optionMenu.summary();
        Tracker.publish("menu/summary", summary);
        String[] lines = optionMenu.lines();
        for (String line : lines) {
            Tracker.printToDs(line);
        }
    }

    /** The full name of the choice selected in the row with this name, such as chosen("Auto"). */
    // 'public' so OpModes outside base can call it. 'public' allows access from any code.
    public String chosen(String rowName) {
        String fullName = optionMenu.chosen(rowName);
        return fullName;
    }

    /** Selects the choice with this full name in the row with this name, such as startAt("Alliance", "Blue"). */
    // 'public' so OpModes outside base can call it. 'public' allows access from any code.
    public void startAt(String rowName, String fullName) {
        optionMenu.startAt(rowName, fullName);
    }

    /** Each row's selected short name, joined by "-", such as B-FS. */
    // 'public' so OpModes outside base can call it. 'public' allows access from any code.
    public String summary() {
        String summary = optionMenu.summary();
        return summary;
    }
}
