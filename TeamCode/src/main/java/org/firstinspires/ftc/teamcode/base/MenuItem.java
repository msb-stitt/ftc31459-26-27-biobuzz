package org.firstinspires.ftc.teamcode.base;

/**
 * One choice in a {@link MenuRow}: its short name, such as F24, for the
 * menu's summary line, and its full name, such as Forward 24, for its row's
 * line and for {@link OptionMenu#chosen(String)}.
 *
 * <p>Uses no other class.
 */
// 'public' so OpModes outside base can make one. 'public' allows access from any code.
public class MenuItem {

    String shortName;
    String fullName;

    // 'public' so OpModes outside base can make one. 'public' allows access from any code.
    public MenuItem(String shortName, String fullName) {
        this.shortName = shortName;
        this.fullName = fullName;
    }

    /** The name in the summary line, such as F24. */
    String shortName() {
        return shortName;
    }

    /** The name in the row's line, such as Forward 24. */
    String fullName() {
        return fullName;
    }
}
