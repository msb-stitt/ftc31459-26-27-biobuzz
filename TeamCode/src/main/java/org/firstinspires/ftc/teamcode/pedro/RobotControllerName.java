package org.firstinspires.ftc.teamcode.pedro;

import org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler;

import java.util.function.Supplier;

/**
 * The Robot Controller name, such as 37523-RC. {@link Constants} picks
 * {@link Constants#thisRobot} from it.
 *
 * <p>This class reads no value from Constants, so using it does not run
 * Constants' static code.
 */
public class RobotControllerName {

    /** Where the Robot Controller name is read from. Tests hand in their own. */
    public static Supplier<String> reader = RobotControllerName::readFromTheSdk;

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
}
