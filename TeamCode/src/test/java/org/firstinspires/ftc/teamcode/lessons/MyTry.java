package org.firstinspires.ftc.teamcode.lessons;

import static org.junit.Assert.fail;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

/**
 * Builds a lesson's class out of {@code mytry} by its name, so the lesson tests
 * compile on the lessons line, where {@code mytry} holds nothing until a student
 * copies a file into it.
 */
final class MyTry {

    static final String PACKAGE = "org.firstinspires.ftc.teamcode.mytry";

    private MyTry() {
    }

    /** A new instance of {@code mytry.<simpleName>}, or a failure naming the copy to make. */
    static OpMode opMode(String simpleName) {
        return make(simpleName, OpMode.class);
    }

    static <T> T make(String simpleName, Class<T> as) {
        Class<?> c;
        try {
            c = Class.forName(PACKAGE + "." + simpleName);
        } catch (ClassNotFoundException e) {
            fail("mytry has no " + simpleName + " yet: copy it into mytry, as its lesson says");
            throw new AssertionError(e);
        }
        try {
            return as.cast(c.getDeclaredConstructor().newInstance());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("mytry." + simpleName + " could not be built", e);
        }
    }
}
