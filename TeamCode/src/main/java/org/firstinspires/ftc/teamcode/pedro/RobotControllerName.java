package org.firstinspires.ftc.teamcode.pedro;

import org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler;

import java.util.function.Supplier;

/**
 * The Robot Controller name, such as 37523-RC, and what {@link Constants}
 * found when its static code ran. Constants picks {@link Constants#thisRobot}
 * from that Robot Controller name, and Robot Name Check shows both.
 *
 * <p>This class reads no value from Constants, so using it does not run
 * Constants' static code.
 */
public class RobotControllerName {

    /** Where the Robot Controller name is read from. Tests hand in their own. */
    public static Supplier<String> reader = RobotControllerName::readFromTheSdk;

    /** How many of the calls that ran Constants' static code are kept, nearest first. */
    static final int CALLS_KEPT = 6;

    /** Set once, when Constants' static code runs. */
    static boolean constantsHasRun = false;
    static String whatRanConstants = "";
    static String nameWhenConstantsRan = "";

    /** True once Constants' static code has run, in this run of the app. */
    public static boolean constantsHasRun() {
        return constantsHasRun;
    }

    /** The calls that led to Constants' static code running, one per line. */
    public static String whatRanConstants() {
        return whatRanConstants;
    }

    /** The Robot Controller name, or why it could not be read, when Constants' static code ran. */
    public static String nameWhenConstantsRan() {
        return nameWhenConstantsRan;
    }

    /** The Robot Controller name now, or why it cannot be read. Never throws. */
    public static String readNow() {
        String robotControllerName;
        try {
            robotControllerName = reader.get();
        } catch (Throwable whyNot) {
            String couldNotRead = "(could not read: " + whyNot + ")";
            return couldNotRead;
        }
        if (robotControllerName == null) {
            String noName = "(could not read: no Robot Controller name yet)";
            return noName;
        }
        return robotControllerName;
    }

    /**
     * The SDK's own record of the Robot Controller name, the one the Driver
     * Station and the Wi-Fi show.
     */
    static String readFromTheSdk() {
        NetworkConnectionHandler network = NetworkConnectionHandler.getInstance();
        String robotControllerName = network.getDeviceName();
        return robotControllerName;
    }

    /**
     * Called by Constants' static code, first: when it ran, what ran it, and the
     * Robot Controller name then.
     */
    static void recordConstantsRunning() {
        StackTraceElement[] calls = new Throwable().getStackTrace();
        StringBuilder callsThatRanIt = new StringBuilder();
        int kept = 0;
        for (StackTraceElement call : calls) {
            String className = call.getClassName();
            boolean isThisClass = className.equals(RobotControllerName.class.getName());
            boolean isConstants = className.equals(Constants.class.getName());
            boolean isConstantsOrThis = isThisClass || isConstants;
            if (isConstantsOrThis) {
                continue;
            }
            if (kept == CALLS_KEPT) {
                break;
            }
            callsThatRanIt.append(call.getClassName()).append('.').append(call.getMethodName()).append('\n');
            kept = kept + 1;
        }
        whatRanConstants = callsThatRanIt.toString();
        nameWhenConstantsRan = readNow();
        constantsHasRun = true;
    }
}
