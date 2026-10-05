package org.firstinspires.ftc.teamcode.lessons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.drivetrain.Drivetrain;
import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;


import org.firstinspires.ftc.teamcode.base.CorbelsOpMode;
import org.firstinspires.ftc.teamcode.OpModeStorage;
import org.firstinspires.ftc.teamcode.base.OpModeHarness;
import org.firstinspires.ftc.teamcode.base.RobotHardware;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * One test per lesson: what the robot should do once the student's code is
 * right. Each of these FAILS against the starter file and PASSES against the
 * finished lesson -- so a student knows when they're done.
 */
public class LessonsTest {

    private static final PoseFactory POSES = PoseFactory.degrees();
    private static final double EPS = 1e-6;

    @After
    public void tearDown() {
        OpModeHarness.restoreFactories();
        Scheduler.reset();
    }

    /** A number the lesson sent to Panels, whatever its Java type was. */
    private static double number(Map<String, Object> values, String key) {
        Object v = values.get(key);
        assertTrue("nothing was logged as \"" + key + "\"; got " + values.keySet(), v instanceof Number);
        return ((Number) v).doubleValue();
    }

    // -------------------------------------------------------------- L020

    @Test
    public void l020_logsBothSticksTheWayTheGamepadGivesThem() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L020SticksOpMode"));
        h.init();
        h.start();
        h.gamepad1.left_stick_y = -1.0f;      // pushed away from the driver
        h.gamepad1.right_stick_y = 0.5f;      // pulled halfway back
        h.loop();
        h.stop();

        Map<String, Object> values = Tracker.values();
        assertEquals("the left stick as the gamepad gives it, with no minus sign yet",
                -1.0, number(values, "stick/leftY"), EPS);
        assertEquals("the right stick as the gamepad gives it",
                0.5, number(values, "stick/rightY"), EPS);
    }

    // -------------------------------------------------------------- L030

    @Test
    public void l030_eachSpeedIsItsStickTurnedRound() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L030SpeedsOpMode"));
        h.init();
        h.start();
        h.gamepad1.left_stick_y = -1.0f;      // pushed away from the driver
        h.gamepad1.right_stick_y = 0.5f;      // pulled halfway back
        h.loop();
        h.stop();

        Map<String, Object> values = Tracker.values();
        assertEquals("the left stick still as the gamepad gives it",
                -1.0, number(values, "stick/leftY"), EPS);
        assertEquals("the left stick pushed away is full speed forward",
                1.0, number(values, "speed/left"), EPS);
        assertEquals("the right stick pulled halfway back is half speed backward",
                -0.5, number(values, "speed/right"), EPS);
    }

    // -------------------------------------------------------------- L040

    @Test
    public void l040_eachSpeedDrivesItsOwnSide() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L040TankOpMode"));
        h.init();
        h.start();
        h.gamepad1.left_stick_y = -1.0f;      // pushed away from the driver
        h.gamepad1.right_stick_y = 0.5f;      // pulled halfway back
        h.loop();

        assertEquals("the left speed drives the front left wheel",
                1.0, h.motors.get(OpModeHarness.FRONT_LEFT).power, EPS);
        assertEquals("and the back left wheel",
                1.0, h.motors.get(OpModeHarness.BACK_LEFT).power, EPS);
        assertEquals("the right speed drives the front right wheel",
                -0.5, h.motors.get(OpModeHarness.FRONT_RIGHT).power, EPS);
        assertEquals("and the back right wheel",
                -0.5, h.motors.get(OpModeHarness.BACK_RIGHT).power, EPS);
        h.stop();
    }

    @Test
    public void l040_bothSticksForwardDriveTheSimulatedRobotForward() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L040TankOpMode"));
        h.init();
        h.start();
        h.gamepad1.left_stick_y = -1.0f;      // both sticks fully forward
        h.gamepad1.right_stick_y = -1.0f;
        h.loops(100, 0);                      // 100 x 10 ms of simulated time

        Pose pose = h.robot.localizer.state().pose();
        assertTrue("drove forward, and got a fair way: " + forwardOf(pose), forwardOf(pose) > 40);
        assertEquals("no sideways drift", 0, lateralOf(pose), EPS);
        h.stop();
    }

    @Test
    public void l050_eachMotorTakesItsDirectionFromTheConfig() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L050BlocksOpMode"));
        h.init();

        assertEquals("front left", Constants.drivetrainConfig.frontLeftDirection.get(),
                h.motors.get(OpModeHarness.FRONT_LEFT).direction);
        assertEquals("front right", Constants.drivetrainConfig.frontRightDirection.get(),
                h.motors.get(OpModeHarness.FRONT_RIGHT).direction);
        assertEquals("back left", Constants.drivetrainConfig.backLeftDirection.get(),
                h.motors.get(OpModeHarness.BACK_LEFT).direction);
        assertEquals("back right", Constants.drivetrainConfig.backRightDirection.get(),
                h.motors.get(OpModeHarness.BACK_RIGHT).direction);
        h.stop();
    }

    @Test
    public void l060_aLetGoStickCountsAsCentred() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L060ShapingOpMode"));
        h.init();
        h.start();
        h.gamepad1.left_stick_y = -0.03f;     // two sticks that were let go
        h.gamepad1.right_stick_y = 0.04f;
        h.loop();

        assertEquals("inside the deadband, so the robot does not creep",
                0.0, h.motors.get(OpModeHarness.FRONT_LEFT).power, EPS);
        assertEquals(0.0, h.motors.get(OpModeHarness.BACK_LEFT).power, EPS);
        assertEquals(0.0, h.motors.get(OpModeHarness.FRONT_RIGHT).power, EPS);
        assertEquals(0.0, h.motors.get(OpModeHarness.BACK_RIGHT).power, EPS);
        h.stop();
    }

    @Test
    public void l060_halfAStickIsAQuarterOfThePower() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L060ShapingOpMode"));
        h.init();
        h.start();
        h.gamepad1.left_stick_y = -0.5f;      // half forward
        h.gamepad1.right_stick_y = 0.5f;      // half back
        h.loop();

        assertEquals("half stick is quarter power",
                0.25, h.motors.get(OpModeHarness.FRONT_LEFT).power, EPS);
        assertEquals(0.25, h.motors.get(OpModeHarness.BACK_LEFT).power, EPS);
        assertEquals("and squaring keeps the sign",
                -0.25, h.motors.get(OpModeHarness.FRONT_RIGHT).power, EPS);
        assertEquals(-0.25, h.motors.get(OpModeHarness.BACK_RIGHT).power, EPS);

        h.gamepad1.left_stick_y = -1.0f;      // fully forward
        h.loop();
        assertEquals("full stick still reaches full power",
                1.0, h.motors.get(OpModeHarness.FRONT_LEFT).power, EPS);
        h.stop();
    }

    @Test
    public void l070_logsTheAButton() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L070ButtonsOpMode"));
        h.init();
        h.start();
        h.gamepad1.a = true;
        h.loop();
        assertEquals("A held down", true, Tracker.values().get("button/a"));

        h.gamepad1.a = false;
        h.loop();
        assertEquals("A let go", false, Tracker.values().get("button/a"));
        h.stop();
    }

    @Test
    public void l070_saysWhenTheAButtonIsPressedAndReleased() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L070ButtonsOpMode"));
        h.init();
        h.start();
        h.loop();
        assertEquals("nothing has changed yet", null, Tracker.values().get("button/event"));

        h.gamepad1.a = true;
        h.loop();
        assertEquals("the loop the button went down on says so",
                "A pressed", Tracker.values().get("button/event"));

        h.loop();
        assertEquals("holding it says nothing new",
                "A pressed", Tracker.values().get("button/event"));

        h.gamepad1.a = false;
        h.loop();
        assertEquals("letting go says so",
                "A released", Tracker.values().get("button/event"));
        h.stop();
    }

    @Test
    public void l070_logsBothSticksSideways() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L070ButtonsOpMode"));
        h.init();
        h.start();
        h.gamepad1.left_stick_x = 0.5f;
        h.gamepad1.right_stick_x = -0.25f;
        h.loop();

        assertEquals(0.5, number(Tracker.values(), "stick/leftX"), EPS);
        assertEquals(-0.25, number(Tracker.values(), "stick/rightX"), EPS);
        h.stop();
    }

    @Test
    public void l080_theLeftStickDrivesBothSides() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L080ArcadeOpMode"));
        h.init();
        h.start();
        h.gamepad1.left_stick_y = -0.5f;      // half forward
        h.loop();

        assertEquals("half forward is a quarter power on every wheel",
                0.25, h.motors.get(OpModeHarness.FRONT_LEFT).power, EPS);
        assertEquals(0.25, h.motors.get(OpModeHarness.BACK_LEFT).power, EPS);
        assertEquals(0.25, h.motors.get(OpModeHarness.FRONT_RIGHT).power, EPS);
        assertEquals(0.25, h.motors.get(OpModeHarness.BACK_RIGHT).power, EPS);
        h.stop();
    }

    @Test
    public void l080_theRightStickTurns() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L080ArcadeOpMode"));
        h.init();
        h.start();
        h.gamepad1.right_stick_x = 0.5f;      // half right
        h.loop();

        assertEquals("turning right drives the left side forward",
                0.25, h.motors.get(OpModeHarness.FRONT_LEFT).power, EPS);
        assertEquals(0.25, h.motors.get(OpModeHarness.BACK_LEFT).power, EPS);
        assertEquals("and the right side back",
                -0.25, h.motors.get(OpModeHarness.FRONT_RIGHT).power, EPS);
        assertEquals(-0.25, h.motors.get(OpModeHarness.BACK_RIGHT).power, EPS);
        h.stop();
    }

    @Test
    public void l080_tooMuchIsScaledNotCut() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L080ArcadeOpMode"));
        h.init();
        h.start();
        h.gamepad1.left_stick_y = -1.0f;      // full forward
        h.gamepad1.right_stick_x = 0.5f;      // half right
        h.loop();

        assertEquals("1.25 asked for, so the biggest is brought down to 1",
                1.0, h.motors.get(OpModeHarness.FRONT_LEFT).power, EPS);
        assertEquals(1.0, h.motors.get(OpModeHarness.BACK_LEFT).power, EPS);
        assertEquals("0.75 asked for, divided by the same 1.25",
                0.6, h.motors.get(OpModeHarness.FRONT_RIGHT).power, EPS);
        assertEquals(0.6, h.motors.get(OpModeHarness.BACK_RIGHT).power, EPS);
        h.stop();
    }

    /** The four wheel powers, front left, front right, back left, back right. */
    private static void assertWheels(String message, OpModeHarness h,
                                     double fl, double fr, double bl, double br) {
        assertEquals(message + ": front left", fl, h.motors.get(OpModeHarness.FRONT_LEFT).power, EPS);
        assertEquals(message + ": front right", fr, h.motors.get(OpModeHarness.FRONT_RIGHT).power, EPS);
        assertEquals(message + ": back left", bl, h.motors.get(OpModeHarness.BACK_LEFT).power, EPS);
        assertEquals(message + ": back right", br, h.motors.get(OpModeHarness.BACK_RIGHT).power, EPS);
    }

    @Test
    public void l090_theLeftStickSlidesLeft() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L090MecanumOpMode"));
        h.init();
        h.start();
        h.gamepad1.left_stick_x = -0.5f;      // half left
        h.loop();

        assertWheels("sliding left runs the front right and back left forward", h,
                -0.25, 0.25, 0.25, -0.25);
        h.stop();
    }

    @Test
    public void l090_forwardAndTurnStillWork() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L090MecanumOpMode"));
        h.init();
        h.start();
        h.gamepad1.left_stick_y = -0.5f;      // half forward
        h.loop();
        assertWheels("forward runs every wheel forward", h, 0.25, 0.25, 0.25, 0.25);

        h.gamepad1.left_stick_y = 0f;
        h.gamepad1.right_stick_x = 0.5f;      // half right
        h.loop();
        assertWheels("turning right runs the left side forward", h, 0.25, -0.25, 0.25, -0.25);
        h.stop();
    }

    @Test
    public void l090_aCornerOfTheStickIsScaled() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L090MecanumOpMode"));
        h.init();
        h.start();
        h.gamepad1.left_stick_y = -1.0f;      // full forward
        h.gamepad1.left_stick_x = -1.0f;      // and full left
        h.loop();

        assertWheels("2 asked for, so every wheel is divided by 2", h, 0, 1, 1, 0);
        h.stop();
    }

    /**
     * L110's mix, driven the way the follower drives it: three numbers in, four
     * powers on the motors. Each assert names the wheel, so a wrong sign says
     * which line it is on.
     */
    @Test
    public void l110_mixGivesEachWheelItsOwnSum() {
        OpModeHarness.FakeMotor fl = new OpModeHarness.FakeMotor();
        OpModeHarness.FakeMotor fr = new OpModeHarness.FakeMotor();
        OpModeHarness.FakeMotor bl = new OpModeHarness.FakeMotor();
        OpModeHarness.FakeMotor br = new OpModeHarness.FakeMotor();
        Drivetrain drivetrain = MyTry.make("L110FollowerDriveTrain", Drivetrain.class, new RobotHardware(
                fl.motor, fr.motor, bl.motor, br.motor,
                new OpModeHarness.FakeImu().imu, OpModeHarness.freshConfig()));

        drivetrain.drive(new DrivePowers(0.5, 0, 0), true);
        assertEquals("forward: front left", 0.5, fl.power, EPS);
        assertEquals("forward: front right", 0.5, fr.power, EPS);
        assertEquals("forward: back left", 0.5, bl.power, EPS);
        assertEquals("forward: back right", 0.5, br.power, EPS);

        drivetrain.drive(new DrivePowers(0, 0.5, 0), true);
        assertEquals("strafe left: front left", -0.5, fl.power, EPS);
        assertEquals("strafe left: front right", 0.5, fr.power, EPS);
        assertEquals("strafe left: back left", 0.5, bl.power, EPS);
        assertEquals("strafe left: back right", -0.5, br.power, EPS);

        drivetrain.drive(new DrivePowers(0, 0, 0.5), true);
        assertEquals("turn left: front left", -0.5, fl.power, EPS);
        assertEquals("turn left: front right", 0.5, fr.power, EPS);
        assertEquals("turn left: back left", -0.5, bl.power, EPS);
        assertEquals("turn left: back right", 0.5, br.power, EPS);
    }

    @Test
    public void l110_theFollowerDrivesTheSticks() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L110FollowerOpMode"));
        h.init();
        h.start();

        h.gamepad1.left_stick_x = -0.5f;      // half left
        h.loop();
        assertWheels("the follower slides it left", h, -0.25, 0.25, 0.25, -0.25);

        h.gamepad1.left_stick_x = 0f;
        h.gamepad1.left_stick_y = -0.5f;      // half forward
        h.loop();
        assertWheels("the follower drives it forward", h, 0.25, 0.25, 0.25, 0.25);

        h.gamepad1.left_stick_y = 0f;
        h.gamepad1.right_stick_x = 0.5f;      // half right
        h.loop();
        assertWheels("the follower turns it right", h, 0.25, -0.25, 0.25, -0.25);
        h.stop();
    }

    @Test
    public void l110_aCornerOfTheStickIsStillScaled() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L110FollowerOpMode"));
        h.init();
        h.start();

        h.gamepad1.left_stick_y = -1f;        // all the way forward
        h.gamepad1.left_stick_x = -1f;        // and all the way left
        h.loop();
        assertWheels("the drivetrain divides by 2 as L090 did", h, 0, 1, 1, 0);
        h.stop();
    }

    // -------------------------------------------------------------- L2a

    @Test
    public void l2a_logsEveryStickAndTheAButton() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L2aSticksOpMode"));
        h.init();
        h.start();
        h.gamepad1.left_stick_y = -1.0f;      // pushed away from the driver
        h.gamepad1.right_stick_x = 0.5f;
        h.gamepad1.a = true;
        h.loop();
        h.gamepad1.a = false;
        h.loop();
        h.stop();

        Map<String, Object> values = Tracker.values();
        assertEquals("forward is positive after negating the stick",
                1.0, number(values, "stick/leftY"), EPS);
        assertEquals(0.5, number(values, "stick/rightX"), EPS);
        assertTrue(values.containsKey("stick/leftX"));
        assertTrue(values.containsKey("stick/rightY"));
        assertTrue("pressing A sends something to Panels: " + values.keySet(),
                values.keySet().stream().anyMatch(k -> k.contains("pressed A")));
        assertEquals("two loops ran, and the lesson counted them",
                2.0, number(values, "lesson/loop_count"), EPS);
        assertTrue("the lesson logged how long it had been running",
                values.containsKey("lesson/seconds_running"));
    }

    @Test
    public void l2a_saysWhenTheButtonIsPressedAndReleased() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L2aSticksOpMode"));
        h.init();
        h.start();

        h.gamepad1.a = true;
        h.loop();
        assertEquals("the loop the button went down on says so",
                "button A pressed", Tracker.values().get("lesson/event"));

        h.loop();
        assertEquals("holding it says nothing new",
                "button A pressed", Tracker.values().get("lesson/event"));

        h.gamepad1.a = false;
        h.loop();
        assertEquals("letting go says so",
                "button A released", Tracker.values().get("lesson/event"));
        h.stop();
    }

    // -------------------------------------------------------------- L2b

    @Test
    public void l2b_theSticksDriveTheWheelsLikeATank() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L2bTankOpMode"));
        h.init();
        h.start();

        h.gamepad1.left_stick_y = -1.0f;      // left stick fully forward
        h.gamepad1.right_stick_y = 0.0f;      // right stick centred
        h.loop();

        assertEquals("the left stick runs the front left wheel",
                1.0, h.motors.get(OpModeHarness.FRONT_LEFT).power, EPS);
        assertEquals("and the back left too",
                1.0, h.motors.get(OpModeHarness.BACK_LEFT).power, EPS);
        assertEquals("the right stick is centred, so the right wheels sit still",
                0.0, h.motors.get(OpModeHarness.FRONT_RIGHT).power, EPS);
        assertEquals(0.0, h.motors.get(OpModeHarness.BACK_RIGHT).power, EPS);

        h.gamepad1.right_stick_y = 1.0f;      // right stick fully back
        h.loop();
        assertEquals("opposed sticks spin the robot",
                -1.0, h.motors.get(OpModeHarness.FRONT_RIGHT).power, EPS);
        assertEquals(1.0, h.motors.get(OpModeHarness.FRONT_LEFT).power, EPS);

        h.stop();
        assertEquals("stopping the OpMode stops the wheels",
                0.0, h.motors.get(OpModeHarness.FRONT_LEFT).power, EPS);
        assertEquals(0.0, h.motors.get(OpModeHarness.FRONT_RIGHT).power, EPS);
    }

    /**
     * What a viewer watching the simulation sees when L2b is driven: the robot
     * goes up the field. The simulator's own tests use a teleop out of
     * {@code base}, so this is the one place a real lesson is asked to move it.
     */
    @Test
    public void l2b_theSticksMoveTheSimulatedRobot() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L2bTankOpMode"));
        h.init();
        h.start();

        h.gamepad1.left_stick_y = -1.0f;      // both sticks fully forward
        h.gamepad1.right_stick_y = -1.0f;
        h.loops(100, 0);                      // 100 x 10 ms of simulated time

        // A teleop starts where the last autonomous left the robot, so these
        // are measured from there and along the way it is facing.
        Pose pose = h.robot.localizer.state().pose();
        assertTrue("drove forward, and got a fair way: " + forwardOf(pose), forwardOf(pose) > 40);
        assertEquals("no sideways drift", 0, lateralOf(pose), EPS);
        h.stop();
    }

    /** How far the robot went along the way it was facing when it started. */
    private static double forwardOf(Pose now) {
        Pose s = OpModeStorage.autonomousEndPose;
        return (now.x() - s.x()) * Math.cos(s.heading())
                + (now.y() - s.y()) * Math.sin(s.heading());
    }

    /** How far it slid across that line, which for a tank drive is nothing. */
    private static double lateralOf(Pose now) {
        Pose s = OpModeStorage.autonomousEndPose;
        return -(now.x() - s.x()) * Math.sin(s.heading())
                + (now.y() - s.y()) * Math.cos(s.heading());
    }

    // -------------------------------------------------------------- L3a

    @Test
    public void l3a_aNearlyCentredStickCountsAsCentred() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L3aDeadbandOpMode"));
        h.init();
        h.start();

        h.gamepad1.left_stick_y = -0.03f;     // a stick that was let go
        h.gamepad1.right_stick_y = 0.04f;
        h.loop();
        assertEquals("inside the deadband, so the robot does not creep",
                0.0, h.motors.get(OpModeHarness.FRONT_LEFT).power, EPS);
        assertEquals(0.0, h.motors.get(OpModeHarness.FRONT_RIGHT).power, EPS);
        assertNotEquals("the raw stick was not 0 though", 0.0,
                number(Tracker.values(), "stick/left_raw"), EPS);

        h.gamepad1.left_stick_y = -0.5f;      // half forward
        h.loop();
        assertEquals("outside the band, the stick is passed through untouched",
                0.5, h.motors.get(OpModeHarness.FRONT_LEFT).power, EPS);

        h.stop();
        assertEquals(0.0, h.motors.get(OpModeHarness.FRONT_LEFT).power, EPS);
    }

    // -------------------------------------------------------------- L3b

    @Test
    public void l3b_halfAStickIsAQuarterOfThePower() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L3bSquaredOpMode"));
        h.init();
        h.start();

        h.gamepad1.left_stick_y = -0.03f;     // the deadband is still there
        h.loop();
        assertEquals(0.0, h.motors.get(OpModeHarness.FRONT_LEFT).power, EPS);

        h.gamepad1.left_stick_y = -0.5f;      // half forward
        h.gamepad1.right_stick_y = 0.5f;      // half back
        h.loop();
        assertEquals("half stick is quarter power",
                0.25, h.motors.get(OpModeHarness.FRONT_LEFT).power, EPS);
        assertEquals("and squaring keeps the sign",
                -0.25, h.motors.get(OpModeHarness.FRONT_RIGHT).power, EPS);

        h.gamepad1.left_stick_y = -1.0f;      // fully forward
        h.loop();
        assertEquals("full stick still reaches full power",
                1.0, h.motors.get(OpModeHarness.FRONT_LEFT).power, EPS);
        assertEquals(1.0, h.motors.get(OpModeHarness.BACK_LEFT).power, EPS);

        h.stop();
        assertEquals(0.0, h.motors.get(OpModeHarness.FRONT_LEFT).power, EPS);
    }

    // -------------------------------------------------------------- L4

    @Test
    public void l4_arcadeUsesOneStickToDriveAndOneToTurn() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L4ArcadeOpMode"));
        h.init();
        h.start();

        h.gamepad1.left_stick_y = -0.5f;      // half forward
        h.gamepad1.right_stick_x = 0.25f;     // and a quarter turn to the right
        h.loop();
        assertEquals("turning right speeds the left wheels up",
                0.75, h.motors.get(OpModeHarness.FRONT_LEFT).power, EPS);
        assertEquals("and slows the right wheels down",
                0.25, h.motors.get(OpModeHarness.FRONT_RIGHT).power, EPS);
        assertEquals("both wheels on a side do the same thing",
                0.75, h.motors.get(OpModeHarness.BACK_LEFT).power, EPS);
        assertEquals(0.25, h.motors.get(OpModeHarness.BACK_RIGHT).power, EPS);

        h.gamepad1.left_stick_y = -1.0f;      // full forward
        h.gamepad1.right_stick_x = -1.0f;     // and a full turn to the left
        h.loop();
        assertEquals("asking for more than a motor can give scales both sides down together",
                0.0, h.motors.get(OpModeHarness.FRONT_LEFT).power, EPS);
        assertEquals(1.0, h.motors.get(OpModeHarness.FRONT_RIGHT).power, EPS);

        h.stop();
        assertEquals(0.0, h.motors.get(OpModeHarness.FRONT_RIGHT).power, EPS);
    }

    // -------------------------------------------------------------- L5

    @Test
    public void l5_holonomicCanStrafe() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L5HolonomicOpMode"));
        h.init();
        h.start();

        h.gamepad1.left_stick_y = -1.0f;      // stick pushed away from the driver
        h.loop();
        assertEquals("driving forward turns every wheel the same way",
                1.0, h.motors.get(OpModeHarness.FRONT_LEFT).power, EPS);
        assertEquals(1.0, h.motors.get(OpModeHarness.FRONT_RIGHT).power, EPS);
        assertEquals(1.0, h.motors.get(OpModeHarness.BACK_LEFT).power, EPS);
        assertEquals(1.0, h.motors.get(OpModeHarness.BACK_RIGHT).power, EPS);

        h.gamepad1.left_stick_y = 0.0f;
        h.gamepad1.left_stick_x = -1.0f;      // stick pushed left
        h.loop();
        assertEquals("sliding left runs one diagonal back and the other forward",
                -1.0, h.motors.get(OpModeHarness.FRONT_LEFT).power, EPS);
        assertEquals(1.0, h.motors.get(OpModeHarness.FRONT_RIGHT).power, EPS);
        assertEquals(1.0, h.motors.get(OpModeHarness.BACK_LEFT).power, EPS);
        assertEquals(-1.0, h.motors.get(OpModeHarness.BACK_RIGHT).power, EPS);

        h.gamepad1.left_stick_x = 0.0f;
        h.gamepad1.right_stick_x = 0.5f;      // turn right, on the spot
        h.loop();
        assertEquals("turning right runs the left side forward",
                0.5, h.motors.get(OpModeHarness.FRONT_LEFT).power, EPS);
        assertEquals("and the right side back",
                -0.5, h.motors.get(OpModeHarness.FRONT_RIGHT).power, EPS);

        h.stop();
        assertEquals(0.0, h.motors.get(OpModeHarness.FRONT_LEFT).power, EPS);
    }

    // -------------------------------------------------------------- L6

    /**
     * L6 no longer asks the follower to drive, so there is no {@code h.wheelsForward()}
     * to read: it commands its drivetrain's four wheels itself. The harness hands
     * every OpMode its own follower and ignores the drivetrain passed to
     * {@code initAfter}, so what L6 does to the wheels is tested in
     * {@link L6FollowerDriveTrainTest#theSticksCommandTheWheelsSoTheDriverStillWins}.
     * What is left to check here is the OpMode's own job: three sticks read, and
     * each one negated the right way.
     */
    @Test
    public void l6_theLoopReadsThreeSticksAndNegatesEachOne() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L6WheelsFollowerOpMode"));
        h.init();
        h.start();

        h.gamepad1.left_stick_y = -1.0f;      // away from the driver
        h.gamepad1.left_stick_x = -0.5f;      // and towards its left
        h.gamepad1.right_stick_x = 0.25f;     // turning to the right
        h.loop();

        assertEquals("away from the driver is forward",
                1.0, (Double) Tracker.values().get("command/forward"), EPS);
        assertEquals("and the stick pushed left asks to slide left",
                0.5, (Double) Tracker.values().get("command/left"), EPS);
        assertEquals("and turning right is clockwise, which is a negative turn",
                -0.25, (Double) Tracker.values().get("command/turn_ccw"), EPS);
        h.stop();
    }

    /**
     * Pushing the right stick right swings the nose right, which is clockwise,
     * which is a negative turn. Every lesson that hands the stick to a chassis
     * mapping had it the other way round until 2026-09-26, and only L4, L6,
     * L13 and L15 had a test that noticed.
     */
    @Test
    public void everyLessonTurnsClockwiseWhenTheRightStickGoesRight() {
        List<String> wrongWay = new ArrayList<>();
        for (OpMode lesson : new OpMode[]{MyTry.opMode("L8CompareLocalizersOpMode"),
                MyTry.opMode("L11FieldRelativeOpMode"),
                MyTry.opMode("L12RobotRelativeButtonOpMode"),
                MyTry.opMode("L14DriveToPoseOpMode")}) {
            OpModeHarness h = new OpModeHarness(lesson);
            h.init();
            h.start();
            h.gamepad1.right_stick_x = 1.0f;
            h.loop();
            double turn = h.wheelsTurn();
            if (Math.abs(turn - -1.0) > 1e-6) {
                wrongWay.add(lesson.getClass().getSimpleName() + " turned " + turn);
            }
            h.stop();
        }
        // Every lesson is named, not just the first, so one run says how many.
        assertEquals("", String.join("; ", wrongWay));
    }

    // -------------------------------------------------------------- L8

    @Test
    public void l8_theEncoderLocalizerRunsAlongsideAndIsLogged() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L8CompareLocalizersOpMode"));
        Follower follower = h.robot.follower;
        h.init();
        h.start();
        h.loop();
        // 45 inches of wheel travel on every wheel = 45 inches forward
        h.setWheelTicks(2025, 2025, 2025, 2025);
        h.loop();
        h.stop();

        Map<String, Object> values = Tracker.values();
        assertTrue("the shadow localizer is logged: " + values.keySet(),
                values.containsKey("Localizer/driveWheelEncoders/x_in"));
        assertEquals("the shadow must not steer the robot", 0.0, h.wheelsForward(), EPS);
        assertNotEquals("it moved in its own estimate",
                0.0, number(values, "Localizer/driveWheelEncoders/x_in"), 0.1);

        // The lesson is a comparison, so the robot's own answer is published
        // under a name too, and both series sit under one prefix.
        assertEquals("the robot's own localizer, to compare against",
                follower.pose().x(), number(values, "Localizer/pinPoint/x_in"), EPS);
        assertEquals(follower.pose().y(), number(values, "Localizer/pinPoint/y_in"), EPS);
    }

    @Test
    public void l120_theEncoderLocalizerRunsAlongsideAndIsLogged() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L120CompareLocalizersOpMode"));
        Follower follower = h.robot.follower;
        h.init();
        h.start();
        h.loop();
        // 45 inches of wheel travel on every wheel = 45 inches forward
        h.setWheelTicks(2025, 2025, 2025, 2025);
        h.loop();
        h.stop();

        Map<String, Object> values = Tracker.values();
        assertTrue("the shadow localizer is logged: " + values.keySet(),
                values.containsKey("Localizer/driveWheelEncoders/x_in"));
        assertEquals("the shadow must not steer the robot", 0.0, h.wheelsForward(), EPS);
        assertNotEquals("it moved in its own estimate",
                0.0, number(values, "Localizer/driveWheelEncoders/x_in"), 0.1);
        assertEquals("the robot's own localizer, to compare against",
                follower.pose().x(), number(values, "Localizer/pinPoint/x_in"), EPS);
        assertEquals(follower.pose().y(), number(values, "Localizer/pinPoint/y_in"), EPS);
    }

    @Test
    public void l125_autoDrives24InchesForwardAndStops() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L125Drive24OpMode"));
        Follower follower = h.robot.follower;
        h.init();
        assertEquals("placed at the start pose", 72.0, follower.pose().x(), EPS);
        assertEquals("against the wall, half a robot out", 10.5, follower.pose().y(), EPS);
        assertEquals("facing +y", 90.0, Math.toDegrees(follower.pose().heading()), EPS);
        h.start();
        runUntilDone(h, follower, 3.0);
        h.stop();

        // Forward is +y at heading 90, so the 24 inches are in y and x holds.
        assertEquals("ends 24 inches further along y", 34.5, follower.pose().y(), 1.0);
        assertEquals("and does not wander in x", 72.0, follower.pose().x(), 1.0);
        assertEquals("and still faces +y", 90.0, Math.toDegrees(follower.pose().heading()), 15.0);
    }

    @Test
    public void l130_autoDrivesForwardThenStrafesSideways() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L130ForwardThenStrafeOpMode"));
        Follower follower = h.robot.follower;
        h.init();
        h.start();
        // The first leg runs straight up x = 72, so x holds until near the corner.
        double drift = 0;
        long deadline = System.nanoTime() + 3_000_000_000L;
        while (follower.pose().y() < 66 && System.nanoTime() < deadline) {
            h.loop();
            drift = Math.max(drift, Math.abs(follower.pose().x() - 72));
            OpModeHarness.sleep(5);
        }
        runUntilDone(h, follower, 6.0);
        h.stop();
        assertEquals("drove straight up to the corner first", 0.0, drift, 2.0);

        // Forward to (72, 72), then 24 inches of strafe to the robot's right.
        Pose end = follower.pose();
        assertEquals("strafed 24 inches in x", 96.0, end.x(), 2.0);
        assertEquals("and stayed on the line it drove up", 72.0, end.y(), 2.0);
        assertEquals("never turned, so it still faces +y",
                90.0, Math.toDegrees(end.heading()), 15.0);
    }

    @Test
    public void l140_fieldRelativeIgnoresWhichWayTheRobotFaces() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L140FieldRelativeOpMode"));
        Follower follower = h.robot.follower;
        h.init();
        h.start();

        follower.setPose(POSES.of(0, 0, 0));
        h.gamepad1.left_stick_y = -1.0f;          // away from the driver
        h.loop();
        assertEquals(1.0, h.wheelsForward(), 1e-3);

        follower.setPose(POSES.of(0, 0, 90));     // robot now faces +y
        h.loop();
        assertEquals("same stick, still moves away from the driver",
                0.0, h.wheelsForward(), 1e-3);
        assertEquals(-1.0, h.wheelsStrafe(), 1e-3);
        h.stop();
    }

    @Test
    public void l150_theBumperSwitchesToRobotRelative() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L150RobotRelativeButtonOpMode"));
        Follower follower = h.robot.follower;
        h.init();
        h.start();
        follower.setPose(POSES.of(0, 0, 90));
        h.gamepad1.left_stick_y = -1.0f;

        h.loop();
        assertEquals("field relative by default", 0.0, h.wheelsForward(), 1e-3);

        h.gamepad1.right_bumper = true;
        h.loop();
        assertEquals("robot relative while held", 1.0, h.wheelsForward(), 1e-3);

        h.gamepad1.right_bumper = false;
        h.loop();
        assertEquals("and back again on release", 0.0, h.wheelsForward(), 1e-3);
        h.stop();
    }

    @Test
    public void l160_theRobotHoldsItsHeadingWhenTheStickIsReleased() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L160HeadingHoldOpMode"));
        Follower follower = h.robot.follower;
        h.init();
        h.start();

        follower.setPose(POSES.of(0, 0, 0));
        h.gamepad1.right_stick_x = 1.0f;           // pushed right: clockwise
        h.loop();
        assertEquals("while steering, the stick wins", -1.0, h.wheelsTurn(), 1e-3);

        h.gamepad1.right_stick_x = 0.0f;
        h.loop();                                  // releases: captures heading 0
        assertEquals("on target, no correction", 0.0, h.wheelsTurn(), 1e-3);

        follower.setPose(POSES.of(0, 0, -10));     // the robot drifts
        h.loop();
        assertTrue("it steers back", h.wheelsTurn() > 0.01);
        h.stop();
    }

    @Test
    public void l170_pressingYDrivesToAPoseAndTheDriverCanTakeOver() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L170DriveToPoseOpMode"));
        Follower follower = h.robot.follower;
        h.init();
        h.start();
        follower.setPose(POSES.of(72, 72, 0));

        h.gamepad1.y = true;
        h.loop();
        h.gamepad1.y = false;
        h.loop();
        assertEquals("the command is driving, not the sticks",
                Follower.Mode.HOLD, follower.mode());

        h.gamepad1.left_stick_y = -1.0f;           // the driver grabs the stick
        h.loop();
        assertEquals("manual control returns", Follower.Mode.MANUAL, follower.mode());
        assertEquals(1.0, h.wheelsForward(), 1e-3);
        h.stop();
    }

    @Test
    public void l180_everythingTogether() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L180CombinedOpMode"));
        Follower follower = h.robot.follower;
        h.init();
        h.start();
        follower.setPose(POSES.of(72, 72, 90));

        h.gamepad1.left_stick_y = -1.0f;
        h.loop();
        assertEquals("field relative with nothing held", 0.0, h.wheelsForward(), 1e-3);

        h.gamepad1.right_bumper = true;
        h.loop();
        assertEquals("robot relative on the bumper", 1.0, h.wheelsForward(), 1e-3);
        h.gamepad1.right_bumper = false;

        h.gamepad1.left_stick_y = 0.0f;
        h.gamepad1.y = true;
        h.loop();
        h.gamepad1.y = false;
        h.loop();
        assertEquals("Y hands the robot to the follower", Follower.Mode.HOLD, follower.mode());

        h.gamepad1.left_stick_y = -1.0f;
        h.loop();
        assertEquals("a stick takes it back", "DRIVER",
                Tracker.values().get("drive/mode"));
        h.stop();

        assertTrue("the shadow localizer is still running",
                Tracker.values().containsKey("Localizer/driveWheelEncoders/x_in"));
    }

    @Test
    public void l180_aPointsAt45DegreesWhileTheDriverKeepsDriving() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L180CombinedOpMode"));
        Follower follower = h.robot.follower;
        h.init();
        h.start();
        follower.setPose(POSES.of(72, 72, 0));       // facing 0, wants 45

        h.gamepad1.a = true;
        h.loop();
        h.gamepad1.a = false;
        h.gamepad1.left_stick_y = -1.0f;             // still translating
        h.loop();

        assertEquals("aiming", true, Tracker.values().get("heading/holding"));
        assertEquals("at 45 degrees", 45.0,
                (Double) Tracker.values().get("heading/target_deg"), 1e-6);
        assertTrue("turning toward it", h.wheelsTurn() > 0);
        assertTrue("and still driving", Math.abs(h.wheelsForward()) + Math.abs(h.wheelsStrafe()) > 0);
    }

    @Test
    public void l180_theTurnStickTakesAimingBack() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L180CombinedOpMode"));
        h.robot.follower.setPose(POSES.of(72, 72, 0));
        h.init();
        h.start();

        h.gamepad1.a = true;
        h.loop();
        h.gamepad1.a = false;
        h.gamepad1.right_stick_x = 1.0f;             // the driver steers, clockwise
        h.loop();

        assertEquals("the stick wins", -1.0, h.wheelsTurn(), 1e-3);
        assertEquals("not aiming any more", false,
                Tracker.values().get("heading/holding"));
    }

    // -------------------------------------------------------------- L9

    @Test
    public void l9_autoDrives24InchesForwardAndStops() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L9Drive24OpMode"));
        Follower follower = h.robot.follower;
        h.init();
        assertEquals("placed at the start pose", 72.0, follower.pose().x(), EPS);
        assertEquals("against the wall, half a robot out", 10.5, follower.pose().y(), EPS);
        assertEquals("facing +y", 90.0, Math.toDegrees(follower.pose().heading()), EPS);
        h.start();
        runUntilDone(h, follower, 3.0);
        h.stop();

        // Forward is +y at heading 90, so the 24 inches are in y and x holds.
        assertEquals("ends 24 inches further along y", 34.5, follower.pose().y(), 1.0);
        assertEquals("and does not wander in x", 72.0, follower.pose().x(), 1.0);
        assertEquals("and still faces +y", 90.0, Math.toDegrees(follower.pose().heading()), 15.0);
    }

    // -------------------------------------------------------------- L10

    @Test
    public void l10_autoDrivesForwardThenStrafesSideways() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L10ForwardThenStrafeOpMode"));
        Follower follower = h.robot.follower;
        h.init();
        h.start();
        runUntilDone(h, follower, 6.0);
        h.stop();

        // Forward to (72, 72), then 24 inches of strafe to the robot's right.
        Pose end = follower.pose();
        assertEquals("strafed 24 inches in x", 96.0, end.x(), 2.0);
        assertEquals("and stayed on the line it drove up", 72.0, end.y(), 2.0);
        assertEquals("never turned, so it still faces +y",
                90.0, Math.toDegrees(end.heading()), 15.0);
    }

    // -------------------------------------------------------------- L11

    @Test
    public void l11_fieldRelativeIgnoresWhichWayTheRobotFaces() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L11FieldRelativeOpMode"));
        Follower follower = h.robot.follower;
        h.init();
        h.start();

        follower.setPose(POSES.of(0, 0, 0));
        h.gamepad1.left_stick_y = -1.0f;          // away from the driver
        h.loop();
        assertEquals(1.0, h.wheelsForward(), 1e-3);

        follower.setPose(POSES.of(0, 0, 90));     // robot now faces +y
        h.loop();
        assertEquals("same stick, still moves away from the driver",
                0.0, h.wheelsForward(), 1e-3);
        assertEquals(-1.0, h.wheelsStrafe(), 1e-3);
        h.stop();
    }

    // -------------------------------------------------------------- L12

    @Test
    public void l12_theBumperSwitchesToRobotRelative() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L12RobotRelativeButtonOpMode"));
        Follower follower = h.robot.follower;
        h.init();
        h.start();
        follower.setPose(POSES.of(0, 0, 90));
        h.gamepad1.left_stick_y = -1.0f;

        h.loop();
        assertEquals("field relative by default", 0.0, h.wheelsForward(), 1e-3);

        h.gamepad1.right_bumper = true;
        h.loop();
        assertEquals("robot relative while held", 1.0, h.wheelsForward(), 1e-3);

        h.gamepad1.right_bumper = false;
        h.loop();
        assertEquals("and back again on release", 0.0, h.wheelsForward(), 1e-3);
        h.stop();
    }

    // -------------------------------------------------------------- L13

    @Test
    public void l13_theRobotHoldsItsHeadingWhenTheStickIsReleased() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L13HeadingHoldOpMode"));
        Follower follower = h.robot.follower;
        h.init();
        h.start();

        follower.setPose(POSES.of(0, 0, 0));
        h.gamepad1.right_stick_x = 0.5f;           // pushed right: clockwise
        h.loop();
        assertEquals("while steering, the stick wins", -0.5, h.wheelsTurn(), 1e-3);

        h.gamepad1.right_stick_x = 0.0f;
        h.loop();                                  // releases: captures heading 0
        assertEquals("on target, no correction", 0.0, h.wheelsTurn(), 1e-3);

        follower.setPose(POSES.of(0, 0, -10));     // the robot drifts
        h.loop();
        assertTrue("it steers back", h.wheelsTurn() > 0.01);
        h.stop();
    }

    // -------------------------------------------------------------- L14

    @Test
    public void l14_pressingYDrivesToAPoseAndTheDriverCanTakeOver() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L14DriveToPoseOpMode"));
        Follower follower = h.robot.follower;
        h.init();
        h.start();
        follower.setPose(POSES.of(72, 72, 0));

        h.gamepad1.y = true;
        h.loop();
        h.gamepad1.y = false;
        h.loop();
        assertEquals("the command is driving, not the sticks",
                Follower.Mode.HOLD, follower.mode());

        h.gamepad1.left_stick_y = -1.0f;           // the driver grabs the stick
        h.loop();
        assertEquals("manual control returns", Follower.Mode.MANUAL, follower.mode());
        assertEquals(1.0, h.wheelsForward(), 1e-3);
        h.stop();
    }

    // -------------------------------------------------------------- L15

    @Test
    public void l15_everythingTogether() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L15CombinedOpMode"));
        Follower follower = h.robot.follower;
        h.init();
        h.start();
        follower.setPose(POSES.of(72, 72, 90));

        h.gamepad1.left_stick_y = -1.0f;
        h.loop();
        assertEquals("field relative with nothing held", 0.0, h.wheelsForward(), 1e-3);

        h.gamepad1.right_bumper = true;
        h.loop();
        assertEquals("robot relative on the bumper", 1.0, h.wheelsForward(), 1e-3);
        h.gamepad1.right_bumper = false;

        h.gamepad1.left_stick_y = 0.0f;
        h.gamepad1.y = true;
        h.loop();
        h.gamepad1.y = false;
        h.loop();
        assertEquals("Y hands the robot to the follower", Follower.Mode.HOLD, follower.mode());
        h.stop();

        assertTrue("the shadow localizer is still running",
                Tracker.values().containsKey("Localizer/driveWheelEncoders/x_in"));
    }

    @Test
    public void l15_aPointsAt45DegreesWhileTheDriverKeepsDriving() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L15CombinedOpMode"));
        Follower follower = h.robot.follower;
        h.init();
        h.start();
        follower.setPose(POSES.of(72, 72, 0));       // facing 0, wants 45

        h.gamepad1.a = true;
        h.loop();
        h.gamepad1.a = false;
        h.gamepad1.left_stick_y = -1.0f;             // still translating
        h.loop();

        assertEquals("aiming", true, Tracker.values().get("drive/aiming"));
        assertEquals("at 45 degrees", 45.0, (Double) Tracker.values().get("drive/target_deg"), 1e-6);
        assertTrue("turning toward it", h.wheelsTurn() > 0);
        assertTrue("and still driving", Math.abs(h.wheelsForward()) + Math.abs(h.wheelsStrafe()) > 0);
    }

    @Test
    public void l15_theTurnStickTakesAimingBack() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L15CombinedOpMode"));
        h.robot.follower.setPose(POSES.of(72, 72, 0));
        h.init();
        h.start();

        h.gamepad1.a = true;
        h.loop();
        h.gamepad1.a = false;
        h.gamepad1.right_stick_x = 0.8f;             // the driver steers, clockwise
        h.loop();

        assertEquals("the stick wins", -0.8, h.wheelsTurn(), 1e-3);
        assertEquals("not aiming any more", false,
                Tracker.values().get("drive/aiming"));
    }

    @Test
    public void l15_yDrivesToTheStatedPoseAndAStickTakesItBack() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L15CombinedOpMode"));
        Follower follower = h.robot.follower;
        h.init();
        h.start();
        follower.setPose(POSES.of(72, 72, 90));

        h.gamepad1.y = true;
        h.loop();
        h.gamepad1.y = false;
        h.loop();
        assertEquals(Follower.Mode.HOLD, follower.mode());
        assertEquals("AUTO", Tracker.values().get("drive/mode"));
        // Pedro normalises headings to [0, 360), so -45 comes back as 315.
        assertEquals("the pose it was told to go to", 315.0,
                (Double) Tracker.values().get("drive/target_deg"), 1e-6);

        h.gamepad1.left_stick_y = -1.0f;
        h.loop();
        assertEquals("a stick takes it back", "FIELD",
                Tracker.values().get("drive/mode"));
    }

    @Test
    public void l16_theSticksCommandASpeedAndTheWheelsAreCorrectedTowardsIt() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L16VelocityDriveOpMode"));
        h.init();
        h.start();

        h.gamepad1.left_stick_y = -1.0f;            // full forward
        h.loop();

        assertEquals("full stick asks for a speed, in inches per second",
                40.0, (Double) Tracker.values().get("command/forward_ips"), 1e-6);
        assertEquals("and every wheel must travel at it",
                40.0, (Double) Tracker.values().get("wheel/frontLeft/target_ips"), 1e-6);
        assertEquals("the wheels are not moving yet, so the error is the whole target",
                40.0, (Double) Tracker.values().get("wheel/frontLeft/error_ips"), 1e-6);

        // the measured feedforward for 40 in/s, plus the feedback on a 40 in/s error
        assertEquals(Constants.powerPerInchPerSecond * 40 + 0.008 * 40,
                (Double) Tracker.values().get("wheel/frontLeft/power"), 1e-6);
    }

    @Test
    public void l16_whenTheWheelsAreUpToSpeedOnlyTheFeedforwardRemains() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L16VelocityDriveOpMode"));
        h.init();
        h.start();
        // 40 in/s at the measured ticks per inch
        int ticks = (int) Math.round(40 * Constants.ticksPerInch);
        h.velocities(ticks, ticks, ticks, ticks);

        h.gamepad1.left_stick_y = -1.0f;
        h.loop();

        assertEquals("measured speed matches the command", 40.0,
                (Double) Tracker.values().get("wheel/frontLeft/actual_ips"), 1e-6);
        assertEquals("so no correction is needed", 0.0,
                (Double) Tracker.values().get("wheel/frontLeft/error_ips"), 1e-6);
        assertEquals("and the power is the feedforward alone",
                Constants.powerPerInchPerSecond * 40,
                (Double) Tracker.values().get("wheel/frontLeft/power"), 1e-6);
    }

    @Test
    public void l16_turningAskesEachSideForOppositeSpeeds() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L16VelocityDriveOpMode"));
        h.init();
        h.start();
        h.gamepad1.right_stick_x = -1.0f;           // full counter-clockwise
        h.loop();

        double left = (Double) Tracker.values().get("wheel/frontLeft/target_ips");
        double right = (Double) Tracker.values().get("wheel/frontRight/target_ips");
        assertEquals("opposite", -left, right, 1e-6);
        assertTrue("turning counter-clockwise drives the left side backwards", left < 0);
    }

    // -------------------------------------------------------------- L17

    /**
     * Both halves of L17 are measurements: the student pushes or spins the robot
     * by hand, so the wheels have to roll all the way through, and brake again
     * afterwards.
     *
     * <p>Two bugs this catches, both of which the lessons had until the
     * drivetrain owned the override. The coast used to last one follower update,
     * because it was restored in afterLoop(), which runs every loop. And a run
     * that ended without completing a loop -- STOP pressed early, or an
     * exception in the loop body -- never restored it at all, so the next OpMode
     * coasted when it should have braked.
     */
    @Test
    public void l17_theWheelsRollThroughoutAMeasurementAndBrakeAgainAfterwards() {
        for (OpMode lesson : new OpMode[]{MyTry.opMode("L17aMeasureTicksPerInchOpMode"),
                MyTry.opMode("L17bMeasureTurnRadiusOpMode")}) {
            String name = lesson.getClass().getSimpleName();
            OpModeHarness h = new OpModeHarness(lesson);
            h.init();
            Drivetrain drivetrain = ((CorbelsOpMode) h.opMode()).drivetrain();
            assertTrue(name + " brakes before the run", braking(drivetrain));

            h.start();
            assertFalse(name + " coasts once it starts", braking(drivetrain));
            h.loops(5, 0);
            assertFalse(name + " still coasts five loops in", braking(drivetrain));

            h.stop();
            assertTrue(name + " brakes again afterwards", braking(drivetrain));
            OpModeHarness.restoreFactories();
        }
    }

    @Test
    public void l17_stoppingBeforeASingleLoopStillPutsBrakingBack() {
        OpModeHarness h = new OpModeHarness(MyTry.opMode("L17aMeasureTicksPerInchOpMode"));
        h.init();
        h.start();
        h.stop();                                  // STOP pressed straight away
        assertTrue("braking is back although no loop ran",
                braking(((CorbelsOpMode) h.opMode()).drivetrain()));
    }

    // ---------------------------------------------------------- helpers

    /** Whether the lesson's drivetrain brakes now, asked of it by name. */
    private static boolean braking(Drivetrain drivetrain) {
        return (Boolean) MyTry.call(drivetrain, "getEffectiveBrakeMode");
    }

    /** Runs loops until the follower stops following, or the time runs out. */
    private static void runUntilDone(OpModeHarness h, Follower follower, double seconds) {
        long deadline = System.nanoTime() + (long) (seconds * 1e9);
        boolean followed = false;
        while (System.nanoTime() < deadline) {
            h.loop();
            if (follower.mode() == Follower.Mode.FOLLOW) followed = true;
            if (followed && follower.mode() != Follower.Mode.FOLLOW && follower.currentPath() == null) {
                h.loop();
                break;
            }
            OpModeHarness.sleep(5);
        }
        assertTrue("the robot never started following a path", followed);
    }
}
