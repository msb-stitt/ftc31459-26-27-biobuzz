package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

/**
 * Robot 31459's own values. {@link Constants#thisRobot} picks this file when it
 * names {@code BOT_31459}; what every robot shares stays in {@link Constants}.
 */
class Bot31459 {

    /** Which way each drive motor is mounted. */
    static void drivetrain(MecanumConfig c) {
        // Not yet measured on 31459: 37523's, as a start. MEASURE: Mecanum Tuner.
        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backLeftDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
    }
}
