# L190SpeedDriveTrain

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-coding-stitt-cc`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L190

```{code-block} diff
:class: answer-diff

--- mytry/L190SpeedDriveTrain.java  (before L190)
+++ mytry/L190SpeedDriveTrain.java  (after L190)
@@ lines 2 to 8 @@
    2
    3 import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
    4 import org.firstinspires.ftc.teamcode.base.RobotHardware;
+   5 import org.firstinspires.ftc.teamcode.pedro.Constants;
    6
    7 /**
    8  * L190: the drivetrain that asks the wheels for a speed.
@@ lines 39 to 52 @@
   39         // When on L190S060, see
   40         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l190.html#l190s060
   41         // for what to do here.
+  42         double[] measured = measuredSpeeds.all();
+  43         double[] powers = new double[4];
+  44         for (int i = 0; i < 4; i++) {
+  45             double feedforward = Constants.powerPerInchPerSecond * wanted[i];
+  46             double feedback = VELOCITY_KP * (wanted[i] - measured[i]);
+  47             powers[i] = clampToPower(feedforward + feedback);
+  48         }
+  49         setCommandedWheels(powers[FL], powers[FR], powers[BL], powers[BR]);
+  50         publishWheelSpeeds(wanted, measured);
   51     }
   52 }
```
