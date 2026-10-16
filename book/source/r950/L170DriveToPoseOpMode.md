# L170DriveToPoseOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-rules.1`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L170

```{code-block} diff
:class: answer-diff

--- mytry/L170DriveToPoseOpMode.java  (before L170)
+++ mytry/L170DriveToPoseOpMode.java  (after L170)
@@ lines 1 to 7 @@
    1 package org.firstinspires.ftc.teamcode.mytry;
    2
    3 import com.pedropathing.api.PoseFactory;
+   4 import com.pedropathing.ivy.commands.Commands;
    5 import com.pedropathing.math.Pose;
    6 import com.qualcomm.robotcore.eventloop.opmode.Disabled;
    7 import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
@@ lines 46 to 55 @@
   46         // When on L170S030, see
   47         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l170.html#l170s030
   48         // for what to do here.
+  49         buttons.whenPressed(() -> gamepad1.y, Commands.instant(() -> {
+  50             follower.hold(target);
+  51             drivingItself = true;
+  52         }));
   53     }
   54
   55     // 'public' to match the parent class. 'public' allows access from any code.
@@ lines 132 to 147 @@
  132         // When on L170S040, see
  133         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l170.html#l170s040
  134         // for what to do here.
+ 135         if (drivingItself) {
+ 136             if (!driverWantsControl) {
+ 137                 Tracker.publish("drive/mode", "AUTO");
+ 138                 drivetrain.releaseCommandedWheels();
+ 139                 return;
+ 140             }
+ 141             drivingItself = false;
+ 142             follower.manual(0.0, 0.0, 0.0);
+ 143         }
+ 144         Tracker.publish("drive/mode", "DRIVER");
  145         double heading = follower.pose().heading();
  146         double robotForward = forward * Math.cos(heading) + strafe * Math.sin(heading);
  147         double robotStrafe = -forward * Math.sin(heading) + strafe * Math.cos(heading);
```
