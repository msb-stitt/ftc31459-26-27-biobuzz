# L135MenuRowTest

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-l135-menu-classes.2`. A line marked + is new, and a line marked - is
gone; each number is the line's place in the file after the lesson.

## L135

```{code-block} diff
:class: answer-diff

--- mytry/L135MenuRowTest.java  (before L135)
+++ mytry/L135MenuRowTest.java  (after L135)
@@ lines 1 to 9 @@
+   1 package org.firstinspires.ftc.teamcode.mytry;
+   2
+   3 import static org.junit.Assert.assertEquals;
+   4
+   5 import org.junit.Test;
+   6
    7 /**
    8  * Tests for {@link L135MenuRow}: a row starts at its first choice,
    9  * selected; left and right offer a choice and wrap at the ends; A selects
@@ lines 13 to 87 @@
   13 // When on L135S050, see
   14 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s050
   15 // for what to write in this file.
+  16 // 'public' so JUnit can run it. 'public' allows access from any code.
+  17 public class L135MenuRowTest {
+  18
+  19     L135MenuItem red = new L135MenuItem("R", "Red");
+  20     L135MenuItem blue = new L135MenuItem("B", "Blue");
+  21     L135MenuRow allianceRow = new L135MenuRow("Alliance", red, blue);
+  22
+  23     @Test
+  24     // 'public' so JUnit can run it. 'public' allows access from any code.
+  25     public void aRowStartsAtItsFirstChoiceSelected() {
+  26         String line = allianceRow.line();
+  27         assertEquals("Alliance: <Red>", line);
+  28         L135MenuItem selected = allianceRow.selected();
+  29         assertEquals(red, selected);
+  30     }
+  31
+  32     @Test
+  33     // 'public' so JUnit can run it. 'public' allows access from any code.
+  34     public void rightOffersTheNextChoice() {
+  35         allianceRow.right();
+  36         String line = allianceRow.line();
+  37         assertEquals("Alliance: Blue? A:OK B:Back", line);
+  38         L135MenuItem selected = allianceRow.selected();
+  39         assertEquals(red, selected);
+  40     }
+  41
+  42     @Test
+  43     // 'public' so JUnit can run it. 'public' allows access from any code.
+  44     public void leftWrapsFromTheFirstChoiceToTheLast() {
+  45         allianceRow.left();
+  46         String line = allianceRow.line();
+  47         assertEquals("Alliance: Blue? A:OK B:Back", line);
+  48     }
+  49
+  50     @Test
+  51     // 'public' so JUnit can run it. 'public' allows access from any code.
+  52     public void rightWrapsFromTheLastChoiceToTheFirst() {
+  53         allianceRow.right();
+  54         allianceRow.select();
+  55         allianceRow.right();
+  56         String line = allianceRow.line();
+  57         assertEquals("Alliance: Red? A:OK B:Back", line);
+  58     }
+  59
+  60     @Test
+  61     // 'public' so JUnit can run it. 'public' allows access from any code.
+  62     public void selectSelectsTheChoiceOffered() {
+  63         allianceRow.right();
+  64         allianceRow.select();
+  65         String line = allianceRow.line();
+  66         assertEquals("Alliance: <Blue>", line);
+  67         L135MenuItem selected = allianceRow.selected();
+  68         assertEquals(blue, selected);
+  69     }
+  70
+  71     @Test
+  72     // 'public' so JUnit can run it. 'public' allows access from any code.
+  73     public void backClearsTheOffer() {
+  74         allianceRow.right();
+  75         allianceRow.back();
+  76         String line = allianceRow.line();
+  77         assertEquals("Alliance: <Red>", line);
+  78     }
+  79
+  80     @Test
+  81     // 'public' so JUnit can run it. 'public' allows access from any code.
+  82     public void startAtSelectsTheChoiceWithThatFullName() {
+  83         allianceRow.startAt("Blue");
+  84         String line = allianceRow.line();
+  85         assertEquals("Alliance: <Blue>", line);
+  86     }
+  87 }
```
