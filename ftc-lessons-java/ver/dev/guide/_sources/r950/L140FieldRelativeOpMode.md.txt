# L140FieldRelativeOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-minor-fixes.1`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L140

```{code-block} diff
:class: answer-diff

--- mytry/L140FieldRelativeOpMode.java  (before L140)
+++ mytry/L140FieldRelativeOpMode.java  (after L140)
@@ lines 83 to 94 @@
   83         // When on L140S040, see
   84         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s040
   85         // for what to do here.
-             drivetrain.sticks(fieldXSpeed, fieldYSpeed, turnCcwSpeed);
+  86         double heading = follower.pose().heading();
+  87         double robotForwardXSpeed = fieldXSpeed * Math.cos(heading) + fieldYSpeed * Math.sin(heading);
+  88         double robotStrafeYSpeed = -fieldXSpeed * Math.sin(heading) + fieldYSpeed * Math.cos(heading);
+  89         Tracker.publish("drivetrain/robotForwardXSpeed", robotForwardXSpeed);
+  90         Tracker.publish("drivetrain/robotStrafeYSpeed", robotStrafeYSpeed);
+  91         drivetrain.sticks(robotForwardXSpeed, robotStrafeYSpeed, turnCcwSpeed);
   92     }
   93
   94     // 'public' to match the parent class. 'public' allows access from any code.
```
