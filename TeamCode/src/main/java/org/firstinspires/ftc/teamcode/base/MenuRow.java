package org.firstinspires.ftc.teamcode.base;

/**
 * One row of a {@link DriverStationMenu}, its name and the choices it steps
 * through. A copy of L135's {@code L135Row}, as solutions-l140-alliances.2
 * solves it, for code that is not a lesson.
 *
 * <p>A choice's letters before its "-" go in the menu's summary line, so
 * "F24-Forward 24" shows as F24. A choice with no "-" shows its first letter,
 * so "Red" shows as R.
 */
public class MenuRow {

    String name;
    String[] choices;

    public MenuRow(String name, String... choices) {
        this.name = name;
        this.choices = choices;
    }
}
