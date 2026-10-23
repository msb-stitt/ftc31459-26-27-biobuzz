# L090MecanumOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-coding-stitt-cc`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L090

```{code-block} diff
:class: answer-diff

--- mytry/L090MecanumOpMode.java  (before L090)
+++ mytry/L090MecanumOpMode.java  (after L090)
@@ lines 86 to 123 @@
   86         // When on L090S030, see
   87         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l090.html#l090s030
   88         // for what to do here.
+  89         double strafe = deadband(-leftStickX, band);
+  90         strafe = signedSquared(strafe);
+  91         Tracker.publish("arcade/strafe", strafe);
   92
   93         // When on L090S040, see
   94         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l090.html#l090s040
   95         // for what to do here.
+  96         double frontLeftPower = forward - strafe - turn;
+  97         double frontRightPower = forward + strafe + turn;
+  98         double backLeftPower = forward + strafe - turn;
+  99         double backRightPower = forward - strafe + turn;
+ 100         double frontLeftSize = Math.abs(frontLeftPower);
+ 101         double frontRightSize = Math.abs(frontRightPower);
+ 102         double backLeftSize = Math.abs(backLeftPower);
+ 103         double backRightSize = Math.abs(backRightPower);
+ 104         double biggest = Math.max(frontLeftSize, frontRightSize);
+ 105         biggest = Math.max(biggest, backLeftSize);
+ 106         biggest = Math.max(biggest, backRightSize);
+ 107         biggest = Math.max(1.0, biggest);
+ 108         frontLeftPower = frontLeftPower / biggest;
+ 109         frontRightPower = frontRightPower / biggest;
+ 110         backLeftPower = backLeftPower / biggest;
+ 111         backRightPower = backRightPower / biggest;
+ 112         Tracker.publish("power/frontLeft", frontLeftPower);
+ 113         Tracker.publish("power/frontRight", frontRightPower);
+ 114         Tracker.publish("power/backLeft", backLeftPower);
+ 115         Tracker.publish("power/backRight", backRightPower);
+ 116
+ 117         hardware.frontLeft.setPower(frontLeftPower);
+ 118         hardware.frontRight.setPower(frontRightPower);
+ 119         hardware.backLeft.setPower(backLeftPower);
+ 120         hardware.backRight.setPower(backRightPower);
  121
  122         boolean buttonA = gamepad1.a;
  123         Tracker.publish("button/a", buttonA);
```
