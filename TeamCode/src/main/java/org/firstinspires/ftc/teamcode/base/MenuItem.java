package org.firstinspires.ftc.teamcode.base;

/**
 * One choice in a {@link MenuRow}: its short name, such as F24, for the
 * menu's summary line, and its full name, such as Forward 24, for its row's
 * line.
 *
 * <p>Uses no other class.
 */
// 'public' so OpModes outside base can make one. 'public' allows access from any code.
public class MenuItem {

    private String shortName;
    private String fullName;

    // 'public' so OpModes outside base can make one. 'public' allows access from any code.
    public MenuItem(String shortName, String fullName) {
        this.shortName = shortName;
        this.fullName = fullName;
    }

    /** The short name, such as F24. */
    String getShortName() {
        return shortName;
    }

    /** The full name, such as Forward 24. */
    // 'public' so OpModes outside base can log it. 'public' allows access from any code.
    public String getFullName() {
        return fullName;
    }
}
