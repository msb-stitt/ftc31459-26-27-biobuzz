# L040TankOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-minor-fixes.1`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L040

```{code-block} diff
:class: answer-diff

--- mytry/L040TankOpMode.java  (before L040)
+++ mytry/L040TankOpMode.java  (after L040)
@@ lines 69 to 82 @@
   69         // When on L040S030, see
   70         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l040.html#l040s030
   71         // for what to do here.
+  72         hardware.frontLeft.setPower(leftSpeed);
+  73         hardware.backLeft.setPower(leftSpeed);
   74
   75         // When on L040S040, see
   76         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l040.html#l040s040
   77         // for what to do here.
+  78         hardware.frontRight.setPower(rightSpeed);
+  79         hardware.backRight.setPower(rightSpeed);
   80     }
   81
   82     // 'public' to match the parent class. 'public' allows access from any code.
```
