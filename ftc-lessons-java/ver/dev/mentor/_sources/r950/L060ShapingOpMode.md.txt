# L060ShapingOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-l135-menu-classes.2`. A line marked + is new, and a line marked - is
gone; each number is the line's place in the file after the lesson.

## L060

```{code-block} diff
:class: answer-diff

--- mytry/L060ShapingOpMode.java  (before L060)
+++ mytry/L060ShapingOpMode.java  (after L060)
@@ lines 75 to 93 @@
   75         // When on L060S040, see
   76         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l060.html#l060s040
   77         // for what to do here.
+  78         double bandToSetToZero = 0.05;
+  79         double leftShapedSpeed = deadband(leftSpeed, bandToSetToZero);
+  80         leftShapedSpeed = signedSquared(leftShapedSpeed);
+  81         Tracker.publish("drivetrain/leftShapedSpeed", leftShapedSpeed);
+  82         double rightShapedSpeed = deadband(rightSpeed, bandToSetToZero);
+  83         rightShapedSpeed = signedSquared(rightShapedSpeed);
+  84         Tracker.publish("drivetrain/rightShapedSpeed", rightShapedSpeed);
   85
-             hardware.frontLeft.setPower(leftSpeed);
-             hardware.backLeft.setPower(leftSpeed);
+  86         hardware.frontLeft.setPower(leftShapedSpeed);
+  87         hardware.backLeft.setPower(leftShapedSpeed);
   88
-             hardware.frontRight.setPower(rightSpeed);
-             hardware.backRight.setPower(rightSpeed);
+  89         hardware.frontRight.setPower(rightShapedSpeed);
+  90         hardware.backRight.setPower(rightShapedSpeed);
   91     }
   92
   93     // 'public' to match the parent class. 'public' allows access from any code.
@@ lines 99 to 119 @@
   99     // When on L060S030, see
  100     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l060.html#l060s030
  101     // for what to do here.
+ 102     double deadband(double value, double band) {
+ 103         double alwaysPositiveMagnitude = Math.abs(value);
+ 104         boolean isCloseToZero = alwaysPositiveMagnitude < band;
+ 105         if (isCloseToZero) {
+ 106             return 0.0;
+ 107         }
+ 108         return value;
+ 109     }
  110
  111     // When on L060S050, see
  112     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l060.html#l060s050
  113     // for what to do here.
+ 114     double signedSquared(double value) {
+ 115         double alwaysPositiveValue = Math.abs(value);
+ 116         double valueSquaredKeepingTheSign = value * alwaysPositiveValue;
+ 117         return valueSquaredKeepingTheSign;
+ 118     }
  119 }
```
