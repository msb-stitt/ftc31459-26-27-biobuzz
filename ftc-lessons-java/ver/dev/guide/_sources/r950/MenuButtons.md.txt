# MenuButtons

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111.1`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L093

```{code-block} diff
:class: answer-diff

--- mytry/l093/MenuButtons.java  (before L093)
+++ mytry/l093/MenuButtons.java  (after L093)
@@ lines 1 to 7 @@
+   1 package org.firstinspires.ftc.teamcode.mytry.l093;
+   2
+   3 import com.qualcomm.robotcore.hardware.Gamepad;
+   4
    5 /**
    6  * The OpMode loop's work of checking the gamepad's buttons, and telling an
    7  * {@link OptionMenu} what to do for each press.
@@ lines 11 to 43 @@
   11 // When on L093S090, see
   12 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l093.html#l093s090
   13 // for what to write in this file.
+  14 class MenuButtons {
+  15
+  16     // 'private' so only this class can use it. 'private' allows access from this class only.
+  17     private OptionMenu optionMenu;
+  18
+  19     MenuButtons(OptionMenu optionMenu) {
+  20         this.optionMenu = optionMenu;
+  21     }
+  22
+  23     void loop(Gamepad gamepad) {
+  24         if (gamepad.dpadUpWasPressed()) {
+  25             optionMenu.goUp();
+  26         }
+  27         if (gamepad.dpadDownWasPressed()) {
+  28             optionMenu.goDown();
+  29         }
+  30         if (gamepad.dpadLeftWasPressed()) {
+  31             optionMenu.goLeft();
+  32         }
+  33         if (gamepad.dpadRightWasPressed()) {
+  34             optionMenu.goRight();
+  35         }
+  36         if (gamepad.aWasPressed()) {
+  37             optionMenu.select();
+  38         }
+  39         if (gamepad.bWasPressed()) {
+  40             optionMenu.goBack();
+  41         }
+  42     }
+  43 }
```
