# L080ArcadeOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-rules.1`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L080

```{code-block} diff
:class: answer-diff

--- mytry/L080ArcadeOpMode.java  (before L080)
+++ mytry/L080ArcadeOpMode.java  (after L080)
@@ lines 68 to 99 @@
   68         // When on L080S030, see
   69         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l080.html#l080s030
   70         // for what to do here.
+  71         double band = 0.05;
+  72         double forward = deadband(-leftStickY, band);
+  73         forward = signedSquared(forward);
+  74         Tracker.publish("arcade/forward", forward);
+  75         double turn = deadband(-rightStickX, band);
+  76         turn = signedSquared(turn);
+  77         Tracker.publish("arcade/turn", turn);
   78
   79         // When on L080S040, see
   80         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l080.html#l080s040
   81         // for what to do here.
+  82         double leftPower = forward - turn;
+  83         double rightPower = forward + turn;
+  84         double leftSize = Math.abs(leftPower);
+  85         double rightSize = Math.abs(rightPower);
+  86         double biggest = Math.max(leftSize, rightSize);
+  87         biggest = Math.max(1.0, biggest);
+  88         leftPower = leftPower / biggest;
+  89         rightPower = rightPower / biggest;
+  90         Tracker.publish("power/left", leftPower);
+  91         Tracker.publish("power/right", rightPower);
+  92
+  93         hardware.frontLeft.setPower(leftPower);
+  94         hardware.backLeft.setPower(leftPower);
+  95         hardware.frontRight.setPower(rightPower);
+  96         hardware.backRight.setPower(rightPower);
   97
   98         boolean buttonA = gamepad1.a;
   99         Tracker.publish("button/a", buttonA);
```
