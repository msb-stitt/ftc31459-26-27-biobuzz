# L135DriverStationMenu

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-l135-menu-classes.1`. A line marked + is new, and a line marked - is
gone; each number is the line's place in the file after the lesson.

## L135

```{code-block} diff
:class: answer-diff

--- mytry/L135DriverStationMenu.java  (before L135)
+++ mytry/L135DriverStationMenu.java  (after L135)
@@ lines 1 to 9 @@
+   1 package org.firstinspires.ftc.teamcode.mytry;
+   2
+   3 import com.qualcomm.robotcore.hardware.Gamepad;
+   4
+   5 import org.firstinspires.ftc.teamcode.base.Tracker;
+   6
    7 /**
    8  * A menu on the Driver Station, worked with a gamepad between INIT and PLAY,
    9  * for an OpMode to make as a field and call from init_loop(). The menu and its
@@ lines 16 to 52 @@
   16 // When on L135S120, see
   17 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s120
   18 // for what to write in this file.
+  19 class L135DriverStationMenu {
+  20
+  21     L135OptionMenu optionMenu;
+  22     L135MenuButtons menuButtons;
+  23
+  24     L135DriverStationMenu(L135OptionMenu optionMenu) {
+  25         this.optionMenu = optionMenu;
+  26         menuButtons = new L135MenuButtons(optionMenu);
+  27     }
+  28
+  29     void loop(Gamepad gamepad) {
+  30         menuButtons.loop(gamepad);
+  31         String summary = optionMenu.summary();
+  32         Tracker.publish("menu/summary", summary);
+  33         String[] lines = optionMenu.lines();
+  34         for (int i = 0; i < lines.length; i++) {
+  35             Tracker.printToDs(lines[i]);
+  36         }
+  37     }
+  38
+  39     String chosen(String rowName) {
+  40         String fullName = optionMenu.chosen(rowName);
+  41         return fullName;
+  42     }
+  43
+  44     void startAt(String rowName, String fullName) {
+  45         optionMenu.startAt(rowName, fullName);
+  46     }
+  47
+  48     String summary() {
+  49         String summary = optionMenu.summary();
+  50         return summary;
+  51     }
+  52 }
```
