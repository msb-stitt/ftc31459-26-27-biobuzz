# L020SticksOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-rules.1`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L020

```{code-block} diff
:class: answer-diff

--- mytry/L020SticksOpMode.java  (before L020)
+++ mytry/L020SticksOpMode.java  (after L020)
@@ lines 4 to 10 @@
    4 import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
    5
    6 import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
+   7 import org.firstinspires.ftc.teamcode.base.Tracker;
    8
    9 /**
   10  * L020: read the two sticks and write down what they say. Nothing moves.
@@ lines 46 to 63 @@
   46         // When on L020S030, see
   47         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l020.html#l020s030
   48         // for what to do here.
+  49         double leftStickY = gamepad1.left_stick_y;
   50
   51         // When on L020S040, see
   52         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l020.html#l020s040
   53         // for what to do here.
+  54         Tracker.publish("stick/leftY", leftStickY);
   55
   56         // When on L020S050, see
   57         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l020.html#l020s050
   58         // for what to do here.
+  59         double rightStickY = gamepad1.right_stick_y;
+  60         Tracker.publish("stick/rightY", rightStickY);
   61
   62         loopAfter();
   63     }
```
