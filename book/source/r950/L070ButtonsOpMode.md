# L070ButtonsOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-minor-fixes`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L070

```{code-block} diff
:class: answer-diff

--- mytry/L070ButtonsOpMode.java  (before L070)
+++ mytry/L070ButtonsOpMode.java  (after L070)
@@ lines 35 to 41 @@
   35     // When on L070S040, see
   36     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l070.html#l070s040
   37     // for what to do here.
+  38     boolean previousButtonA;
   39
   40     // 'public' to match the parent class. 'public' allows access from any code.
   41     @Override
@@ lines 76 to 85 @@
   76         // When on L070S060, see
   77         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l070.html#l070s060
   78         // for what to do here.
+  79         double leftStickX = gamepad1.left_stick_x;
+  80         Tracker.publish("stick/leftX", leftStickX);
+  81         double rightStickX = gamepad1.right_stick_x;
+  82         Tracker.publish("stick/rightX", rightStickX);
   83
   84         double leftSpeed = -leftStickY;
   85         Tracker.publish("speed/left", leftSpeed);
@@ lines 103 to 122 @@
  103         // When on L070S030, see
  104         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l070.html#l070s030
  105         // for what to do here.
+ 106         boolean buttonA = gamepad1.a;
+ 107         Tracker.publish("button/a", buttonA);
  108
  109         // When on L070S050, see
  110         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l070.html#l070s050
  111         // for what to do here.
+ 112         if (buttonA != previousButtonA) {
+ 113             if (buttonA) {
+ 114                 Tracker.publish("button/event", "A pressed");
+ 115             } else {
+ 116                 Tracker.publish("button/event", "A released");
+ 117             }
+ 118         }
+ 119         previousButtonA = buttonA;
  120     }
  121
  122     // 'public' to match the parent class. 'public' allows access from any code.
```
