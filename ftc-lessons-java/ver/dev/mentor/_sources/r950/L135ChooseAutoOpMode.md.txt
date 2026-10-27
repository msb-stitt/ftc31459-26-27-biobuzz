# L135ChooseAutoOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l140-alliances.1`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L135

```{code-block} diff
:class: answer-diff

--- mytry/L135ChooseAutoOpMode.java  (before L135)
+++ mytry/L135ChooseAutoOpMode.java  (after L135)
@@ lines 58 to 64 @@
   58         // When on L135S030, see
   59         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s030
   60         // for what to do here.
+  61         menu.loop(gamepad1);
   62         initLoopAfter();
   63     }
   64
@@ lines 71 to 84 @@
   71         // When on L135S060, see
   72         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s060
   73         // for what to do here.
+  74         switch (auto) {
+  75             case "F24-Forward 24":
+  76                 chosenAuto = driveForward24();
+  77                 break;
+  78             case "FS-Forward Then Strafe":
+  79                 chosenAuto = driveForwardThenStrafe();
+  80                 break;
+  81         }
   82         Scheduler.schedule(chosenAuto);
   83         startAfter();
   84     }
@@ lines 105 to 114 @@
  105         // When on L135S070, see
  106         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s070
  107         // for what to do here.
+ 108         String alliance = menu.chosen("Alliance");
+ 109         if (alliance.equals("Blue")) {
+ 110             chosenPoses = redPoses.rotateAround(72.0, 72.0, 180.0);
+ 111         }
  112         return chosenPoses;
  113     }
  114
```
