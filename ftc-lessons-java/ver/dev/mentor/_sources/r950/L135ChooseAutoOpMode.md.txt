# L135ChooseAutoOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l140-alliances.2`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L135

```{code-block} diff
:class: answer-diff

--- mytry/L135ChooseAutoOpMode.java  (before L135)
+++ mytry/L135ChooseAutoOpMode.java  (after L135)
@@ lines 36 to 44 @@
   36     // When on L135S030, see
   37     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s030
   38     // for what to do here.
+  39     L135AutoMenu menu = new L135AutoMenu(
+  40             new L135Row("Alliance", "Red", "Blue"),
+  41             new L135Row("Auto", "F24-Forward 24", "FS-Forward Then Strafe"));
   42
   43     double robotHalfLength_in = 9.0;
   44     double fieldPerimeterWidth_in = 1.5;
@@ lines 61 to 67 @@
   61         // When on L135S030, see
   62         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s030
   63         // for what to do here.
+  64         menu.loop(gamepad1);
   65         initLoopAfter();
   66     }
   67
@@ lines 72 to 88 @@
   72         // When on L135S060, see
   73         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s060
   74         // for what to do here.
+  75         String auto = menu.chosen("Auto");
+  76         Command chosenAuto = Command.NOOP;
+  77         switch (auto) {
+  78             case "F24-Forward 24":
+  79                 chosenAuto = driveForward24();
+  80                 break;
+  81             case "FS-Forward Then Strafe":
+  82                 chosenAuto = driveForwardThenStrafe();
+  83                 break;
+  84         }
+  85         Scheduler.schedule(chosenAuto);
   86         startAfter();
   87     }
   88
@@ lines 93 to 99 @@
   93         // When on L135S060, see
   94         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s060
   95         // for what to do here.
+  96         Tracker.printToDs(menu.summary());
   97         Tracker.printPoseSpeedLoopToDs(follower);
   98         loopAfter();
   99     }
@@ lines 111 to 120 @@
  111         // When on L135S070, see
  112         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s070
  113         // for what to do here.
+ 114         String alliance = menu.chosen("Alliance");
+ 115         if (alliance.equals("Blue")) {
+ 116             chosenPoses = redPoses.rotateAround(72.0, 72.0, 180.0);
+ 117         }
  118         return chosenPoses;
  119     }
  120
```
