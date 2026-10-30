# L122CompareLocalizersOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L122

```{code-block} diff
:class: answer-diff

--- mytry/L122CompareLocalizersOpMode.java  (before L122)
+++ mytry/L122CompareLocalizersOpMode.java  (after L122)
@@ lines 10 to 16 @@
   10 import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
   11 import org.firstinspires.ftc.teamcode.base.StickShaper;
   12 import org.firstinspires.ftc.teamcode.base.Tracker;
+  13 import org.firstinspires.ftc.teamcode.base.odometry.HardwareWheelSource;
   14
   15 /**
   16  * L122: drive around while two localizers disagree. Your localizer from L113
@@ lines 94 to 102 @@
   94         // When on L122S030, see
   95         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l122.html#l122s030
   96         // for what to do here.
+  97         HardwareWheelSource wheelSource = new HardwareWheelSource(hardware);
+  98         L113EncoderLocalizer driveWheelEncoders = new L113EncoderLocalizer(wheelSource);
+  99         shadowLocalizers.add("driveWheelEncoders", driveWheelEncoders);
  100     }
  101
  102     // 'public' to match the parent class. 'public' allows access from any code.
```
