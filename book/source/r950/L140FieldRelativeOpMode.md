# L140FieldRelativeOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l140-alliances.2`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L140

```{code-block} diff
:class: answer-diff

--- mytry/L140FieldRelativeOpMode.java  (before L140)
+++ mytry/L140FieldRelativeOpMode.java  (after L140)
@@ lines 43 to 51 @@
   43     // When on L140S050, see
   44     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s050
   45     // for what to do here.
+  46     L135AutoMenu menu = new L135AutoMenu(
+  47             new L135Row("Alliance", "Red", "Blue"),
+  48             new L135Row("Start", "Auto End", "Wall"));
   49
   50     double robotHalfLength_in = 9.0;
   51     double fieldPerimeterWidth_in = 1.5;
@@ lines 64 to 70 @@
   64         // When on L140S050, see
   65         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s050
   66         // for what to do here.
+  67         menu.startAt("Alliance", OpModeStorage.alliance);
   68     }
   69
   70     /** Again and again between INIT and PLAY. */
@@ lines 74 to 80 @@
   74         // When on L140S050, see
   75         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s050
   76         // for what to do here.
+  77         menu.loop(gamepad1);
   78         initLoopAfter();
   79     }
   80
@@ lines 85 to 96 @@
   85         // When on L140S070, see
   86         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s070
   87         // for what to do here.
+  88         String startPlace = menu.chosen("Start");
+  89         if (startPlace.equals("Wall")) {
+  90             PoseFactory poses = poses();
+  91             Pose wall = poses.of(72.0, botStartY_in, 90.0);
+  92             follower.setPose(wall);
+  93         }
   94         Command driveFromTheSticksEveryLoop = Commands.infinite(this::driveFromTheSticks);
   95         Scheduler.schedule(driveFromTheSticksEveryLoop);
   96         startAfter();
@@ lines 103 to 109 @@
  103         // When on L140S050, see
  104         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s050
  105         // for what to do here.
+ 106         Tracker.printToDs(menu.summary());
  107         loopAfter();
  108     }
  109
@@ lines 120 to 135 @@
  120         // When on L140S060, see
  121         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s060
  122         // for what to do here.
-             double fieldXSpeed = -leftStickY;
-             double fieldYSpeed = -leftStickX;
+ 123         double fieldXSpeed;
+ 124         double fieldYSpeed;
+ 125         String alliance = menu.chosen("Alliance");
+ 126         if (alliance.equals("Red")) {
+ 127             fieldXSpeed = -leftStickY;
+ 128             fieldYSpeed = -leftStickX;
+ 129         } else {
+ 130             fieldXSpeed = leftStickY;
+ 131             fieldYSpeed = leftStickX;
+ 132         }
  133
  134         fieldXSpeed = speedShaped(fieldXSpeed, bandToSetToZero);
  135         Tracker.publish("drivetrain/fieldXSpeed", fieldXSpeed);
@@ lines 143 to 156 @@
  143         // When on L140S040, see
  144         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s040
  145         // for what to do here.
-             drivetrain.sticks(fieldXSpeed, fieldYSpeed, turnCcwSpeed);
+ 146         double heading_rad = follower.pose().heading();
+ 147         double sinHeading = Math.sin(heading_rad);
+ 148         double cosHeading = Math.cos(heading_rad);
+ 149         double robotForwardXSpeed = fieldXSpeed * cosHeading + fieldYSpeed * sinHeading;
+ 150         double robotStrafeYSpeed = -fieldXSpeed * sinHeading + fieldYSpeed * cosHeading;
+ 151         Tracker.publish("drivetrain/robotForwardXSpeed", robotForwardXSpeed);
+ 152         Tracker.publish("drivetrain/robotStrafeYSpeed", robotStrafeYSpeed);
+ 153         drivetrain.sticks(robotForwardXSpeed, robotStrafeYSpeed, turnCcwSpeed);
  154     }
  155
  156     // 'public' to match the parent class. 'public' allows access from any code.
@@ lines 166 to 175 @@
  166         // When on L140S070, see
  167         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s070
  168         // for what to do here.
+ 169         String alliance = menu.chosen("Alliance");
+ 170         if (alliance.equals("Blue")) {
+ 171             chosenPoses = redPoses.rotateAround(72.0, 72.0, 180.0);
+ 172         }
  173         return chosenPoses;
  174     }
  175
```
