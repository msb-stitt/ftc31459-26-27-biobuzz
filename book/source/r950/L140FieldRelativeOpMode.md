# L140FieldRelativeOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L140

```{code-block} diff
:class: answer-diff

--- mytry/L140FieldRelativeOpMode.java  (before L140)
+++ mytry/L140FieldRelativeOpMode.java  (after L140)
@@ lines 15 to 24 @@
   15 // When on L140S050, see
   16 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s050
   17 // for what to do here.
+  18 import org.firstinspires.ftc.teamcode.mytry.l135.DriverStationMenu;
+  19 import org.firstinspires.ftc.teamcode.mytry.l135.MenuItem;
+  20 import org.firstinspires.ftc.teamcode.mytry.l135.MenuRow;
+  21 import org.firstinspires.ftc.teamcode.mytry.l135.OptionMenu;
   22
   23 /**
   24  * L140: field relative. Push the stick away from you and the robot goes away
@@ lines 50 to 63 @@
   50     // When on L140S050, see
   51     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s050
   52     // for what to do here.
+  53     MenuItem red = new MenuItem("R", "Red");
+  54     MenuItem blue = new MenuItem("B", "Blue");
+  55     MenuRow allianceRow = new MenuRow("Alliance");
+  56     MenuItem autoEnd = new MenuItem("A", "Auto End");
+  57     MenuItem wall = new MenuItem("W", "Wall");
+  58     MenuRow startRow = new MenuRow("Start");
+  59     OptionMenu optionMenu = new OptionMenu();
+  60     DriverStationMenu menu = new DriverStationMenu(optionMenu);
   61
   62     double robotHalfLength_in = 9.0;
   63     double fieldPerimeterWidth_in = 1.5;
@@ lines 76 to 92 @@
   76         // When on L140S050, see
   77         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s050
   78         // for what to do here.
+  79         if (OpModeStorage.isRed) {
+  80             allianceRow.add(red);
+  81             allianceRow.add(blue);
+  82         } else {
+  83             allianceRow.add(blue);
+  84             allianceRow.add(red);
+  85         }
+  86         startRow.add(autoEnd);
+  87         startRow.add(wall);
+  88         optionMenu.add(allianceRow);
+  89         optionMenu.add(startRow);
   90     }
   91
   92     /** Again and again between INIT and PLAY. */
@@ lines 96 to 102 @@
   96         // When on L140S050, see
   97         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s050
   98         // for what to do here.
+  99         menu.loop(gamepad1);
  100         initLoopAfter();
  101     }
  102
@@ lines 107 to 117 @@
  107         // When on L140S070, see
  108         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s070
  109         // for what to do here.
+ 110         if (startRow.getSelected() == wall) {
+ 111             PoseFactory poses = poses();
+ 112             Pose wallPose = poses.of(72.0, botStartY_in, 90.0);
+ 113             follower.setPose(wallPose);
+ 114         }
  115         Command driveFromTheSticksEveryLoop = Commands.infinite(this::driveFromTheSticks);
  116         Scheduler.schedule(driveFromTheSticksEveryLoop);
  117         startAfter();
@@ lines 124 to 130 @@
  124         // When on L140S050, see
  125         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s050
  126         // for what to do here.
+ 127         Tracker.printToDs(menu.getSummary());
  128         loopAfter();
  129     }
  130
@@ lines 141 to 155 @@
  141         // When on L140S060, see
  142         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s060
  143         // for what to do here.
-             double fieldXSpeed = -leftStickY;
-             double fieldYSpeed = -leftStickX;
+ 144         double fieldXSpeed;
+ 145         double fieldYSpeed;
+ 146         if (allianceRow.getSelected() == red) {
+ 147             fieldXSpeed = -leftStickY;
+ 148             fieldYSpeed = -leftStickX;
+ 149         } else {
+ 150             fieldXSpeed = leftStickY;
+ 151             fieldYSpeed = leftStickX;
+ 152         }
  153
  154         fieldXSpeed = speedShaped(fieldXSpeed, bandToSetToZero);
  155         Tracker.publish("drivetrain/fieldXSpeed", fieldXSpeed);
@@ lines 163 to 176 @@
  163         // When on L140S040, see
  164         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s040
  165         // for what to do here.
-             drivetrain.sticks(fieldXSpeed, fieldYSpeed, turnCcwSpeed);
+ 166         double heading_rad = follower.pose().heading();
+ 167         double sinHeading = Math.sin(heading_rad);
+ 168         double cosHeading = Math.cos(heading_rad);
+ 169         double robotForwardXSpeed = fieldXSpeed * cosHeading + fieldYSpeed * sinHeading;
+ 170         double robotStrafeYSpeed = -fieldXSpeed * sinHeading + fieldYSpeed * cosHeading;
+ 171         Tracker.publish("drivetrain/robotForwardXSpeed", robotForwardXSpeed);
+ 172         Tracker.publish("drivetrain/robotStrafeYSpeed", robotStrafeYSpeed);
+ 173         drivetrain.sticks(robotForwardXSpeed, robotStrafeYSpeed, turnCcwSpeed);
  174     }
  175
  176     // 'public' to match the parent class. 'public' allows access from any code.
@@ lines 186 to 194 @@
  186         // When on L140S070, see
  187         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s070
  188         // for what to do here.
+ 189         if (allianceRow.getSelected() == blue) {
+ 190             chosenPoses = redPoses.rotateAround(72.0, 72.0, 180.0);
+ 191         }
  192         return chosenPoses;
  193     }
  194
```
