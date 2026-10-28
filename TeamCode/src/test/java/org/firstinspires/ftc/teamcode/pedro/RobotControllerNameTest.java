package org.firstinspires.ftc.teamcode.pedro;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

/** Reading the Robot Controller's name never throws, and what ran Constants is recorded. */
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

    @Test
    public void constantsRecordsWhenItRanAndWhatRanIt() {
        assertEquals("reading a value runs Constants", 45.0, Constants.ticksPerInch, 0);
        assertTrue(RobotControllerName.constantsHasRun());
        assertFalse(RobotControllerName.whatRanConstants().isEmpty());
        // Off the robot the SDK has no network, so the Robot Controller name could not be read.
        assertTrue(RobotControllerName.nameWhenConstantsRan().startsWith("(could not read"));
    }

    @Test
    public void theCallsThatRanItStartWithTheCallerAndLeaveOutConstantsAndThisClass() {
        String whatRanItBefore = RobotControllerName.whatRanConstants();
        String nameBefore = RobotControllerName.nameWhenConstantsRan();
        try {
            RobotControllerName.reader = () -> "88888-RC";
            RobotControllerName.recordConstantsRunning();
            String[] calls = RobotControllerName.whatRanConstants().split("\n");
            String thisTest = getClass().getName()
                    + ".theCallsThatRanItStartWithTheCallerAndLeaveOutConstantsAndThisClass";
            assertEquals(thisTest, calls[0]);
            assertEquals(RobotControllerName.CALLS_KEPT, calls.length);
            assertEquals("88888-RC", RobotControllerName.nameWhenConstantsRan());
        } finally {
            RobotControllerName.whatRanConstants = whatRanItBefore;
            RobotControllerName.nameWhenConstantsRan = nameBefore;
        }
    }
}
