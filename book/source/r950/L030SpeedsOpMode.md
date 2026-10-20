# L030SpeedsOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-mlt-fable.1`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L030

```{code-block} diff
:class: answer-diff

--- mytry/L030SpeedsOpMode.java  (before L030)
+++ mytry/L030SpeedsOpMode.java  (after L030)
@@ lines 51 to 64 @@
   51         // When on L030S030, see
   52         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l030.html#l030s030
   53         // for what to do here.
+  54         double leftSpeed = -leftStickY;
+  55         Tracker.publish("speed/left", leftSpeed);
   56
   57         // When on L030S040, see
   58         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l030.html#l030s040
   59         // for what to do here.
+  60         double rightSpeed = -rightStickY;
+  61         Tracker.publish("speed/right", rightSpeed);
   62
   63         loopAfter();
   64     }
```
