# L140FieldRelativeOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-minor-fixes`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L140

```{code-block} diff
:class: answer-diff

--- mytry/L140FieldRelativeOpMode.java  (before L140)
+++ mytry/L140FieldRelativeOpMode.java  (after L140)
@@ lines 83 to 92 @@
   83         // When on L140S040, see
   84         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s040
   85         // for what to do here.
-             drivetrain.sticks(forward, strafe, turn);
+  86         double heading = follower.pose().heading();
+  87         double robotForward = forward * Math.cos(heading) + strafe * Math.sin(heading);
+  88         double robotStrafe = -forward * Math.sin(heading) + strafe * Math.cos(heading);
+  89         drivetrain.sticks(robotForward, robotStrafe, turn);
   90     }
   91
   92     // 'public' to match the parent class. 'public' allows access from any code.
```
