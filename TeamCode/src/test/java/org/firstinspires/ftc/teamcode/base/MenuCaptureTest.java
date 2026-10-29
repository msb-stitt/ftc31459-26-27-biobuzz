package org.firstinspires.ftc.teamcode.base;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** The screens L135's figures are drawn from are what base's menu shows. */
public class MenuCaptureTest {

    @Test
    public void theScreensFileIsWhatTheMenuShows() throws IOException {
        Path root = MenuCapture.root();
        String shown = MenuCapture.screens(
                Files.readAllLines(root.resolve(MenuCapture.PRESSES), StandardCharsets.UTF_8));
        String written = new String(Files.readAllBytes(root.resolve(MenuCapture.SCREENS)),
                StandardCharsets.UTF_8);
        assertEquals("run ./gradlew :TeamCode:menuCapture to write it again", shown, written);
    }
}
