package org.firstinspires.ftc.teamcode.base;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.teamcode.Intake;
import org.firstinspires.ftc.teamcode.Launcher;
import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.function.DoubleSupplier;
import java.util.function.Function;

/**
 * A stand-in intake and launcher for {@link SimRun}, which has no
 * HardwareMap to look the real ones up in.
 *
 * <p>An OpMode that takes them through a static {@code intakes} or
 * {@code launchers}, as StarterBot TeleOp, Launcher Check, Example Auto and
 * Example TeleOp do, is given these instead. The intake and launcher are the
 * real classes on motors and servos that keep what they are given and do
 * nothing more; each publishes what it always does, so AdvantageScope shows it.
 * The launcher motor reports the speed it was last given, so the windmill feeds
 * from the second loop it is asked to; with {@link SimMujoco} on, it reports
 * the speed MuJoCo's model of its hub measures, and keeps the P, I, D and F the
 * code gives it for MuJoCo to use.
 *
 * <p>Passes when: SimMechanismsTest.
 */
final class SimMechanisms {

    private SimMechanisms() {
    }

    /** The launcher motor's P, I, D and F as the code last set them, or null. */
    static volatile double[] launcherPidf;

    /** The launcher's speed as MuJoCo measures it, or null to report the speed last given. */
    static volatile DoubleSupplier launcherMeasured;

    /**
     * Sets each static {@code intakes} and {@code launchers} of {@code opModeClass}
     * to hand out the stand-ins. Returns how many it set.
     */
    static int install(Class<?> opModeClass) throws IllegalAccessException {
        Intake intake = new Intake(device(DcMotorEx.class), device(CRServo.class),
                device(CRServo.class));
        launcherPidf = null;
        Launcher launcher = new Launcher(launcherMotor(), device(CRServo.class),
                device(VoltageSensor.class));
        int set = 0;
        for (Field field : opModeClass.getDeclaredFields()) {
            boolean factory = Modifier.isStatic(field.getModifiers())
                    && field.getType() == Function.class;
            if (factory && field.getName().equals("intakes")) {
                field.setAccessible(true);
                field.set(null, (Function<Object, Intake>) map -> intake);
                set++;
            }
            if (factory && field.getName().equals("launchers")) {
                field.setAccessible(true);
                field.set(null, (Function<Object, Launcher>) map -> launcher);
                set++;
            }
        }
        return set;
    }

    /**
     * The launcher's motor: a {@link #device} that also keeps the gains the code
     * sets in {@link #launcherPidf}, and reports {@link #launcherMeasured} when
     * MuJoCo moves it.
     */
    static DcMotorEx launcherMotor() {
        DcMotorEx plain = device(DcMotorEx.class);
        Object proxy = Proxy.newProxyInstance(DcMotorEx.class.getClassLoader(),
                new Class<?>[]{DcMotorEx.class},
                (self, method, args) -> {
                    switch (method.getName()) {
                        case "setPIDFCoefficients":
                            PIDFCoefficients g = (PIDFCoefficients) args[1];
                            launcherPidf = new double[]{g.p, g.i, g.d, g.f};
                            return null;
                        case "setVelocityPIDFCoefficients":
                            launcherPidf = new double[]{(Double) args[0], (Double) args[1],
                                    (Double) args[2], (Double) args[3]};
                            return null;
                        case "getVelocity":
                            DoubleSupplier measured = launcherMeasured;
                            if (measured != null) {
                                return measured.getAsDouble();
                            }
                            return method.invoke(plain, args);
                        default:
                            return method.invoke(plain, args);
                    }
                });
        return (DcMotorEx) proxy;
    }

    /**
     * A motor or servo that keeps the power and the velocity it was last given,
     * and reports that velocity back, or a battery that reports
     * {@link Constants#NOMINAL_BATTERY_VOLTAGE}. Every other call gets a
     * harmless default.
     */
    static <T> T device(Class<T> type) {
        double[] power = {0};
        double[] velocity = {0};
        Object proxy = Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (self, method, args) -> {
                    switch (method.getName()) {
                        case "setPower":
                            power[0] = (Double) args[0];
                            return null;
                        case "getPower":
                            return power[0];
                        case "setVelocity":
                            velocity[0] = (Double) args[0];
                            return null;
                        case "getVelocity":
                            return velocity[0];
                        case "getVoltage":
                            return Constants.NOMINAL_BATTERY_VOLTAGE;
                        default:
                            return OpModeHarness.defaultValue(method.getReturnType());
                    }
                });
        return type.cast(proxy);
    }
}
