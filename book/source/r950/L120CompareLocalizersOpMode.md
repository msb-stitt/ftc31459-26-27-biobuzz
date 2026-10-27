# L120CompareLocalizersOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l140-alliances.1`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L120

```{code-block} diff
:class: answer-diff

--- mytry/L120CompareLocalizersOpMode.java  (before L120)
+++ mytry/L120CompareLocalizersOpMode.java  (after L120)
@@ lines 9 to 15 @@
    9 import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
   10 import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
   11 import org.firstinspires.ftc.teamcode.base.Tracker;
+  12 import org.firstinspires.ftc.teamcode.base.odometry.HardwareWheelSource;
   13
   14 /**
   15  * L120: drive around while two localizers disagree. Your localizer reads the
@@ lines 92 to 100 @@
   92         // When on L120S030, see
   93         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l120.html#l120s030
   94         // for what to do here.
+  95         HardwareWheelSource wheelSource = new HardwareWheelSource(hardware);
+  96         L120EncoderLocalizer driveWheelEncoders = new L120EncoderLocalizer(wheelSource);
+  97         shadowLocalizers.add("driveWheelEncoders", driveWheelEncoders);
   98     }
   99
  100     // 'public' to match the parent class. 'public' allows access from any code.
```
