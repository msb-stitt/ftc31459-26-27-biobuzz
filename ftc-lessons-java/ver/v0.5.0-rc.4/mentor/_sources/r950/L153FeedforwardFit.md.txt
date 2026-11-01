# L153FeedforwardFit

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111.2`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L153

```{code-block} diff
:class: answer-diff

--- mytry/L153FeedforwardFit.java  (before L153)
+++ mytry/L153FeedforwardFit.java  (after L153)
@@ lines 25 to 35 @@
   25         // When on L153S030, see
   26         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l153.html#l153s030
   27         // for what to do here.
+  28         count += 1;
+  29         sumSpeed += speed;
+  30         sumVolts += volts;
+  31         sumSpeedSquared += speed * speed;
+  32         sumSpeedTimesVolts += speed * volts;
   33     }
   34
   35     /** How many more volts each unit of speed takes: the line's slope. */
@@ lines 38 to 46 @@
   38         // When on L153S030, see
   39         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l153.html#l153s030
   40         // for what to do here.
+  41         double top = count * sumSpeedTimesVolts - sumSpeed * sumVolts;
+  42         double bottom = count * sumSpeedSquared - sumSpeed * sumSpeed;
+  43         kV = top / bottom;
   44         return kV;
   45     }
   46
@@ lines 50 to 56 @@
   50         // When on L153S040, see
   51         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l153.html#l153s040
   52         // for what to do here.
+  53         kS = (sumVolts - kV() * sumSpeed) / count;
   54         return kS;
   55     }
   56 }
```
