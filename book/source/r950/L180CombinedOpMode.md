# L180CombinedOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-mlt-fable.1`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L180

```{code-block} diff
:class: answer-diff

--- mytry/L180CombinedOpMode.java  (before L180)
+++ mytry/L180CombinedOpMode.java  (after L180)
@@ lines 10 to 16 @@
   10 import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
   11 import org.firstinspires.ftc.teamcode.base.HeadingHold;
   12 import org.firstinspires.ftc.teamcode.base.Tracker;
+  13 import org.firstinspires.ftc.teamcode.base.odometry.HardwareWheelSource;
   14 import org.firstinspires.ftc.teamcode.pedro.Constants;
   15
   16 /**
@@ lines 102 to 109 @@
  102         // When on L180S030, see
  103         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l180.html#l180s030
  104         // for what to do here.
+ 105         shadowLocalizers.add("driveWheelEncoders",
+ 106                 new L120EncoderLocalizer(new HardwareWheelSource(hardware)));
  107     }
  108
  109     // 'public' to match the parent class. 'public' allows access from any code.
```
