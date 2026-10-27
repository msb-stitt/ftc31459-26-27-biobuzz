# L170DriveToPoseOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-minor-fixes`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L170

```{code-block} diff
:class: answer-diff

--- mytry/L170DriveToPoseOpMode.java  (before L170)
+++ mytry/L170DriveToPoseOpMode.java  (after L170)
@@ lines 82 to 93 @@
   82         // When on L170S030, see
   83         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l170.html#l170s030
   84         // for what to do here.
+  85         boolean buttonY = gamepad1.y;
+  86         boolean buttonYJustPressed = buttonY && !previousButtonY;
+  87         if (buttonYJustPressed) {
+  88             Scheduler.schedule(driveToTheTarget);
+  89         }
+  90         previousButtonY = buttonY;
   91
   92         loopAfter();
   93     }
@@ lines 150 to 157 @@
  150         // When on L170S040, see
  151         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l170.html#l170s040
  152         // for what to do here.
+ 153         follower.hold(target);
+ 154         drivetrain.releaseCommandedWheels();
  155     }
  156
  157     /** Each loop of the drive to the target: the follower drives, not the sticks. */
```
