# L120EncoderLocalizer

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l140-alliances.2`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L120

```{code-block} diff
:class: answer-diff

--- mytry/L120EncoderLocalizer.java  (before L120)
+++ mytry/L120EncoderLocalizer.java  (after L120)
@@ lines 53 to 80 @@
   53         // When on L120S040, see
   54         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l120.html#l120s040
   55         // for what to do here.
+  56         if (lastWheels_in == null) {
+  57             lastWheels_in = wheels_in.clone();
+  58             lastRawHeading_rad = rawHeading_rad;
+  59             return;
+  60         }
+  61
+  62         double[] delta_in = new double[wheels_in.length];
+  63         for (int i = 0; i < wheels_in.length; i++) {
+  64             delta_in[i] = wheels_in[i] - lastWheels_in[i]; // Your math operation here
+  65         }
+  66
+  67         double forward_in = (delta_in[FL] + delta_in[FR] + delta_in[BL] + delta_in[BR]) / 4.0;
+  68         double strafe_in = (-delta_in[FL] + delta_in[FR] + delta_in[BL] - delta_in[BR]) / 4.0;
   69
   70         // When on L120S050, see
   71         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l120.html#l120s050
   72         // for what to do here.
+  73         double heading_rad = rawHeading_rad + headingOffset_rad;
+  74         double sinHeading = Math.sin(heading_rad);
+  75         double cosHeading = Math.cos(heading_rad);
+  76         x_in += forward_in * cosHeading - strafe_in * sinHeading;
+  77         y_in += forward_in * sinHeading + strafe_in * cosHeading;
   78
   79         lastWheels_in = wheels_in.clone();
   80         lastRawHeading_rad = rawHeading_rad;
```
