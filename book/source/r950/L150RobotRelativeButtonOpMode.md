# L150RobotRelativeButtonOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-l135-menu-classes.2`. A line marked + is new, and a line marked - is
gone; each number is the line's place in the file after the lesson.

## L150

```{code-block} diff
:class: answer-diff

--- mytry/L150RobotRelativeButtonOpMode.java  (before L150)
+++ mytry/L150RobotRelativeButtonOpMode.java  (after L150)
@@ lines 85 to 104 @@
   85         // When on L150S030, see
   86         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l150.html#l150s030
   87         // for what to do here.
-             double heading_rad = follower.pose().heading();
-             double sinHeading = Math.sin(heading_rad);
-             double cosHeading = Math.cos(heading_rad);
-             robotForwardXSpeed = fieldXSpeed * cosHeading + fieldYSpeed * sinHeading;
-             robotStrafeYSpeed = -fieldXSpeed * sinHeading + fieldYSpeed * cosHeading;
+  88         boolean robotRelative = gamepad1.right_bumper;
+  89         Tracker.publish("drivetrain/robotRelative", robotRelative);
+  90         if (robotRelative) {
+  91             robotForwardXSpeed = -leftStickY;
+  92             robotForwardXSpeed = speedShaped(robotForwardXSpeed, bandToSetToZero);
+  93             robotStrafeYSpeed = -leftStickX;
+  94             robotStrafeYSpeed = speedShaped(robotStrafeYSpeed, bandToSetToZero);
+  95         } else {
+  96             double heading_rad = follower.pose().heading();
+  97             double sinHeading = Math.sin(heading_rad);
+  98             double cosHeading = Math.cos(heading_rad);
+  99             robotForwardXSpeed = fieldXSpeed * cosHeading + fieldYSpeed * sinHeading;
+ 100             robotStrafeYSpeed = -fieldXSpeed * sinHeading + fieldYSpeed * cosHeading;
+ 101         }
  102         Tracker.publish("drivetrain/robotForwardXSpeed", robotForwardXSpeed);
  103         Tracker.publish("drivetrain/robotStrafeYSpeed", robotStrafeYSpeed);
  104         drivetrain.sticks(robotForwardXSpeed, robotStrafeYSpeed, turnCcwSpeed);
```
