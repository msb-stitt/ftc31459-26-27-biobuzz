package org.firstinspires.ftc.teamcode.base.odometry;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;

import org.firstinspires.ftc.robotcore.external.navigation.UnnormalizedAngleUnit;

/**
 * Reads the robot's Pinpoint. The only part of a Pinpoint localizer that
 * touches hardware, so the maths can be tested on a laptop.
 */
public class HardwarePinpointSource implements PinpointSource {

    private final GoBildaPinpointDriver pinpoint;

    public HardwarePinpointSource(GoBildaPinpointDriver pinpoint) {
        this.pinpoint = pinpoint;
    }

    @Override
    public void update() {
        pinpoint.update();
    }

    @Override
    public int getEncoderX() {
        return pinpoint.getEncoderX();
    }

    @Override
    public int getEncoderY() {
        return pinpoint.getEncoderY();
    }

    @Override
    public double getHeadingRadians() {
        return pinpoint.getHeading(UnnormalizedAngleUnit.RADIANS);
    }
}
