# L113LocalizerOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111.2`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L113

```{code-block} diff
:class: answer-diff

--- mytry/L113LocalizerOpMode.java  (before L113)
+++ mytry/L113LocalizerOpMode.java  (after L113)
@@ lines 10 to 16 @@
   10 import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
   11 import org.firstinspires.ftc.teamcode.base.StickShaper;
   12 import org.firstinspires.ftc.teamcode.base.Tracker;
+  13 import org.firstinspires.ftc.teamcode.base.odometry.HardwareWheelSource;
   14
   15 /**
   16  * L113: drive around with your own localizer running beside the robot's. Yours
@@ lines 93 to 101 @@
   93         // When on L113S030, see
   94         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l113.html#l113s030
   95         // for what to do here.
+  96         HardwareWheelSource wheelSource = new HardwareWheelSource(hardware);
+  97         L113EncoderLocalizer driveWheelEncoders = new L113EncoderLocalizer(wheelSource);
+  98         shadowLocalizers.add("driveWheelEncoders", driveWheelEncoders);
   99     }
  100
  101     // 'public' to match the parent class. 'public' allows access from any code.
```
