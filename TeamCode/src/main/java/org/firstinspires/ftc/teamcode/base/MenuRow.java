package org.firstinspires.ftc.teamcode.base;

/**
 * One row of an {@link OptionMenu}: its name and its choices. Left and right
 * offer the choice before or after, and wrap at the ends. A selects the choice
 * offered; B clears the offer. The row starts at its first choice, selected.
 *
 * <p>Uses {@link MenuItem} for each choice.
 */
// 'public' so OpModes outside base can make one. 'public' allows access from any code.
public class MenuRow {

    String name;
    MenuItem[] items;
    int selectedIndex = 0;
    int offeredIndex = 0;

    // 'public' so OpModes outside base can make one. 'public' allows access from any code.
    public MenuRow(String name, MenuItem... items) {
        this.name = name;
        this.items = items;
    }

    /** The row's name, such as Alliance. */
    String name() {
        return name;
    }

    /** Offers the choice after, or the first choice from the last. */
    void right() {
        int lastIndex = items.length - 1;
        offeredIndex = offeredIndex + 1;
        if (offeredIndex > lastIndex) {
            offeredIndex = 0;
        }
    }

    /** Offers the choice before, or the last choice from the first. */
    void left() {
        int lastIndex = items.length - 1;
        offeredIndex = offeredIndex - 1;
        if (offeredIndex < 0) {
            offeredIndex = lastIndex;
        }
    }

    /** The choice offered is selected. */
    void select() {
        selectedIndex = offeredIndex;
    }

    /** The offer is cleared. */
    void back() {
        offeredIndex = selectedIndex;
    }

    /** The choice selected. */
    MenuItem selected() {
        MenuItem selectedItem = items[selectedIndex];
        return selectedItem;
    }

    /** Selects the choice with this full name. A name the row does not hold changes nothing. */
    void startAt(String fullName) {
        for (int i = 0; i < items.length; i++) {
            if (items[i].fullName().equals(fullName)) {
                selectedIndex = i;
                offeredIndex = i;
            }
        }
    }

    /** The row as the Driver Station shows it, such as Alliance: <Red> or Alliance: Blue? A:OK B:Back. */
    String line() {
        MenuItem offeredItem = items[offeredIndex];
        String choiceShown = "<" + offeredItem.fullName() + ">";
        if (offeredIndex != selectedIndex) {
            choiceShown = offeredItem.fullName() + "? A:OK B:Back";
        }
        String rowLine = name + ": " + choiceShown;
        return rowLine;
    }
}
