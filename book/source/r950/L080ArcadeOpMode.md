# L080ArcadeOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-minor-fixes`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L080

```{code-block} diff
:class: answer-diff

--- mytry/L080ArcadeOpMode.java  (before L080)
+++ mytry/L080ArcadeOpMode.java  (after L080)
@@ lines 79 to 110 @@
   79         // When on L080S030, see
   80         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l080.html#l080s030
   81         // for what to do here.
+  82         double band = 0.05;
+  83         double forward = deadband(-leftStickY, band);
+  84         forward = signedSquared(forward);
+  85         Tracker.publish("arcade/forward", forward);
+  86         double turn = deadband(-rightStickX, band);
+  87         turn = signedSquared(turn);
+  88         Tracker.publish("arcade/turn", turn);
   89
   90         // When on L080S040, see
   91         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l080.html#l080s040
   92         // for what to do here.
+  93         double leftMixedSpeed = forward - turn;
+  94         double rightMixedSpeed = forward + turn;
+  95         double leftSize = Math.abs(leftMixedSpeed);
+  96         double rightSize = Math.abs(rightMixedSpeed);
+  97         double biggest = Math.max(leftSize, rightSize);
+  98         double biggestOrOne = Math.max(1.0, biggest);
+  99         double leftSpeed = leftMixedSpeed / biggestOrOne;
+ 100         double rightSpeed = rightMixedSpeed / biggestOrOne;
+ 101         Tracker.publish("drivetrain/leftSpeed", leftSpeed);
+ 102         Tracker.publish("drivetrain/rightSpeed", rightSpeed);
+ 103
+ 104         hardware.frontLeft.setPower(leftSpeed);
+ 105         hardware.backLeft.setPower(leftSpeed);
+ 106         hardware.frontRight.setPower(rightSpeed);
+ 107         hardware.backRight.setPower(rightSpeed);
  108
  109         boolean buttonA = gamepad1.a;
  110         Tracker.publish("button/a", buttonA);
```
