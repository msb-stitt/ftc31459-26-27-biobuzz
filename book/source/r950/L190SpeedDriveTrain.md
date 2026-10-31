# L190SpeedDriveTrain

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

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
@@ lines 39 to 55 @@
   39         // When on L190S060, see
   40         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l190.html#l190s060
   41         // for what to do here.
+  42         double[] measured_ips = measuredSpeeds.all();
+  43         double batteryVolts = hardware.batteryVolts();
+  44         double[] wheelSpeeds = new double[4];
+  45         for (int i = 0; i < 4; i++) {
+  46             double feedforwardVolts = Math.signum(wanted_ips[i]) * Constants.kSForwardVolts
+  47                     + Constants.kVForwardVoltSecondsPerInch * wanted_ips[i];
+  48             double feedforward = feedforwardVolts / batteryVolts;
+  49             double feedback = VELOCITY_KP * (wanted_ips[i] - measured_ips[i]);
+  50             wheelSpeeds[i] = clampToFullSpeed(feedforward + feedback);
+  51         }
+  52         setCommandedWheels(wheelSpeeds[FL], wheelSpeeds[FR], wheelSpeeds[BL], wheelSpeeds[BR]);
+  53         publishWheelSpeeds(wanted_ips, measured_ips);
   54     }
   55 }
```
