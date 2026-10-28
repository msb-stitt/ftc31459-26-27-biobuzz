# L140FieldRelativeOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-l135-menu-classes.1`. A line marked + is new, and a line marked - is
gone; each number is the line's place in the file after the lesson.

## L140

```{code-block} diff
:class: answer-diff

--- mytry/L140FieldRelativeOpMode.java  (before L140)
+++ mytry/L140FieldRelativeOpMode.java  (after L140)
@@ lines 43 to 56 @@
   43     // When on L140S050, see
   44     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s050
   45     // for what to do here.
+  46     L135MenuItem red = new L135MenuItem("R", "Red");
+  47     L135MenuItem blue = new L135MenuItem("B", "Blue");
+  48     L135MenuRow allianceRow = new L135MenuRow("Alliance", red, blue);
+  49     L135MenuItem autoEnd = new L135MenuItem("A", "Auto End");
+  50     L135MenuItem wall = new L135MenuItem("W", "Wall");
+  51     L135MenuRow startRow = new L135MenuRow("Start", autoEnd, wall);
+  52     L135OptionMenu optionMenu = new L135OptionMenu(allianceRow, startRow);
+  53     L135DriverStationMenu menu = new L135DriverStationMenu(optionMenu);
   54
   55     double robotHalfLength_in = 9.0;
   56     double fieldPerimeterWidth_in = 1.5;
@@ lines 69 to 75 @@
   69         // When on L140S050, see
   70         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s050
   71         // for what to do here.
+  72         menu.startAt("Alliance", OpModeStorage.alliance);
   73     }
   74
   75     /** Again and again between INIT and PLAY. */
@@ lines 79 to 85 @@
   79         // When on L140S050, see
   80         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s050
   81         // for what to do here.
+  82         menu.loop(gamepad1);
   83         initLoopAfter();
   84     }
   85
@@ lines 90 to 101 @@
   90         // When on L140S070, see
   91         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s070
   92         // for what to do here.
+  93         String startPlace = menu.chosen("Start");
+  94         if (startPlace.equals("Wall")) {
+  95             PoseFactory poses = poses();
+  96             Pose wall = poses.of(72.0, botStartY_in, 90.0);
+  97             follower.setPose(wall);
+  98         }
   99         Command driveFromTheSticksEveryLoop = Commands.infinite(this::driveFromTheSticks);
  100         Scheduler.schedule(driveFromTheSticksEveryLoop);
  101         startAfter();
@@ lines 108 to 114 @@
  108         // When on L140S050, see
  109         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s050
  110         // for what to do here.
+ 111         Tracker.printToDs(menu.summary());
  112         loopAfter();
  113     }
  114
@@ lines 125 to 140 @@
  125         // When on L140S060, see
  126         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s060
  127         // for what to do here.
-             double fieldXSpeed = -leftStickY;
-             double fieldYSpeed = -leftStickX;
+ 128         double fieldXSpeed;
+ 129         double fieldYSpeed;
+ 130         String alliance = menu.chosen("Alliance");
+ 131         if (alliance.equals("Red")) {
+ 132             fieldXSpeed = -leftStickY;
+ 133             fieldYSpeed = -leftStickX;
+ 134         } else {
+ 135             fieldXSpeed = leftStickY;
+ 136             fieldYSpeed = leftStickX;
+ 137         }
  138
  139         fieldXSpeed = speedShaped(fieldXSpeed, bandToSetToZero);
  140         Tracker.publish("drivetrain/fieldXSpeed", fieldXSpeed);
@@ lines 148 to 161 @@
  148         // When on L140S040, see
  149         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s040
  150         // for what to do here.
-             drivetrain.sticks(fieldXSpeed, fieldYSpeed, turnCcwSpeed);
+ 151         double heading_rad = follower.pose().heading();
+ 152         double sinHeading = Math.sin(heading_rad);
+ 153         double cosHeading = Math.cos(heading_rad);
+ 154         double robotForwardXSpeed = fieldXSpeed * cosHeading + fieldYSpeed * sinHeading;
+ 155         double robotStrafeYSpeed = -fieldXSpeed * sinHeading + fieldYSpeed * cosHeading;
+ 156         Tracker.publish("drivetrain/robotForwardXSpeed", robotForwardXSpeed);
+ 157         Tracker.publish("drivetrain/robotStrafeYSpeed", robotStrafeYSpeed);
+ 158         drivetrain.sticks(robotForwardXSpeed, robotStrafeYSpeed, turnCcwSpeed);
  159     }
  160
  161     // 'public' to match the parent class. 'public' allows access from any code.
@@ lines 171 to 180 @@
  171         // When on L140S070, see
  172         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s070
  173         // for what to do here.
+ 174         String alliance = menu.chosen("Alliance");
+ 175         if (alliance.equals("Blue")) {
+ 176             chosenPoses = redPoses.rotateAround(72.0, 72.0, 180.0);
+ 177         }
  178         return chosenPoses;
  179     }
  180
```
