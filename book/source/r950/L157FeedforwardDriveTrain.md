# L157FeedforwardDriveTrain

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111.2`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L157

```{code-block} diff
:class: answer-diff

--- mytry/L157FeedforwardDriveTrain.java  (before L157)
+++ mytry/L157FeedforwardDriveTrain.java  (after L157)
@@ lines 60 to 68 @@
   60             // When on L157S060, see
   61             // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l157.html#l157s060
   62             // for what to do here.
+  63             double kVVolts = turnVolts[i] + scale * (forwardVolts[i] + strafeVolts[i]);
+  64             double volts = kVVolts + steppedKs(i, kVVolts, kS[i]);
+  65             powers[i] = volts / batteryVolts;
   66         }
   67
   68         double[] wheels = normalized(powers);
@@ lines 76 to 89 @@
   76         // When on L157S060, see
   77         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l157.html#l157s060
   78         // for what to do here.
+  79         double forwardShare = Math.abs(forwardVolts);
+  80         double strafeShare = Math.abs(strafeVolts);
+  81         double turnShare = Math.abs(turnVolts);
+  82         double allShares = forwardShare + strafeShare + turnShare;
+  83         if (allShares > 0.0) {
+  84             kS = (forwardShare * Constants.kSForwardVolts + strafeShare * Constants.kSStrafeVolts
+  85                     + turnShare * Constants.kSTurnVolts) / allShares;
+  86         }
   87         return kS;
   88     }
   89
@@ lines 93 to 107 @@
   93         // When on L157S070, see
   94         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l157.html#l157s070
   95         // for what to do here.
+  96         double kVSize = Math.abs(kVVolts);
+  97         if (kSOn[wheel] && kVSize < KS_OFF_SHARE * kS) {
+  98             kSOn[wheel] = false;
+  99         } else if (!kSOn[wheel] && kVSize > KS_ON_SHARE * kS) {
+ 100             kSOn[wheel] = true;
+ 101         }
+ 102         if (!kSOn[wheel]) {
+ 103             stepped = 0.0;
+ 104         }
  105         return stepped;
  106     }
  107
```
