package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L135: a menu on the Driver Station, worked with a gamepad between INIT and
 * PLAY.
 *
 * <p>The D-pad's up and down pick a row, and its left and right step through
 * that row's choices. A keeps the choice shown. B puts back the choice kept
 * before. Each row starts at its first choice.
 *
 * <p>Passes when: LessonsTest.l135_theMenuChoosesTheAllianceAndTheAuto
 */
class L135AutoMenu {

    L135Row[] rows;
    int row = 0;
    int[] kept;
    int[] shown;

    L135AutoMenu(L135Row... rows) {
        this.rows = rows;
        kept = new int[rows.length];
        shown = new int[rows.length];
    }

    /** Called from init_loop(): reads the buttons, then prints the menu. */
    void loop(Gamepad gamepad) {
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
            keep();
        }
        if (gamepad.bWasPressed()) {
            putBack();
        }
        print();
    }

    /** The row above, or the last row from the first. */
    void up() {
        putBack();
        row = row - 1;
        if (row < 0) {
            row = rows.length - 1;
        }
    }

    /** The row below, or the first row from the last. */
    void down() {
        putBack();
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
        // When on L135S030, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s030
        // for what to do here.
    }

    /** The choice shown becomes the one kept. */
    void keep() {
        // When on L135S040, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s040
        // for what to do here.
    }

    /** The choice kept is shown again. */
    void putBack() {
        // When on L135S040, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s040
        // for what to do here.
    }

    /** The choice kept in the row with this name, such as chosen("Alliance"). */
    String chosen(String rowName) {
        for (int i = 0; i < rows.length; i++) {
            if (rows[i].name.equals(rowName)) {
                String choice = rows[i].choices[kept[i]];
                return choice;
            }
        }
        throw new IllegalArgumentException("No row named " + rowName);
    }

    /**
     * Starts the row with this name at this choice, kept and shown, such as
     * startAt("Alliance", "Blue"). A choice the row does not hold changes
     * nothing.
     */
    void startAt(String rowName, String choice) {
        for (int i = 0; i < rows.length; i++) {
            if (rows[i].name.equals(rowName)) {
                for (int j = 0; j < rows[i].choices.length; j++) {
                    if (rows[i].choices[j].equals(choice)) {
                        kept[i] = j;
                        shown[i] = j;
                    }
                }
            }
        }
    }

    /**
     * Each kept choice's letters before its "-", or its first letter if it has
     * no "-", joined by "-", such as R-F24.
     */
    String summary() {
        String letters = "";
        for (int i = 0; i < rows.length; i++) {
            String choice = rows[i].choices[kept[i]];
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

    /** Each row, with > at the one the D-pad is on, and the summary last. */
    void print() {
        for (int i = 0; i < rows.length; i++) {
            String pointer = "  ";
            if (i == row) {
                pointer = "> ";
            }
            String choice = rows[i].choices[shown[i]];
            String notKept = "";
            if (shown[i] != kept[i]) {
                notKept = "   (A keeps, B puts back)";
            }
            Tracker.printToDs(pointer + rows[i].name + ": < " + choice + " >" + notKept);
        }
        Tracker.printToDs(summary());
    }
}
