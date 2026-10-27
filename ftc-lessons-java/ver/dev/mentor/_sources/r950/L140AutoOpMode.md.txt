# L140AutoOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l140-alliances.2`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L140

```{code-block} diff
:class: answer-diff

--- mytry/L140AutoOpMode.java  (before L140)
+++ mytry/L140AutoOpMode.java  (after L140)
@@ lines 67 to 74 @@
   67         // When on L140S090, see
   68         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s090
   69         // for what to do here.
+  70         String alliance = menu.chosen("Alliance");
+  71         OpModeStorage.alliance = alliance;
   72
   73         String auto = menu.chosen("Auto");
   74         Command chosenAuto = Command.NOOP;
```
