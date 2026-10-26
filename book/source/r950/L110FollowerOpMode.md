# L110FollowerOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-minor-fixes`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L110

```{code-block} diff
:class: answer-diff

--- mytry/L110FollowerOpMode.java  (before L110)
+++ mytry/L110FollowerOpMode.java  (after L110)
@@ lines 34 to 40 @@
   34     // When on L110S040, see
   35     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l110.html#l110s040
   36     // for what to do here.
+  37     L110FollowerDriveTrain drivetrain;
   38
   39     // 'public' to match the parent class. 'public' allows access from any code.
   40     @Override
@@ lines 44 to 52 @@
   44         // When on L110S050, see
   45         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l110.html#l110s050
   46         // for what to do here.
+  47         drivetrain = new L110FollowerDriveTrain(hardware);
   48
-             initAfter();
+  49         initAfter(drivetrain);
   50     }
   51
   52     // 'public' to match the parent class. 'public' allows access from any code.
@@ lines 89 to 95 @@
   89         // When on L110S060, see
   90         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l110.html#l110s060
   91         // for what to do here.
+  92         drivetrain.sticks(forwardXSpeed, strafeYSpeed, turnCcwSpeed);
   93     }
   94
   95     // 'public' to match the parent class. 'public' allows access from any code.
```
