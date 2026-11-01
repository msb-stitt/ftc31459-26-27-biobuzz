# L110FollowerDriveTrain

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111.2`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L110

```{code-block} diff
:class: answer-diff

--- mytry/L110FollowerDriveTrain.java  (before L110)
+++ mytry/L110FollowerDriveTrain.java  (after L110)
@@ lines 44 to 53 @@
   44         // When on L110S030, see
   45         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l110.html#l110s030
   46         // for what to do here.
+  47         wheelSpeeds[FL] = forwardXSpeed - strafeYSpeed - turnCcwSpeed;
+  48         wheelSpeeds[FR] = forwardXSpeed + strafeYSpeed + turnCcwSpeed;
+  49         wheelSpeeds[BL] = forwardXSpeed + strafeYSpeed - turnCcwSpeed;
+  50         wheelSpeeds[BR] = forwardXSpeed - strafeYSpeed + turnCcwSpeed;
   51
   52         return wheelSpeeds;
   53     }
```
