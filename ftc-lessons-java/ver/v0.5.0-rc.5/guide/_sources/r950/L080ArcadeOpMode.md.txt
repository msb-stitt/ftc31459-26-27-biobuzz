# L080ArcadeOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111.2`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L080

```{code-block} diff
:class: answer-diff

--- mytry/L080ArcadeOpMode.java  (before L080)
+++ mytry/L080ArcadeOpMode.java  (after L080)
@@ lines 11 to 17 @@
   11 // When on L080S030, see
   12 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l080.html#l080s030
   13 // for what to do here.
+  14 import org.firstinspires.ftc.teamcode.mytry.l065.StickShaper;
   15
   16 /**
   17  * L080: arcade drive. The left stick drives forward and back, the right stick
@@ lines 40 to 46 @@
   40     // When on L080S030, see
   41     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l080.html#l080s030
   42     // for what to do here.
+  43     StickShaper stickShaper = new StickShaper(0.05);
   44
   45     boolean previousButtonA;
   46
@@ lines 88 to 118 @@
   88         // When on L080S040, see
   89         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l080.html#l080s040
   90         // for what to do here.
+  91         double forwardXSpeed = -leftStickY;
+  92         forwardXSpeed = stickShaper.shaped(forwardXSpeed);
+  93         Tracker.publish("drivetrain/forwardXSpeed", forwardXSpeed);
+  94         double turnCcwSpeed = -rightStickX;
+  95         turnCcwSpeed = stickShaper.shaped(turnCcwSpeed);
+  96         Tracker.publish("drivetrain/turnCcwSpeed", turnCcwSpeed);
   97
   98         // When on L080S050, see
   99         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l080.html#l080s050
  100         // for what to do here.
+ 101         double leftMixedSpeed = forwardXSpeed - turnCcwSpeed;
+ 102         double rightMixedSpeed = forwardXSpeed + turnCcwSpeed;
+ 103         double leftSize = Math.abs(leftMixedSpeed);
+ 104         double rightSize = Math.abs(rightMixedSpeed);
+ 105         double biggest = Math.max(leftSize, rightSize);
+ 106         double biggestOrOne = Math.max(1.0, biggest);
+ 107         double leftSpeed = leftMixedSpeed / biggestOrOne;
+ 108         double rightSpeed = rightMixedSpeed / biggestOrOne;
+ 109         Tracker.publish("drivetrain/leftSpeed", leftSpeed);
+ 110         Tracker.publish("drivetrain/rightSpeed", rightSpeed);
+ 111
+ 112         hardware.frontLeft.setPower(leftSpeed);
+ 113         hardware.backLeft.setPower(leftSpeed);
+ 114         hardware.frontRight.setPower(rightSpeed);
+ 115         hardware.backRight.setPower(rightSpeed);
  116
  117         boolean buttonA = gamepad1.a;
  118         Tracker.publish("gamepad1/buttonA", buttonA);
```
