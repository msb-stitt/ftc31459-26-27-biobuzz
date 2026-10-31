package org.firstinspires.ftc.teamcode.base;

import com.pedropathing.drivetrain.Drivetrain;
import com.pedropathing.follower.Follower;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.base.odometry.HardwarePinpointSource;
import org.firstinspires.ftc.teamcode.base.odometry.PinpointSource;
import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Where the robot's parts come from. On the robot these are the real thing;
 * tests replace them with simulated hardware and a temporary log file.
 *
 * <p>Students never touch this.
 */
public final class RobotFactory {

    /** Builds the follower over the drivetrain given. Tests swap in a simulated one. */
    public static BiFunction<HardwareMap, Drivetrain, Follower> follower = Constants::create;

    /**
     * Finds every device, by the names in {@link Constants#drivetrainConfig}.
     * Tests swap in fakes, which is also why nothing constructs a HardwareMap in
     * a test: the real one needs an Android context.
     */
    public static Function<HardwareMap, RobotHardware> hardware =
            map -> new RobotHardware(map, Constants.drivetrainConfig);

    /**
     * The Pinpoint, by the name in {@link Constants#localizerConfig}, for a
     * localizer that reads its pods itself. Tests swap in a simulated one.
     */
    public static Function<HardwareMap, PinpointSource> pinpoint =
            map -> new HardwarePinpointSource(
                    map.get(GoBildaPinpointDriver.class, Constants.localizerConfig.name.get()));

    private RobotFactory() {
    }
}
