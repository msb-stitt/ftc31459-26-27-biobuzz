# L090MecanumOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L090

```{code-block} diff
:class: answer-diff

--- mytry/L090MecanumOpMode.java  (before L090)
+++ mytry/L090MecanumOpMode.java  (after L090)
@@ lines 11 to 17 @@
   11 // When on L090S020, see
   12 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l090.html#l090s020
   13 // for what to do here.
-     //import org.firstinspires.ftc.teamcode.mytry.l065.StickShaper;
+  14 import org.firstinspires.ftc.teamcode.mytry.l065.StickShaper;
   15
   16 /**
   17  * L090: mecanum drive. The left stick drives and slides, the right stick
@@ lines 39 to 45 @@
   39     // When on L090S020, see
   40     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l090.html#l090s020
   41     // for what to do here.
-     //    StickShaper stickShaper = new StickShaper(0.05);
+  42     StickShaper stickShaper = new StickShaper(0.05);
   43
   44     // 'public' to match the parent class. 'public' allows access from any code.
   45     @Override
@@ lines 83 to 135 @@
   83         // When on L090S020, see
   84         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l090.html#l090s020
   85         // for what to do here.
-     //        forwardXSpeed = stickShaper.shaped(forwardXSpeed);
+  86         forwardXSpeed = stickShaper.shaped(forwardXSpeed);
   87         Tracker.publish("drivetrain/forwardXSpeed", forwardXSpeed);
   88         double turnCcwSpeed = -rightStickX;
   89         // When on L090S020, see
   90         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l090.html#l090s020
   91         // for what to do here.
-     //        turnCcwSpeed = stickShaper.shaped(turnCcwSpeed);
+  92         turnCcwSpeed = stickShaper.shaped(turnCcwSpeed);
   93         Tracker.publish("drivetrain/turnCcwSpeed", turnCcwSpeed);
   94
   95         // When on L090S030, see
   96         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l090.html#l090s030
   97         // for what to do here.
+  98         double strafeYSpeed = -leftStickX;
+  99         // When on L090S020, see
+ 100         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l090.html#l090s020
+ 101         // for what to do here.
+ 102         strafeYSpeed = stickShaper.shaped(strafeYSpeed);
+ 103         Tracker.publish("drivetrain/strafeYSpeed", strafeYSpeed);
  104
  105         // When on L090S040, see
  106         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l090.html#l090s040
  107         // for what to do here.
+ 108         double fl_mixedSpeed = forwardXSpeed - strafeYSpeed - turnCcwSpeed;
+ 109         double fr_mixedSpeed = forwardXSpeed + strafeYSpeed + turnCcwSpeed;
+ 110         double bl_mixedSpeed = forwardXSpeed + strafeYSpeed - turnCcwSpeed;
+ 111         double br_mixedSpeed = forwardXSpeed - strafeYSpeed + turnCcwSpeed;
+ 112         double fl_size = Math.abs(fl_mixedSpeed);
+ 113         double fr_size = Math.abs(fr_mixedSpeed);
+ 114         double bl_size = Math.abs(bl_mixedSpeed);
+ 115         double br_size = Math.abs(br_mixedSpeed);
+ 116         double biggest = Math.max(fl_size, fr_size);
+ 117         biggest = Math.max(biggest, bl_size);
+ 118         biggest = Math.max(biggest, br_size);
+ 119         double biggestOrOne = Math.max(1.0, biggest);
+ 120         double fl_speed = fl_mixedSpeed / biggestOrOne;
+ 121         double fr_speed = fr_mixedSpeed / biggestOrOne;
+ 122         double bl_speed = bl_mixedSpeed / biggestOrOne;
+ 123         double br_speed = br_mixedSpeed / biggestOrOne;
+ 124         Tracker.publish("drivetrain/fl_speed", fl_speed);
+ 125         Tracker.publish("drivetrain/fr_speed", fr_speed);
+ 126         Tracker.publish("drivetrain/bl_speed", bl_speed);
+ 127         Tracker.publish("drivetrain/br_speed", br_speed);
+ 128
+ 129         hardware.frontLeft.setPower(fl_speed);
+ 130         hardware.frontRight.setPower(fr_speed);
+ 131         hardware.backLeft.setPower(bl_speed);
+ 132         hardware.backRight.setPower(br_speed);
  133     }
  134
  135     // 'public' to match the parent class. 'public' allows access from any code.
```
