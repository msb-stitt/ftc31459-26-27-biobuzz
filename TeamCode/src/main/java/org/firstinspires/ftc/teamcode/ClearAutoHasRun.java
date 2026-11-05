package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;

/**
 * Forgets the last autonomous, as restarting the Robot Controller app does, so
 * the next teleop starts where it would with no autonomous run. Nothing moves.
 *
 * <p>INIT says whether an autonomous has run, and pressing Start clears it with
 * {@link OpModeStorage#clear}.
 */
@TeleOp(name = "Clear Auto Has Run", group = "1 Experiments")
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class ClearAutoHasRun extends CorbelsTeleOp {

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init() {
        initBefore();
        String now = OpModeStorage.autoHasRun ? "has run" : "has not run";
        Tracker.printToDs("An auto %s. Press Start to clear it.", now);
        initAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void start() {
        startBefore();
        OpModeStorage.clear();
        startAfter();
    }

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();
        Tracker.printToDs("Cleared: no auto has run. Press Stop.");
        loopAfter();
    }
}
