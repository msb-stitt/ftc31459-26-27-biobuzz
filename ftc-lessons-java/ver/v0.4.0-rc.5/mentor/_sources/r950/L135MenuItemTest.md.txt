# L135MenuItemTest

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-l135-menu-classes.2`. A line marked + is new, and a line marked - is
gone; each number is the line's place in the file after the lesson.

## L135

```{code-block} diff
:class: answer-diff

--- mytry/L135MenuItemTest.java  (before L135)
+++ mytry/L135MenuItemTest.java  (after L135)
@@ lines 1 to 9 @@
+   1 package org.firstinspires.ftc.teamcode.mytry;
+   2
+   3 import static org.junit.Assert.assertEquals;
+   4
+   5 import org.junit.Test;
+   6
    7 /**
    8  * Tests for {@link L135MenuItem}: an item gives back the short name and
    9  * the full name it was made with.
@@ lines 11 to 26 @@
   11 // When on L135S040, see
   12 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s040
   13 // for what to write in this file.
+  14 // 'public' so JUnit can run it. 'public' allows access from any code.
+  15 public class L135MenuItemTest {
+  16
+  17     @Test
+  18     // 'public' so JUnit can run it. 'public' allows access from any code.
+  19     public void anItemHasAShortNameAndAFullName() {
+  20         L135MenuItem forward24 = new L135MenuItem("F24", "Forward 24");
+  21         String shortName = forward24.shortName();
+  22         assertEquals("F24", shortName);
+  23         String fullName = forward24.fullName();
+  24         assertEquals("Forward 24", fullName);
+  25     }
+  26 }
```
