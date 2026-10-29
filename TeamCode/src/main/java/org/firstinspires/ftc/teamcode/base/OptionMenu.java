package org.firstinspires.ftc.teamcode.base;

import java.util.ArrayList;

/**
 * The rows of a menu, and which row the pointer is on. Up and down move the
 * pointer, wrap at the ends, and clear the offer in the row it leaves. Left,
 * right, A and B go to the row the pointer is on.
 *
 * <p>Uses {@link MenuRow} for each row, and through it {@link MenuItem}.
 */
// 'public' so OpModes outside base can make one. 'public' allows access from any code.
public class OptionMenu {

    private ArrayList<MenuRow> rows = new ArrayList<>();
    private int pointedIndex = 0;

    // 'public' so OpModes outside base can make one. 'public' allows access from any code.
    public OptionMenu() {
    }

    /** Adds a row below the ones already added. */
    // 'public' so OpModes outside base can fill one. 'public' allows access from any code.
    public void add(MenuRow row) {
        rows.add(row);
    }

    /** Clears the offer, and moves the pointer up a row, or to the last from the first. */
    void goUp() {
        goBack();
        pointedIndex = pointedIndex - 1;
        if (pointedIndex < 0) {
            pointedIndex = rows.size() - 1;
        }
    }

    /** Clears the offer, and moves the pointer down a row, or to the first from the last. */
    void goDown() {
        goBack();
        pointedIndex = pointedIndex + 1;
        if (pointedIndex == rows.size()) {
            pointedIndex = 0;
        }
    }

    void goRight() {
        rows.get(pointedIndex).goRight();
    }

    void goLeft() {
        rows.get(pointedIndex).goLeft();
    }

    void select() {
        rows.get(pointedIndex).select();
    }

    void goBack() {
        rows.get(pointedIndex).goBack();
    }

    /** Each row's selected short name, joined by "-", such as B-FS. */
    String getSummary() {
        String summary = "";
        String between = "";
        for (MenuRow row : rows) {
            summary = summary + between + row.getSelected().getShortName();
            between = "-";
        }
        return summary;
    }

    /** The summary, then each row's line, the row the pointer is on marked "> ". */
    ArrayList<String> getLines() {
        ArrayList<String> lines = new ArrayList<>();
        lines.add(getSummary());
        MenuRow pointed = rows.get(pointedIndex);
        for (MenuRow row : rows) {
            String pointer = "  ";
            if (row == pointed) {
                pointer = "> ";
            }
            lines.add(pointer + row.getLine());
        }
        return lines;
    }
}
