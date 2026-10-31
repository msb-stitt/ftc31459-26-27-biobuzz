package org.firstinspires.ftc.teamcode.base;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Runs base's menu through a list of presses, with L093's rows, L095's and L135's,
 * and writes what the Driver Station shows after each, for the figures
 * book/tools/draw.py draws.
 *
 * <pre>
 * ./gradlew :TeamCode:menuCapture
 * </pre>
 *
 * <p>For each lesson in {@link #LESSONS}, reads its {@link #presses} file, one
 * screen a line: {@code INIT}, or the buttons pressed, such as {@code right} or
 * {@code A}, a space between two. Writes its {@link #screens} file: each
 * screen's line, the Driver Station's lines, and a blank line.
 * {@link MenuCaptureTest} fails when a file written is not what the menu shows.
 */
public class MenuCapture {

    static final String[] LESSONS = {"l093", "l095", "l135"};

    static Path presses(String lesson) {
        return Paths.get("book/tools/menu/" + lesson + "-presses.txt");
    }

    static Path screens(String lesson) {
        return Paths.get("book/tools/menu/" + lesson + "-screens.txt");
    }

    public static void main(String[] args) throws IOException {
        Path root = root();
        for (String lesson : LESSONS) {
            String screens = screens(optionMenu(lesson),
                    Files.readAllLines(root.resolve(presses(lesson)), StandardCharsets.UTF_8));
            Files.write(root.resolve(screens(lesson)), screens.getBytes(StandardCharsets.UTF_8));
            System.out.println("wrote " + screens(lesson));
        }
    }

    /** The repository's top folder: the working folder, or the first above it holding the presses. */
    static Path root() {
        Path at = Paths.get("").toAbsolutePath();
        while (at != null && !Files.exists(at.resolve(presses(LESSONS[0])))) {
            at = at.getParent();
        }
        if (at == null) {
            throw new IllegalStateException("no " + presses(LESSONS[0]) + " here or above");
        }
        return at;
    }

    /** The lesson's menu, with the rows its OpMode makes. */
    static OptionMenu optionMenu(String lesson) {
        OptionMenu optionMenu = new OptionMenu();
        if (lesson.equals("l093")) {
            MenuRow lightRow = new MenuRow("Light");
            lightRow.add(new MenuItem("ON", "On"));
            lightRow.add(new MenuItem("OFF", "Off"));
            MenuRow speedRow = new MenuRow("Speed");
            speedRow.add(new MenuItem("S", "Slow"));
            speedRow.add(new MenuItem("M", "Medium"));
            speedRow.add(new MenuItem("F", "Fast"));
            MenuRow colorRow = new MenuRow("Color");
            colorRow.add(new MenuItem("R", "Red"));
            colorRow.add(new MenuItem("G", "Green"));
            colorRow.add(new MenuItem("B", "Blue"));
            optionMenu.add(lightRow);
            optionMenu.add(speedRow);
            optionMenu.add(colorRow);
            return optionMenu;
        }
        if (lesson.equals("l095")) {
            MenuRow bandRow = new MenuRow("Band");
            bandRow.add(new MenuItem("0.05", "0.05"));
            bandRow.add(new MenuItem("0.00", "0.00"));
            bandRow.add(new MenuItem("0.025", "0.025"));
            bandRow.add(new MenuItem("0.075", "0.075"));
            bandRow.add(new MenuItem("0.10", "0.10"));
            MenuRow shapeRow = new MenuRow("Shape");
            shapeRow.add(new MenuItem("S", "Squared"));
            shapeRow.add(new MenuItem("L", "Linear"));
            shapeRow.add(new MenuItem("C", "Cubed"));
            optionMenu.add(bandRow);
            optionMenu.add(shapeRow);
            return optionMenu;
        }
        MenuRow allianceRow = new MenuRow("Alliance");
        allianceRow.add(new MenuItem("R", "Red"));
        allianceRow.add(new MenuItem("B", "Blue"));
        MenuRow autoRow = new MenuRow("Auto");
        autoRow.add(new MenuItem("N", "Nothing"));
        autoRow.add(new MenuItem("F24", "Forward 24"));
        autoRow.add(new MenuItem("FS", "Forward Then Strafe"));
        optionMenu.add(allianceRow);
        optionMenu.add(autoRow);
        return optionMenu;
    }

    /** What the Driver Station shows after each screen's presses. */
    static String screens(OptionMenu optionMenu, List<String> presses) {
        MenuButtons menuButtons = new MenuButtons(optionMenu);
        Gamepad gamepad = new Gamepad();

        StringBuilder out = new StringBuilder();
        for (String screen : presses) {
            if (!screen.equals("INIT")) {
                for (String button : screen.split(" ")) {
                    press(menuButtons, gamepad, button);
                }
            }
            out.append(screen).append('\n');
            for (String line : optionMenu.getLines()) {
                out.append(line).append('\n');
            }
            out.append('\n');
        }
        return out.toString();
    }

    /** One press and its release, as the robot sees them in two loops. */
    static void press(MenuButtons menuButtons, Gamepad gamepad, String button) {
        Gamepad held = new Gamepad();
        switch (button) {
            case "up": held.dpad_up = true; break;
            case "down": held.dpad_down = true; break;
            case "left": held.dpad_left = true; break;
            case "right": held.dpad_right = true; break;
            case "A": held.a = true; break;
            case "B": held.b = true; break;
            default: throw new IllegalArgumentException("no button named " + button);
        }
        gamepad.copy(held);
        menuButtons.loop(gamepad);
        gamepad.copy(new Gamepad());
        menuButtons.loop(gamepad);
    }
}
