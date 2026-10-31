# MenuItemTest

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111.1`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L093

```{code-block} diff
:class: answer-diff

--- mytry/l093/MenuItemTest.java  (before L093)
+++ mytry/l093/MenuItemTest.java  (after L093)
@@ lines 1 to 9 @@
+   1 package org.firstinspires.ftc.teamcode.mytry.l093;
+   2
+   3 import static org.junit.Assert.assertEquals;
+   4
+   5 import org.junit.Test;
+   6
    7 /**
    8  * Tests for {@link MenuItem}: an item gives back the short name and the full
    9  * name it was made with.
@@ lines 11 to 26 @@
   11 // When on L093S030, see
   12 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l093.html#l093s030
   13 // for what to write in this file.
+  14 // 'public' so JUnit can run it. 'public' allows access from any code.
+  15 public class MenuItemTest {
+  16
+  17     @Test
+  18     // 'public' so JUnit can run it. 'public' allows access from any code.
+  19     public void anItemHasAShortNameAndAFullName() {
+  20         MenuItem forward24 = new MenuItem("F24", "Forward 24");
+  21         String shortName = forward24.getShortName();
+  22         assertEquals("F24", shortName);
+  23         String fullName = forward24.getFullName();
+  24         assertEquals("Forward 24", fullName);
+  25     }
+  26 }
```
