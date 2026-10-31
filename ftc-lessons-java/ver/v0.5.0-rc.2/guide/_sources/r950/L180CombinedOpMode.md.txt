# L180CombinedOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111.2`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L180

```{code-block} diff
:class: answer-diff

--- mytry/L180CombinedOpMode.java  (before L180)
+++ mytry/L180CombinedOpMode.java  (after L180)
@@ lines 15 to 21 @@
   15 import org.firstinspires.ftc.teamcode.base.HeadingHold;
   16 import org.firstinspires.ftc.teamcode.base.StickShaper;
   17 import org.firstinspires.ftc.teamcode.base.Tracker;
+  18 import org.firstinspires.ftc.teamcode.base.odometry.HardwareWheelSource;
   19 import org.firstinspires.ftc.teamcode.pedro.Constants;
   20
   21 /**
@@ lines 112 to 120 @@
  112         // When on L180S030, see
  113         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l180.html#l180s030
  114         // for what to do here.
+ 115         HardwareWheelSource wheelSource = new HardwareWheelSource(hardware);
+ 116         L113EncoderLocalizer driveWheelEncoders = new L113EncoderLocalizer(wheelSource);
+ 117         shadowLocalizers.add("driveWheelEncoders", driveWheelEncoders);
  118     }
  119
  120     // 'public' to match the parent class. 'public' allows access from any code.
```
