# L030SpeedsOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-auto-schedules`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L030

```{code-block} diff
:class: answer-diff

--- mytry/L030SpeedsOpMode.java  (before L030)
+++ mytry/L030SpeedsOpMode.java  (after L030)
@@ lines 62 to 75 @@
   62         // When on L030S030, see
   63         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l030.html#l030s030
   64         // for what to do here.
+  65         double leftSpeed = -leftStickY;
+  66         Tracker.publish("speed/left", leftSpeed);
   67
   68         // When on L030S040, see
   69         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l030.html#l030s040
   70         // for what to do here.
+  71         double rightSpeed = -rightStickY;
+  72         Tracker.publish("speed/right", rightSpeed);
   73     }
   74
   75     // 'public' to match the parent class. 'public' allows access from any code.
```
