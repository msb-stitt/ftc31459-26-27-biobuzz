# L150RobotRelativeButtonOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111.1`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L150

```{code-block} diff
:class: answer-diff

--- mytry/L150RobotRelativeButtonOpMode.java  (before L150)
+++ mytry/L150RobotRelativeButtonOpMode.java  (after L150)
@@ lines 87 to 106 @@
   87         // When on L150S030, see
   88         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l150.html#l150s030
   89         // for what to do here.
-             double heading_rad = follower.pose().heading();
-             double sinHeading = Math.sin(heading_rad);
-             double cosHeading = Math.cos(heading_rad);
-             robotForwardXSpeed = fieldXSpeed * cosHeading + fieldYSpeed * sinHeading;
-             robotStrafeYSpeed = -fieldXSpeed * sinHeading + fieldYSpeed * cosHeading;
+  90         boolean robotRelative = gamepad1.right_bumper;
+  91         Tracker.publish("drivetrain/robotRelative", robotRelative);
+  92         if (robotRelative) {
+  93             robotForwardXSpeed = -leftStickY;
+  94             robotForwardXSpeed = stickShaper.shaped(robotForwardXSpeed);
+  95             robotStrafeYSpeed = -leftStickX;
+  96             robotStrafeYSpeed = stickShaper.shaped(robotStrafeYSpeed);
+  97         } else {
+  98             double heading_rad = follower.pose().heading();
+  99             double sinHeading = Math.sin(heading_rad);
+ 100             double cosHeading = Math.cos(heading_rad);
+ 101             robotForwardXSpeed = fieldXSpeed * cosHeading + fieldYSpeed * sinHeading;
+ 102             robotStrafeYSpeed = -fieldXSpeed * sinHeading + fieldYSpeed * cosHeading;
+ 103         }
  104         Tracker.publish("drivetrain/robotForwardXSpeed", robotForwardXSpeed);
  105         Tracker.publish("drivetrain/robotStrafeYSpeed", robotStrafeYSpeed);
  106         drivetrain.sticks(robotForwardXSpeed, robotStrafeYSpeed, turnCcwSpeed);
```
