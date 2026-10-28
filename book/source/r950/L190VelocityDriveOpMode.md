# L190VelocityDriveOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l135-menu-classes`. A line marked + is new, and a line marked -
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
   14 import static org.firstinspires.ftc.teamcode.base.CorbelsDriveTrain.*;
   15
@@ lines 46 to 52 @@
   46     // When on L190S030, see
   47     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l190.html#l190s030
   48     // for what to do here.
+  49     L190SpeedDriveTrain drivetrain;
   50
   51     // 'public' to match the parent class. 'public' allows access from any code.
   52     @Override
@@ lines 56 to 64 @@
   56         // When on L190S040, see
   57         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l190.html#l190s040
   58         // for what to do here.
+  59         drivetrain = new L190SpeedDriveTrain(hardware);
   60
-             initAfter();
+  61         initAfter(drivetrain);
   62     }
   63
   64     // 'public' to match the parent class. 'public' allows access from any code.
@@ lines 90 to 112 @@
   90         // When on L190S050, see
   91         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l190.html#l190s050
   92         // for what to do here.
-             double forwardX_ips = 0.0;
+  93         double forwardXSpeed = -leftStickY;
+  94         forwardXSpeed = deadband(forwardXSpeed, bandToSetToZero);
+  95         double forwardX_ips = forwardXSpeed * MAX_IPS;
   96         Tracker.publish("drivetrain/forwardX_ips", forwardX_ips);
-             double strafeY_ips = 0.0;
+  97         double strafeYSpeed = -leftStickX;
+  98         strafeYSpeed = deadband(strafeYSpeed, bandToSetToZero);
+  99         double strafeY_ips = strafeYSpeed * MAX_IPS;
  100         Tracker.publish("drivetrain/strafeY_ips", strafeY_ips);
-             double turn_radps = 0.0;
+ 101         double turnCcwSpeed = -rightStickX;
+ 102         turnCcwSpeed = deadband(turnCcwSpeed, bandToSetToZero);
+ 103         double turn_radps = turnCcwSpeed * MAX_TURN_RADPS;
  104         Tracker.publish("drivetrain/turn_radps", turn_radps);
+ 105
+ 106         double[] wheel_ips = WheelTargets.forMecanum(forwardX_ips, strafeY_ips, turn_radps,
+ 107                 Constants.turnRadiusInches);
+ 108         drivetrain.setCommandedWheelSpeeds(wheel_ips[FL], wheel_ips[FR], wheel_ips[BL],
+ 109                 wheel_ips[BR]);
  110     }
  111
  112     // 'public' to match the parent class. 'public' allows access from any code.
```
