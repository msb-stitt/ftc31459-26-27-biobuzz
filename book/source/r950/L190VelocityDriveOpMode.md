# L190VelocityDriveOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-auto-schedules`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L190

```{code-block} diff
:class: answer-diff

--- mytry/L190VelocityDriveOpMode.java  (before L190)
+++ mytry/L190VelocityDriveOpMode.java  (after L190)
@@ lines 8 to 15 @@
    8
    9 import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
   10 import org.firstinspires.ftc.teamcode.base.Tracker;
+  11 import org.firstinspires.ftc.teamcode.base.WheelTargets;
+  12 import org.firstinspires.ftc.teamcode.pedro.Constants;
   13
   14 /**
   15  * L190: ask the wheels for a speed. Full stick means 40 inches per second, not
@@ lines 44 to 50 @@
   44     // When on L190S030, see
   45     // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l190.html#l190s030
   46     // for what to do here.
+  47     L190SpeedDriveTrain drivetrain;
   48
   49     // 'public' to match the parent class. 'public' allows access from any code.
   50     @Override
@@ lines 54 to 62 @@
   54         // When on L190S040, see
   55         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l190.html#l190s040
   56         // for what to do here.
+  57         drivetrain = new L190SpeedDriveTrain(hardware);
   58
-             initAfter();
+  59         initAfter(drivetrain);
   60     }
   61
   62     // 'public' to match the parent class. 'public' allows access from any code.
@@ lines 88 to 101 @@
   88         // When on L190S050, see
   89         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l190.html#l190s050
   90         // for what to do here.
-             double forwardInPerS = 0.0;
-             double strafeInPerS = 0.0;
-             double turnRadPerS = 0.0;
+  91         double forwardInPerS = deadband(-leftStickY, band) * MAX_IPS;
+  92         double strafeInPerS = deadband(-leftStickX, band) * MAX_IPS;
+  93         double turnRadPerS = deadband(-rightStickX, band) * MAX_TURN_RADPS;
   94         Tracker.publish("command/forward_ips", forwardInPerS);
   95         Tracker.publish("command/left_ips", strafeInPerS);
   96         Tracker.publish("command/turn_radps", turnRadPerS);
+  97         double[] target = WheelTargets.forMecanum(forwardInPerS, strafeInPerS, turnRadPerS, Constants.turnRadiusInches);
+  98         drivetrain.setCommandedWheelSpeeds(target[0], target[1], target[2], target[3]);
   99     }
  100
  101     // 'public' to match the parent class. 'public' allows access from any code.
```
