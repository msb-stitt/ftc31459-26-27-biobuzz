# L113LocalizerOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L113

```{code-block} diff
:class: answer-diff

--- mytry/L113LocalizerOpMode.java  (before L113)
+++ mytry/L113LocalizerOpMode.java  (after L113)
@@ lines 9 to 15 @@
    9 import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
   10 import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
   11 import org.firstinspires.ftc.teamcode.base.Tracker;
+  12 import org.firstinspires.ftc.teamcode.base.odometry.HardwareWheelSource;
   13
   14 /**
   15  * L113: drive around with your own localizer running beside the robot's. Yours
@@ lines 91 to 99 @@
   91         // When on L113S030, see
   92         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l113.html#l113s030
   93         // for what to do here.
+  94         HardwareWheelSource wheelSource = new HardwareWheelSource(hardware);
+  95         L113EncoderLocalizer driveWheelEncoders = new L113EncoderLocalizer(wheelSource);
+  96         shadowLocalizers.add("driveWheelEncoders", driveWheelEncoders);
   97     }
   98
   99     // 'public' to match the parent class. 'public' allows access from any code.
```
