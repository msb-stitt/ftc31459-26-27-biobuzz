package org.firstinspires.ftc.teamcode.base;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.teamcode.Intake;
import org.firstinspires.ftc.teamcode.Launcher;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
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
 * from the second loop it is asked to.
 *
 * <p>Passes when: SimMechanismsTest.
 */
final class SimMechanisms {

    private SimMechanisms() {
    }

    /**
     * Sets each static {@code intakes} and {@code launchers} of {@code opModeClass}
     * to hand out the stand-ins. Returns how many it set.
     */
    static int install(Class<?> opModeClass) throws IllegalAccessException {
        Intake intake = new Intake(device(DcMotor.class), device(CRServo.class),
                device(CRServo.class));
        Launcher launcher = new Launcher(device(DcMotorEx.class), device(CRServo.class));
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
     * A motor or servo that keeps the power and the velocity it was last given,
     * and reports that velocity back. Every other call gets a harmless default.
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
                        default:
                            return OpModeHarness.defaultValue(method.getReturnType());
                    }
                });
        return type.cast(proxy);
    }
}
