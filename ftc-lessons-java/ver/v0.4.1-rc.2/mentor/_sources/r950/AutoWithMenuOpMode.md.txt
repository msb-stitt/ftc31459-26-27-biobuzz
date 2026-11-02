# AutoWithMenuOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-l135-menu-classes.3`. A line marked + is new, and a line marked - is
gone; each number is the line's place in the file after the lesson.

## L135

```{code-block} diff
:class: answer-diff

--- mytry/l135/AutoWithMenuOpMode.java  (before L135)
+++ mytry/l135/AutoWithMenuOpMode.java  (after L135)
@@ lines 41 to 55 @@
   41     // When on L135S100, see
   42     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s100
   43     // for what to do here.
+  44     MenuItem red = new MenuItem("R", "Red");
+  45     MenuItem blue = new MenuItem("B", "Blue");
+  46     MenuRow allianceRow = new MenuRow("Alliance");
+  47     MenuItem nothing = new MenuItem("N", "Nothing");
+  48     MenuItem forward24 = new MenuItem("F24", "Forward 24");
+  49     MenuItem forwardThenStrafe = new MenuItem("FS", "Forward Then Strafe");
+  50     MenuRow autoRow = new MenuRow("Auto");
+  51     OptionMenu optionMenu = new OptionMenu();
+  52     DriverStationMenu menu = new DriverStationMenu(optionMenu);
   53
   54     double robotHalfLength_in = 9.0;
   55     double fieldPerimeterWidth_in = 1.5;
@@ lines 63 to 80 @@
   63         // When on L135S100, see
   64         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s100
   65         // for what to do here.
+  66         if (OpModeStorage.isRed) {
+  67             allianceRow.add(red);
+  68             allianceRow.add(blue);
+  69         } else {
+  70             allianceRow.add(blue);
+  71             allianceRow.add(red);
+  72         }
+  73         autoRow.add(nothing);
+  74         autoRow.add(forward24);
+  75         autoRow.add(forwardThenStrafe);
+  76         optionMenu.add(allianceRow);
+  77         optionMenu.add(autoRow);
   78         initBefore();
   79         drivetrain = new CorbelsMecanum(hardware);
   80         initAfter(drivetrain);
@@ lines 87 to 93 @@
   87         // When on L135S100, see
   88         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s100
   89         // for what to do here.
+  90         menu.loop(gamepad1);
   91         initLoopAfter();
   92     }
   93
@@ lines 98 to 118 @@
   98         // When on L135S110, see
   99         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s110
  100         // for what to do here.
+ 101         MenuItem alliance = allianceRow.getSelected();
+ 102         String allianceName = alliance.getFullName();
+ 103         Tracker.publish("menu/alliance", allianceName);
+ 104
+ 105         MenuItem auto = autoRow.getSelected();
+ 106         String autoName = auto.getFullName();
+ 107         Tracker.publish("menu/auto", autoName);
+ 108         Command chosenAuto = Command.NOOP;
+ 109         if (auto == forward24) {
+ 110             chosenAuto = driveForward24();
+ 111         }
+ 112         if (auto == forwardThenStrafe) {
+ 113             chosenAuto = driveForwardThenStrafe();
+ 114         }
+ 115         Scheduler.schedule(chosenAuto);
  116         startAfter();
  117     }
  118
@@ lines 123 to 130 @@
  123         // When on L135S110, see
  124         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s110
  125         // for what to do here.
+ 126         String summary = menu.getSummary();
+ 127         Tracker.printToDs(summary);
  128         Tracker.printPoseSpeedLoopToDs(follower);
  129         loopAfter();
  130     }
@@ lines 142 to 151 @@
  142         // When on L135S120, see
  143         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s120
  144         // for what to do here.
+ 145         MenuItem alliance = allianceRow.getSelected();
+ 146         if (alliance == blue) {
+ 147             chosenPoses = redPoses.rotateAround(72.0, 72.0, 180.0);
+ 148         }
  149         return chosenPoses;
  150     }
  151
```
