package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

/**
 * Robot 88888's own values. {@link Constants#thisRobot} picks this file when it
 * names {@code BOT_88888}; what every robot shares stays in {@link Constants}.
 */
class Bot88888 {

    /** Which way each drive motor is mounted. */
    static void drivetrain(MecanumConfig c) {
        // goBILDA's stock StarterBot reverses the left two. MEASURE: Mecanum Tuner.
        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
    }
}
