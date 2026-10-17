package org.firstinspires.ftc.teamcode;

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
        buttons.whileHeld(() -> gamepad1.right_bumper, launcher.fire());
    }

    @Override
    public void start() {
        startBefore();
        Scheduler.schedule(intake.run(() -> gamepad1.right_trigger - gamepad1.left_trigger
                //+ (launcher.feeding() ? Launcher.INTAKE_BOOST : 0)
                )
        );
        startAfter();
    }

    @Override
    public void loop() {
        loopBefore();
        drivetrain.sticks(-gamepad1.left_stick_y, -gamepad1.left_stick_x, -gamepad1.right_stick_x);
        loopAfter();
    }
}
