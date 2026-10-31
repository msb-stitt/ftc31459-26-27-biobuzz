# L070ButtonsOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111.2`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L070

```{code-block} diff
:class: answer-diff

--- mytry/L070ButtonsOpMode.java  (before L070)
+++ mytry/L070ButtonsOpMode.java  (after L070)
@@ lines 11 to 17 @@
   11 // When on L070S020, see
   12 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l070.html#l070s020
   13 // for what to do here.
-     //import org.firstinspires.ftc.teamcode.mytry.l065.StickShaper;
+  14 import org.firstinspires.ftc.teamcode.mytry.l065.StickShaper;
   15
   16 /**
   17  * L070: the A button, the moment it changes, and both sticks sideways, all
@@ lines 39 to 50 @@
   39     // When on L070S020, see
   40     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l070.html#l070s020
   41     // for what to do here.
-     //    StickShaper stickShaper = new StickShaper(0.05);
+  42     StickShaper stickShaper = new StickShaper(0.05);
   43
   44     // When on L070S040, see
   45     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l070.html#l070s040
   46     // for what to do here.
+  47     boolean previousButtonA;
   48
   49     // 'public' to match the parent class. 'public' allows access from any code.
   50     @Override
@@ lines 85 to 94 @@
   85         // When on L070S060, see
   86         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l070.html#l070s060
   87         // for what to do here.
+  88         double leftStickX = gamepad1.left_stick_x;
+  89         Tracker.publish("gamepad1/leftStickX", leftStickX);
+  90         double rightStickX = gamepad1.right_stick_x;
+  91         Tracker.publish("gamepad1/rightStickX", rightStickX);
   92
   93         double leftSpeed = -leftStickY;
   94         Tracker.publish("drivetrain/leftSpeed", leftSpeed);
@@ lines 98 to 131 @@
   98         // When on L070S020, see
   99         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l070.html#l070s020
  100         // for what to do here.
-     //        double leftShapedSpeed = stickShaper.shaped(leftSpeed);
-     //        Tracker.publish("drivetrain/leftShapedSpeed", leftShapedSpeed);
-     //        double rightShapedSpeed = stickShaper.shaped(rightSpeed);
-     //        Tracker.publish("drivetrain/rightShapedSpeed", rightShapedSpeed);
-     //
-     //        hardware.frontLeft.setPower(leftShapedSpeed);
-     //        hardware.backLeft.setPower(leftShapedSpeed);
-     //
-     //        hardware.frontRight.setPower(rightShapedSpeed);
-     //        hardware.backRight.setPower(rightShapedSpeed);
+ 101         double leftShapedSpeed = stickShaper.shaped(leftSpeed);
+ 102         Tracker.publish("drivetrain/leftShapedSpeed", leftShapedSpeed);
+ 103         double rightShapedSpeed = stickShaper.shaped(rightSpeed);
+ 104         Tracker.publish("drivetrain/rightShapedSpeed", rightShapedSpeed);
+ 105
+ 106         hardware.frontLeft.setPower(leftShapedSpeed);
+ 107         hardware.backLeft.setPower(leftShapedSpeed);
+ 108
+ 109         hardware.frontRight.setPower(rightShapedSpeed);
+ 110         hardware.backRight.setPower(rightShapedSpeed);
  111
  112         // When on L070S030, see
  113         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l070.html#l070s030
  114         // for what to do here.
+ 115         boolean buttonA = gamepad1.a;
+ 116         Tracker.publish("gamepad1/buttonA", buttonA);
  117
  118         // When on L070S050, see
  119         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l070.html#l070s050
  120         // for what to do here.
+ 121         if (buttonA != previousButtonA) {
+ 122             if (buttonA) {
+ 123                 Tracker.publish("gamepad1/buttonAEvent", "A pressed");
+ 124             } else {
+ 125                 Tracker.publish("gamepad1/buttonAEvent", "A released");
+ 126             }
+ 127         }
+ 128         previousButtonA = buttonA;
  129     }
  130
  131     // 'public' to match the parent class. 'public' allows access from any code.
```
