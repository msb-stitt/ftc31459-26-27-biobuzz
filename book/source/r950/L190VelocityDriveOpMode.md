# L190VelocityDriveOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-minor-fixes`. A line marked + is new, and a line marked -
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
   45     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l190.html#l190s030
   46     // for what to do here.
+  47     L190SpeedDriveTrain drivetrain;
   48
   49     // 'public' to match the parent class. 'public' allows access from any code.
   50     @Override
@@ lines 54 to 62 @@
   54         // When on L190S040, see
   55         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l190.html#l190s040
   56         // for what to do here.
+  57         drivetrain = new L190SpeedDriveTrain(hardware);
   58
-             initAfter();
+  59         initAfter(drivetrain);
   60     }
   61
   62     // 'public' to match the parent class. 'public' allows access from any code.
@@ lines 88 to 110 @@
   88         // When on L190S050, see
   89         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l190.html#l190s050
   90         // for what to do here.
-             double forwardXInPerS = 0.0;
+  91         double forwardXSpeed = -leftStickY;
+  92         forwardXSpeed = deadband(forwardXSpeed, bandToSetToZero);
+  93         double forwardXInPerS = forwardXSpeed * MAX_IPS;
   94         Tracker.publish("drivetrain/forwardXInPerS", forwardXInPerS);
-             double strafeYInPerS = 0.0;
+  95         double strafeYSpeed = -leftStickX;
+  96         strafeYSpeed = deadband(strafeYSpeed, bandToSetToZero);
+  97         double strafeYInPerS = strafeYSpeed * MAX_IPS;
   98         Tracker.publish("drivetrain/strafeYInPerS", strafeYInPerS);
-             double turnRadPerS = 0.0;
+  99         double turnCcwSpeed = -rightStickX;
+ 100         turnCcwSpeed = deadband(turnCcwSpeed, bandToSetToZero);
+ 101         double turnRadPerS = turnCcwSpeed * MAX_TURN_RADPS;
  102         Tracker.publish("drivetrain/turnRadPerS", turnRadPerS);
+ 103
+ 104         double[] wheelInPerS = WheelTargets.forMecanum(forwardXInPerS, strafeYInPerS, turnRadPerS,
+ 105                 Constants.turnRadiusInches);
+ 106         drivetrain.setCommandedWheelSpeeds(wheelInPerS[0], wheelInPerS[1], wheelInPerS[2],
+ 107                 wheelInPerS[3]);
  108     }
  109
  110     // 'public' to match the parent class. 'public' allows access from any code.
```
