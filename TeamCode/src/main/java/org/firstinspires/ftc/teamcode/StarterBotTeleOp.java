package org.firstinspires.ftc.teamcode;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;

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
 */
@TeleOp(name = "StarterBot TeleOp", group = "StarterBot")
public class StarterBotTeleOp extends CorbelsTeleOp {

    /** Where the mechanisms come from. Tests hand in fakes. */
    static Function<HardwareMap, Intake> intakes = Intake::from;
    static Function<HardwareMap, Launcher> launchers = Launcher::from;

    private CorbelsMecanum drivetrain;
    private Intake intake;
    private Launcher launcher;

    @Override
    public void init() {
        initBefore();
        intake = intakes.apply(hardwareMap);
        launcher = launchers.apply(hardwareMap);
        drivetrain = new CorbelsMecanum(hardware);
        initAfter(drivetrain);
    }

    @Override
    public void bindings() {
        Command fireTheLauncher = launcher.fire();
        buttons.whileHeld(this::rightBumperIsHeld, fireTheLauncher);
    }

    @Override
    public void start() {
        startBefore();
        Command runTheIntake = intake.run(this::intakePowerThisLoop);
        Scheduler.schedule(runTheIntake);
        startAfter();
    }

    @Override
    public void loop() {
        loopBefore();
        double forwardSpeed = -gamepad1.left_stick_y;
        double strafeLeftSpeed = -gamepad1.left_stick_x;
        double turnCounterclockwiseSpeed = -gamepad1.right_stick_x;
        drivetrain.sticks(forwardSpeed, strafeLeftSpeed, turnCounterclockwiseSpeed);
        loopAfter();
    }

    /** The right bumper spins the launcher up while it is held. */
    private boolean rightBumperIsHeld() {
        return gamepad1.right_bumper;
    }

    /**
     * The right trigger pulls in, the left pushes out, and while the launcher
     * feeds the intake gets goBILDA's boost.
     */
    private double intakePowerThisLoop() {
        double pullInPower = gamepad1.right_trigger;
        double pushOutPower = gamepad1.left_trigger;
        double triggersPower = pullInPower - pushOutPower;
        double boostWhileFeeding = 0;
        if (launcher.feeding()) {
            boostWhileFeeding = Launcher.INTAKE_BOOST;
        }
        double intakePower = triggersPower + boostWhileFeeding;
        return intakePower;
    }
}
