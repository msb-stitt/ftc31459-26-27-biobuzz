# L125Drive24OpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-l135-menu-classes.1`. A line marked + is new, and a line marked - is
gone; each number is the line's place in the file after the lesson.

## L125

```{code-block} diff
:class: answer-diff

--- mytry/L125Drive24OpMode.java  (before L125)
+++ mytry/L125Drive24OpMode.java  (after L125)
@@ lines 1 to 8 @@
    1 package org.firstinspires.ftc.teamcode.mytry;
+   2
+   3 import static com.pedropathing.api.Paths.line;
+   4 import static com.pedropathing.ivy.groups.Groups.sequential;
+   5 import static com.pedropathing.ivy.pedro.PedroCommands.follow;
    6
    7 import com.pedropathing.api.PoseFactory;
    8 import com.pedropathing.ivy.Command;
@@ lines 86 to 92 @@
   86         // When on L125S030, see
   87         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l125.html#l125s030
   88         // for what to do here.
-             return Command.NOOP;
+  89         Command forward24 = sequential(follow(follower, line(start, end).constant(start)));
+  90         return forward24;
   91     }
   92 }
```
