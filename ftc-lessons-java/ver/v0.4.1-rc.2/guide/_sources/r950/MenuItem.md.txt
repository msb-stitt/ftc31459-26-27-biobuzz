# MenuItem

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-l135-menu-classes.3`. A line marked + is new, and a line marked - is
gone; each number is the line's place in the file after the lesson.

## L135

```{code-block} diff
:class: answer-diff

--- mytry/l135/MenuItem.java  (before L135)
+++ mytry/l135/MenuItem.java  (after L135)
@@ lines 1 to 5 @@
+   1 package org.firstinspires.ftc.teamcode.mytry.l135;
+   2
    3 /**
    4  * One choice in a {@link MenuRow}: its short name, such as F24, for the
    5  * menu's summary line, and its full name, such as Forward 24, for its row's
@@ lines 10 to 34 @@
   10 // When on L135S030, see
   11 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s030
   12 // for what to write in this file.
+  13 // 'public' so L140, in another package, can use it. 'public' allows access from any code.
+  14 public class MenuItem {
+  15
+  16     // 'private' so only this class can use it. 'private' allows access from this class only.
+  17     private String shortName;
+  18     // 'private' so only this class can use it. 'private' allows access from this class only.
+  19     private String fullName;
+  20
+  21     // 'public' so L140, in another package, can use it. 'public' allows access from any code.
+  22     public MenuItem(String shortName, String fullName) {
+  23         this.shortName = shortName;
+  24         this.fullName = fullName;
+  25     }
+  26
+  27     String getShortName() {
+  28         return shortName;
+  29     }
+  30
+  31     String getFullName() {
+  32         return fullName;
+  33     }
+  34 }
```
