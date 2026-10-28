package org.firstinspires.ftc.teamcode.base;

import com.qualcomm.robotcore.hardware.Gamepad;

/**
 * A menu on the Driver Station, worked with a gamepad between INIT and PLAY. A
 * copy of L135's {@code L135AutoMenu}, as solutions-l140-alliances.2 solves it,
 * for code that is not a lesson: the lesson's own has blanks in it for a
 * student to fill. It works as L135's does, and differs in what it prints and
 * in its names: the summary comes first, a selected choice shows as
 * {@code <Blue>}, and L135's "kept" is "selected" here.
 *
 * <p>The D-pad's up and down pick a row, and its left and right step through
 * that row's choices. A choice is selected only by A. B, and moving to another
 * row, go back to the row's selected choice. Each row starts at its first
 * choice, selected.
 */
public class DriverStationMenu {

    MenuRow[] rows;
    int row = 0;
    int[] selected;
    int[] shown;

    public DriverStationMenu(MenuRow... rows) {
        this.rows = rows;
        selected = new int[rows.length];
        shown = new int[rows.length];
    }

    /** Called from init_loop(): reads the buttons, then prints the menu. */
    public void loop(Gamepad gamepad) {
        if (gamepad.dpadUpWasPressed()) {
            up();
        }
        if (gamepad.dpadDownWasPressed()) {
            down();
        }
        if (gamepad.dpadLeftWasPressed()) {
            left();
        }
        if (gamepad.dpadRightWasPressed()) {
            right();
        }
        if (gamepad.aWasPressed()) {
            select();
        }
        if (gamepad.bWasPressed()) {
            back();
        }
        print();
    }

    /** The row above, or the last row from the first. */
    void up() {
        back();
        row = row - 1;
        if (row < 0) {
            row = rows.length - 1;
        }
    }

    /** The row below, or the first row from the last. */
    void down() {
        back();
        row = row + 1;
        if (row == rows.length) {
            row = 0;
        }
    }

    /** The choice before, or the last choice from the first. */
    void left() {
        int lastChoice = rows[row].choices.length - 1;
        shown[row] = shown[row] - 1;
        if (shown[row] < 0) {
            shown[row] = lastChoice;
        }
    }

    /** The choice after, or the first choice from the last. */
    void right() {
        int lastChoice = rows[row].choices.length - 1;
        shown[row] = shown[row] + 1;
        if (shown[row] > lastChoice) {
            shown[row] = 0;
        }
    }

    /** The choice shown becomes the one selected. */
    void select() {
        selected[row] = shown[row];
    }

    /** The choice selected is shown again. */
    void back() {
        shown[row] = selected[row];
    }

    /** The choice selected in the row with this name, such as chosen("Alliance"). */
    public String chosen(String rowName) {
        for (int i = 0; i < rows.length; i++) {
            if (rows[i].name.equals(rowName)) {
                String choice = rows[i].choices[selected[i]];
                return choice;
            }
        }
        throw new IllegalArgumentException("No row named " + rowName);
    }

    /**
     * Starts the row with this name at this choice, selected and shown, such as
     * startAt("Alliance", "Blue"). A choice the row does not hold changes
     * nothing.
     */
    public void startAt(String rowName, String choice) {
        for (int i = 0; i < rows.length; i++) {
            if (rows[i].name.equals(rowName)) {
                for (int j = 0; j < rows[i].choices.length; j++) {
                    if (rows[i].choices[j].equals(choice)) {
                        selected[i] = j;
                        shown[i] = j;
                    }
                }
            }
        }
    }

    /**
     * Each selected choice's letters before its "-", or its first letter if it
     * has no "-", joined by "-", such as R-F24.
     */
    public String summary() {
        String letters = "";
        for (int i = 0; i < rows.length; i++) {
            String choice = rows[i].choices[selected[i]];
            String shortName = choice.substring(0, 1);
            if (choice.contains("-")) {
                shortName = choice.split("-")[0];
            }
            if (i > 0) {
                letters = letters + "-";
            }
            letters = letters + shortName;
        }
        return letters;
    }

    /**
     * The summary first, then each row, with > at the one the D-pad is on. A
     * selected choice shows as {@code <Blue>}; one not yet selected as
     * {@code Blue? A:OK B:Back}.
     */
    void print() {
        Tracker.printToDs(summary());
        for (int i = 0; i < rows.length; i++) {
            String pointer = "  ";
            if (i == row) {
                pointer = "> ";
            }
            String choice = rows[i].choices[shown[i]];
            String choiceAsShown = "<" + choice + ">";
            if (shown[i] != selected[i]) {
                choiceAsShown = choice + "? A:OK B:Back";
            }
            Tracker.printToDs(pointer + rows[i].name + ": " + choiceAsShown);
        }
    }
}
