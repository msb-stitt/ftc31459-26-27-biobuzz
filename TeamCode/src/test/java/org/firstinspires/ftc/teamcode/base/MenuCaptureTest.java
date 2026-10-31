package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** The screens L093's, L095's and L135's figures are drawn from are what base's menu shows. */
public class MenuCaptureTest {

    @Test
    public void eachScreensFileIsWhatTheMenuShows() throws IOException {
        Path root = MenuCapture.root();
        for (String lesson : MenuCapture.LESSONS) {
            String shown = MenuCapture.screens(MenuCapture.optionMenu(lesson), Files.readAllLines(
                    root.resolve(MenuCapture.presses(lesson)), StandardCharsets.UTF_8));
            // Git on Windows may check the file out with \r\n line endings.
            String written = new String(Files.readAllBytes(root.resolve(MenuCapture.screens(lesson))),
                    StandardCharsets.UTF_8).replace("\r\n", "\n");
            assertEquals(lesson + ": run ./gradlew :TeamCode:menuCapture to write it again",
                    shown, written);
        }
    }
}
