# L110FollowerOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111.1`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L110

```{code-block} diff
:class: answer-diff

--- mytry/L110FollowerOpMode.java  (before L110)
+++ mytry/L110FollowerOpMode.java  (after L110)
@@ lines 11 to 17 @@
   11 // When on L110S020, see
   12 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l110.html#l110s020
   13 // for what to do here.
-     //import org.firstinspires.ftc.teamcode.mytry.l065.StickShaper;
+  14 import org.firstinspires.ftc.teamcode.mytry.l065.StickShaper;
   15
   16 /**
   17  * L110: hand the wheels to the path follower. The robot drives on the sticks
@@ lines 38 to 49 @@
   38     // When on L110S020, see
   39     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l110.html#l110s020
   40     // for what to do here.
-     //    StickShaper stickShaper = new StickShaper(0.05);
+  41     StickShaper stickShaper = new StickShaper(0.05);
   42
   43     // When on L110S040, see
   44     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l110.html#l110s040
   45     // for what to do here.
+  46     L110FollowerDriveTrain drivetrain;
   47
   48     // 'public' to match the parent class. 'public' allows access from any code.
   49     @Override
@@ lines 53 to 61 @@
   53         // When on L110S050, see
   54         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l110.html#l110s050
   55         // for what to do here.
+  56         drivetrain = new L110FollowerDriveTrain(hardware);
   57
-             initAfter();
+  58         initAfter(drivetrain);
   59     }
   60
   61     // 'public' to match the parent class. 'public' allows access from any code.
@@ lines 87 to 112 @@
   87         // When on L110S020, see
   88         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l110.html#l110s020
   89         // for what to do here.
-     //        forwardXSpeed = stickShaper.shaped(forwardXSpeed);
+  90         forwardXSpeed = stickShaper.shaped(forwardXSpeed);
   91         Tracker.publish("drivetrain/forwardXSpeed", forwardXSpeed);
   92         double turnCcwSpeed = -rightStickX;
   93         // When on L110S020, see
   94         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l110.html#l110s020
   95         // for what to do here.
-     //        turnCcwSpeed = stickShaper.shaped(turnCcwSpeed);
+  96         turnCcwSpeed = stickShaper.shaped(turnCcwSpeed);
   97         Tracker.publish("drivetrain/turnCcwSpeed", turnCcwSpeed);
   98
   99         double strafeYSpeed = -leftStickX;
  100         // When on L110S020, see
  101         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l110.html#l110s020
  102         // for what to do here.
-     //        strafeYSpeed = stickShaper.shaped(strafeYSpeed);
+ 103         strafeYSpeed = stickShaper.shaped(strafeYSpeed);
  104         Tracker.publish("drivetrain/strafeYSpeed", strafeYSpeed);
  105
  106         // When on L110S060, see
  107         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l110.html#l110s060
  108         // for what to do here.
+ 109         drivetrain.sticks(forwardXSpeed, strafeYSpeed, turnCcwSpeed);
  110     }
  111
  112     // 'public' to match the parent class. 'public' allows access from any code.
```
