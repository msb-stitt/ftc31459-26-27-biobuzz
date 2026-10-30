# L140FieldRelativeOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L140

```{code-block} diff
:class: answer-diff

--- mytry/L140FieldRelativeOpMode.java  (before L140)
+++ mytry/L140FieldRelativeOpMode.java  (after L140)
@@ lines 16 to 25 @@
   16 // When on L140S050, see
   17 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s050
   18 // for what to do here.
+  19 import org.firstinspires.ftc.teamcode.mytry.l135.DriverStationMenu;
+  20 import org.firstinspires.ftc.teamcode.mytry.l135.MenuItem;
+  21 import org.firstinspires.ftc.teamcode.mytry.l135.MenuRow;
+  22 import org.firstinspires.ftc.teamcode.mytry.l135.OptionMenu;
   23
   24 /**
   25  * L140: field relative. Push the stick away from you and the robot goes away
@@ lines 53 to 66 @@
   53     // When on L140S050, see
   54     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s050
   55     // for what to do here.
+  56     MenuItem red = new MenuItem("R", "Red");
+  57     MenuItem blue = new MenuItem("B", "Blue");
+  58     MenuRow allianceRow = new MenuRow("Alliance");
+  59     MenuItem autoEnd = new MenuItem("A", "Auto End");
+  60     MenuItem wall = new MenuItem("W", "Wall");
+  61     MenuRow startRow = new MenuRow("Start");
+  62     OptionMenu optionMenu = new OptionMenu();
+  63     DriverStationMenu menu = new DriverStationMenu(optionMenu);
   64
   65     double robotHalfLength_in = 9.0;
   66     double fieldPerimeterWidth_in = 1.5;
@@ lines 79 to 95 @@
   79         // When on L140S050, see
   80         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s050
   81         // for what to do here.
+  82         if (OpModeStorage.isRed) {
+  83             allianceRow.add(red);
+  84             allianceRow.add(blue);
+  85         } else {
+  86             allianceRow.add(blue);
+  87             allianceRow.add(red);
+  88         }
+  89         startRow.add(autoEnd);
+  90         startRow.add(wall);
+  91         optionMenu.add(allianceRow);
+  92         optionMenu.add(startRow);
   93     }
   94
   95     /** Again and again between INIT and PLAY. */
@@ lines 99 to 105 @@
   99         // When on L140S050, see
  100         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s050
  101         // for what to do here.
+ 102         menu.loop(gamepad1);
  103         initLoopAfter();
  104     }
  105
@@ lines 110 to 120 @@
  110         // When on L140S070, see
  111         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s070
  112         // for what to do here.
+ 113         if (startRow.getSelected() == wall) {
+ 114             PoseFactory poses = poses();
+ 115             Pose wallPose = poses.of(72.0, botStartY_in, 90.0);
+ 116             follower.setPose(wallPose);
+ 117         }
  118         Command driveFromTheSticksEveryLoop = Commands.infinite(this::driveFromTheSticks);
  119         Scheduler.schedule(driveFromTheSticksEveryLoop);
  120         startAfter();
@@ lines 127 to 133 @@
  127         // When on L140S050, see
  128         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s050
  129         // for what to do here.
+ 130         Tracker.printToDs(menu.getSummary());
  131         loopAfter();
  132     }
  133
@@ lines 143 to 157 @@
  143         // When on L140S060, see
  144         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s060
  145         // for what to do here.
-             double fieldXSpeed = -leftStickY;
-             double fieldYSpeed = -leftStickX;
+ 146         double fieldXSpeed;
+ 147         double fieldYSpeed;
+ 148         if (allianceRow.getSelected() == red) {
+ 149             fieldXSpeed = -leftStickY;
+ 150             fieldYSpeed = -leftStickX;
+ 151         } else {
+ 152             fieldXSpeed = leftStickY;
+ 153             fieldYSpeed = leftStickX;
+ 154         }
  155
  156         fieldXSpeed = stickShaper.shaped(fieldXSpeed);
  157         Tracker.publish("drivetrain/fieldXSpeed", fieldXSpeed);
@@ lines 165 to 178 @@
  165         // When on L140S040, see
  166         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s040
  167         // for what to do here.
-             drivetrain.sticks(fieldXSpeed, fieldYSpeed, turnCcwSpeed);
+ 168         double heading_rad = follower.pose().heading();
+ 169         double sinHeading = Math.sin(heading_rad);
+ 170         double cosHeading = Math.cos(heading_rad);
+ 171         double robotForwardXSpeed = fieldXSpeed * cosHeading + fieldYSpeed * sinHeading;
+ 172         double robotStrafeYSpeed = -fieldXSpeed * sinHeading + fieldYSpeed * cosHeading;
+ 173         Tracker.publish("drivetrain/robotForwardXSpeed", robotForwardXSpeed);
+ 174         Tracker.publish("drivetrain/robotStrafeYSpeed", robotStrafeYSpeed);
+ 175         drivetrain.sticks(robotForwardXSpeed, robotStrafeYSpeed, turnCcwSpeed);
  176     }
  177
  178     // 'public' to match the parent class. 'public' allows access from any code.
@@ lines 188 to 196 @@
  188         // When on L140S070, see
  189         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s070
  190         // for what to do here.
+ 191         if (allianceRow.getSelected() == blue) {
+ 192             chosenPoses = redPoses.rotateAround(72.0, 72.0, 180.0);
+ 193         }
  194         return chosenPoses;
  195     }
  196 }
```
