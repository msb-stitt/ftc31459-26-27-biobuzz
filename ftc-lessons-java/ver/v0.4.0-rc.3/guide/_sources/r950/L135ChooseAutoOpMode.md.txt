# L135ChooseAutoOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-l135-menu-classes.1`. A line marked + is new, and a line marked - is
gone; each number is the line's place in the file after the lesson.

## L135

```{code-block} diff
:class: answer-diff

--- mytry/L135ChooseAutoOpMode.java  (before L135)
+++ mytry/L135ChooseAutoOpMode.java  (after L135)
@@ lines 10 to 15 @@
   10 import com.pedropathing.ivy.Scheduler;
   11 import com.pedropathing.math.Pose;
   12 import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
-     import com.qualcomm.robotcore.eventloop.opmode.Disabled;
   13
   14 import org.firstinspires.ftc.teamcode.base.CorbelsAuto;
   15 import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
@@ lines 38 to 52 @@
   38     // When on L135S120, see
   39     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s120
   40     // for what to do here.
+  41     L135MenuItem red = new L135MenuItem("R", "Red");
+  42     L135MenuItem blue = new L135MenuItem("B", "Blue");
+  43     L135MenuRow allianceRow = new L135MenuRow("Alliance", red, blue);
+  44     L135MenuItem nothing = new L135MenuItem("N", "Nothing");
+  45     L135MenuItem forward24 = new L135MenuItem("F24", "Forward 24");
+  46     L135MenuItem forwardThenStrafe = new L135MenuItem("FS", "Forward Then Strafe");
+  47     L135MenuRow autoRow = new L135MenuRow("Auto", nothing, forward24, forwardThenStrafe);
+  48     L135OptionMenu optionMenu = new L135OptionMenu(allianceRow, autoRow);
+  49     L135DriverStationMenu menu = new L135DriverStationMenu(optionMenu);
   50
   51     double robotHalfLength_in = 9.0;
   52     double fieldPerimeterWidth_in = 1.5;
@@ lines 69 to 75 @@
   69         // When on L135S120, see
   70         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s120
   71         // for what to do here.
+  72         menu.loop(gamepad1);
   73         initLoopAfter();
   74     }
   75
@@ lines 80 to 100 @@
   80         // When on L135S130, see
   81         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s130
   82         // for what to do here.
+  83         String alliance = menu.chosen("Alliance");
+  84         Tracker.publish("menu/alliance", alliance);
+  85
+  86         String auto = menu.chosen("Auto");
+  87         Tracker.publish("menu/auto", auto);
+  88         Command chosenAuto = Command.NOOP;
+  89         switch (auto) {
+  90             case "Forward 24":
+  91                 chosenAuto = driveForward24();
+  92                 break;
+  93             case "Forward Then Strafe":
+  94                 chosenAuto = driveForwardThenStrafe();
+  95                 break;
+  96         }
+  97         Scheduler.schedule(chosenAuto);
   98         startAfter();
   99     }
  100
@@ lines 105 to 111 @@
  105         // When on L135S130, see
  106         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s130
  107         // for what to do here.
+ 108         Tracker.printToDs(menu.summary());
  109         Tracker.printPoseSpeedLoopToDs(follower);
  110         loopAfter();
  111     }
@@ lines 123 to 132 @@
  123         // When on L135S140, see
  124         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s140
  125         // for what to do here.
+ 126         String alliance = menu.chosen("Alliance");
+ 127         if (alliance.equals("Blue")) {
+ 128             chosenPoses = redPoses.rotateAround(72.0, 72.0, 180.0);
+ 129         }
  130         return chosenPoses;
  131     }
  132
```
