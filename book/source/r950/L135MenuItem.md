# L135MenuItem

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l135-menu-classes`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L135

```{code-block} diff
:class: answer-diff

--- mytry/L135MenuItem.java  (before L135)
+++ mytry/L135MenuItem.java  (after L135)
@@ lines 1 to 5 @@
+   1 package org.firstinspires.ftc.teamcode.mytry;
+   2
    3 /**
    4  * One choice in a {@link L135MenuRow}: its short name, such as F24, for the
    5  * menu's summary line, and its full name, such as Forward 24, for its row's
@@ lines 10 to 30 @@
   10 // When on L135S040, see
   11 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s040
   12 // for what to write in this file.
+  13 class L135MenuItem {
+  14
+  15     String shortName;
+  16     String fullName;
+  17
+  18     L135MenuItem(String shortName, String fullName) {
+  19         this.shortName = shortName;
+  20         this.fullName = fullName;
+  21     }
+  22
+  23     String shortName() {
+  24         return shortName;
+  25     }
+  26
+  27     String fullName() {
+  28         return fullName;
+  29     }
+  30 }
```
