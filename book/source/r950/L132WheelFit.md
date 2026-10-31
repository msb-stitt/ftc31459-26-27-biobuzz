# L132WheelFit

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L132

```{code-block} diff
:class: answer-diff

--- mytry/L132WheelFit.java  (before L132)
+++ mytry/L132WheelFit.java  (after L132)
@@ lines 30 to 45 @@
   30         // When on L132S090, see
   31         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s090
   32         // for what to do here.
+  33         double wheelStrafeStep_in = (-wheelSteps_in[FL] + wheelSteps_in[FR]
+  34                 + wheelSteps_in[BL] - wheelSteps_in[BR]) / 4.0;
+  35         wheelStrafe_in += wheelStrafeStep_in;
   36
   37         // When on L132S120, see
   38         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s120
   39         // for what to do here.
+  40         double wheelTurnStep_in = (-wheelSteps_in[FL] + wheelSteps_in[FR]
+  41                 - wheelSteps_in[BL] + wheelSteps_in[BR]) / 4.0;
+  42         wheelTurn_in += wheelTurnStep_in;
   43     }
   44
   45     /** One loop's move of the robot, the robot's way, as the Pinpoint saw it. */
@@ lines 47 to 58 @@
   47         // When on L132S090, see
   48         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s090
   49         // for what to do here.
+  50         robotStrafe_in += strafe_in;
   51
   52         // When on L132S120, see
   53         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s120
   54         // for what to do here.
+  55         robotTurn_rad += turn_rad;
   56     }
   57
   58     /** How far the wheels rolled for each inch the robot slid. */
@@ lines 61 to 67 @@
   61         // When on L132S090, see
   62         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s090
   63         // for what to do here.
+  64         cotAlpha = wheelStrafe_in / robotStrafe_in;
   65         return cotAlpha;
   66     }
   67
@@ lines 71 to 77 @@
   71         // When on L132S120, see
   72         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s120
   73         // for what to do here.
+  74         turnRadius_in = wheelTurn_in / robotTurn_rad;
   75         return turnRadius_in;
   76     }
   77 }
```
