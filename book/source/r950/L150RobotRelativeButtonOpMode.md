# L150RobotRelativeButtonOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-minor-fixes.1`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L150

```{code-block} diff
:class: answer-diff

--- mytry/L150RobotRelativeButtonOpMode.java  (before L150)
+++ mytry/L150RobotRelativeButtonOpMode.java  (after L150)
@@ lines 85 to 102 @@
   85         // When on L150S030, see
   86         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l150.html#l150s030
   87         // for what to do here.
-             double heading = follower.pose().heading();
-             robotForwardXSpeed = fieldXSpeed * Math.cos(heading) + fieldYSpeed * Math.sin(heading);
-             robotStrafeYSpeed = -fieldXSpeed * Math.sin(heading) + fieldYSpeed * Math.cos(heading);
+  88         boolean robotRelative = gamepad1.right_bumper;
+  89         Tracker.publish("drivetrain/robotRelative", robotRelative);
+  90         if (robotRelative) {
+  91             robotForwardXSpeed = -leftStickY;
+  92             robotForwardXSpeed = speedShaped(robotForwardXSpeed, bandToSetToZero);
+  93             robotStrafeYSpeed = -leftStickX;
+  94             robotStrafeYSpeed = speedShaped(robotStrafeYSpeed, bandToSetToZero);
+  95         } else {
+  96             double heading = follower.pose().heading();
+  97             robotForwardXSpeed = fieldXSpeed * Math.cos(heading) + fieldYSpeed * Math.sin(heading);
+  98             robotStrafeYSpeed = -fieldXSpeed * Math.sin(heading) + fieldYSpeed * Math.cos(heading);
+  99         }
  100         Tracker.publish("drivetrain/robotForwardXSpeed", robotForwardXSpeed);
  101         Tracker.publish("drivetrain/robotStrafeYSpeed", robotStrafeYSpeed);
  102         drivetrain.sticks(robotForwardXSpeed, robotStrafeYSpeed, turnCcwSpeed);
```
