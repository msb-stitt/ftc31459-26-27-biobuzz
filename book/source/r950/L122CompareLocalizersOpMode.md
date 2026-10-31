# L122CompareLocalizersOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L122

```{code-block} diff
:class: answer-diff

--- mytry/L122CompareLocalizersOpMode.java  (before L122)
+++ mytry/L122CompareLocalizersOpMode.java  (after L122)
@@ lines 10 to 19 @@
   10 import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
   11 import org.firstinspires.ftc.teamcode.base.StickShaper;
   12 import org.firstinspires.ftc.teamcode.base.Tracker;
+  13 import org.firstinspires.ftc.teamcode.base.odometry.HardwarePinpointSource;
+  14 import org.firstinspires.ftc.teamcode.base.odometry.HardwareWheelSource;
+  15 import org.firstinspires.ftc.teamcode.base.odometry.PinpointSource;
+  16 import org.firstinspires.ftc.teamcode.pedro.Constants;
   17
   18 /**
   19  * L122: drive around while three localizers disagree. Your localizer from L113
@@ lines 98 to 111 @@
   98         // When on L122S030, see
   99         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l122.html#l122s030
  100         // for what to do here.
+ 101         HardwareWheelSource wheelSource = new HardwareWheelSource(hardware);
+ 102         L113EncoderLocalizer driveWheelEncoders = new L113EncoderLocalizer(wheelSource);
+ 103         shadowLocalizers.add("driveWheelEncoders", driveWheelEncoders);
+ 104
+ 105         PinpointSource pinpointSource = HardwarePinpointSource.find(hardwareMap);
+ 106         L120PinpointLocalizer pinpointPods =
+ 107                 new L120PinpointLocalizer(pinpointSource, Constants.localizerConfig);
+ 108         shadowLocalizers.add("pinpointPods", pinpointPods);
  109     }
  110
  111     // 'public' to match the parent class. 'public' allows access from any code.
```
