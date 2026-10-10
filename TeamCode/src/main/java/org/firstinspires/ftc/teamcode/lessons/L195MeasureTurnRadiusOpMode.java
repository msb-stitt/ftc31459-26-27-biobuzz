package org.firstinspires.ftc.teamcode.lessons;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.Calibration;
import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.firstinspires.ftc.teamcode.pedro.Constants;

/**
 * L195: measure how far a wheel is from the middle of the robot -- by spinning
 * the robot.
 *
 * <p>L190 turned "turn at one radian per second" into a wheel speed by
 * multiplying by a radius. This measures that radius, the same way as ticks per inch: spin
 * the robot by hand, and compare what the Pinpoint says it turned with how far
 * the wheels travelled.
 *
 * <p>A wheel {@code r} inches from the middle travels {@code r} inches for every
 * radian the robot turns. So radius is wheel inches divided by radians.
 *
 * <p><b>Run L195MeasureTicksPerInchOpMode first</b> -- this one converts ticks to inches with
 * {@link Constants#ticksPerInch}, so a wrong value there makes a wrong answer
 * here.
 *
 * <p><b>What to do.</b> Press start and spin the robot on the spot, at least
 * {@link #NEEDED_TURNS} full turns, keeping it in roughly the same place. Read
 * the number off the Driver Station.
 *
 * <p>Passes when: LessonsTest.l195_theWheelsRollThroughoutAMeasurementAndBrakeAgainAfterwards
 */
@TeleOp(name = "L195 Measure turn radius", group = "Lessons")
// When on L195S020, see
// https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l195.html#l195s020
// for what to do here.
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L195MeasureTurnRadiusOpMode extends CorbelsTeleOp {

    static final double NEEDED_TURNS = 2.0;

    int[] startTicks;
    double previousHeading;
    double radians, wheelInches;

    L195TicksDriveTrain drivetrain;

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init() {
        initBefore();
        drivetrain = new L195TicksDriveTrain(hardware);
        initAfter(drivetrain);
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void start() {
        startBefore();
        // The wheels must roll freely, so no braking while we spin.
        drivetrain.forceCoastForCharacterization();
        startTicks = drivetrain.wheelTicks();
        previousHeading = follower.pose().heading();
        startAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void stop() {
        // Braking is allowed again, however this run ended -- STOP pressed
        // early, or an exception in the loop. Then hand the wheels back before
        // the follower's last update, or they keep whatever power the last loop
        // commanded.
        drivetrain.allowConfiguredBrakeMode();
        drivetrain.stop();
        stopAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();
        drivetrain.setCommandedWheels(0.0, 0.0, 0.0, 0.0);

        double heading = follower.pose().heading();
        radians += Calibration.unwrap(previousHeading, heading);
        previousHeading = heading;

        int[] now = drivetrain.wheelTicks();
        double turnTicks = Calibration.turnPart(now[0] - startTicks[0], now[1] - startTicks[1],
                now[2] - startTicks[2], now[3] - startTicks[3]);
        wheelInches = turnTicks / Constants.ticksPerInch;

        double measured = Calibration.turnRadiusInches(wheelInches, radians);
        Tracker.publish("measure/radians", radians);
        Tracker.publish("measure/wheel_inches", wheelInches);
        Tracker.publish("measure/turnRadius_in", measured);

        Tracker.printToDs("Spin the robot  %.2f of %.0f turns",
                Math.abs(radians) / (2.0 * Math.PI), NEEDED_TURNS);
        Tracker.printToDs("Wheels travelled  %.1f inches", wheelInches);
        if (Math.abs(radians) >= NEEDED_TURNS * 2.0 * Math.PI) {
            Tracker.printToDs();
            Tracker.printToDs("turn radius  %.2f inches", measured);
            Tracker.printToDs("Measure the diagonal between wheels and halve it; they should agree.");
        } else {
            Tracker.printToDs("Currently  %.2f inches", measured);
        }
        Tracker.printToDs("ticksPerInch in use  %.2f", Constants.ticksPerInch);

        loopAfter();
    }
}
