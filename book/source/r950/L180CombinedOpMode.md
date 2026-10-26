# L180CombinedOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-minor-fixes`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L180

```{code-block} diff
:class: answer-diff

--- mytry/L180CombinedOpMode.java  (before L180)
+++ mytry/L180CombinedOpMode.java  (after L180)
@@ lines 14 to 20 @@
   14 import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
   15 import org.firstinspires.ftc.teamcode.base.HeadingHold;
   16 import org.firstinspires.ftc.teamcode.base.Tracker;
+  17 import org.firstinspires.ftc.teamcode.base.odometry.HardwareWheelSource;
   18 import org.firstinspires.ftc.teamcode.pedro.Constants;
   19
   20 /**
@@ lines 109 to 116 @@
  109         // When on L180S030, see
  110         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l180.html#l180s030
  111         // for what to do here.
+ 112         shadowLocalizers.add("driveWheelEncoders",
+ 113                 new L120EncoderLocalizer(new HardwareWheelSource(hardware)));
  114     }
  115
  116     // 'public' to match the parent class. 'public' allows access from any code.
```
