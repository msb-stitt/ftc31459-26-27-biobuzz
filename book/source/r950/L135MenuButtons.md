# L135MenuButtons

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l135-menu-classes`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L135

```{code-block} diff
:class: answer-diff

--- mytry/L135MenuButtons.java  (before L135)
+++ mytry/L135MenuButtons.java  (after L135)
@@ lines 1 to 7 @@
+   1 package org.firstinspires.ftc.teamcode.mytry;
+   2
+   3 import com.qualcomm.robotcore.hardware.Gamepad;
+   4
    5 /**
    6  * Turns each new press of a gamepad's D-pad, A and B into one move of an
    7  * {@link L135OptionMenu}.
@@ lines 11 to 42 @@
   11 // When on L135S110, see
   12 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s110
   13 // for what to write in this file.
+  14 class L135MenuButtons {
+  15
+  16     L135OptionMenu optionMenu;
+  17
+  18     L135MenuButtons(L135OptionMenu optionMenu) {
+  19         this.optionMenu = optionMenu;
+  20     }
+  21
+  22     void loop(Gamepad gamepad) {
+  23         if (gamepad.dpadUpWasPressed()) {
+  24             optionMenu.up();
+  25         }
+  26         if (gamepad.dpadDownWasPressed()) {
+  27             optionMenu.down();
+  28         }
+  29         if (gamepad.dpadLeftWasPressed()) {
+  30             optionMenu.left();
+  31         }
+  32         if (gamepad.dpadRightWasPressed()) {
+  33             optionMenu.right();
+  34         }
+  35         if (gamepad.aWasPressed()) {
+  36             optionMenu.select();
+  37         }
+  38         if (gamepad.bWasPressed()) {
+  39             optionMenu.back();
+  40         }
+  41     }
+  42 }
```
