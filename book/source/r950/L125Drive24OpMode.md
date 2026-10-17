# L125Drive24OpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-mlt-fable`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L125

```{code-block} diff
:class: answer-diff

--- mytry/L125Drive24OpMode.java  (before L125)
+++ mytry/L125Drive24OpMode.java  (after L125)
@@ lines 85 to 90 @@
   85         // When on L125S030, see
   86         // https://msb-stitt.github.io/ftc31459-26-27-biobuzz/guide/tasks/l125.html#l125s030
   87         // for what to do here.
-             return Command.NOOP;
+  88         return sequential(follow(follower, line(start, end).constant(start)));
   89     }
   90 }
```
