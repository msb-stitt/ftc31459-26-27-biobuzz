package org.firstinspires.ftc.teamcode.lessons;

import static org.junit.Assert.fail;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Builds a lesson's class out of {@code mytry} by its name, and calls its
 * methods by name, so the lesson tests compile on the lessons line, where
 * {@code mytry} holds nothing until a student copies a file into it.
 *
 * <p>A test holds what it builds as an interface the robot code already has,
 * such as Pedro's {@code Drivetrain}, and reaches the lesson's own methods with
 * {@link #call}.
 */
final class MyTry {

    static final String PACKAGE = "org.firstinspires.ftc.teamcode.mytry";

    private MyTry() {
    }

    /** A new instance of {@code mytry.<simpleName>}, or a failure naming the copy to make. */
    static OpMode opMode(String simpleName) {
        return make(simpleName, OpMode.class);
    }

    /** A new {@code mytry.<simpleName>}, built with the constructor that takes {@code args}. */
    static <T> T make(String simpleName, Class<T> as, Object... args) {
        Class<?> c;
        try {
            c = Class.forName(PACKAGE + "." + simpleName);
        } catch (ClassNotFoundException e) {
            fail("mytry has no " + simpleName + " yet: copy it into mytry, as its lesson says");
            throw new AssertionError(e);
        }
        for (Constructor<?> k : c.getDeclaredConstructors()) {
            if (fits(k.getParameterTypes(), args)) {
                try {
                    k.setAccessible(true);
                    return as.cast(k.newInstance(widened(k.getParameterTypes(), args)));
                } catch (InvocationTargetException e) {
                    throw rethrown(e);
                } catch (ReflectiveOperationException e) {
                    throw new AssertionError("mytry." + simpleName + " could not be built", e);
                }
            }
        }
        throw new AssertionError("mytry." + simpleName + " has no constructor for "
                + args.length + " argument(s)");
    }

    /** Calls {@code name} on {@code target}, searching its class and every superclass. */
    static Object call(Object target, String name, Object... args) {
        for (Class<?> c = target.getClass(); c != null; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(name) && fits(m.getParameterTypes(), args)) {
                    try {
                        m.setAccessible(true);
                        return m.invoke(target, widened(m.getParameterTypes(), args));
                    } catch (InvocationTargetException e) {
                        throw rethrown(e);
                    } catch (IllegalAccessException e) {
                        throw new AssertionError(e);
                    }
                }
            }
        }
        throw new AssertionError(target.getClass().getSimpleName() + " has no " + name
                + " taking " + args.length + " argument(s)");
    }

    /** What the lesson's own code threw, so a test sees its message and type unchanged. */
    private static RuntimeException rethrown(InvocationTargetException e) {
        Throwable cause = e.getCause();
        if (cause instanceof RuntimeException) {
            return (RuntimeException) cause;
        }
        if (cause instanceof Error) {
            throw (Error) cause;
        }
        return new RuntimeException(cause);
    }

    private static boolean fits(Class<?>[] params, Object[] args) {
        if (params.length != args.length) {
            return false;
        }
        for (int i = 0; i < params.length; i++) {
            Class<?> p = params[i].isPrimitive() ? boxed(params[i]) : params[i];
            boolean widens = p == Double.class && args[i] instanceof Integer;
            if (args[i] != null && !p.isInstance(args[i]) && !widens) {
                return false;
            }
        }
        return true;
    }

    /** The arguments, with a whole number turned into a double where the method takes one. */
    private static Object[] widened(Class<?>[] params, Object[] args) {
        Object[] out = args.clone();
        for (int i = 0; i < params.length; i++) {
            boolean takesDouble = params[i] == double.class || params[i] == Double.class;
            if (takesDouble && out[i] instanceof Integer) {
                out[i] = ((Integer) out[i]).doubleValue();
            }
        }
        return out;
    }

    private static Class<?> boxed(Class<?> primitive) {
        if (primitive == double.class) return Double.class;
        if (primitive == int.class) return Integer.class;
        if (primitive == boolean.class) return Boolean.class;
        if (primitive == long.class) return Long.class;
        if (primitive == float.class) return Float.class;
        throw new IllegalArgumentException(primitive.getName());
    }
}
