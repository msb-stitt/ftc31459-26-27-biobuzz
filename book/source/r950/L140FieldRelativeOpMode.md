# L140FieldRelativeOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-auto-schedules.1`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L140

```{code-block} diff
:class: answer-diff

--- mytry/L140FieldRelativeOpMode.java  (before L140)
+++ mytry/L140FieldRelativeOpMode.java  (after L140)
@@ lines 88 to 97 @@
   88         // When on L140S040, see
   89         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l140.html#l140s040
   90         // for what to do here.
-             drivetrain.sticks(forward, strafe, turn);
+  91         double heading = follower.pose().heading();
+  92         double robotForward = forward * Math.cos(heading) + strafe * Math.sin(heading);
+  93         double robotStrafe = -forward * Math.sin(heading) + strafe * Math.cos(heading);
+  94         drivetrain.sticks(robotForward, robotStrafe, turn);
   95
   96         boolean buttonA = gamepad1.a;
   97         Tracker.publish("button/a", buttonA);
```
