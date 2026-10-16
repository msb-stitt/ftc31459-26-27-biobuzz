# L160HeadingHoldOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-rules.1`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L160

```diff
--- TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L160HeadingHoldOpMode.java  (before L160)
+++ TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L160HeadingHoldOpMode.java  (after L160)
@@ lines 5 to 13 @@
    5
    6 import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
    7 import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
+   8 import org.firstinspires.ftc.teamcode.base.HeadingHold;
    9 import org.firstinspires.ftc.teamcode.base.Tracker;
+  10 import org.firstinspires.ftc.teamcode.pedro.Constants;
   11
   12 /**
   13  * L160: hold the heading the driver left. Let go of the turn stick and the
@@ lines 31 to 37 @@
   31     boolean previousButtonA;
   32
   33     CorbelsMecanum drivetrain;
+  34     HeadingHold hold = new HeadingHold(Constants.foresightConfig.headingFeedback.get());
   35
   36     // 'public' to match the parent class. 'public' allows access from any code.
   37     @Override
@@ lines 75 to 82 @@
   75         // When on L160S040, see
   76         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l160.html#l160s040
   77         // for what to do here.
+  78         turn = hold.turn(follower, turn);
+  79         Tracker.publish("heading/holding", hold.target() != null);
   80
   81         double strafe = deadband(-leftStickX, band);
   82         strafe = signedSquared(strafe);
```
