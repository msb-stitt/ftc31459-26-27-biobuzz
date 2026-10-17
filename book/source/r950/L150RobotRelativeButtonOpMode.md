# L150RobotRelativeButtonOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-mlt-fable`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L150

```{code-block} diff
:class: answer-diff

--- mytry/L150RobotRelativeButtonOpMode.java  (before L150)
+++ mytry/L150RobotRelativeButtonOpMode.java  (after L150)
@@ lines 80 to 92 @@
   80         // When on L150S030, see
   81         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l150.html#l150s030
   82         // for what to do here.
-             drivetrain.sticks(robotForward, robotStrafe, turn);
+  83         boolean robotRelative = gamepad1.right_bumper;
+  84         Tracker.publish("drive/robotRelative", robotRelative);
+  85         if (robotRelative) {
+  86             drivetrain.sticks(forward, strafe, turn);
+  87         } else {
+  88             drivetrain.sticks(robotForward, robotStrafe, turn);
+  89         }
   90
   91         boolean buttonA = gamepad1.a;
   92         Tracker.publish("button/a", buttonA);
```
