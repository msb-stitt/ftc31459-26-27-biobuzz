# L160HeadingHoldOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111.1`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L160

```{code-block} diff
:class: answer-diff

--- mytry/L160HeadingHoldOpMode.java  (before L160)
+++ mytry/L160HeadingHoldOpMode.java  (after L160)
@@ lines 8 to 17 @@
    8
    9 import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
   10 import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
+  11 import org.firstinspires.ftc.teamcode.base.HeadingHold;
   12 import org.firstinspires.ftc.teamcode.base.StickShaper;
   13 import org.firstinspires.ftc.teamcode.base.Tracker;
+  14 import org.firstinspires.ftc.teamcode.pedro.Constants;
   15
   16 /**
   17  * L160: hold the heading the driver left. Let go of the turn stick and the
@@ lines 37 to 43 @@
   37     StickShaper stickShaper = new StickShaper(0.05);
   38
   39     CorbelsMecanum drivetrain;
+  40     HeadingHold hold = new HeadingHold(Constants.foresightConfig.headingFeedback.get());
   41
   42     // 'public' to match the parent class. 'public' allows access from any code.
   43     @Override
@@ lines 83 to 91 @@
   83         // When on L160S040, see
   84         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l160.html#l160s040
   85         // for what to do here.
+  86         turnCcwSpeed = hold.turn(follower, turnCcwSpeed);
+  87         boolean holdingHeading = hold.target() != null;
+  88         Tracker.publish("drivetrain/holdingHeading", holdingHeading);
   89
   90         double fieldYSpeed = -leftStickX;
   91         fieldYSpeed = stickShaper.shaped(fieldYSpeed);
```
