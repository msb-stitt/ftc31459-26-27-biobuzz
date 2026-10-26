# L090MecanumOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-minor-fixes`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L090

```{code-block} diff
:class: answer-diff

--- mytry/L090MecanumOpMode.java  (before L090)
+++ mytry/L090MecanumOpMode.java  (after L090)
@@ lines 81 to 118 @@
   81         // When on L090S030, see
   82         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l090.html#l090s030
   83         // for what to do here.
+  84         double strafe = deadband(-leftStickX, band);
+  85         strafe = signedSquared(strafe);
+  86         Tracker.publish("arcade/strafe", strafe);
   87
   88         // When on L090S040, see
   89         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l090.html#l090s040
   90         // for what to do here.
+  91         double fl_mixedSpeed = forward - strafe - turn;
+  92         double fr_mixedSpeed = forward + strafe + turn;
+  93         double bl_mixedSpeed = forward + strafe - turn;
+  94         double br_mixedSpeed = forward - strafe + turn;
+  95         double fl_size = Math.abs(fl_mixedSpeed);
+  96         double fr_size = Math.abs(fr_mixedSpeed);
+  97         double bl_size = Math.abs(bl_mixedSpeed);
+  98         double br_size = Math.abs(br_mixedSpeed);
+  99         double biggest = Math.max(fl_size, fr_size);
+ 100         biggest = Math.max(biggest, bl_size);
+ 101         biggest = Math.max(biggest, br_size);
+ 102         double biggestOrOne = Math.max(1.0, biggest);
+ 103         double fl_speed = fl_mixedSpeed / biggestOrOne;
+ 104         double fr_speed = fr_mixedSpeed / biggestOrOne;
+ 105         double bl_speed = bl_mixedSpeed / biggestOrOne;
+ 106         double br_speed = br_mixedSpeed / biggestOrOne;
+ 107         Tracker.publish("drivetrain/fl_speed", fl_speed);
+ 108         Tracker.publish("drivetrain/fr_speed", fr_speed);
+ 109         Tracker.publish("drivetrain/bl_speed", bl_speed);
+ 110         Tracker.publish("drivetrain/br_speed", br_speed);
+ 111
+ 112         hardware.frontLeft.setPower(fl_speed);
+ 113         hardware.frontRight.setPower(fr_speed);
+ 114         hardware.backLeft.setPower(bl_speed);
+ 115         hardware.backRight.setPower(br_speed);
  116     }
  117
  118     // 'public' to match the parent class. 'public' allows access from any code.
```
