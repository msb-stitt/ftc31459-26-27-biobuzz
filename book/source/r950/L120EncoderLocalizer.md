# L120EncoderLocalizer

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-remove-buttons`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L120

```{code-block} diff
:class: answer-diff

--- mytry/L120EncoderLocalizer.java  (before L120)
+++ mytry/L120EncoderLocalizer.java  (after L120)
@@ lines 51 to 76 @@
   51         // When on L120S040, see
   52         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l120.html#l120s040
   53         // for what to do here.
+  54         if (lastWheels == null) {
+  55             lastWheels = wheels.clone();
+  56             lastRawHeading = rawHeading;
+  57             return;
+  58         }
+  59
+  60         double frontLeft = wheels[0] - lastWheels[0];
+  61         double frontRight = wheels[1] - lastWheels[1];
+  62         double backLeft = wheels[2] - lastWheels[2];
+  63         double backRight = wheels[3] - lastWheels[3];
+  64
+  65         double forward = (frontLeft + frontRight + backLeft + backRight) / 4.0;
+  66         double strafe = (-frontLeft + frontRight + backLeft - backRight) / 4.0;
   67
   68         // When on L120S050, see
   69         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l120.html#l120s050
   70         // for what to do here.
+  71         double heading = rawHeading + headingOffset;
+  72         x += forward * Math.cos(heading) - strafe * Math.sin(heading);
+  73         y += forward * Math.sin(heading) + strafe * Math.cos(heading);
   74
   75         lastWheels = wheels.clone();
   76         lastRawHeading = rawHeading;
```
