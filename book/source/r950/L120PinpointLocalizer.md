# L120PinpointLocalizer

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L120

```{code-block} diff
:class: answer-diff

--- mytry/L120PinpointLocalizer.java  (before L120)
+++ mytry/L120PinpointLocalizer.java  (after L120)
@@ lines 68 to 82 @@
   68         // When on L120S040, see
   69         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l120.html#l120s040
   70         // for what to do here.
+  71         ticksPerInch = PinpointSource.ticksPerMm(config.podType.get()) * 25.4;
+  72         if (config.xPodDirection.get() == GoBildaPinpointDriver.EncoderDirection.REVERSED) {
+  73             xSign = -1.0;
+  74         }
+  75         if (config.yPodDirection.get() == GoBildaPinpointDriver.EncoderDirection.REVERSED) {
+  76             ySign = -1.0;
+  77         }
+  78         xPodOffset_in = config.xPodOffset.get();
+  79         yPodOffset_in = config.yPodOffset.get();
   80     }
   81
   82     // 'public' to match Pedro's Localizer interface. 'public' allows access from any code.
@@ lines 91 to 134 @@
   91         // When on L120S040, see
   92         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l120.html#l120s040
   93         // for what to do here.
+  94         forwardPod_in = xSign * source.getEncoderX() / ticksPerInch;
+  95         strafePod_in = ySign * source.getEncoderY() / ticksPerInch;
+  96
+  97         if (firstLoop) {
+  98             firstLoop = false;
+  99             lastForwardPod_in = forwardPod_in;
+ 100             lastStrafePod_in = strafePod_in;
+ 101             lastRawHeading_rad = rawHeading_rad;
+ 102             lastReadTime_s = readTime_s;
+ 103             return;
+ 104         }
+ 105
+ 106         double forwardPodStep_in = forwardPod_in - lastForwardPod_in;
+ 107         double strafePodStep_in = strafePod_in - lastStrafePod_in;
+ 108         double turn_rad = rawHeading_rad - lastRawHeading_rad;
  109
  110         // When on L120S050, see
  111         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l120.html#l120s050
  112         // for what to do here.
+ 113         double forward_in = forwardPodStep_in + xPodOffset_in * turn_rad;
+ 114         double strafe_in = strafePodStep_in - yPodOffset_in * turn_rad;
  115
  116         // When on L120S060, see
  117         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l120.html#l120s060
  118         // for what to do here.
+ 119         double heading_rad = rawHeading_rad + headingOffset_rad;
+ 120         double sinHeading = Math.sin(heading_rad);
+ 121         double cosHeading = Math.cos(heading_rad);
+ 122         x_in += forward_in * cosHeading - strafe_in * sinHeading;
+ 123         y_in += forward_in * sinHeading + strafe_in * cosHeading;
  124
  125         // When on L120S070, see
  126         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l120.html#l120s070
  127         // for what to do here.
+ 128         double period_s = readTime_s - lastReadTime_s;
+ 129         forwardSpeed_ips = forward_in / period_s;
+ 130         strafeSpeed_ips = strafe_in / period_s;
+ 131         turnSpeed_radps = turn_rad / period_s;
  132
  133         lastForwardPod_in = forwardPod_in;
  134         lastStrafePod_in = strafePod_in;
```
