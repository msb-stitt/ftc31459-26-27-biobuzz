# L050BlocksOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-rules.1`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L050

```{code-block} diff
:class: answer-diff

--- mytry/L050BlocksOpMode.java  (before L050)
+++ mytry/L050BlocksOpMode.java  (after L050)
@@ lines 33 to 42 @@
   33         // When on L050S040, see
   34         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l050.html#l050s040
   35         // for what to do here.
+  36         hardware.frontLeft.setDirection(hardware.mecanumConfig.frontLeftDirection.get());
+  37         hardware.frontRight.setDirection(hardware.mecanumConfig.frontRightDirection.get());
+  38         hardware.backLeft.setDirection(hardware.mecanumConfig.backLeftDirection.get());
+  39         hardware.backRight.setDirection(hardware.mecanumConfig.backRightDirection.get());
   40
   41         initAfter();
   42     }
```
