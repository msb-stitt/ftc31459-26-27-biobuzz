# L120PinpointOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111.1`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L120

```{code-block} diff
:class: answer-diff

--- mytry/L120PinpointOpMode.java  (before L120)
+++ mytry/L120PinpointOpMode.java  (after L120)
@@ lines 10 to 18 @@
   10 import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
   11 import org.firstinspires.ftc.teamcode.base.StickShaper;
   12 import org.firstinspires.ftc.teamcode.base.Tracker;
+  13 import org.firstinspires.ftc.teamcode.base.odometry.HardwarePinpointSource;
+  14 import org.firstinspires.ftc.teamcode.base.odometry.PinpointSource;
+  15 import org.firstinspires.ftc.teamcode.pedro.Constants;
   16
   17 /**
   18  * L120: drive around with your own Pinpoint localizer running beside the robot's. Yours
@@ lines 95 to 104 @@
   95         // When on L120S030, see
   96         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l120.html#l120s030
   97         // for what to do here.
+  98         PinpointSource pinpointSource = HardwarePinpointSource.find(hardwareMap);
+  99         L120PinpointLocalizer pinpointPods =
+ 100                 new L120PinpointLocalizer(pinpointSource, Constants.localizerConfig);
+ 101         shadowLocalizers.add("pinpointPods", pinpointPods);
  102     }
  103
  104     // 'public' to match the parent class. 'public' allows access from any code.
```
