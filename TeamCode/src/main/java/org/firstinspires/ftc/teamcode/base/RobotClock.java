package org.firstinspires.ftc.teamcode.base;

import java.util.function.LongSupplier;

/**
 * The robot's clock, in nanoseconds: the wall clock on the robot, and the
 * simulated clock in a sim run, which sets it with {@link #use}.
 *
 * <p>Robot code reads the time here rather than from {@code System.nanoTime()},
 * so a simulation run faster than real time, or held still at a breakpoint,
 * reads the simulation's time everywhere.
 */
public final class RobotClock {
    private static volatile LongSupplier source = System::nanoTime;

    private RobotClock() {
    }

    /** The time now, in nanoseconds. */
    public static long nanoTime() {
        return source.getAsLong();
    }

    /** Reads the time from {@code clock} from now on: a sim run's clock. */
    public static void use(LongSupplier clock) {
        source = clock;
    }

    /** Reads the wall clock again, as on the robot. */
    public static void useWallClock() {
        source = System::nanoTime;
    }
}
