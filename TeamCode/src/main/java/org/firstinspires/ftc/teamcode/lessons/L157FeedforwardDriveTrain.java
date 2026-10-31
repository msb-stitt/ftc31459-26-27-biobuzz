package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.drivetrain.DrivePowers;

import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.RobotHardware;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.firstinspires.ftc.teamcode.pedro.Constants;

/**
 * L157: ask for a speed, and send each wheel the volts L153 says it takes.
 *
 * <p>Each wheel's volts are kV times its speed, plus kS to get it moving. The
 * forward, strafe and turn parts are mixed into the four wheels the same way
 * powers are, so each wheel gets a forward part, a strafe part and a turn
 * part, each in volts. Turning comes first: if the battery cannot give every
 * wheel its volts, the forward and strafe parts are cut by one scale, so the
 * robot still goes the way it was asked, only slower.
 *
 * <p>Wheel order is front left, front right, back left, back right.
 *
 * <p>Passes when: LessonsTest.l157_theVoltsAreKsPlusKvTimesTheSpeed,
 * LessonsTest.l157_kSWaitsForTheWheelToBeAskedToMove and
 * LessonsTest.l157_aRequestTooBigIsCutToFit
 */
class L157FeedforwardDriveTrain extends CorbelsMecanum {

    /** A wheel's kS turns on when its kV volts pass this share of its kS. */
    static final double KS_ON_SHARE = 0.05;
    /** And turns off again only when they drop below this share. */
    static final double KS_OFF_SHARE = 0.025;

    /** Whether each wheel's kS is on, so it does not flicker on and off at the edge. */
    boolean[] kSOn = new boolean[4];

    L157FeedforwardDriveTrain(RobotHardware hardware) {
        super(hardware);
    }

    /** Drives at these speeds: forward and to the left in inches per second, turning in radians per second. */
    void speeds(double forwardX_ips, double strafeY_ips, double turnCcw_radps) {
        double batteryVolts = hardware.batteryVolts();
        double[] forwardVolts = mixArcadeToWheelSpeeds(new DrivePowers(
                Constants.kVForwardVoltSecondsPerInch * forwardX_ips, 0.0, 0.0));
        double[] strafeVolts = mixArcadeToWheelSpeeds(new DrivePowers(
                0.0, Constants.kVStrafeVoltSecondsPerInch * strafeY_ips, 0.0));
        double[] turnVolts = mixArcadeToWheelSpeeds(new DrivePowers(
                0.0, 0.0, Constants.kVTurnVoltSecondsPerRadian * turnCcw_radps));

        double[] kS = new double[4];
        double scale = 1.0;
        for (int i = 0; i < 4; i++) {
            kS[i] = blendedKs(forwardVolts[i], strafeVolts[i], turnVolts[i]);
            double moveVolts = forwardVolts[i] + strafeVolts[i];
            scale = Math.min(scale, scaleThatFits(turnVolts[i], moveVolts, batteryVolts - kS[i]));
        }

        double[] powers = new double[4];
        for (int i = 0; i < 4; i++) {
            // When on L157S060, see
            // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l157.html#l157s060
            // for what to do here.
        }

        double[] wheels = normalized(powers);
        setCommandedWheels(wheels[FL], wheels[FR], wheels[BL], wheels[BR]);
        Tracker.publish("feedforward/scale", scale);
    }

    /** One wheel's kS: each way's kS, counted by how much of the wheel's volts that way asks for. */
    double blendedKs(double forwardVolts, double strafeVolts, double turnVolts) {
        double kS = 0.0;
        // When on L157S060, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l157.html#l157s060
        // for what to do here.
        return kS;
    }

    /** One wheel's kS, with the sign of its speed, or 0 V while it is asked to barely move. */
    double steppedKs(int wheel, double kVVolts, double kS) {
        double stepped = Math.signum(kVVolts) * kS;
        // When on L157S070, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l157.html#l157s070
        // for what to do here.
        return stepped;
    }

    /**
     * How much of the forward and strafe volts fit, from 0 to 1, once the turn
     * has taken its volts: the turn's volts plus that much of {@code moveVolts}
     * must stay within {@code voltsLeft}.
     */
    static double scaleThatFits(double turnVolts, double moveVolts, double voltsLeft) {
        if (moveVolts == 0.0) {
            return 1.0;
        }
        double fits = (voltsLeft - Math.signum(moveVolts) * turnVolts) / Math.abs(moveVolts);
        return Math.max(0.0, Math.min(1.0, fits));
    }
}
