package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.firstinspires.ftc.teamcode.base.Tracker.LiveSetting;

/**
 * Turns live NetworkTables on or off for the OpModes that run after it, the
 * way Panels' own Enable/Disable OpMode turns Panels on or off. Nothing moves.
 *
 * <p>INIT says whether it is on, and pressing Start turns it the other way.
 * The setting lasts until the Robot Controller app restarts. Until this OpMode
 * sets it, a lesson is live and any other OpMode is not.
 *
 * <p>Before a competition, run this until it says off: FTC rule R704 forbids
 * third-party telemetry over Wi-Fi there.
 */
@TeleOp(name = "Live NetworkTables on/off", group = "Corbels")
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class LiveNetworkTablesSwitch extends CorbelsTeleOp {

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init() {
        initBefore();
        LiveSetting setting = Tracker.liveSetting();
        String now;
        String other;
        if (setting == LiveSetting.ON) {
            now = "on";
            other = "off";
        } else if (setting == LiveSetting.OFF) {
            now = "off";
            other = "on";
        } else {
            now = "off, but on for a lesson";
            other = "on";
        }
        Tracker.printToDs("Live NetworkTables is currently %s. Press Start to turn it %s.", now, other);
        initAfter();
    }

    /** Turns it the other way. Not set counts as off, so Start turns it on. */
    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void start() {
        startBefore();
        boolean wasOn = Tracker.liveSetting() == LiveSetting.ON;
        LiveSetting turned = wasOn ? LiveSetting.OFF : LiveSetting.ON;
        Tracker.setLive(turned);
        startAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();
        boolean isOn = Tracker.liveSetting() == LiveSetting.ON;
        String now = isOn ? "on" : "off";
        Tracker.printToDs("Live NetworkTables is now %s. Press Stop.", now);
        loopAfter();
    }
}
