# L135AutoMenu

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l140-alliances.2`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L135

```{code-block} diff
:class: answer-diff

--- mytry/L135AutoMenu.java  (before L135)
+++ mytry/L135AutoMenu.java  (after L135)
@@ lines 82 to 92 @@
   82         // When on L135S040, see
   83         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s040
   84         // for what to do here.
+  85         int lastChoice = rows[row].choices.length - 1;
+  86         shown[row] = shown[row] + 1;
+  87         if (shown[row] > lastChoice) {
+  88             shown[row] = 0;
+  89         }
   90     }
   91
   92     /** The choice shown becomes the one kept. */
@@ lines 94 to 100 @@
   94         // When on L135S050, see
   95         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s050
   96         // for what to do here.
+  97         kept[row] = shown[row];
   98     }
   99
  100     /** The choice kept is shown again. */
@@ lines 102 to 108 @@
  102         // When on L135S050, see
  103         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s050
  104         // for what to do here.
+ 105         shown[row] = kept[row];
  106     }
  107
  108     /** The choice kept in the row with this name, such as chosen("Alliance"). */
```
