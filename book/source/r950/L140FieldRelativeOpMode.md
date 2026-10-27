# L140FieldRelativeOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l140-alliances`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L140

```{code-block} diff
:class: answer-diff

--- mytry/L140FieldRelativeOpMode.java  (before L140)
+++ mytry/L140FieldRelativeOpMode.java  (after L140)
@@ lines 83 to 96 @@
   83         // When on L140S040, see
   84         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s040
   85         // for what to do here.
-             drivetrain.sticks(fieldXSpeed, fieldYSpeed, turnCcwSpeed);
+  86         double heading_rad = follower.pose().heading();
+  87         double sinHeading = Math.sin(heading_rad);
+  88         double cosHeading = Math.cos(heading_rad);
+  89         double robotForwardXSpeed = fieldXSpeed * cosHeading + fieldYSpeed * sinHeading;
+  90         double robotStrafeYSpeed = -fieldXSpeed * sinHeading + fieldYSpeed * cosHeading;
+  91         Tracker.publish("drivetrain/robotForwardXSpeed", robotForwardXSpeed);
+  92         Tracker.publish("drivetrain/robotStrafeYSpeed", robotStrafeYSpeed);
+  93         drivetrain.sticks(robotForwardXSpeed, robotStrafeYSpeed, turnCcwSpeed);
   94     }
   95
   96     // 'public' to match the parent class. 'public' allows access from any code.
```
