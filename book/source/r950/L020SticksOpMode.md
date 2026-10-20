# L020SticksOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-remove-buttons.1`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L020

```{code-block} diff
:class: answer-diff

--- mytry/L020SticksOpMode.java  (before L020)
+++ mytry/L020SticksOpMode.java  (after L020)
@@ lines 7 to 13 @@
    7 import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
    8
    9 import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
+  10 import org.firstinspires.ftc.teamcode.base.Tracker;
   11
   12 /**
   13  * L020: read the two sticks and write down what they say. Nothing moves.
@@ lines 57 to 74 @@
   57         // When on L020S030, see
   58         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l020.html#l020s030
   59         // for what to do here.
+  60         double leftStickY = gamepad1.left_stick_y;
   61
   62         // When on L020S040, see
   63         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l020.html#l020s040
   64         // for what to do here.
+  65         Tracker.publish("stick/leftY", leftStickY);
   66
   67         // When on L020S050, see
   68         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l020.html#l020s050
   69         // for what to do here.
+  70         double rightStickY = gamepad1.right_stick_y;
+  71         Tracker.publish("stick/rightY", rightStickY);
   72     }
   73
   74     // 'public' to match the parent class. 'public' allows access from any code.
```
