package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

/**
 * Robot 37523's own values. {@link Constants#thisRobot} picks this file when it
 * names {@code BOT_37523}; what every robot shares stays in {@link Constants}.
 */
class Bot37523 {

    /** Which way each drive motor is mounted. */
    static void drivetrain(MecanumConfig c) {
        // Set on 2026-10-17, when 37523 first drove. MEASURE: Mecanum Tuner.
        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backLeftDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
    }
}
