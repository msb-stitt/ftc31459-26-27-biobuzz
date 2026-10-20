# L110FollowerOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-mlt-fable.1`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L110

```{code-block} diff
:class: answer-diff

--- mytry/L110FollowerOpMode.java  (before L110)
+++ mytry/L110FollowerOpMode.java  (after L110)
@@ lines 31 to 37 @@
   31     // When on L110S040, see
   32     // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l110.html#l110s040
   33     // for what to do here.
+  34     L110FollowerDriveTrain drivetrain;
   35
   36     // 'public' to match the parent class. 'public' allows access from any code.
   37     @Override
@@ lines 41 to 49 @@
   41         // When on L110S050, see
   42         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l110.html#l110s050
   43         // for what to do here.
+  44         drivetrain = new L110FollowerDriveTrain(hardware);
   45
-             initAfter();
+  46         initAfter(drivetrain);
   47     }
   48
   49     // 'public' to match the parent class. 'public' allows access from any code.
@@ lines 83 to 89 @@
   83         // When on L110S060, see
   84         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l110.html#l110s060
   85         // for what to do here.
+  86         drivetrain.sticks(forward, strafe, turn);
   87
   88         boolean buttonA = gamepad1.a;
   89         Tracker.publish("button/a", buttonA);
```
