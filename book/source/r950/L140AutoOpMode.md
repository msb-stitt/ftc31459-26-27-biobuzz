# L140AutoOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L140

```{code-block} diff
:class: answer-diff

--- mytry/L140AutoOpMode.java  (before L140)
+++ mytry/L140AutoOpMode.java  (after L140)
@@ lines 19 to 28 @@
   19 // When on L140S020, see
   20 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s020
   21 // for what to do here.
-     //import org.firstinspires.ftc.teamcode.mytry.l135.DriverStationMenu;
-     //import org.firstinspires.ftc.teamcode.mytry.l135.MenuItem;
-     //import org.firstinspires.ftc.teamcode.mytry.l135.MenuRow;
-     //import org.firstinspires.ftc.teamcode.mytry.l135.OptionMenu;
+  22 import org.firstinspires.ftc.teamcode.mytry.l135.DriverStationMenu;
+  23 import org.firstinspires.ftc.teamcode.mytry.l135.MenuItem;
+  24 import org.firstinspires.ftc.teamcode.mytry.l135.MenuRow;
+  25 import org.firstinspires.ftc.teamcode.mytry.l135.OptionMenu;
   26
   27 /**
   28  * L140: L135's auto, with a choice to do nothing, which hands its alliance to
@@ lines 44 to 58 @@
   44     // When on L140S020, see
   45     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s020
   46     // for what to do here.
-     //    MenuItem red = new MenuItem("R", "Red");
-     //    MenuItem blue = new MenuItem("B", "Blue");
-     //    MenuRow allianceRow = new MenuRow("Alliance");
-     //    MenuItem nothing = new MenuItem("N", "Nothing");
-     //    MenuItem forward24 = new MenuItem("F24", "Forward 24");
-     //    MenuItem forwardThenStrafe = new MenuItem("FS", "Forward Then Strafe");
-     //    MenuRow autoRow = new MenuRow("Auto");
-     //    OptionMenu optionMenu = new OptionMenu();
-     //    DriverStationMenu menu = new DriverStationMenu(optionMenu);
+  47     MenuItem red = new MenuItem("R", "Red");
+  48     MenuItem blue = new MenuItem("B", "Blue");
+  49     MenuRow allianceRow = new MenuRow("Alliance");
+  50     MenuItem nothing = new MenuItem("N", "Nothing");
+  51     MenuItem forward24 = new MenuItem("F24", "Forward 24");
+  52     MenuItem forwardThenStrafe = new MenuItem("FS", "Forward Then Strafe");
+  53     MenuRow autoRow = new MenuRow("Auto");
+  54     OptionMenu optionMenu = new OptionMenu();
+  55     DriverStationMenu menu = new DriverStationMenu(optionMenu);
   56
   57     double robotHalfLength_in = 9.0;
   58     double fieldPerimeterWidth_in = 1.5;
@@ lines 66 to 78 @@
   66         // When on L140S020, see
   67         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s020
   68         // for what to do here.
-     //        allianceRow.add(red);
-     //        allianceRow.add(blue);
-     //        autoRow.add(nothing);
-     //        autoRow.add(forward24);
-     //        autoRow.add(forwardThenStrafe);
-     //        optionMenu.add(allianceRow);
-     //        optionMenu.add(autoRow);
+  69         allianceRow.add(red);
+  70         allianceRow.add(blue);
+  71         autoRow.add(nothing);
+  72         autoRow.add(forward24);
+  73         autoRow.add(forwardThenStrafe);
+  74         optionMenu.add(allianceRow);
+  75         optionMenu.add(autoRow);
   76         initBefore();
   77         drivetrain = new CorbelsMecanum(hardware);
   78         initAfter(drivetrain);
@@ lines 85 to 91 @@
   85         // When on L140S020, see
   86         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s020
   87         // for what to do here.
-     //        menu.loop(gamepad1);
+  88         menu.loop(gamepad1);
   89         initLoopAfter();
   90     }
   91
@@ lines 96 to 115 @@
   96         // When on L140S090, see
   97         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s090
   98         // for what to do here.
+  99         OpModeStorage.isRed = allianceRow.getSelected() == red;
  100
  101         // When on L140S020, see
  102         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s020
  103         // for what to do here.
-     //        MenuItem auto = autoRow.getSelected();
-     //        Command chosenAuto = Command.NOOP;
-     //        if (auto == forward24) {
-     //            chosenAuto = driveForward24();
-     //        }
-     //        if (auto == forwardThenStrafe) {
-     //            chosenAuto = driveForwardThenStrafe();
-     //        }
-     //        Scheduler.schedule(chosenAuto);
+ 104         MenuItem auto = autoRow.getSelected();
+ 105         Command chosenAuto = Command.NOOP;
+ 106         if (auto == forward24) {
+ 107             chosenAuto = driveForward24();
+ 108         }
+ 109         if (auto == forwardThenStrafe) {
+ 110             chosenAuto = driveForwardThenStrafe();
+ 111         }
+ 112         Scheduler.schedule(chosenAuto);
  113         startAfter();
  114     }
  115
@@ lines 120 to 126 @@
  120         // When on L140S020, see
  121         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s020
  122         // for what to do here.
-     //        Tracker.printToDs(menu.getSummary());
+ 123         Tracker.printToDs(menu.getSummary());
  124         Tracker.printPoseSpeedLoopToDs(follower);
  125         loopAfter();
  126     }
@@ lines 138 to 146 @@
  138         // When on L140S020, see
  139         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s020
  140         // for what to do here.
-     //        if (allianceRow.getSelected() == blue) {
-     //            chosenPoses = redPoses.rotateAround(72.0, 72.0, 180.0);
-     //        }
+ 141         if (allianceRow.getSelected() == blue) {
+ 142             chosenPoses = redPoses.rotateAround(72.0, 72.0, 180.0);
+ 143         }
  144         return chosenPoses;
  145     }
  146
```
