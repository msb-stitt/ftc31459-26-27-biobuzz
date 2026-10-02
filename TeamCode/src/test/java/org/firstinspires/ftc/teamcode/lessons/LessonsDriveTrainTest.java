package org.firstinspires.ftc.teamcode.lessons;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.drivetrain.Drivetrain;

import org.firstinspires.ftc.teamcode.base.OpModeHarness;
import org.firstinspires.ftc.teamcode.base.RobotHardware;
import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.junit.Before;
import org.junit.Test;

/**
 * One test per part the lessons add to {@link LessonsDriveTrain}: the immediate
 * write L2 fills in, the scaling L4 fills in, and the wheel sharing L6 fills in.
 *
 * <p>The two drivetrains are the student's own, loaded from {@code mytry} by
 * name. {@code L2TankDriveTrain} is L2 through L5: it drives its own wheels and
 * has no {@code mix}, so the follower cannot drive it. {@code
 * L6FollowerDriveTrain} is L6 onwards.
 */
public class LessonsDriveTrainTest {

    private static final double EPS = 1e-9;

    private OpModeHarness.FakeMotor frontLeft;
    private OpModeHarness.FakeMotor frontRight;
    private OpModeHarness.FakeMotor backLeft;
    private OpModeHarness.FakeMotor backRight;
    private RobotHardware hardware;

    /** L2 through L5's drivetrain: it drives its own wheels and has no mix. */
    private Drivetrain sides() {
        return MyTry.make("L2TankDriveTrain", Drivetrain.class, hardware);
    }

    /** L6 onwards' drivetrain, which the follower can drive. */
    private Drivetrain mecanum() {
        return MyTry.make("L6FollowerDriveTrain", Drivetrain.class, hardware);
    }

    /** Whether the drivetrain brakes now, asked of it by name. */
    private static boolean braking(Drivetrain drivetrain) {
        return (Boolean) call(drivetrain, "getEffectiveBrakeMode");
    }

    /** A lesson's own method, which {@code Drivetrain} does not have, called by name. */
    private static Object call(Drivetrain drivetrain, String method, Object... args) {
        return MyTry.call(drivetrain, method, args);
    }

    @Before
    public void setUp() {
        frontLeft = new OpModeHarness.FakeMotor();
        frontRight = new OpModeHarness.FakeMotor();
        backLeft = new OpModeHarness.FakeMotor();
        backRight = new OpModeHarness.FakeMotor();
        hardware = new RobotHardware(frontLeft.motor, frontRight.motor,
                backLeft.motor, backRight.motor, new OpModeHarness.FakeImu().imu,
                OpModeHarness.freshConfig());
    }

    private double[] motorPowers() {
        return new double[]{frontLeft.power, frontRight.power, backLeft.power, backRight.power};
    }

    // ------------------------------------------------------------ L2's part

    @Test
    public void drivingTheWheelsNowReachesAllFourMotorsInOrder() {
        call(sides(), "driveWheelsNow", 0.1, 0.2, 0.3, 0.4);
        assertArrayEquals("front left, front right, back left, back right",
                new double[]{0.1, 0.2, 0.3, 0.4}, motorPowers(), EPS);
    }

    @Test
    public void tankSticksRunBothWheelsOnEachSide() {
        call(sides(), "sticks", 1, -1);
        assertArrayEquals(new double[]{1, -1, 1, -1}, motorPowers(), EPS);
    }

    // ------------------------------------------------------------ L4's part

    @Test
    public void askingForMoreThanFullPowerScalesEveryWheelDownTogether() {
        call(sides(), "driveWheelsNow", 2, 1, 0, -1);
        assertArrayEquals("everything divided by 2, so the ratios survive",
                new double[]{1, 0.5, 0, -0.5}, motorPowers(), EPS);
    }

    @Test
    public void powersInsideFullPowerAreLeftAlone() {
        call(sides(), "driveWheelsNow", 0.5, 0.25, 0, -0.75);
        assertArrayEquals(new double[]{0.5, 0.25, 0, -0.75}, motorPowers(), EPS);
    }

    // ------------------------------------------------------------ L6's parts

    @Test
    public void theFollowerDrivesADrivetrainThatHasAMix() {
        mecanum().drive(new DrivePowers(1, 0, 0), true);
        assertArrayEquals("forward runs all four the same way",
                new double[]{1, 1, 1, 1}, motorPowers(), EPS);
    }

    @Test
    public void commandedWheelsBeatWhatTheFollowerWorkedOut() {
        Drivetrain drivetrain = mecanum();
        call(drivetrain, "setCommandedWheels", 0.1, 0.2, 0.3, 0.4);
        assertTrue((Boolean) call(drivetrain, "commandedWheelsAreSet"));

        drivetrain.drive(new DrivePowers(1, 0, 0), true);
        assertArrayEquals("the lesson wins while the wheels are commanded",
                new double[]{0.1, 0.2, 0.3, 0.4}, motorPowers(), EPS);
    }

    @Test
    public void releasingTheWheelsHandsThemBackToTheFollower() {
        Drivetrain drivetrain = mecanum();
        call(drivetrain, "setCommandedWheels", 0.1, 0.2, 0.3, 0.4);
        call(drivetrain, "releaseCommandedWheels");
        assertFalse((Boolean) call(drivetrain, "commandedWheelsAreSet"));

        drivetrain.drive(new DrivePowers(1, 0, 0), true);
        assertArrayEquals(new double[]{1, 1, 1, 1}, motorPowers(), EPS);
    }

    @Test
    public void stoppingReleasesTheWheelsAndZeroesThem() {
        Drivetrain drivetrain = mecanum();
        call(drivetrain, "setCommandedWheels", 1, 1, 1, 1);
        drivetrain.stop();
        assertFalse((Boolean) call(drivetrain, "commandedWheelsAreSet"));
        assertArrayEquals(new double[]{0, 0, 0, 0}, motorPowers(), EPS);
    }

    @Test
    public void theFollowerScalesAPathTheWayPedroExpects() {
        Drivetrain drivetrain = mecanum();
        assertEquals(1.0, drivetrain.maxScaling(DrivePowers.zero(), new DrivePowers(1, 0, 0)), EPS);
        assertEquals(0.0, drivetrain.maxScaling(new DrivePowers(1, 0, 0), new DrivePowers(1, 0, 0)), EPS);
        assertEquals(0.5, drivetrain.maxScaling(new DrivePowers(0.5, 0, 0), new DrivePowers(1, 0, 0)), EPS);
    }

    // ------------------------------------------------------------ the two traps

    @Test
    public void aDrivetrainWithNoMixSaysSoRatherThanSittingStill() {
        try {
            sides().drive(new DrivePowers(1, 0, 0), true);
            fail("a drivetrain with no mix() must refuse the follower, not do nothing");
        } catch (UnsupportedOperationException expected) {
            assertTrue("the message names the class: " + expected.getMessage(),
                    expected.getMessage().contains("L2TankDriveTrain"));
        }
    }

    // ------------------------------------------------------------ brake mode

    @Test
    public void theEffectiveBrakeModeIsWhatTheConfigSaysUntilCharacterizationOverridesIt() {
        Drivetrain drivetrain = mecanum();
        assertTrue("the config asks for braking", braking(drivetrain));
        assertFalse((Boolean) call(drivetrain, "isCoastForCharacterization"));

        call(drivetrain, "forceCoastForCharacterization");
        assertTrue((Boolean) call(drivetrain, "isCoastForCharacterization"));
        assertFalse("a measurement needs the wheels to roll", braking(drivetrain));

        call(drivetrain, "allowConfiguredBrakeMode");
        assertFalse((Boolean) call(drivetrain, "isCoastForCharacterization"));
        assertTrue("back to whatever the config says", braking(drivetrain));
    }

    @Test
    public void coastingForCharacterizationSurvivesAFollowerUpdate() {
        // The bug this replaces: L17 wrote the flag and restored it in
        // afterLoop(), which runs every loop, so the coast lasted one update.
        Drivetrain drivetrain = mecanum();
        call(drivetrain, "forceCoastForCharacterization");
        drivetrain.drive(new DrivePowers(0, 0, 0), true);
        drivetrain.drive(new DrivePowers(0, 0, 0), true);
        assertFalse("still coasting on the second update", braking(drivetrain));
    }

    @Test
    public void aConfigThatAsksForCoastingGetsItWithoutAnyOverride() {
        hardware.mecanumConfig.manualBrakeMode.set(false);
        Drivetrain drivetrain = mecanum();
        assertFalse("nothing was overridden, and it still coasts",
                braking(drivetrain));
        assertFalse((Boolean) call(drivetrain, "isCoastForCharacterization"));
    }

    // ------------------------------------------------------------ L11's part

    @Test
    public void fieldRelativeDrivingTurnsTheDriversViewIntoTheRobots() {
        Drivetrain drivetrain = mecanum();

        // Facing along the field's x axis, the two views agree.
        call(drivetrain, "fieldRelative", 0, 1, 0, 0);
        drivetrain.drive(DrivePowers.zero(), true);
        assertArrayEquals("straight down the field is straight ahead",
                new double[]{1, 1, 1, 1}, motorPowers(), EPS);

        // Turned a quarter turn to the left, going down the field is strafing
        // to the robot's right, which runs one diagonal pair each way.
        call(drivetrain, "fieldRelative", Math.PI / 2, 1, 0, 0);
        drivetrain.drive(DrivePowers.zero(), true);
        assertArrayEquals("the same journey, sideways to the robot",
                new double[]{1, -1, -1, 1}, motorPowers(), EPS);
    }

    @Test
    public void turningTheDriversViewNeverChangesHowFastTheRobotGoes() {
        // A small stick, so no wheel asks for more than full power and nothing
        // is scaled: then the four powers can be read back as a speed. A fresh
        // drivetrain each time, because these powers are small enough for the
        // write cache to swallow a step between two headings.
        for (int deg = 0; deg < 360; deg += 30) {
            Drivetrain drivetrain = mecanum();
            call(drivetrain, "fieldRelative", Math.toRadians(deg), 0.06, -0.08, 0);
            drivetrain.drive(DrivePowers.zero(), true);
            double[] w = motorPowers();
            double forward = (w[0] + w[1] + w[2] + w[3]) / 4;
            double strafeLeft = (-w[0] + w[1] + w[2] - w[3]) / 4;
            assertEquals("the same speed whichever way the robot faces at " + deg + " deg",
                    0.1, Math.hypot(forward, strafeLeft), 1e-9);
        }
    }

    // ------------------------------------------------------- L3a's and L3b's parts

    @Test
    public void theDeadbandIgnoresAStickThatIsNearlyCentred() {
        Drivetrain drivetrain = sides();
        assertEquals("inside the band",
                0.0, (Double) call(drivetrain, "deadband", 0.04, 0.05), EPS);
        assertEquals("outside it, the stick itself",
                0.5, (Double) call(drivetrain, "deadband", 0.5, 0.05), EPS);
    }

    @Test
    public void squaringTheStickKeepsItsSign() {
        Drivetrain drivetrain = sides();
        assertEquals(0.25, (Double) call(drivetrain, "squared", 0.5), EPS);
        assertEquals("keeps its sign", -0.25, (Double) call(drivetrain, "squared", -0.5), EPS);
    }

    // ------------------------------------------------------------ L16's part

    @Test
    public void aWantedSpeedBecomesAFeedforwardGuessPlusACorrection() {
        Drivetrain drivetrain = mecanum();
        // Every wheel is stopped, so the whole error is the speed asked for.
        double wanted = 10.0;
        double expected = Constants.powerPerInchPerSecond * wanted + 0.008 * wanted;

        call(drivetrain, "setCommandedWheelSpeeds", wanted, wanted, wanted, wanted);
        drivetrain.drive(DrivePowers.zero(), true);
        assertArrayEquals(new double[]{expected, expected, expected, expected},
                motorPowers(), EPS);
    }

    @Test
    public void aWheelAlreadyAtTheWantedSpeedGetsTheGuessAndNoCorrection() {
        Drivetrain drivetrain = mecanum();
        double wanted = 10.0;
        // getVelocity() is in ticks per second, and ticksPerInch converts it.
        frontLeft.velocity = wanted * Constants.ticksPerInch;

        call(drivetrain, "setCommandedWheelSpeeds", wanted, wanted, wanted, wanted);
        drivetrain.drive(DrivePowers.zero(), true);
        assertEquals("no error, so no correction",
                Constants.powerPerInchPerSecond * wanted, frontLeft.power, EPS);
        assertEquals("and the stopped wheel still gets one",
                Constants.powerPerInchPerSecond * wanted + 0.008 * wanted, frontRight.power, EPS);
    }

    // ------------------------------------------------------------ L17's part

    @Test
    public void theTicksDoorReportsWhatEachEncoderCounted() {
        frontLeft.ticks = 10;
        frontRight.ticks = 20;
        backLeft.ticks = 30;
        backRight.ticks = 40;
        assertArrayEquals("front left, front right, back left, back right",
                new int[]{10, 20, 30, 40}, (int[]) call(sides(), "wheelTicks"));
    }

    @Test
    public void drivingNowAndLettingTheFollowerDriveTooIsRefused() {
        Drivetrain drivetrain = mecanum();
        call(drivetrain, "driveWheelsNow", 1, 1, 1, 1);
        try {
            drivetrain.drive(new DrivePowers(1, 0, 0), true);
            fail("two writers to one motor must be refused, not silently fought over");
        } catch (IllegalStateException expected) {
            assertTrue("the message says what to do instead: " + expected.getMessage(),
                    expected.getMessage().contains("setCommandedWheels"));
        }
    }
}
