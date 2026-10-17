# L110FollowerDriveTrain

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-rules.1`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L110

```{code-block} diff
:class: answer-diff

--- mytry/L110FollowerDriveTrain.java  (before L110)
+++ mytry/L110FollowerDriveTrain.java  (after L110)
@@ lines 44 to 53 @@
   44         // When on L110S030, see
   45         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l110.html#l110s030
   46         // for what to do here.
+  47         wheels[FL] = forward - strafe - turn;
+  48         wheels[FR] = forward + strafe + turn;
+  49         wheels[BL] = forward + strafe - turn;
+  50         wheels[BR] = forward - strafe + turn;
   51
   52         return wheels;
   53     }
```
