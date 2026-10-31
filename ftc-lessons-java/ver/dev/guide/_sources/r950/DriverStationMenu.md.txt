# DriverStationMenu

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111.1`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L093

```{code-block} diff
:class: answer-diff

--- mytry/l093/DriverStationMenu.java  (before L093)
+++ mytry/l093/DriverStationMenu.java  (after L093)
@@ lines 1 to 11 @@
+   1 package org.firstinspires.ftc.teamcode.mytry.l093;
+   2
+   3 import com.qualcomm.robotcore.hardware.Gamepad;
+   4
+   5 import org.firstinspires.ftc.teamcode.base.Tracker;
+   6
+   7 import java.util.ArrayList;
+   8
    9 /**
   10  * A menu on the Driver Station, worked with a gamepad between INIT and PLAY,
   11  * for an OpMode to make as a field and call from init_loop(). The menu and its
@@ lines 18 to 51 @@
   18 // When on L093S100, see
   19 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l093.html#l093s100
   20 // for what to write in this file.
+  21 // 'public' so later lessons, in other packages, can use it. 'public' allows access from any code.
+  22 public class DriverStationMenu {
+  23
+  24     // 'private' so only this class can use it. 'private' allows access from this class only.
+  25     private OptionMenu optionMenu;
+  26     // 'private' so only this class can use it. 'private' allows access from this class only.
+  27     private MenuButtons menuButtons;
+  28
+  29     // 'public' so later lessons, in other packages, can use it. 'public' allows access from any code.
+  30     public DriverStationMenu(OptionMenu optionMenu) {
+  31         this.optionMenu = optionMenu;
+  32         menuButtons = new MenuButtons(optionMenu);
+  33     }
+  34
+  35     // 'public' so later lessons, in other packages, can use it. 'public' allows access from any code.
+  36     public void loop(Gamepad gamepad) {
+  37         menuButtons.loop(gamepad);
+  38         String summary = optionMenu.getSummary();
+  39         Tracker.publish("menu/summary", summary);
+  40         ArrayList<String> lines = optionMenu.getLines();
+  41         for (String line : lines) {
+  42             Tracker.printToDs(line);
+  43         }
+  44     }
+  45
+  46     // 'public' so later lessons, in other packages, can use it. 'public' allows access from any code.
+  47     public String getSummary() {
+  48         String summary = optionMenu.getSummary();
+  49         return summary;
+  50     }
+  51 }
```
