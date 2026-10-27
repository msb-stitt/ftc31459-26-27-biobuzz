package org.firstinspires.ftc.teamcode.lessons;

/**
 * L135: one row of {@link L135AutoMenu}, its name and the choices it steps
 * through.
 *
 * <p>A choice's letters before its "-" go in the menu's summary line, so
 * "F24-Forward 24" shows as F24. A choice with no "-" shows its first letter,
 * so "Red" shows as R.
 */
class L135Row {

    String name;
    String[] choices;

    L135Row(String name, String... choices) {
        this.name = name;
        this.choices = choices;
    }
}
