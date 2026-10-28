package org.firstinspires.ftc.teamcode.base;

/**
 * The rows of a menu, and which row the pointer is on. Up and down move the
 * pointer, wrap at the ends, and clear the offer in the row it leaves. Left,
 * right, A and B go to the row the pointer is on.
 *
 * <p>Uses {@link MenuRow} for each row, and through it {@link MenuItem}.
 */
// 'public' so OpModes outside base can make one. 'public' allows access from any code.
public class OptionMenu {

    MenuRow[] rows;
    int pointedRow = 0;

    // 'public' so OpModes outside base can make one. 'public' allows access from any code.
    public OptionMenu(MenuRow... rows) {
        this.rows = rows;
    }

    /** The pointer moves to the row above, or the last row from the first. */
    void up() {
        back();
        pointedRow = pointedRow - 1;
        if (pointedRow < 0) {
            pointedRow = rows.length - 1;
        }
    }

    /** The pointer moves to the row below, or the first row from the last. */
    void down() {
        back();
        pointedRow = pointedRow + 1;
        if (pointedRow == rows.length) {
            pointedRow = 0;
        }
    }

    /** The pointed row offers the choice before. */
    void left() {
        rows[pointedRow].left();
    }

    /** The pointed row offers the choice after. */
    void right() {
        rows[pointedRow].right();
    }

    /** The pointed row's offer is selected. */
    void select() {
        rows[pointedRow].select();
    }

    /** The pointed row's offer is cleared. */
    void back() {
        rows[pointedRow].back();
    }

    /** The full name of the choice selected in the row with this name, such as chosen("Auto"). */
    String chosen(String rowName) {
        MenuRow row = rowNamed(rowName);
        MenuItem selectedItem = row.selected();
        String fullName = selectedItem.fullName();
        return fullName;
    }

    /** Selects the choice with this full name in the row with this name, such as startAt("Alliance", "Blue"). */
    void startAt(String rowName, String fullName) {
        MenuRow row = rowNamed(rowName);
        row.startAt(fullName);
    }

    /** Each row's selected short name, joined by "-", such as B-FS. */
    String summary() {
        String summary = "";
        for (int i = 0; i < rows.length; i++) {
            MenuItem selectedItem = rows[i].selected();
            if (i > 0) {
                summary = summary + "-";
            }
            summary = summary + selectedItem.shortName();
        }
        return summary;
    }

    /** The summary, then each row's line, with > at the row the pointer is on. */
    String[] lines() {
        String[] lines = new String[rows.length + 1];
        lines[0] = summary();
        for (int i = 0; i < rows.length; i++) {
            String pointer = "  ";
            if (i == pointedRow) {
                pointer = "> ";
            }
            lines[i + 1] = pointer + rows[i].line();
        }
        return lines;
    }

    /** The row with this name. */
    MenuRow rowNamed(String rowName) {
        for (int i = 0; i < rows.length; i++) {
            if (rows[i].name().equals(rowName)) {
                return rows[i];
            }
        }
        throw new IllegalArgumentException("No row named " + rowName);
    }
}
