# L190VelocityDriveOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-rules.1`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L190

```{code-block} diff
:class: answer-diff

--- mytry/L190VelocityDriveOpMode.java  (before L190)
+++ mytry/L190VelocityDriveOpMode.java  (after L190)
@@ lines 5 to 12 @@
    5
    6 import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
    7 import org.firstinspires.ftc.teamcode.base.Tracker;
+   8 import org.firstinspires.ftc.teamcode.base.WheelTargets;
+   9 import org.firstinspires.ftc.teamcode.pedro.Constants;
   10
   11 /**
   12  * L190: ask the wheels for a speed. Full stick means 40 inches per second, not
@@ lines 39 to 45 @@
   39     // When on L190S030, see
   40     // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l190.html#l190s030
   41     // for what to do here.
+  42     L190SpeedDriveTrain drivetrain;
   43
   44     // 'public' to match the parent class. 'public' allows access from any code.
   45     @Override
@@ lines 49 to 57 @@
   49         // When on L190S040, see
   50         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l190.html#l190s040
   51         // for what to do here.
+  52         drivetrain = new L190SpeedDriveTrain(hardware);
   53
-             initAfter();
+  54         initAfter(drivetrain);
   55     }
   56
   57     // 'public' to match the parent class. 'public' allows access from any code.
@@ lines 77 to 90 @@
   77         // When on L190S050, see
   78         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l190.html#l190s050
   79         // for what to do here.
-             double forwardInPerS = 0.0;
-             double strafeInPerS = 0.0;
-             double turnRadPerS = 0.0;
+  80         double forwardInPerS = deadband(-leftStickY, band) * MAX_IPS;
+  81         double strafeInPerS = deadband(-leftStickX, band) * MAX_IPS;
+  82         double turnRadPerS = deadband(-rightStickX, band) * MAX_TURN_RADPS;
   83         Tracker.publish("command/forward_ips", forwardInPerS);
   84         Tracker.publish("command/left_ips", strafeInPerS);
   85         Tracker.publish("command/turn_radps", turnRadPerS);
+  86         double[] target = WheelTargets.forMecanum(forwardInPerS, strafeInPerS, turnRadPerS, Constants.turnRadiusInches);
+  87         drivetrain.setCommandedWheelSpeeds(target[0], target[1], target[2], target[3]);
   88
   89         loopAfter();
   90     }
```
