# L110FollowerOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-coding-stitt-cc`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L110

```{code-block} diff
:class: answer-diff

--- mytry/L110FollowerOpMode.java  (before L110)
+++ mytry/L110FollowerOpMode.java  (after L110)
@@ lines 36 to 42 @@
   36     // When on L110S040, see
   37     // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l110.html#l110s040
   38     // for what to do here.
+  39     L110FollowerDriveTrain drivetrain;
   40
   41     // 'public' to match the parent class. 'public' allows access from any code.
   42     @Override
@@ lines 46 to 54 @@
   46         // When on L110S050, see
   47         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l110.html#l110s050
   48         // for what to do here.
+  49         drivetrain = new L110FollowerDriveTrain(hardware);
   50
-             initAfter();
+  51         initAfter(drivetrain);
   52     }
   53
   54     // 'public' to match the parent class. 'public' allows access from any code.
@@ lines 94 to 100 @@
   94         // When on L110S060, see
   95         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l110.html#l110s060
   96         // for what to do here.
+  97         drivetrain.sticks(forward, strafe, turn);
   98
   99         boolean buttonA = gamepad1.a;
  100         Tracker.publish("button/a", buttonA);
```
