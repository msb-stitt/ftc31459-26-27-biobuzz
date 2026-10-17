# L070ButtonsOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-mlt-fable.1`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L070

```{code-block} diff
:class: answer-diff

--- mytry/L070ButtonsOpMode.java  (before L070)
+++ mytry/L070ButtonsOpMode.java  (after L070)
@@ lines 30 to 36 @@
   30     // When on L070S040, see
   31     // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l070.html#l070s040
   32     // for what to do here.
+  33     boolean previousButtonA;
   34
   35     // 'public' to match the parent class. 'public' allows access from any code.
   36     @Override
@@ lines 65 to 74 @@
   65         // When on L070S060, see
   66         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l070.html#l070s060
   67         // for what to do here.
+  68         double leftStickX = gamepad1.left_stick_x;
+  69         Tracker.publish("stick/leftX", leftStickX);
+  70         double rightStickX = gamepad1.right_stick_x;
+  71         Tracker.publish("stick/rightX", rightStickX);
   72
   73         double leftSpeed = -leftStickY;
   74         Tracker.publish("speed/left", leftSpeed);
@@ lines 92 to 111 @@
   92         // When on L070S030, see
   93         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l070.html#l070s030
   94         // for what to do here.
+  95         boolean buttonA = gamepad1.a;
+  96         Tracker.publish("button/a", buttonA);
   97
   98         // When on L070S050, see
   99         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l070.html#l070s050
  100         // for what to do here.
+ 101         if (buttonA != previousButtonA) {
+ 102             if (buttonA) {
+ 103                 Tracker.publish("button/event", "A pressed");
+ 104             } else {
+ 105                 Tracker.publish("button/event", "A released");
+ 106             }
+ 107         }
+ 108         previousButtonA = buttonA;
  109
  110         loopAfter();
  111     }
```
