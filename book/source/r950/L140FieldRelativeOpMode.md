# L140FieldRelativeOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-mlt-fable`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L140

```{code-block} diff
:class: answer-diff

--- mytry/L140FieldRelativeOpMode.java  (before L140)
+++ mytry/L140FieldRelativeOpMode.java  (after L140)
@@ lines 77 to 86 @@
   77         // When on L140S040, see
   78         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l140.html#l140s040
   79         // for what to do here.
-             drivetrain.sticks(forward, strafe, turn);
+  80         double heading = follower.pose().heading();
+  81         double robotForward = forward * Math.cos(heading) + strafe * Math.sin(heading);
+  82         double robotStrafe = -forward * Math.sin(heading) + strafe * Math.cos(heading);
+  83         drivetrain.sticks(robotForward, robotStrafe, turn);
   84
   85         boolean buttonA = gamepad1.a;
   86         Tracker.publish("button/a", buttonA);
```
