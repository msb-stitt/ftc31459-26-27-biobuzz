# L095ShapingMenuOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111.2`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L095

```{code-block} diff
:class: answer-diff

--- mytry/L095ShapingMenuOpMode.java  (before L095)
+++ mytry/L095ShapingMenuOpMode.java  (after L095)
@@ lines 11 to 21 @@
   11 // When on L095S020, see
   12 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l095.html#l095s020
   13 // for what to do here.
-     //import org.firstinspires.ftc.teamcode.mytry.l065.StickShaper;
-     //import org.firstinspires.ftc.teamcode.mytry.l093.DriverStationMenu;
-     //import org.firstinspires.ftc.teamcode.mytry.l093.MenuItem;
-     //import org.firstinspires.ftc.teamcode.mytry.l093.MenuRow;
-     //import org.firstinspires.ftc.teamcode.mytry.l093.OptionMenu;
+  14 import org.firstinspires.ftc.teamcode.mytry.l065.StickShaper;
+  15 import org.firstinspires.ftc.teamcode.mytry.l093.DriverStationMenu;
+  16 import org.firstinspires.ftc.teamcode.mytry.l093.MenuItem;
+  17 import org.firstinspires.ftc.teamcode.mytry.l093.MenuRow;
+  18 import org.firstinspires.ftc.teamcode.mytry.l093.OptionMenu;
   19
   20 /**
   21  * L095: L090's mecanum drive, with L093's menu choosing the deadband and the
@@ lines 34 to 56 @@
   34     // When on L095S020, see
   35     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l095.html#l095s020
   36     // for what to do here.
-     //    StickShaper stickShaper = new StickShaper(0.05);
+  37     StickShaper stickShaper = new StickShaper(0.05);
   38
   39     // When on L095S030, see
   40     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l095.html#l095s030
   41     // for what to do here.
+  42     MenuItem band005 = new MenuItem("0.05", "0.05");
+  43     MenuItem band000 = new MenuItem("0.00", "0.00");
+  44     MenuItem band0025 = new MenuItem("0.025", "0.025");
+  45     MenuItem band0075 = new MenuItem("0.075", "0.075");
+  46     MenuItem band010 = new MenuItem("0.10", "0.10");
+  47     MenuRow bandRow = new MenuRow("Band");
+  48     MenuItem squared = new MenuItem("S", "Squared");
+  49     MenuItem linear = new MenuItem("L", "Linear");
+  50     MenuItem cubed = new MenuItem("C", "Cubed");
+  51     MenuRow shapeRow = new MenuRow("Shape");
+  52     OptionMenu optionMenu = new OptionMenu();
+  53     DriverStationMenu menu = new DriverStationMenu(optionMenu);
   54
   55     // 'public' to match the parent class. 'public' allows access from any code.
   56     @Override
@@ lines 65 to 80 @@
   65         // When on L095S030, see
   66         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l095.html#l095s030
   67         // for what to do here.
+  68         bandRow.add(band005);
+  69         bandRow.add(band000);
+  70         bandRow.add(band0025);
+  71         bandRow.add(band0075);
+  72         bandRow.add(band010);
+  73         shapeRow.add(squared);
+  74         shapeRow.add(linear);
+  75         shapeRow.add(cubed);
+  76         optionMenu.add(bandRow);
+  77         optionMenu.add(shapeRow);
   78         initAfter();
   79     }
   80
@@ lines 85 to 91 @@
   85         // When on L095S030, see
   86         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l095.html#l095s030
   87         // for what to do here.
+  88         menu.loop(gamepad1);
   89         initLoopAfter();
   90     }
   91
@@ lines 96 to 131 @@
   96         // When on L095S040, see
   97         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l095.html#l095s040
   98         // for what to do here.
+  99         MenuItem band = bandRow.getSelected();
+ 100         Tracker.publish("menu/band", band.getFullName());
+ 101         if (band == band000) {
+ 102             stickShaper.setBand(0.00);
+ 103         }
+ 104         if (band == band0025) {
+ 105             stickShaper.setBand(0.025);
+ 106         }
+ 107         if (band == band005) {
+ 108             stickShaper.setBand(0.05);
+ 109         }
+ 110         if (band == band0075) {
+ 111             stickShaper.setBand(0.075);
+ 112         }
+ 113         if (band == band010) {
+ 114             stickShaper.setBand(0.10);
+ 115         }
+ 116
+ 117         MenuItem shape = shapeRow.getSelected();
+ 118         Tracker.publish("menu/shape", shape.getFullName());
+ 119         if (shape == linear) {
+ 120             stickShaper.setShape(StickShaper.Shape.LINEAR);
+ 121         }
+ 122         if (shape == squared) {
+ 123             stickShaper.setShape(StickShaper.Shape.SQUARED);
+ 124         }
+ 125         if (shape == cubed) {
+ 126             stickShaper.setShape(StickShaper.Shape.CUBED);
+ 127         }
+ 128
  129         Command driveFromTheSticksEveryLoop = Commands.infinite(this::driveFromTheSticks);
  130         Scheduler.schedule(driveFromTheSticksEveryLoop);
  131         startAfter();
@@ lines 138 to 145 @@
  138         // When on L095S030, see
  139         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l095.html#l095s030
  140         // for what to do here.
+ 141         String summary = menu.getSummary();
+ 142         Tracker.printToDs(summary);
  143         loopAfter();
  144     }
  145
@@ lines 156 to 174 @@
  156         // When on L095S020, see
  157         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l095.html#l095s020
  158         // for what to do here.
-     //        forwardXSpeed = stickShaper.shaped(forwardXSpeed);
+ 159         forwardXSpeed = stickShaper.shaped(forwardXSpeed);
  160         Tracker.publish("drivetrain/forwardXSpeed", forwardXSpeed);
  161         double turnCcwSpeed = -rightStickX;
  162         // When on L095S020, see
  163         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l095.html#l095s020
  164         // for what to do here.
-     //        turnCcwSpeed = stickShaper.shaped(turnCcwSpeed);
+ 165         turnCcwSpeed = stickShaper.shaped(turnCcwSpeed);
  166         Tracker.publish("drivetrain/turnCcwSpeed", turnCcwSpeed);
  167         double strafeYSpeed = -leftStickX;
  168         // When on L095S020, see
  169         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l095.html#l095s020
  170         // for what to do here.
-     //        strafeYSpeed = stickShaper.shaped(strafeYSpeed);
+ 171         strafeYSpeed = stickShaper.shaped(strafeYSpeed);
  172         Tracker.publish("drivetrain/strafeYSpeed", strafeYSpeed);
  173
  174         double fl_mixedSpeed = forwardXSpeed - strafeYSpeed - turnCcwSpeed;
```
