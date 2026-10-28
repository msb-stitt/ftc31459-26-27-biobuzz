# L140AutoOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l135-menu-classes`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L140

```{code-block} diff
:class: answer-diff

--- mytry/L140AutoOpMode.java  (before L140)
+++ mytry/L140AutoOpMode.java  (after L140)
@@ lines 37 to 51 @@
   37     // When on L140S020, see
   38     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s020
   39     // for what to do here.
-     //    L135MenuItem red = new L135MenuItem("R", "Red");
-     //    L135MenuItem blue = new L135MenuItem("B", "Blue");
-     //    L135MenuRow allianceRow = new L135MenuRow("Alliance", red, blue);
-     //    L135MenuItem nothing = new L135MenuItem("N", "Nothing");
-     //    L135MenuItem forward24 = new L135MenuItem("F24", "Forward 24");
-     //    L135MenuItem forwardThenStrafe = new L135MenuItem("FS", "Forward Then Strafe");
-     //    L135MenuRow autoRow = new L135MenuRow("Auto", nothing, forward24, forwardThenStrafe);
-     //    L135OptionMenu optionMenu = new L135OptionMenu(allianceRow, autoRow);
-     //    L135DriverStationMenu menu = new L135DriverStationMenu(optionMenu);
+  40     L135MenuItem red = new L135MenuItem("R", "Red");
+  41     L135MenuItem blue = new L135MenuItem("B", "Blue");
+  42     L135MenuRow allianceRow = new L135MenuRow("Alliance", red, blue);
+  43     L135MenuItem nothing = new L135MenuItem("N", "Nothing");
+  44     L135MenuItem forward24 = new L135MenuItem("F24", "Forward 24");
+  45     L135MenuItem forwardThenStrafe = new L135MenuItem("FS", "Forward Then Strafe");
+  46     L135MenuRow autoRow = new L135MenuRow("Auto", nothing, forward24, forwardThenStrafe);
+  47     L135OptionMenu optionMenu = new L135OptionMenu(allianceRow, autoRow);
+  48     L135DriverStationMenu menu = new L135DriverStationMenu(optionMenu);
   49
   50     double robotHalfLength_in = 9.0;
   51     double fieldPerimeterWidth_in = 1.5;
@@ lines 68 to 74 @@
   68         // When on L140S020, see
   69         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s020
   70         // for what to do here.
-     //        menu.loop(gamepad1);
+  71         menu.loop(gamepad1);
   72         initLoopAfter();
   73     }
   74
@@ lines 79 to 101 @@
   79         // When on L140S090, see
   80         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s090
   81         // for what to do here.
+  82         String alliance = menu.chosen("Alliance");
+  83         OpModeStorage.alliance = alliance;
   84
   85         // When on L140S020, see
   86         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s020
   87         // for what to do here.
-     //        String auto = menu.chosen("Auto");
-     //        Command chosenAuto = Command.NOOP;
-     //        switch (auto) {
-     //            case "Forward 24":
-     //                chosenAuto = driveForward24();
-     //                break;
-     //            case "Forward Then Strafe":
-     //                chosenAuto = driveForwardThenStrafe();
-     //                break;
-     //        }
-     //        Scheduler.schedule(chosenAuto);
+  88         String auto = menu.chosen("Auto");
+  89         Command chosenAuto = Command.NOOP;
+  90         switch (auto) {
+  91             case "Forward 24":
+  92                 chosenAuto = driveForward24();
+  93                 break;
+  94             case "Forward Then Strafe":
+  95                 chosenAuto = driveForwardThenStrafe();
+  96                 break;
+  97         }
+  98         Scheduler.schedule(chosenAuto);
   99         startAfter();
  100     }
  101
@@ lines 106 to 112 @@
  106         // When on L140S020, see
  107         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s020
  108         // for what to do here.
-     //        Tracker.printToDs(menu.summary());
+ 109         Tracker.printToDs(menu.summary());
  110         Tracker.printPoseSpeedLoopToDs(follower);
  111         loopAfter();
  112     }
@@ lines 124 to 133 @@
  124         // When on L140S020, see
  125         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s020
  126         // for what to do here.
-     //        String alliance = menu.chosen("Alliance");
-     //        if (alliance.equals("Blue")) {
-     //            chosenPoses = redPoses.rotateAround(72.0, 72.0, 180.0);
-     //        }
+ 127         String alliance = menu.chosen("Alliance");
+ 128         if (alliance.equals("Blue")) {
+ 129             chosenPoses = redPoses.rotateAround(72.0, 72.0, 180.0);
+ 130         }
  131         return chosenPoses;
  132     }
  133
```
