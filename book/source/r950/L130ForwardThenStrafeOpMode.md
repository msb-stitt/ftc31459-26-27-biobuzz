# L130ForwardThenStrafeOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-mlt-fable`. A line marked + is new, and a line marked -
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
@@ lines 83 to 94 @@
   83     @Override
   84     public Command routine() {
   85         return sequential(
-                     follow(follower, line(start, corner).constant(start))
+  86                 follow(follower, line(start, corner).constant(start)),
   87                 // When on L130S030, see
   88                 // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l130.html#l130s030
   89                 // for what to do here.
+  90                 follow(follower, line(corner, end).constant(start)),
+  91                 hold(follower, end)
   92         );
   93     }
   94 }
```
