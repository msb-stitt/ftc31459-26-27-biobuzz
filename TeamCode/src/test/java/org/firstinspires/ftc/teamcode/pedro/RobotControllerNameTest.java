package org.firstinspires.ftc.teamcode.pedro;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Test;

/** Reading the Robot Controller's name never throws. */
public class RobotControllerNameTest {

    @After
    public void tearDown() {
        RobotControllerName.reader = RobotControllerName::readFromTheSdk;
    }

    @Test
    public void theRobotControllerNameIsWhatTheReaderGives() {
        RobotControllerName.reader = () -> "37523-RC";
        assertEquals("37523-RC", RobotControllerName.readNow());
    }

    @Test
    public void aReaderThatThrowsOrGivesNothingIsShownNotThrown() {
        RobotControllerName.reader = () -> {
            throw new NullPointerException("no network yet");
        };
        assertEquals("(could not read: java.lang.NullPointerException: no network yet)",
                RobotControllerName.readNow());
        RobotControllerName.reader = () -> null;
        assertEquals("(could not read: no Robot Controller name yet)",
                RobotControllerName.readNow());
    }
}
