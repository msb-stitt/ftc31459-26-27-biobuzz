# AutoWithMenuOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L135

```{code-block} diff
:class: answer-diff

--- mytry/l135/AutoWithMenuOpMode.java  (before L135)
+++ mytry/l135/AutoWithMenuOpMode.java  (after L135)
@@ lines 19 to 28 @@
   19 // When on L135S020, see
   20 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s020
   21 // for what to do here.
-     //import org.firstinspires.ftc.teamcode.mytry.l093.DriverStationMenu;
-     //import org.firstinspires.ftc.teamcode.mytry.l093.MenuItem;
-     //import org.firstinspires.ftc.teamcode.mytry.l093.MenuRow;
-     //import org.firstinspires.ftc.teamcode.mytry.l093.OptionMenu;
+  22 import org.firstinspires.ftc.teamcode.mytry.l093.DriverStationMenu;
+  23 import org.firstinspires.ftc.teamcode.mytry.l093.MenuItem;
+  24 import org.firstinspires.ftc.teamcode.mytry.l093.MenuRow;
+  25 import org.firstinspires.ftc.teamcode.mytry.l093.OptionMenu;
   26
   27 /**
   28  * L135: choose the alliance and the auto on the Driver Station between INIT
@@ lines 48 to 62 @@
   48     // When on L135S030, see
   49     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s030
   50     // for what to do here.
+  51     MenuItem red = new MenuItem("R", "Red");
+  52     MenuItem blue = new MenuItem("B", "Blue");
+  53     MenuRow allianceRow = new MenuRow("Alliance");
+  54     MenuItem nothing = new MenuItem("N", "Nothing");
+  55     MenuItem forward24 = new MenuItem("F24", "Forward 24");
+  56     MenuItem forwardThenStrafe = new MenuItem("FS", "Forward Then Strafe");
+  57     MenuRow autoRow = new MenuRow("Auto");
+  58     OptionMenu optionMenu = new OptionMenu();
+  59     DriverStationMenu menu = new DriverStationMenu(optionMenu);
   60
   61     double robotHalfLength_in = 9.0;
   62     double fieldPerimeterWidth_in = 1.5;
@@ lines 70 to 87 @@
   70         // When on L135S030, see
   71         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s030
   72         // for what to do here.
+  73         if (OpModeStorage.isRed) {
+  74             allianceRow.add(red);
+  75             allianceRow.add(blue);
+  76         } else {
+  77             allianceRow.add(blue);
+  78             allianceRow.add(red);
+  79         }
+  80         autoRow.add(nothing);
+  81         autoRow.add(forward24);
+  82         autoRow.add(forwardThenStrafe);
+  83         optionMenu.add(allianceRow);
+  84         optionMenu.add(autoRow);
   85         initBefore();
   86         drivetrain = new CorbelsMecanum(hardware);
   87         initAfter(drivetrain);
@@ lines 94 to 100 @@
   94         // When on L135S030, see
   95         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s030
   96         // for what to do here.
+  97         menu.loop(gamepad1);
   98         initLoopAfter();
   99     }
  100
@@ lines 105 to 125 @@
  105         // When on L135S040, see
  106         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s040
  107         // for what to do here.
+ 108         MenuItem alliance = allianceRow.getSelected();
+ 109         String allianceName = alliance.getFullName();
+ 110         Tracker.publish("menu/alliance", allianceName);
+ 111
+ 112         MenuItem auto = autoRow.getSelected();
+ 113         String autoName = auto.getFullName();
+ 114         Tracker.publish("menu/auto", autoName);
+ 115         Command chosenAuto = Command.NOOP;
+ 116         if (auto == forward24) {
+ 117             chosenAuto = driveForward24();
+ 118         }
+ 119         if (auto == forwardThenStrafe) {
+ 120             chosenAuto = driveForwardThenStrafe();
+ 121         }
+ 122         Scheduler.schedule(chosenAuto);
  123         startAfter();
  124     }
  125
@@ lines 130 to 137 @@
  130         // When on L135S040, see
  131         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s040
  132         // for what to do here.
+ 133         String summary = menu.getSummary();
+ 134         Tracker.printToDs(summary);
  135         Tracker.printPoseSpeedLoopToDs(follower);
  136         loopAfter();
  137     }
@@ lines 149 to 158 @@
  149         // When on L135S050, see
  150         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s050
  151         // for what to do here.
+ 152         MenuItem alliance = allianceRow.getSelected();
+ 153         if (alliance == blue) {
+ 154             chosenPoses = redPoses.rotateAround(72.0, 72.0, 180.0);
+ 155         }
  156         return chosenPoses;
  157     }
  158
```
