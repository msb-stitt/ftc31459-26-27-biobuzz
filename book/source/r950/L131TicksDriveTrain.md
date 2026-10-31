# L131TicksDriveTrain

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L131

```{code-block} diff
:class: answer-diff

--- mytry/L131TicksDriveTrain.java  (before L131)
+++ mytry/L131TicksDriveTrain.java  (after L131)
@@ lines 30 to 39 @@
   30         // When on L131S030, see
   31         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l131.html#l131s030
   32         // for what to do here.
+  33         ticks[FL] = hardware.frontLeft.getCurrentPosition();
+  34         ticks[FR] = hardware.frontRight.getCurrentPosition();
+  35         ticks[BL] = hardware.backLeft.getCurrentPosition();
+  36         ticks[BR] = hardware.backRight.getCurrentPosition();
   37         return ticks;
   38     }
   39 }
```
