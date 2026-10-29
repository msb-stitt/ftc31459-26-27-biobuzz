package org.firstinspires.ftc.teamcode.base;

import java.util.ArrayList;

/**
 * One row of an {@link OptionMenu}: its name and its choices. Left and right
 * offer the choice before or after, and wrap at the ends. A selects the choice
 * offered; B clears the offer. The row starts at the first choice added,
 * selected.
 *
 * <p>Uses {@link MenuItem} for each choice.
 */
// 'public' so OpModes outside base can make one. 'public' allows access from any code.
public class MenuRow {

    private String name;
    private ArrayList<MenuItem> items = new ArrayList<>();
    private int selectedIndex = 0;
    private int offeredIndex = 0;

    // 'public' so OpModes outside base can make one. 'public' allows access from any code.
    public MenuRow(String name) {
        this.name = name;
    }

    /** Adds a choice after the ones already added. */
    // 'public' so OpModes outside base can fill one. 'public' allows access from any code.
    public void add(MenuItem item) {
        items.add(item);
    }

    /** Offers the choice after, or the first choice from the last. */
    void goRight() {
        offeredIndex = offeredIndex + 1;
        if (offeredIndex == items.size()) {
            offeredIndex = 0;
        }
    }

    /** Offers the choice before, or the last choice from the first. */
    void goLeft() {
        offeredIndex = offeredIndex - 1;
        if (offeredIndex < 0) {
            offeredIndex = items.size() - 1;
        }
    }

    /** Selects the choice offered. */
    void select() {
        selectedIndex = offeredIndex;
    }

    /** Clears the offer: the choice selected is offered again. */
    void goBack() {
        offeredIndex = selectedIndex;
    }

    /** The choice selected. */
    // 'public' so OpModes outside base can ask it. 'public' allows access from any code.
    public MenuItem getSelected() {
        return items.get(selectedIndex);
    }

    /** The row's line, such as "Alliance: <Red>" or "Alliance: Blue? A:OK B:Back". */
    String getLine() {
        MenuItem offered = items.get(offeredIndex);
        String choiceShown = "<" + offered.getFullName() + ">";
        if (offeredIndex != selectedIndex) {
            choiceShown = offered.getFullName() + "? A:OK B:Back";
        }
        return name + ": " + choiceShown;
    }
}
