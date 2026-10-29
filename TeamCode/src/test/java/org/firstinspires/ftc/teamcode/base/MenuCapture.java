package org.firstinspires.ftc.teamcode.base;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Runs base's menu through a list of presses, with L135's rows, and writes what
 * the Driver Station shows after each, for the figures book/tools/draw.py draws.
 *
 * <pre>
 * ./gradlew :TeamCode:menuCapture
 * </pre>
 *
 * <p>Reads {@link #PRESSES}, one screen a line: {@code INIT}, or the buttons
 * pressed, such as {@code right} or {@code A}, a space between two. Writes
 * {@link #SCREENS}: each screen's line, the Driver Station's lines, and a blank
 * line. {@link MenuCaptureTest} fails when the file written is not what the menu
 * shows.
 */
public class MenuCapture {

    static final Path PRESSES = Paths.get("book/tools/menu/l135-presses.txt");
    static final Path SCREENS = Paths.get("book/tools/menu/l135-screens.txt");

    public static void main(String[] args) throws IOException {
        Path root = root();
        String screens = screens(Files.readAllLines(root.resolve(PRESSES), StandardCharsets.UTF_8));
        Files.write(root.resolve(SCREENS), screens.getBytes(StandardCharsets.UTF_8));
        System.out.println("wrote " + SCREENS);
    }

    /** The repository's top folder: the working folder, or the first above it holding the presses. */
    static Path root() {
        Path at = Paths.get("").toAbsolutePath();
        while (at != null && !Files.exists(at.resolve(PRESSES))) {
            at = at.getParent();
        }
        if (at == null) {
            throw new IllegalStateException("no " + PRESSES + " here or above");
        }
        return at;
    }

    /** What the Driver Station shows after each screen's presses. */
    static String screens(List<String> presses) {
        MenuRow allianceRow = new MenuRow("Alliance");
        allianceRow.add(new MenuItem("R", "Red"));
        allianceRow.add(new MenuItem("B", "Blue"));
        MenuRow autoRow = new MenuRow("Auto");
        autoRow.add(new MenuItem("N", "Nothing"));
        autoRow.add(new MenuItem("F24", "Forward 24"));
        autoRow.add(new MenuItem("FS", "Forward Then Strafe"));
        OptionMenu optionMenu = new OptionMenu();
        optionMenu.add(allianceRow);
        optionMenu.add(autoRow);
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
