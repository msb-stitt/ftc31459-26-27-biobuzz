# L170DriveToPoseOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111.1`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L170

```{code-block} diff
:class: answer-diff

--- mytry/L170DriveToPoseOpMode.java  (before L170)
+++ mytry/L170DriveToPoseOpMode.java  (after L170)
@@ lines 85 to 96 @@
   85         // When on L170S030, see
   86         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l170.html#l170s030
   87         // for what to do here.
+  88         boolean buttonY = gamepad1.y;
+  89         boolean buttonYJustPressed = buttonY && !previousButtonY;
+  90         if (buttonYJustPressed) {
+  91             Scheduler.schedule(driveToTheTarget);
+  92         }
+  93         previousButtonY = buttonY;
   94
   95         loopAfter();
   96     }
@@ lines 154 to 161 @@
  154         // When on L170S040, see
  155         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l170.html#l170s040
  156         // for what to do here.
+ 157         follower.hold(target);
+ 158         drivetrain.releaseCommandedWheels();
  159     }
  160
  161     /** Each loop of the drive to the target: the follower drives, not the sticks. */
```
