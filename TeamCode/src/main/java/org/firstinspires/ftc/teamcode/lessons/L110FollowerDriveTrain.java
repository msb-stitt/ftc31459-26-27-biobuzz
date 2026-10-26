package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.drivetrain.DrivePowers;

import org.firstinspires.ftc.teamcode.base.CorbelsDriveTrain;
import org.firstinspires.ftc.teamcode.base.RobotHardware;

/**
 * L110: the drivetrain the path follower holds.
 *
 * <p>Everything a drivetrain does is already written in
 * {@link CorbelsDriveTrain}: it sets each motor's direction, scales the four
 * speeds when one asks for more than 1, and sends them to the motors. The one
 * part it leaves open is {@link #mixArcadeToWheelSpeeds}, which turns an
 * arcade command into wheel speeds. Those are L090's mixing lines.
 *
 * <p>The follower calls {@code drive} every loop with its arcade command, and
 * {@code drive} calls {@code mixArcadeToWheelSpeeds}. The sticks go through it
 * too, by {@code sticks}, so the driver and the follower agree about which
 * wheel does what.
 *
 * <p>Passes when: LessonsTest.l110_mixGivesEachWheelItsOwnSum
 */
class L110FollowerDriveTrain extends CorbelsDriveTrain {

    L110FollowerDriveTrain(RobotHardware hardware) {
        super(hardware);
    }

    /**
     * The arcade command, as wheel speeds. {@code forwardSpeed} is along the
     * robot's nose, {@code strafeSpeed} towards its left, and {@code turnSpeed}
     * counter-clockwise, as in L090. {@code wheelSpeeds[FL]} is the front left
     * wheel's speed, and {@code FR}, {@code BL} and {@code BR} name the others.
     */
    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public double[] mixArcadeToWheelSpeeds(DrivePowers speeds) {
        double forwardSpeed = speeds.forward();
        double strafeSpeed = speeds.strafe();
        double turnSpeed = speeds.turn();
        double[] wheelSpeeds = new double[4];

        // When on L110S030, see
        // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l110.html#l110s030
        // for what to do here.

        return wheelSpeeds;
    }
}
