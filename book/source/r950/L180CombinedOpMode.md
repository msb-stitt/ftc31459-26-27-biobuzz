# L180CombinedOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-minor-fixes.1`. A line marked + is new, and a line marked -
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
@@ lines 109 to 117 @@
  109         // When on L180S030, see
  110         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l180.html#l180s030
  111         // for what to do here.
+ 112         HardwareWheelSource wheelSource = new HardwareWheelSource(hardware);
+ 113         L120EncoderLocalizer driveWheelEncoders = new L120EncoderLocalizer(wheelSource);
+ 114         shadowLocalizers.add("driveWheelEncoders", driveWheelEncoders);
  115     }
  116
  117     // 'public' to match the parent class. 'public' allows access from any code.
```
