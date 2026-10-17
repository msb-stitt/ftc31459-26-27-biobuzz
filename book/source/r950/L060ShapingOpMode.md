# L060ShapingOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-mlt-fable`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L060

```{code-block} diff
:class: answer-diff

--- mytry/L060ShapingOpMode.java  (before L060)
+++ mytry/L060ShapingOpMode.java  (after L060)
@@ lines 64 to 82 @@
   64         // When on L060S040, see
   65         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l060.html#l060s040
   66         // for what to do here.
+  67         double band = 0.05;
+  68         double leftPower = deadband(leftSpeed, band);
+  69         leftPower = signedSquared(leftPower);
+  70         Tracker.publish("power/left", leftPower);
+  71         double rightPower = deadband(rightSpeed, band);
+  72         rightPower = signedSquared(rightPower);
+  73         Tracker.publish("power/right", rightPower);
   74
-             hardware.frontLeft.setPower(leftSpeed);
-             hardware.backLeft.setPower(leftSpeed);
+  75         hardware.frontLeft.setPower(leftPower);
+  76         hardware.backLeft.setPower(leftPower);
   77
-             hardware.frontRight.setPower(rightSpeed);
-             hardware.backRight.setPower(rightSpeed);
+  78         hardware.frontRight.setPower(rightPower);
+  79         hardware.backRight.setPower(rightPower);
   80
   81         loopAfter();
   82     }
@@ lines 90 to 108 @@
   90     // When on L060S030, see
   91     // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l060.html#l060s030
   92     // for what to do here.
+  93     double deadband(double value, double band) {
+  94         double alwaysPositiveMagnitude = Math.abs(value);
+  95         boolean isCloseToZero = alwaysPositiveMagnitude < band;
+  96         if (isCloseToZero) {
+  97             return 0.0;
+  98         }
+  99         return value;
+ 100     }
  101
  102     // When on L060S050, see
  103     // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l060.html#l060s050
  104     // for what to do here.
+ 105     double signedSquared(double value) {
+ 106         return value * Math.abs(value);
+ 107     }
  108 }
```
