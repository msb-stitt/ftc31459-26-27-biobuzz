package org.firstinspires.ftc.teamcode.example;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import io.github.mikestitt.corbelsflightlog.nt.Nt4Server;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

import java.io.IOException;

/**
 * Checks the robot's NetworkTables server by eye: the Driver Station shows a
 * few values, and AdvantageScope, connected to the robot, should show the same.
 *
 * <p>The wheels are never driven. The sticks and the A button are read and
 * published, nothing more.
 *
 * <p>In AdvantageScope: <b>AdvantageScope > Settings…</b>, set <b>Robot
 * Address</b> to {@code 192.168.43.1}, then <b>File > Connect to Robot >
 * NetworkTables 4</b>. The values are under {@code ntcheck/}, and the pose under
 * {@code Robot/Pose}.
 *
 * <p>For practice only: FTC rule R704 forbids third-party telemetry over Wi-Fi
 * at competitions, and this OpMode turns the server on.
 *
 * <p>Passes when: NtCheckOpModeTest
 */
@TeleOp(name = "NetworkTables check", group = "Corbels")
public class NtCheckOpMode extends CorbelsTeleOp {

    /** One sine wave every 4 s, so a live graph is easy to tell from a stuck one. */
    static final double SINE_PERIOD_S = 4.0;

    private boolean serving;
    private long startNs;

    @Override
    public void init() {
        initBefore();
        serving = Tracker.serveLive();
        initAfter();
    }

    @Override
    public void start() {
        super.start();
        startNs = System.nanoTime();
    }

    @Override
    public void loop() {
        loopBefore();

        double seconds = (System.nanoTime() - startNs) / 1e9;
        double sine = Math.sin(2 * Math.PI * seconds / SINE_PERIOD_S);
        double leftY = -gamepad1.left_stick_y;
        boolean a = gamepad1.a;
        Tracker.publish("ntcheck/seconds", seconds);
        Tracker.publish("ntcheck/sine", sine);
        Tracker.publish("ntcheck/leftY", leftY);
        Tracker.publish("ntcheck/a", a);
        Tracker.publish("ntcheck/word", a ? "A held" : "A up");

        Tracker.printToDs("NetworkTables %s, port %d", serving ? "on" : "OFF", Nt4Server.DEFAULT_PORT);
        Tracker.printToDs("AdvantageScope connections: %d", connections());
        Tracker.printToDs("seconds %.0f   sine %.2f", seconds, sine);
        Tracker.printToDs("leftY %.2f   a %b", leftY, a);

        loopAfter();
    }

    /** How many sockets AdvantageScope has open: it opens two on NetworkTables 4.1. */
    private int connections() {
        try {
            return serving ? Nt4Server.shared().connectionCount() : 0;
        } catch (IOException e) {
            return 0;
        }
    }
}
