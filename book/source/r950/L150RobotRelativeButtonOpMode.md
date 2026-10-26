# L150RobotRelativeButtonOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-minor-fixes`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L150

```{code-block} diff
:class: answer-diff

--- mytry/L150RobotRelativeButtonOpMode.java  (before L150)
+++ mytry/L150RobotRelativeButtonOpMode.java  (after L150)
@@ lines 86 to 98 @@
   86         // When on L150S030, see
   87         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l150.html#l150s030
   88         // for what to do here.
-             drivetrain.sticks(robotForward, robotStrafe, turn);
+  89         boolean robotRelative = gamepad1.right_bumper;
+  90         Tracker.publish("drive/robotRelative", robotRelative);
+  91         if (robotRelative) {
+  92             drivetrain.sticks(forward, strafe, turn);
+  93         } else {
+  94             drivetrain.sticks(robotForward, robotStrafe, turn);
+  95         }
   96     }
   97
   98     // 'public' to match the parent class. 'public' allows access from any code.
```
