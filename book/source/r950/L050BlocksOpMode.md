# L050BlocksOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-l135-menu-classes.2`. A line marked + is new, and a line marked - is
gone; each number is the line's place in the file after the lesson.

## L050

```{code-block} diff
:class: answer-diff

--- mytry/L050BlocksOpMode.java  (before L050)
+++ mytry/L050BlocksOpMode.java  (after L050)
@@ lines 38 to 47 @@
   38         // When on L050S040, see
   39         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l050.html#l050s040
   40         // for what to do here.
+  41         hardware.frontLeft.setDirection(hardware.mecanumConfig.frontLeftDirection.get());
+  42         hardware.frontRight.setDirection(hardware.mecanumConfig.frontRightDirection.get());
+  43         hardware.backLeft.setDirection(hardware.mecanumConfig.backLeftDirection.get());
+  44         hardware.backRight.setDirection(hardware.mecanumConfig.backRightDirection.get());
   45
   46         initAfter();
   47     }
```
