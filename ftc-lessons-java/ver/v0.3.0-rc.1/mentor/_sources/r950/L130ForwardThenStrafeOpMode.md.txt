# L130ForwardThenStrafeOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-minor-fixes.1`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L130

```{code-block} diff
:class: answer-diff

--- mytry/L130ForwardThenStrafeOpMode.java  (before L130)
+++ mytry/L130ForwardThenStrafeOpMode.java  (after L130)
@@ lines 3 to 9 @@
    3 import static com.pedropathing.api.Paths.line;
    4 import static com.pedropathing.ivy.groups.Groups.sequential;
    5 import static com.pedropathing.ivy.pedro.PedroCommands.follow;
+   6 import static com.pedropathing.ivy.pedro.PedroCommands.hold;
    7
    8 import com.pedropathing.api.PoseFactory;
    9 import com.pedropathing.ivy.Command;
@@ lines 87 to 98 @@
   87
   88     Command driveForwardThenStrafe() {
   89         Command forwardThenStrafeRoute = sequential(
-                     follow(follower, line(start, corner).constant(start))
+  90                 follow(follower, line(start, corner).constant(start)),
   91                 // When on L130S030, see
   92                 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l130.html#l130s030
   93                 // for what to do here.
+  94                 follow(follower, line(corner, end).constant(start)),
+  95                 hold(follower, end)
   96         );
   97         return forwardThenStrafeRoute;
   98     }
```
