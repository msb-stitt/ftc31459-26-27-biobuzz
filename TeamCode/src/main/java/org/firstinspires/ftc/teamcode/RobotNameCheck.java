package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
import org.firstinspires.ftc.teamcode.base.Tracker;
import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.teamcode.pedro.RobotControllerName;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows whether the Robot Controller name, such as 37523-RC, picked the robot
 * in place of the {@link Constants#thisRobot} written in Constants. Nothing
 * moves.
 *
 * <p>INIT shows, on the Driver Station:
 *
 * <ul>
 *   <li>whether Constants' static code had already run before this INIT, in
 *       this run of the app;</li>
 *   <li>the calls that ran it, nearest first;</li>
 *   <li>the Robot Controller name as it could be read then, and as it can be
 *       read now;</li>
 *   <li>the robot thisRobot picks, and whether the Robot Controller name or the
 *       line written in Constants picked it.</li>
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
        boolean pickedByRobotControllerName = Constants.pickedByRobotControllerName;
        String whatRanConstants = RobotControllerName.whatRanConstants();
        String robotControllerNameWhenConstantsRan = RobotControllerName.nameWhenConstantsRan();
        String robotControllerNameNow = RobotControllerName.readNow();
        List<String> lines = linesToShow(constantsHadRunBefore, whatRanConstants,
                robotControllerNameWhenConstantsRan, robotControllerNameNow, pickedByThisRobot,
                pickedByRobotControllerName);
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
                                    String robotControllerNameWhenConstantsRan,
                                    String robotControllerNameNow,
                                    Constants.Robot pickedByThisRobot,
                                    boolean pickedByRobotControllerName) {
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
        lines.add("Robot Controller name when Constants ran: "
                + robotControllerNameWhenConstantsRan);
        lines.add("Robot Controller name now: " + robotControllerNameNow);
        String pickedBy = "as written in Constants";
        if (pickedByRobotControllerName) {
            pickedBy = "by the Robot Controller name";
        }
        lines.add("thisRobot picks: " + pickedByThisRobot + ", " + pickedBy);
        return lines;
    }
}
