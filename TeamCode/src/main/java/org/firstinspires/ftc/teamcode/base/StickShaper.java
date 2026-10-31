package org.firstinspires.ftc.teamcode.base;

/**
 * Shapes one stick's reading: a deadband, then linear, squared or cubed.
 * The object holds the band and the shape, so the code that asks for a shaped
 * value does not need to know either. The same class a student writes in L065,
 * which lessons from L113 on use in its place.
 *
 * <p>Uses no other class.
 */
// 'public' so OpModes outside base can use it. 'public' allows access from any code.
public class StickShaper {

    /** How a speed outside the band is shaped. */
    // 'public' so OpModes outside base can use it. 'public' allows access from any code.
    public enum Shape {
        /** Left as it is. */
        LINEAR,
        /** Squared, keeping its sign: half a stick is a quarter of the speed. */
        SQUARED,
        /** Cubed, which keeps its sign: half a stick is an eighth of the speed. */
        CUBED
    }

    private double band;
    private Shape shape = Shape.SQUARED;

    /** A shaper with the given band, and {@link Shape#SQUARED}. */
    // 'public' so OpModes outside base can use it. 'public' allows access from any code.
    public StickShaper(double bandToSetToZero) {
        band = bandToSetToZero;
    }

    /** A reading closer to zero than this counts as zero. */
    // 'public' so OpModes outside base can use it. 'public' allows access from any code.
    public void setBand(double bandToSetToZero) {
        band = bandToSetToZero;
    }

    /** Which shape {@link #shaped} uses from now on. */
    // 'public' so OpModes outside base can use it. 'public' allows access from any code.
    public void setShape(Shape newShape) {
        shape = newShape;
    }

    /** The deadband, then the shape this object holds. */
    // 'public' so OpModes outside base can use it. 'public' allows access from any code.
    public double shaped(double value) {
        double outsideTheBand = deadband(value, band);
        double shapedValue = outsideTheBand;
        switch (shape) {
            case LINEAR:
                break;
            case SQUARED:
                shapedValue = signedSquared(outsideTheBand);
                break;
            case CUBED:
                shapedValue = cubed(outsideTheBand);
                break;
        }
        return shapedValue;
    }

    /** Zero when the value is inside the band, and the value itself when it is not. */
    private double deadband(double value, double band) {
        double alwaysPositiveMagnitude = Math.abs(value);
        boolean isCloseToZero = alwaysPositiveMagnitude < band;
        if (isCloseToZero) {
            return 0.0;
        }
        return value;
    }

    /** The value times itself, with the sign it started with. */
    private double signedSquared(double value) {
        double alwaysPositiveValue = Math.abs(value);
        double valueSquaredKeepingTheSign = value * alwaysPositiveValue;
        return valueSquaredKeepingTheSign;
    }

    /** The value cubed, which keeps its sign. */
    private double cubed(double value) {
        double valueCubed = value * value * value;
        return valueCubed;
    }
}
