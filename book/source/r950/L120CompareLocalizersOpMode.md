# L120CompareLocalizersOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-rules.1`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L120

```diff
--- TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L120CompareLocalizersOpMode.java  (before L120)
+++ TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L120CompareLocalizersOpMode.java  (after L120)
@@ lines 6 to 12 @@
    6 import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
    7 import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
    8 import org.firstinspires.ftc.teamcode.base.Tracker;
+   9 import org.firstinspires.ftc.teamcode.base.odometry.HardwareWheelSource;
   10
   11 /**
   12  * L120: drive around while two localizers disagree. Your localizer reads the
@@ lines 100 to 107 @@
  100         // When on L120S030, see
  101         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l120.html#l120s030
  102         // for what to do here.
+ 103         shadowLocalizers.add("driveWheelEncoders",
+ 104                 new L120EncoderLocalizer(new HardwareWheelSource(hardware)));
  105     }
  106
  107     // 'public' to match the parent class. 'public' allows access from any code.
```
