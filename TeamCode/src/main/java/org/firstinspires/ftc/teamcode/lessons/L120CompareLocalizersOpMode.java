package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * L120: drive around while two localizers disagree. Your localizer reads the
 * four drive wheels, and the robot's own reads the Pinpoint's two pods. Both
 * are published under Localizer/, so they can be graphed against each other
 * and checked with a tape measure.
 *
 * <p>Every OpMode has four methods. {@code init} runs once when INIT is
 * pressed, {@code start} once when PLAY is pressed, {@code loop} over and over
 * until STOP, and {@code stop} once at the end. The {@code Before} and
 * {@code After} calls are the robot's own housekeeping: the hardware, the
 * flight log and the live values. The code between them is the lesson's.
 *
 * <p>Passes when: LessonsTest.l120_theEncoderLocalizerRunsAlongsideAndIsLogged
 */
@TeleOp(name = "L120 Compare Localizers", group = "Lessons")
// When on L120S020, see
// https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l120.html#l120s020
// for what to do here.
@Disabled
// public: the robot only runs an OpMode that is public.
public class L120CompareLocalizersOpMode extends CorbelsTeleOp {

    boolean previousButtonA;

    CorbelsMecanum drivetrain;

    // public: OpMode's own init is public, so this one has to be too.
    @Override
    public void init() {
        initBefore();

        drivetrain = new CorbelsMecanum(hardware);

        initAfter(drivetrain);
    }

    // public: OpMode's own start is public, so this one has to be too.
    @Override
    public void start() {
        startBefore();
        startAfter();
    }

    // public: OpMode's own loop is public, so this one has to be too.
    @Override
    public void loop() {
        loopBefore();

        double leftStickY = gamepad1.left_stick_y;
        Tracker.publish("stick/leftY", leftStickY);
        double rightStickY = gamepad1.right_stick_y;
        Tracker.publish("stick/rightY", rightStickY);

        double leftStickX = gamepad1.left_stick_x;
        Tracker.publish("stick/leftX", leftStickX);
        double rightStickX = gamepad1.right_stick_x;
        Tracker.publish("stick/rightX", rightStickX);

        double band = 0.05;
        double forward = squared(deadband(-leftStickY, band));
        Tracker.publish("arcade/forward", forward);
        double turn = squared(deadband(-rightStickX, band));
        Tracker.publish("arcade/turn", turn);

        double strafe = squared(deadband(-leftStickX, band));
        Tracker.publish("arcade/strafe", strafe);

        drivetrain.sticks(forward, strafe, turn);

        boolean buttonA = gamepad1.a;
        Tracker.publish("button/a", buttonA);

        if (buttonA != previousButtonA) {
            if (buttonA) {
                Tracker.publish("button/event", "A pressed");
            } else {
                Tracker.publish("button/event", "A released");
            }
        }
        previousButtonA = buttonA;

        loopAfter();
    }

    // protected: CorbelsOpMode's own shadowLocalizers is protected, so this one has to be too.
    @Override
    protected void shadowLocalizers() {
        // When on L120S030, see
        // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l120.html#l120s030
        // for what to do here.
    }

    // public: OpMode's own stop is public, so this one has to be too.
    @Override
    public void stop() {
        stopAfter();
    }

    double deadband(double value, double band) {
        if (Math.abs(value) < band) {
            return 0;
        }
        return value;
    }

    double squared(double value) {
        return value * Math.abs(value);
    }
}
