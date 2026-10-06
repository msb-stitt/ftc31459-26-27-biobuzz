package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L050: tank drive on the robot, up on blocks. Each motor is told which way it
 * is mounted, so a positive power turns its wheel forward.
 *
 * <p>Every OpMode has four methods. {@code init} runs once when INIT is
 * pressed, {@code start} once when PLAY is pressed, {@code loop} over and over
 * until STOP, and {@code stop} once at the end. The {@code Before} and
 * {@code After} calls are the robot's own housekeeping: the hardware, the
 * flight log and the live values. The code between them is the lesson's.
 *
 * <p>Passes when: LessonsTest.l050_eachMotorTakesItsDirectionFromTheConfig
 */
@TeleOp(name = "L050 Blocks", group = "Lessons")
// When on L050S020, see
// https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l050.html#l050s020
// for what to do here.
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L050BlocksOpMode extends CorbelsTeleOp {

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init() {
        initBefore();

        // When on L050S040, see
        // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l050.html#l050s040
        // for what to do here.

        initAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void start() {
        startBefore();
        startAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();

        double leftStickY = gamepad1.left_stick_y;
        Tracker.publish("stick/leftY", leftStickY);
        double rightStickY = gamepad1.right_stick_y;
        Tracker.publish("stick/rightY", rightStickY);

        double leftSpeed = -leftStickY;
        Tracker.publish("speed/left", leftSpeed);
        double rightSpeed = -rightStickY;
        Tracker.publish("speed/right", rightSpeed);

        hardware.frontLeft.setPower(leftSpeed);
        hardware.backLeft.setPower(leftSpeed);

        hardware.frontRight.setPower(rightSpeed);
        hardware.backRight.setPower(rightSpeed);

        loopAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void stop() {
        stopAfter();
    }
}
