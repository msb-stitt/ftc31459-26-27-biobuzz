package org.firstinspires.ftc.teamcode.base;

import io.github.mikestitt.corbelsflightlog.FlightLog;

/**
 * Writes the channels WPILib's SysId analyser reads.
 *
 * <p>SysId works in volts, and an FTC motor takes a speed, a fraction of
 * battery voltage. So applied voltage is the commanded speed times whatever
 * the battery is giving at that moment --
 * which means battery sag appears as fewer volts for the same speed, rather than
 * as noise in the fit.
 *
 * <p>The analyser asks the user which channels to use, so the names here need
 * not match WPILib's exactly; the {@link State} values do have to, because it
 * splits them on the last dash and refuses anything else.
 */
public class SysIdRecorder {

    /** The states the analyser recognises. Anything else is discarded. */
    public enum State {
        QUASISTATIC_FORWARD("quasistatic-forward"),
        QUASISTATIC_REVERSE("quasistatic-reverse"),
        DYNAMIC_FORWARD("dynamic-forward"),
        DYNAMIC_REVERSE("dynamic-reverse"),
        NONE("none");

        private final String text;

        State(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private final FlightLog flightlog;
    private final String name;

    /**
     * @param flightlog where the channels go
     * @param name      what this mechanism is called, e.g. "drive"
     */
    public SysIdRecorder(FlightLog flightlog, String name) {
        this.flightlog = flightlog;
        this.name = name;
    }

    /** The channel the analyser wants for "Test State". */
    public String stateChannel() {
        return "sysid-test-state-" + name;
    }

    /** Records which test is running, or {@link State#NONE} between them. */
    public void state(State state) {
        if (flightlog != null) flightlog.recordOutput(stateChannel(), state.toString());
    }

    /**
     * One motor's sample.
     *
     * @param motor        what the motor is called, e.g. "frontLeft"
     * @param speed        what was commanded, -1 to 1
     * @param batteryVolts what the battery is giving right now
     * @param inches       how far this wheel has travelled
     * @param inchesPerSec how fast it is travelling
     */
    public void motor(String motor, double speed, double batteryVolts,
                      double inches, double inchesPerSec) {
        if (flightlog == null) return;
        flightlog.recordOutput("voltage-" + motor + "-" + name, appliedVolts(speed, batteryVolts));
        flightlog.recordOutput("position-" + motor + "-" + name, inches);
        flightlog.recordOutput("velocity-" + motor + "-" + name, inchesPerSec);
    }

    /**
     * What the motor actually saw. A speed of 1 on a 12.4 V battery is 12.4 V;
     * the same speed at the end of a match is less, and that is the point.
     */
    public static double appliedVolts(double speed, double batteryVolts) {
        return speed * batteryVolts;
    }
}
