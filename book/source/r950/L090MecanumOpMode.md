# L090MecanumOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-rules.1`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L090

```diff
--- TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L090MecanumOpMode.java  (before L090)
+++ TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L090MecanumOpMode.java  (after L090)
@@ lines 75 to 112 @@
   75         // When on L090S030, see
   76         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l090.html#l090s030
   77         // for what to do here.
+  78         double strafe = deadband(-leftStickX, band);
+  79         strafe = signedSquared(strafe);
+  80         Tracker.publish("arcade/strafe", strafe);
   81
   82         // When on L090S040, see
   83         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l090.html#l090s040
   84         // for what to do here.
+  85         double frontLeftPower = forward - strafe - turn;
+  86         double frontRightPower = forward + strafe + turn;
+  87         double backLeftPower = forward + strafe - turn;
+  88         double backRightPower = forward - strafe + turn;
+  89         double frontLeftSize = Math.abs(frontLeftPower);
+  90         double frontRightSize = Math.abs(frontRightPower);
+  91         double backLeftSize = Math.abs(backLeftPower);
+  92         double backRightSize = Math.abs(backRightPower);
+  93         double biggest = Math.max(frontLeftSize, frontRightSize);
+  94         biggest = Math.max(biggest, backLeftSize);
+  95         biggest = Math.max(biggest, backRightSize);
+  96         biggest = Math.max(1.0, biggest);
+  97         frontLeftPower = frontLeftPower / biggest;
+  98         frontRightPower = frontRightPower / biggest;
+  99         backLeftPower = backLeftPower / biggest;
+ 100         backRightPower = backRightPower / biggest;
+ 101         Tracker.publish("power/frontLeft", frontLeftPower);
+ 102         Tracker.publish("power/frontRight", frontRightPower);
+ 103         Tracker.publish("power/backLeft", backLeftPower);
+ 104         Tracker.publish("power/backRight", backRightPower);
+ 105
+ 106         hardware.frontLeft.setPower(frontLeftPower);
+ 107         hardware.frontRight.setPower(frontRightPower);
+ 108         hardware.backLeft.setPower(backLeftPower);
+ 109         hardware.backRight.setPower(backRightPower);
  110
  111         boolean buttonA = gamepad1.a;
  112         Tracker.publish("button/a", buttonA);
```
