# L040TankOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-rules.1`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L040

```{code-block} diff
:class: answer-diff

--- mytry/L040TankOpMode.java  (before L040)
+++ mytry/L040TankOpMode.java  (after L040)
@@ lines 58 to 71 @@
   58         // When on L040S030, see
   59         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l040.html#l040s030
   60         // for what to do here.
+  61         hardware.frontLeft.setPower(leftSpeed);
+  62         hardware.backLeft.setPower(leftSpeed);
   63
   64         // When on L040S040, see
   65         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l040.html#l040s040
   66         // for what to do here.
+  67         hardware.frontRight.setPower(rightSpeed);
+  68         hardware.backRight.setPower(rightSpeed);
   69
   70         loopAfter();
   71     }
```
