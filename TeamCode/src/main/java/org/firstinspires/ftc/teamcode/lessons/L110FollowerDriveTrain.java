package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.drivetrain.DrivePowers;

import org.firstinspires.ftc.teamcode.base.CorbelsDriveTrain;
import org.firstinspires.ftc.teamcode.base.RobotHardware;

/**
 * L110: the drivetrain the path follower holds.
 *
 * <p>Everything a drivetrain does is already written in
 * {@link CorbelsDriveTrain}: it sets each motor's direction, scales the four
 * powers when one asks for more than 1, and sends them to the motors. The one
 * part it leaves open is {@link #mix}, which turns three numbers into four
 * wheel powers. Those are L090's four lines.
 *
 * <p>The follower calls {@code drive} every loop with its three numbers, and
 * {@code drive} calls {@code mix}. The sticks go through {@code mix} too, by
 * {@code sticks}, so the driver and the follower agree about which wheel does
 * what.
 *
 * <p>Passes when: LessonsTest.l110_mixGivesEachWheelItsOwnSum
 */
class L110FollowerDriveTrain extends CorbelsDriveTrain {

    L110FollowerDriveTrain(RobotHardware hardware) {
        super(hardware);
    }

    /**
     * Three numbers, as four wheel powers. {@code forward} is along the robot's
     * nose, {@code strafe} towards its left, and {@code turn} counter-clockwise,
     * as in L090. {@code wheels[FL]} is the front left wheel's power, and
     * {@code FR}, {@code BL} and {@code BR} name the other three.
     */
    // protected: CorbelsDriveTrain's own mix is protected, so this one has to be too.
    @Override
    protected double[] mix(DrivePowers powers) {
        double forward = powers.forward();
        double strafe = powers.strafe();
        double turn = powers.turn();
        double[] wheels = new double[4];

        // When on L110S030, see
        // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l110.html#l110s030
        // for what to do here.

        return wheels;
    }
}
