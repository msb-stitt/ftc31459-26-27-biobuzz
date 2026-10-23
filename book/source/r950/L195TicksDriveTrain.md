# L195TicksDriveTrain

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-coding-stitt-cc`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L195

```{code-block} diff
:class: answer-diff

--- mytry/L195TicksDriveTrain.java  (before L195)
+++ mytry/L195TicksDriveTrain.java  (after L195)
@@ lines 30 to 39 @@
   30         // When on L195S030, see
   31         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l195.html#l195s030
   32         // for what to do here.
+  33         ticks[FL] = hardware.frontLeft.getCurrentPosition();
+  34         ticks[FR] = hardware.frontRight.getCurrentPosition();
+  35         ticks[BL] = hardware.backLeft.getCurrentPosition();
+  36         ticks[BR] = hardware.backRight.getCurrentPosition();
   37         return ticks;
   38     }
   39 }
```
