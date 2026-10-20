# L150RobotRelativeButtonOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-remove-buttons.1`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L150

```{code-block} diff
:class: answer-diff

--- mytry/L150RobotRelativeButtonOpMode.java  (before L150)
+++ mytry/L150RobotRelativeButtonOpMode.java  (after L150)
@@ lines 91 to 103 @@
   91         // When on L150S030, see
   92         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l150.html#l150s030
   93         // for what to do here.
-             drivetrain.sticks(robotForward, robotStrafe, turn);
+  94         boolean robotRelative = gamepad1.right_bumper;
+  95         Tracker.publish("drive/robotRelative", robotRelative);
+  96         if (robotRelative) {
+  97             drivetrain.sticks(forward, strafe, turn);
+  98         } else {
+  99             drivetrain.sticks(robotForward, robotStrafe, turn);
+ 100         }
  101
  102         boolean buttonA = gamepad1.a;
  103         Tracker.publish("button/a", buttonA);
```
