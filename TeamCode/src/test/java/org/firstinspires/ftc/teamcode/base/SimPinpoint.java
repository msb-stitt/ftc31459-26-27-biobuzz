package org.firstinspires.ftc.teamcode.base;

import com.pedropathing.revhub.localizers.PinpointConfig;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;

import org.firstinspires.ftc.teamcode.base.odometry.PinpointSource;

/**
 * A Pinpoint on the simulated robot: each pod's raw count from how far the
 * robot has moved and turned, as the robot's {@link PinpointConfig} says the
 * pods are mounted.
 *
 * <p>The forward pod sits {@code xPodOffset} to the robot's left of its middle,
 * so turning counterclockwise rolls it backward by that offset times the turn.
 * The strafe pod sits {@code yPodOffset} ahead of the middle, so the same turn
 * rolls it to the left. Those are the SDK's meanings, from
 * {@code GoBildaPinpointDriver.setOffsets}. A pod set {@code REVERSED} counts
 * down as the robot moves its way, which is what the setting is there to undo.
 */
public final class SimPinpoint implements PinpointSource {

    private final SimRobot.SimLocalizer robot;
    private final PinpointConfig config;
    private int encoderX;
    private int encoderY;
    private double heading;

    public SimPinpoint(SimRobot.SimLocalizer robot, PinpointConfig config) {
        this.robot = robot;
        this.config = config;
    }

    @Override
    public void update() {
        double xOffset_in = config.offsetUnits.get().toInches(config.xPodOffset.get());
        double yOffset_in = config.offsetUnits.get().toInches(config.yPodOffset.get());
        double turned_rad = robot.turnedRadians();
        double forwardPod_in = robot.forwardInches() - xOffset_in * turned_rad;
        double strafePod_in = robot.strafeInches() + yOffset_in * turned_rad;
        double ticksPerInch = PinpointSource.ticksPerMm(config.podType.get()) * 25.4;
        encoderX = (int) Math.round(sign(config.xPodDirection.get()) * forwardPod_in * ticksPerInch);
        encoderY = (int) Math.round(sign(config.yPodDirection.get()) * strafePod_in * ticksPerInch);
        heading = turned_rad;
    }

    private static int sign(GoBildaPinpointDriver.EncoderDirection direction) {
        return direction == GoBildaPinpointDriver.EncoderDirection.REVERSED ? -1 : 1;
    }

    @Override
    public int getEncoderX() {
        return encoderX;
    }

    @Override
    public int getEncoderY() {
        return encoderY;
    }

    @Override
    public double getHeadingRadians() {
        return heading;
    }
}
