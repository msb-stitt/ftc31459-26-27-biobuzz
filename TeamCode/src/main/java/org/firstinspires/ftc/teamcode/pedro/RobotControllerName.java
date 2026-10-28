package org.firstinspires.ftc.teamcode.pedro;

import org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler;

import java.util.function.Supplier;

/**
 * The Robot Controller's name, such as 37523-RC, and what {@link Constants}
 * found when its static code ran. Robot Name Check shows both, to find out
 * whether the name could pick the robot in place of {@link Constants#thisRobot}.
 *
 * <p>This class reads no value from Constants, so using it does not run
 * Constants' static code.
 */
public class RobotControllerName {

    /** Where the name is read from. Tests hand in their own. */
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

    /** The name, or why it could not be read, at the time Constants' static code ran. */
    public static String nameWhenConstantsRan() {
        return nameWhenConstantsRan;
    }

    /** The name now, or why it cannot be read. Never throws. */
    public static String readNow() {
        String name;
        try {
            name = reader.get();
        } catch (Throwable whyNot) {
            String couldNotRead = "(could not read: " + whyNot + ")";
            return couldNotRead;
        }
        if (name == null) {
            String noName = "(could not read: no name yet)";
            return noName;
        }
        return name;
    }

    /** The SDK's own record of the name, the one the Driver Station and the Wi-Fi show. */
    static String readFromTheSdk() {
        NetworkConnectionHandler network = NetworkConnectionHandler.getInstance();
        String name = network.getDeviceName();
        return name;
    }

    /** Called by Constants' static code, first: when it ran, what ran it, and the name then. */
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
