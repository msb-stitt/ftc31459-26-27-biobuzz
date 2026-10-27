# L140AutoOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l140-alliances.1`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L140

```{code-block} diff
:class: answer-diff

--- mytry/L140AutoOpMode.java  (before L140)
+++ mytry/L140AutoOpMode.java  (after L140)
@@ lines 68 to 74 @@
   68         // When on L140S070, see
   69         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s070
   70         // for what to do here.
+  71         OpModeStorage.alliance = alliance;
   72
   73         String auto = menu.chosen("Auto");
   74         Command chosenAuto = Command.NOOP;
```
