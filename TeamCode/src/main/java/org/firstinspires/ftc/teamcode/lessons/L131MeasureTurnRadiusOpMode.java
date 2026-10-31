package org.firstinspires.ftc.teamcode.lessons;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.commands.Commands;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.Calibration;
import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.firstinspires.ftc.teamcode.pedro.Constants;

import static org.firstinspires.ftc.teamcode.base.CorbelsDriveTrain.*;

/**
 * L131: measure how far a wheel is from the middle of the robot -- by spinning
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
 * <p><b>Run L131MeasureTicksPerInchOpMode first</b> -- this one converts ticks to inches with
 * {@link Constants#ticksPerInch}, so a wrong value there makes a wrong answer
 * here.
 *
 * <p><b>What to do.</b> Press start and spin the robot on the spot, at least
 * {@link #neededTurns} full turns, keeping it in roughly the same place. Read
 * the number off the Driver Station.
 *
 * <p>Passes when: LessonsTest.l131_theWheelsRollThroughoutAMeasurementAndBrakeAgainAfterwards
 */
@TeleOp(name = "L131 Measure turn radius", group = "Lessons")
// When on L131S020, see
// https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l131.html#l131s020
// for what to do here.
@Disabled
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class L131MeasureTurnRadiusOpMode extends CorbelsTeleOp {

    double neededTurns = 2.0;

    int[] startTicks;
    double previousHeading_rad;
    double turned_rad, wheel_in;

    L131TicksDriveTrain drivetrain;

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init() {
        initBefore();
        drivetrain = new L131TicksDriveTrain(hardware);
        initAfter(drivetrain);
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void start() {
        startBefore();
        // The wheels must roll freely, so no braking while we spin.
        drivetrain.forceCoastForCharacterization();
        startTicks = drivetrain.wheelTicks();
        previousHeading_rad = follower.pose().heading();
        Command measureTurnRadiusEveryLoop = Commands.infinite(this::measureTurnRadius);
        Scheduler.schedule(measureTurnRadiusEveryLoop);
        startAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void stop() {
        // Braking is allowed again, however this run ended -- STOP pressed
        // early, or an exception in the loop. Then hand the wheels back before
        // the follower's last update, or they keep whatever speed the last loop
        // commanded.
        drivetrain.allowConfiguredBrakeMode();
        drivetrain.stop();
        stopAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();
        loopAfter();
    }

    /** Once every loop, from PLAY until STOP: the scheduler runs it. */
    void measureTurnRadius() {
        drivetrain.setCommandedWheels(0.0, 0.0, 0.0, 0.0);

        double heading_rad = follower.pose().heading();
        turned_rad += Calibration.unwrap(previousHeading_rad, heading_rad);
        previousHeading_rad = heading_rad;

        int[] now = drivetrain.wheelTicks();
        int frontLeftTicks = now[FL] - startTicks[FL];
        int frontRightTicks = now[FR] - startTicks[FR];
        int backLeftTicks = now[BL] - startTicks[BL];
        int backRightTicks = now[BR] - startTicks[BR];
        double turnTicks = Calibration.turnPart(frontLeftTicks, frontRightTicks, backLeftTicks,
                backRightTicks);
        wheel_in = turnTicks / Constants.ticksPerInch;

        double measuredTurnRadius_in = Calibration.turnRadiusInches(wheel_in, turned_rad);
        Tracker.publish("measure/turned_rad", turned_rad);
        Tracker.publish("measure/wheel_in", wheel_in);
        Tracker.publish("measure/measuredTurnRadius_in", measuredTurnRadius_in);

        double absTurned_rad = Math.abs(turned_rad);
        double turnsDone = absTurned_rad / (2.0 * Math.PI);
        Tracker.printToDs("Spin the robot  %.2f of %.0f turns", turnsDone, neededTurns);
        Tracker.printToDs("Wheels travelled  %.1f inches", wheel_in);
        boolean turnedEnough = turnsDone >= neededTurns;
        if (turnedEnough) {
            Tracker.printToDs();
            Tracker.printToDs("turn radius  %.2f inches", measuredTurnRadius_in);
            Tracker.printToDs("Measure the diagonal between wheels and halve it; they should agree.");
        } else {
            Tracker.printToDs("Currently  %.2f inches", measuredTurnRadius_in);
        }
        Tracker.printToDs("ticksPerInch in use  %.2f", Constants.ticksPerInch);
    }
}
