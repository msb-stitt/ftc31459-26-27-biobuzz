# L090MecanumOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-minor-fixes`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L090

```{code-block} diff
:class: answer-diff

--- mytry/L090MecanumOpMode.java  (before L090)
+++ mytry/L090MecanumOpMode.java  (after L090)
@@ lines 86 to 123 @@
   86         // When on L090S030, see
   87         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l090.html#l090s030
   88         // for what to do here.
+  89         double strafe = deadband(-leftStickX, band);
+  90         strafe = signedSquared(strafe);
+  91         Tracker.publish("arcade/strafe", strafe);
   92
   93         // When on L090S040, see
   94         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l090.html#l090s040
   95         // for what to do here.
+  96         double frontLeftMixedSpeed = forward - strafe - turn;
+  97         double frontRightMixedSpeed = forward + strafe + turn;
+  98         double backLeftMixedSpeed = forward + strafe - turn;
+  99         double backRightMixedSpeed = forward - strafe + turn;
+ 100         double frontLeftSize = Math.abs(frontLeftMixedSpeed);
+ 101         double frontRightSize = Math.abs(frontRightMixedSpeed);
+ 102         double backLeftSize = Math.abs(backLeftMixedSpeed);
+ 103         double backRightSize = Math.abs(backRightMixedSpeed);
+ 104         double biggest = Math.max(frontLeftSize, frontRightSize);
+ 105         biggest = Math.max(biggest, backLeftSize);
+ 106         biggest = Math.max(biggest, backRightSize);
+ 107         biggest = Math.max(1.0, biggest);
+ 108         frontLeftMixedSpeed = frontLeftMixedSpeed / biggest;
+ 109         frontRightMixedSpeed = frontRightMixedSpeed / biggest;
+ 110         backLeftMixedSpeed = backLeftMixedSpeed / biggest;
+ 111         backRightMixedSpeed = backRightMixedSpeed / biggest;
+ 112         Tracker.publish("mixedSpeed/frontLeft", frontLeftMixedSpeed);
+ 113         Tracker.publish("mixedSpeed/frontRight", frontRightMixedSpeed);
+ 114         Tracker.publish("mixedSpeed/backLeft", backLeftMixedSpeed);
+ 115         Tracker.publish("mixedSpeed/backRight", backRightMixedSpeed);
+ 116
+ 117         hardware.frontLeft.setPower(frontLeftMixedSpeed);
+ 118         hardware.frontRight.setPower(frontRightMixedSpeed);
+ 119         hardware.backLeft.setPower(backLeftMixedSpeed);
+ 120         hardware.backRight.setPower(backRightMixedSpeed);
  121
  122         boolean buttonA = gamepad1.a;
  123         Tracker.publish("button/a", buttonA);
```
