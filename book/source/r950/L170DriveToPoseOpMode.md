# L170DriveToPoseOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-auto-schedules.1`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L170

```{code-block} diff
:class: answer-diff

--- mytry/L170DriveToPoseOpMode.java  (before L170)
+++ mytry/L170DriveToPoseOpMode.java  (after L170)
@@ lines 83 to 94 @@
   83         // When on L170S030, see
   84         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l170.html#l170s030
   85         // for what to do here.
+  86         boolean buttonY = gamepad1.y;
+  87         boolean buttonYJustPressed = buttonY && !previousButtonY;
+  88         if (buttonYJustPressed) {
+  89             Scheduler.schedule(driveToTheTarget);
+  90         }
+  91         previousButtonY = buttonY;
   92
   93         loopAfter();
   94     }
@@ lines 158 to 165 @@
  158         // When on L170S040, see
  159         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l170.html#l170s040
  160         // for what to do here.
+ 161         follower.hold(target);
+ 162         drivetrain.releaseCommandedWheels();
  163     }
  164
  165     /** Each loop of the drive to the target: the follower drives, not the sticks. */
```
