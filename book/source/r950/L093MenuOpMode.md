# L093MenuOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L093

```{code-block} diff
:class: answer-diff

--- mytry/l093/L093MenuOpMode.java  (before L093)
+++ mytry/l093/L093MenuOpMode.java  (after L093)
@@ lines 24 to 42 @@
   24     // When on L093S110, see
   25     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l093.html#l093s110
   26     // for what to do here.
+  27     MenuItem on = new MenuItem("ON", "On");
+  28     MenuItem off = new MenuItem("OFF", "Off");
+  29     MenuRow lightRow = new MenuRow("Light");
+  30     MenuItem slow = new MenuItem("S", "Slow");
+  31     MenuItem medium = new MenuItem("M", "Medium");
+  32     MenuItem fast = new MenuItem("F", "Fast");
+  33     MenuRow speedRow = new MenuRow("Speed");
+  34     MenuItem red = new MenuItem("R", "Red");
+  35     MenuItem green = new MenuItem("G", "Green");
+  36     MenuItem blue = new MenuItem("B", "Blue");
+  37     MenuRow colorRow = new MenuRow("Color");
+  38     OptionMenu optionMenu = new OptionMenu();
+  39     DriverStationMenu menu = new DriverStationMenu(optionMenu);
   40
   41     // 'public' to match the parent class. 'public' allows access from any code.
   42     @Override
@@ lines 44 to 60 @@
   44         // When on L093S110, see
   45         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l093.html#l093s110
   46         // for what to do here.
+  47         lightRow.add(on);
+  48         lightRow.add(off);
+  49         speedRow.add(slow);
+  50         speedRow.add(medium);
+  51         speedRow.add(fast);
+  52         colorRow.add(red);
+  53         colorRow.add(green);
+  54         colorRow.add(blue);
+  55         optionMenu.add(lightRow);
+  56         optionMenu.add(speedRow);
+  57         optionMenu.add(colorRow);
   58         initBefore();
   59         initAfter();
   60     }
@@ lines 66 to 72 @@
   66         // When on L093S110, see
   67         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l093.html#l093s110
   68         // for what to do here.
+  69         menu.loop(gamepad1);
   70         initLoopAfter();
   71     }
   72
@@ lines 84 to 91 @@
   84         // When on L093S110, see
   85         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l093.html#l093s110
   86         // for what to do here.
+  87         String summary = menu.getSummary();
+  88         Tracker.printToDs(summary);
   89         loopAfter();
   90     }
   91
```
