# L157FeedforwardOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111.2`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L157

```{code-block} diff
:class: answer-diff

--- mytry/L157FeedforwardOpMode.java  (before L157)
+++ mytry/L157FeedforwardOpMode.java  (after L157)
@@ lines 14 to 20 @@
   14 // When on L157S030, see
   15 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l157.html#l157s030
   16 // for what to do here.
-     //import org.firstinspires.ftc.teamcode.mytry.l157.StickShaper;
+  17 import org.firstinspires.ftc.teamcode.mytry.l157.StickShaper;
   18
   19 /**
   20  * L157: the sticks ask for a speed, and the drivetrain sends the volts it
@@ lines 40 to 46 @@
   40     // When on L157S030, see
   41     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l157.html#l157s030
   42     // for what to do here.
-     //    StickShaper stickShaper = new StickShaper(0.05);
+  43     StickShaper stickShaper = new StickShaper(0.05);
   44
   45     L157FeedforwardDriveTrain drivetrain;
   46
@@ lines 61 to 67 @@
   61         // When on L157S040, see
   62         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l157.html#l157s040
   63         // for what to do here.
+  64         stickShaper.setRespread(true);
   65
   66         initAfter(drivetrain);
   67     }
@@ lines 90 to 98 @@
   90         // When on L157S030, see
   91         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l157.html#l157s030
   92         // for what to do here.
-     //        forwardFraction = stickShaper.shaped(forwardFraction);
-     //        leftFraction = stickShaper.shaped(leftFraction);
-     //        turnFraction = stickShaper.shaped(turnFraction);
+  93         forwardFraction = stickShaper.shaped(forwardFraction);
+  94         leftFraction = stickShaper.shaped(leftFraction);
+  95         turnFraction = stickShaper.shaped(turnFraction);
   96
   97         double turnCcw_radps = turnFraction * maxTurn_radps;
   98         double forwardX_ips;
```
