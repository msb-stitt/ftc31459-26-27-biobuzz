# L065ShaperOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111.1`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L065

```{code-block} diff
:class: answer-diff

--- mytry/l065/L065ShaperOpMode.java  (before L065)
+++ mytry/l065/L065ShaperOpMode.java  (after L065)
@@ lines 35 to 41 @@
   35     // When on L065S060, see
   36     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l065.html#l065s060
   37     // for what to do here.
+  38     StickShaper stickShaper = new StickShaper(0.05);
   39
   40     // 'public' to match the parent class. 'public' allows access from any code.
   41     @Override
@@ lines 85 to 100 @@
   85         // When on L065S070, see
   86         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l065.html#l065s070
   87         // for what to do here.
+  88         double leftShapedSpeed = stickShaper.shaped(leftSpeed);
+  89         Tracker.publish("drivetrain/leftShapedSpeed", leftShapedSpeed);
+  90         double rightShapedSpeed = stickShaper.shaped(rightSpeed);
+  91         Tracker.publish("drivetrain/rightShapedSpeed", rightShapedSpeed);
   92
-             hardware.frontLeft.setPower(leftSpeed);
-             hardware.backLeft.setPower(leftSpeed);
+  93         hardware.frontLeft.setPower(leftShapedSpeed);
+  94         hardware.backLeft.setPower(leftShapedSpeed);
   95
-             hardware.frontRight.setPower(rightSpeed);
-             hardware.backRight.setPower(rightSpeed);
+  96         hardware.frontRight.setPower(rightShapedSpeed);
+  97         hardware.backRight.setPower(rightShapedSpeed);
   98     }
   99
  100     // 'public' to match the parent class. 'public' allows access from any code.
```
