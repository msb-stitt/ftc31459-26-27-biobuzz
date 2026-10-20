package org.firstinspires.ftc.teamcode;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.behaviors.InterruptedBehavior;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

import java.util.function.Function;

/**
 * goBILDA's StarterBot TeleOp, on our drivetrain, with its mechanisms as Ivy
 * commands. The controls are goBILDA's, all on gamepad 1:
 *
 * <ul>
 *   <li>Left stick drives and strafes; the right stick's x turns.</li>
 *   <li>The right trigger, less the left trigger, runs the intake.</li>
 *   <li>Holding the right bumper spins the launcher up and feeds it.</li>
 * </ul>
 *
 * <p>Three commands start when PLAY is pressed and run until STOP: one drives
 * from the sticks, one runs the intake from the triggers, one fires the
 * launcher from the bumper. A command that needs the drivetrain, the intake or
 * the launcher sets the one using it aside, and that one comes back when it ends.
 *
 * <p>Every stick, trigger and button read, the drive asked for, and each
 * wheel's power go to the flight log. Pedro's pose is already there, under
 * {@code Robot/Pose}, and the intake and launcher log their own.
 */
@TeleOp(name = "StarterBot TeleOp", group = "StarterBot")
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class StarterBotTeleOp extends CorbelsTeleOp {

    /** Where the mechanisms come from. Tests hand in fakes. */
    static Function<HardwareMap, Intake> intakes = Intake::from;
    static Function<HardwareMap, Launcher> launchers = Launcher::from;

    CorbelsMecanum drivetrain;
    Intake intake;
    Launcher launcher;
    Command driveFromTheSticks;
    Command runTheIntakeFromTheTriggers;
    Command fireTheLauncherFromTheBumper;

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init() {
        initBefore();
        intake = intakes.apply(hardwareMap);
        launcher = launchers.apply(hardwareMap);
        drivetrain = new CorbelsMecanum(hardware);
        initAfter(drivetrain);
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void start() {
        startBefore();
        driveFromTheSticks = driveWhileTheSticksAreRead();
        runTheIntakeFromTheTriggers = intake.run(this::intakePowerThisLoop);
        fireTheLauncherFromTheBumper = launcher.fireWhile(this::rightBumperIsHeld);
        Scheduler.schedule(driveFromTheSticks);
        Scheduler.schedule(runTheIntakeFromTheTriggers);
        Scheduler.schedule(fireTheLauncherFromTheBumper);
        startAfter();
    }

    /** Nothing of its own: the commands do the work, inside loopAfter(). */
    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();
        loopAfter();
    }

    /** Each wheel's power, after the follower has written them this loop. */
    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void afterLoop() {
        double[] wheelPowers = drivetrain.wheelPowers();
        double frontLeftPower = wheelPowers[0];
        double frontRightPower = wheelPowers[1];
        double backLeftPower = wheelPowers[2];
        double backRightPower = wheelPowers[3];
        Tracker.publish("wheel/frontLeft/power", frontLeftPower);
        Tracker.publish("wheel/frontRight/power", frontRightPower);
        Tracker.publish("wheel/backLeft/power", backLeftPower);
        Tracker.publish("wheel/backRight/power", backRightPower);
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
        Tracker.publish("stick/leftX", leftStickX);
        Tracker.publish("stick/leftY", leftStickY);
        Tracker.publish("stick/rightX", rightStickX);

        double forwardSpeed = -leftStickY;
        double strafeLeftSpeed = -leftStickX;
        double turnCounterclockwiseSpeed = -rightStickX;
        Tracker.publish("drive/forward", forwardSpeed);
        Tracker.publish("drive/strafeLeft", strafeLeftSpeed);
        Tracker.publish("drive/turnCounterclockwise", turnCounterclockwiseSpeed);
        drivetrain.sticks(forwardSpeed, strafeLeftSpeed, turnCounterclockwiseSpeed);
    }

    /** The drive never finishes by itself. */
    boolean runsUntilCancelled() {
        return false;
    }

    /** The right bumper spins the launcher up while it is held. */
    boolean rightBumperIsHeld() {
        boolean rightBumper = gamepad1.right_bumper;
        Tracker.publish("button/rightBumper", rightBumper);
        return rightBumper;
    }

    /**
     * The right trigger pulls in, the left pushes out, and while the launcher
     * feeds the intake gets goBILDA's boost.
     */
    double intakePowerThisLoop() {
        double pullInPower = gamepad1.right_trigger;
        double pushOutPower = gamepad1.left_trigger;
        Tracker.publish("trigger/right", pullInPower);
        Tracker.publish("trigger/left", pushOutPower);
        double triggersPower = pullInPower - pushOutPower;
        double boostWhileFeeding = 0;
        if (launcher.feeding()) {
            boostWhileFeeding = Launcher.INTAKE_BOOST;
        }
        double intakePower = triggersPower + boostWhileFeeding;
        return intakePower;
    }
}
