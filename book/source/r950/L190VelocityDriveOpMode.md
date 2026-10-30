# L190VelocityDriveOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L190

```{code-block} diff
:class: answer-diff

--- mytry/L190VelocityDriveOpMode.java  (before L190)
+++ mytry/L190VelocityDriveOpMode.java  (after L190)
@@ lines 9 to 16 @@
    9 import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
   10 import org.firstinspires.ftc.teamcode.base.StickShaper;
   11 import org.firstinspires.ftc.teamcode.base.Tracker;
+  12 import org.firstinspires.ftc.teamcode.base.WheelTargets;
+  13 import org.firstinspires.ftc.teamcode.pedro.Constants;
   14
   15 import static org.firstinspires.ftc.teamcode.base.CorbelsDriveTrain.*;
   16
@@ lines 49 to 55 @@
   49     // When on L190S030, see
   50     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l190.html#l190s030
   51     // for what to do here.
+  52     L190SpeedDriveTrain drivetrain;
   53
   54     // 'public' to match the parent class. 'public' allows access from any code.
   55     @Override
@@ lines 60 to 68 @@
   60         // When on L190S040, see
   61         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l190.html#l190s040
   62         // for what to do here.
+  63         drivetrain = new L190SpeedDriveTrain(hardware);
   64
-             initAfter();
+  65         initAfter(drivetrain);
   66     }
   67
   68     // 'public' to match the parent class. 'public' allows access from any code.
@@ lines 93 to 115 @@
   93         // When on L190S050, see
   94         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l190.html#l190s050
   95         // for what to do here.
-             double forwardX_ips = 0.0;
+  96         double forwardXSpeed = -leftStickY;
+  97         forwardXSpeed = stickShaper.shaped(forwardXSpeed);
+  98         double forwardX_ips = forwardXSpeed * MAX_IPS;
   99         Tracker.publish("drivetrain/forwardX_ips", forwardX_ips);
-             double strafeY_ips = 0.0;
+ 100         double strafeYSpeed = -leftStickX;
+ 101         strafeYSpeed = stickShaper.shaped(strafeYSpeed);
+ 102         double strafeY_ips = strafeYSpeed * MAX_IPS;
  103         Tracker.publish("drivetrain/strafeY_ips", strafeY_ips);
-             double turn_radps = 0.0;
+ 104         double turnCcwSpeed = -rightStickX;
+ 105         turnCcwSpeed = stickShaper.shaped(turnCcwSpeed);
+ 106         double turn_radps = turnCcwSpeed * MAX_TURN_RADPS;
  107         Tracker.publish("drivetrain/turn_radps", turn_radps);
+ 108
+ 109         double[] wheel_ips = WheelTargets.forMecanum(forwardX_ips, strafeY_ips, turn_radps,
+ 110                 Constants.turnRadiusInches);
+ 111         drivetrain.setCommandedWheelSpeeds(wheel_ips[FL], wheel_ips[FR], wheel_ips[BL],
+ 112                 wheel_ips[BR]);
  113     }
  114
  115     // 'public' to match the parent class. 'public' allows access from any code.
```
