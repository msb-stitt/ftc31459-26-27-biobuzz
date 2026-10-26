# L160HeadingHoldOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-minor-fixes`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L160

```{code-block} diff
:class: answer-diff

--- mytry/L160HeadingHoldOpMode.java  (before L160)
+++ mytry/L160HeadingHoldOpMode.java  (after L160)
@@ lines 8 to 16 @@
    8
    9 import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
   10 import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
+  11 import org.firstinspires.ftc.teamcode.base.HeadingHold;
   12 import org.firstinspires.ftc.teamcode.base.Tracker;
+  13 import org.firstinspires.ftc.teamcode.pedro.Constants;
   14
   15 /**
   16  * L160: hold the heading the driver left. Let go of the turn stick and the
@@ lines 34 to 40 @@
   34 public class L160HeadingHoldOpMode extends CorbelsTeleOp {
   35
   36     CorbelsMecanum drivetrain;
+  37     HeadingHold hold = new HeadingHold(Constants.foresightConfig.headingFeedback.get());
   38
   39     // 'public' to match the parent class. 'public' allows access from any code.
   40     @Override
@@ lines 81 to 88 @@
   81         // When on L160S040, see
   82         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l160.html#l160s040
   83         // for what to do here.
+  84         turn = hold.turn(follower, turn);
+  85         Tracker.publish("heading/holding", hold.target() != null);
   86
   87         double strafe = deadband(-leftStickX, band);
   88         strafe = signedSquared(strafe);
```
