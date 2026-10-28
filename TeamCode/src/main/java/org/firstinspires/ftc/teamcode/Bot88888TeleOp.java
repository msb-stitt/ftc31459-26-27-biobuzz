package org.firstinspires.ftc.teamcode;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.behaviors.InterruptedBehavior;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * Robot 88888's TeleOp: StarterBot TeleOp's driving, and nothing else, since
 * 88888 is a Pinpoint and a drivetrain with no intake, launcher or windmill.
 * Set {@code Constants.thisRobot} to {@code BOT_88888} before running it.
 *
 * <ul>
 *   <li>Left stick drives and strafes; the right stick's x turns.</li>
 * </ul>
 *
 * <p>One command starts when PLAY is pressed and drives from the sticks until
 * STOP. A command that needs the drivetrain sets it aside, and it comes back
 * when that one ends.
 *
 * <p>Every stick read and the drive asked for go to the flight log. Pedro's
 * pose is already there, under {@code Robot/Pose}.
 */
@TeleOp(name = "88888 TeleOp", group = "88888")
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class Bot88888TeleOp extends CorbelsTeleOp {

    CorbelsMecanum drivetrain;
    Command driveFromTheSticks;

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init() {
        initBefore();
        // Live in AdvantageScope from INIT, at 192.168.43.1, only when the
        // Live NetworkTables switch OpMode has turned it on. For practice only:
        // FTC rule R704 forbids it at competitions.
        Tracker.serveLive();
        drivetrain = new CorbelsMecanum(hardware);
        initAfter(drivetrain);
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void start() {
        startBefore();
        driveFromTheSticks = driveWhileTheSticksAreRead();
        Scheduler.schedule(driveFromTheSticks);
        startAfter();
    }

    /** Nothing of its own: the command does the work, inside loopAfter(). */
    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();
        loopAfter();
    }

    /**
     * Drives from the sticks for as long as the OpMode runs. A command that
     * also needs the drivetrain sets it aside, and it comes back when that ends.
     */
    Command driveWhileTheSticksAreRead() {
        Command driveWhileTheSticksAreRead = Command.build()
                .setExecute(this::driveFromTheSticksThisLoop)
                .setDone(this::runsUntilCancelled)
                .requiring(drivetrain)
                .setInterruptedBehavior(InterruptedBehavior.SUSPEND);
        return driveWhileTheSticksAreRead;
    }

    /** One loop of {@link #driveWhileTheSticksAreRead()}: the sticks read now, and driven. */
    void driveFromTheSticksThisLoop() {
        double leftStickX = gamepad1.left_stick_x;
        double leftStickY = gamepad1.left_stick_y;
        double rightStickX = gamepad1.right_stick_x;
        Tracker.publish("gamepad1/leftStickX", leftStickX);
        Tracker.publish("gamepad1/leftStickY", leftStickY);
        Tracker.publish("gamepad1/rightStickX", rightStickX);

        double forwardXSpeed = -leftStickY;
        double strafeYSpeed = -leftStickX;
        double turnCcwSpeed = -rightStickX;
        Tracker.publish("drivetrain/forwardXSpeed", forwardXSpeed);
        Tracker.publish("drivetrain/strafeYSpeed", strafeYSpeed);
        Tracker.publish("drivetrain/turnCcwSpeed", turnCcwSpeed);
        drivetrain.sticks(forwardXSpeed, strafeYSpeed, turnCcwSpeed);
    }

    /** The drive never finishes by itself. */
    boolean runsUntilCancelled() {
        return false;
    }
}
