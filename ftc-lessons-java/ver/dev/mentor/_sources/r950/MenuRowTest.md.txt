# MenuRowTest

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111.1`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L093

```{code-block} diff
:class: answer-diff

--- mytry/l093/MenuRowTest.java  (before L093)
+++ mytry/l093/MenuRowTest.java  (after L093)
@@ lines 1 to 10 @@
+   1 package org.firstinspires.ftc.teamcode.mytry.l093;
+   2
+   3 import static org.junit.Assert.assertEquals;
+   4
+   5 import org.junit.Before;
+   6 import org.junit.Test;
+   7
    8 /**
    9  * Tests for {@link MenuRow}: a row starts at the first choice added,
   10  * selected; left and right offer a choice and wrap at the ends; A selects
@@ lines 14 to 89 @@
   14 // When on L093S040, see
   15 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l093.html#l093s040
   16 // for what to write in this file.
+  17 // 'public' so JUnit can run it. 'public' allows access from any code.
+  18 public class MenuRowTest {
+  19
+  20     MenuItem nothing = new MenuItem("N", "Nothing");
+  21     MenuItem forward24 = new MenuItem("F24", "Forward 24");
+  22     MenuItem forwardThenStrafe = new MenuItem("FS", "Forward Then Strafe");
+  23     MenuRow autoRow = new MenuRow("Auto");
+  24
+  25     @Before
+  26     // 'public' so JUnit can run it. 'public' allows access from any code.
+  27     public void addTheItems() {
+  28         autoRow.add(nothing);
+  29         autoRow.add(forward24);
+  30         autoRow.add(forwardThenStrafe);
+  31     }
+  32
+  33     @Test
+  34     // 'public' so JUnit can run it. 'public' allows access from any code.
+  35     public void aRowStartsAtItsFirstChoiceSelected() {
+  36         String line = autoRow.getLine();
+  37         assertEquals("Auto: <Nothing>", line);
+  38         MenuItem selected = autoRow.getSelected();
+  39         assertEquals(nothing, selected);
+  40     }
+  41
+  42     @Test
+  43     // 'public' so JUnit can run it. 'public' allows access from any code.
+  44     public void rightOffersTheNextChoice() {
+  45         autoRow.goRight();
+  46         String line = autoRow.getLine();
+  47         assertEquals("Auto: Forward 24? A:OK B:Back", line);
+  48         MenuItem selected = autoRow.getSelected();
+  49         assertEquals(nothing, selected);
+  50     }
+  51
+  52     @Test
+  53     // 'public' so JUnit can run it. 'public' allows access from any code.
+  54     public void leftWrapsFromTheFirstChoiceToTheLast() {
+  55         autoRow.goLeft();
+  56         String line = autoRow.getLine();
+  57         assertEquals("Auto: Forward Then Strafe? A:OK B:Back", line);
+  58     }
+  59
+  60     @Test
+  61     // 'public' so JUnit can run it. 'public' allows access from any code.
+  62     public void rightWrapsFromTheLastChoiceToTheFirst() {
+  63         autoRow.goRight();
+  64         autoRow.goRight();
+  65         autoRow.goRight();
+  66         String line = autoRow.getLine();
+  67         assertEquals("Auto: <Nothing>", line);
+  68     }
+  69
+  70     @Test
+  71     // 'public' so JUnit can run it. 'public' allows access from any code.
+  72     public void selectSelectsTheChoiceOffered() {
+  73         autoRow.goRight();
+  74         autoRow.select();
+  75         String line = autoRow.getLine();
+  76         assertEquals("Auto: <Forward 24>", line);
+  77         MenuItem selected = autoRow.getSelected();
+  78         assertEquals(forward24, selected);
+  79     }
+  80
+  81     @Test
+  82     // 'public' so JUnit can run it. 'public' allows access from any code.
+  83     public void backClearsTheOffer() {
+  84         autoRow.goRight();
+  85         autoRow.goBack();
+  86         String line = autoRow.getLine();
+  87         assertEquals("Auto: <Nothing>", line);
+  88     }
+  89 }
```
