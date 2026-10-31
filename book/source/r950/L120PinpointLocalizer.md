# L120PinpointLocalizer

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111.1`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L120

```{code-block} diff
:class: answer-diff

--- mytry/L120PinpointLocalizer.java  (before L120)
+++ mytry/L120PinpointLocalizer.java  (after L120)
@@ lines 61 to 75 @@
   61         // When on L120S040, see
   62         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l120.html#l120s040
   63         // for what to do here.
+  64         ticksPerInch = PinpointSource.ticksPerMm(config.podType.get()) * 25.4;
+  65         if (config.xPodDirection.get() == GoBildaPinpointDriver.EncoderDirection.REVERSED) {
+  66             xSign = -1.0;
+  67         }
+  68         if (config.yPodDirection.get() == GoBildaPinpointDriver.EncoderDirection.REVERSED) {
+  69             ySign = -1.0;
+  70         }
+  71         xPodOffset_in = config.xPodOffset.get();
+  72         yPodOffset_in = config.yPodOffset.get();
   73     }
   74
   75     // 'public' to match Pedro's Localizer interface. 'public' allows access from any code.
@@ lines 83 to 117 @@
   83         // When on L120S040, see
   84         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l120.html#l120s040
   85         // for what to do here.
+  86         forwardPod_in = xSign * source.getEncoderX() / ticksPerInch;
+  87         strafePod_in = ySign * source.getEncoderY() / ticksPerInch;
+  88
+  89         if (firstLoop) {
+  90             firstLoop = false;
+  91             lastForwardPod_in = forwardPod_in;
+  92             lastStrafePod_in = strafePod_in;
+  93             lastRawHeading_rad = rawHeading_rad;
+  94             return;
+  95         }
+  96
+  97         double forwardPodStep_in = forwardPod_in - lastForwardPod_in;
+  98         double strafePodStep_in = strafePod_in - lastStrafePod_in;
+  99         double turn_rad = rawHeading_rad - lastRawHeading_rad;
  100
  101         // When on L120S050, see
  102         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l120.html#l120s050
  103         // for what to do here.
+ 104         double forward_in = forwardPodStep_in + xPodOffset_in * turn_rad;
+ 105         double strafe_in = strafePodStep_in - yPodOffset_in * turn_rad;
  106
  107         // When on L120S060, see
  108         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l120.html#l120s060
  109         // for what to do here.
+ 110         double heading_rad = rawHeading_rad + headingOffset_rad;
+ 111         double sinHeading = Math.sin(heading_rad);
+ 112         double cosHeading = Math.cos(heading_rad);
+ 113         x_in += forward_in * cosHeading - strafe_in * sinHeading;
+ 114         y_in += forward_in * sinHeading + strafe_in * cosHeading;
  115
  116         lastForwardPod_in = forwardPod_in;
  117         lastStrafePod_in = strafePod_in;
```
