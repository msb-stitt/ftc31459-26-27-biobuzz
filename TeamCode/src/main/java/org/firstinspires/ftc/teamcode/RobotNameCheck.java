package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.teamcode.pedro.RobotControllerName;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds out whether the Robot Controller's name, such as 37523-RC, could pick
 * the robot in place of {@link Constants#thisRobot}. Nothing moves.
 *
 * <p>INIT shows, on the Driver Station:
 *
 * <ul>
 *   <li>whether Constants' static code had already run before this INIT, in
 *       this run of the app;</li>
 *   <li>the calls that ran it, nearest first;</li>
 *   <li>the name as it could be read then, and as it can be read now;</li>
 *   <li>the robot thisRobot picks.</li>
 * </ul>
 *
 * <p>It reads what Constants found before {@code initBefore()}, which looks up
 * the drivetrain by the names in Constants and so runs Constants' static code.
 */
@TeleOp(name = "Robot Name Check", group = "Corbels")
// 'public' so the robot can run this OpMode. 'public' allows access from any code.
public class RobotNameCheck extends CorbelsTeleOp {

    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void init() {
        // Read before initBefore(), which runs Constants' static code.
        boolean constantsHadRunBefore = RobotControllerName.constantsHasRun();
        initBefore();
        Constants.Robot pickedByThisRobot = Constants.thisRobot;
        String whatRanConstants = RobotControllerName.whatRanConstants();
        String nameWhenConstantsRan = RobotControllerName.nameWhenConstantsRan();
        String nameNow = RobotControllerName.readNow();
        List<String> lines = linesToShow(constantsHadRunBefore, whatRanConstants,
                nameWhenConstantsRan, nameNow, pickedByThisRobot);
        for (String line : lines) {
            Tracker.printToDs(line);
        }
        initAfter();
    }

    /** Nothing of its own: INIT has shown everything. */
    // 'public' to match the parent class. 'public' allows access from any code.
    @Override
    public void loop() {
        loopBefore();
        loopAfter();
    }

    /** What INIT shows, one line each. */
    static List<String> linesToShow(boolean constantsHadRunBefore, String whatRanConstants,
                                    String nameWhenConstantsRan, String nameNow,
                                    Constants.Robot pickedByThisRobot) {
        List<String> lines = new ArrayList<>();
        if (constantsHadRunBefore) {
            lines.add("Constants had already run before this INIT.");
        } else {
            lines.add("Constants had not run before this INIT; this INIT ran it.");
        }
        lines.add("What ran Constants, nearest first:");
        for (String call : whatRanConstants.split("\n")) {
            lines.add("  " + call);
        }
        lines.add("Name when Constants ran: " + nameWhenConstantsRan);
        lines.add("Name now: " + nameNow);
        lines.add("thisRobot picks: " + pickedByThisRobot);
        return lines;
    }
}
