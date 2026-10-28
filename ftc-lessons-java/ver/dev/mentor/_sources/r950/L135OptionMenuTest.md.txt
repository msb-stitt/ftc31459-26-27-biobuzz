# L135OptionMenuTest

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-l135-menu-classes.1`. A line marked + is new, and a line marked - is
gone; each number is the line's place in the file after the lesson.

## L135

```{code-block} diff
:class: answer-diff

--- mytry/L135OptionMenuTest.java  (before L135)
+++ mytry/L135OptionMenuTest.java  (after L135)
@@ lines 1 to 10 @@
+   1 package org.firstinspires.ftc.teamcode.mytry;
+   2
+   3 import static org.junit.Assert.assertArrayEquals;
+   4 import static org.junit.Assert.assertEquals;
+   5
+   6 import org.junit.Test;
+   7
    8 /**
    9  * Tests for {@link L135OptionMenu}: the summary first, then each row's
   10  * line with a pointer; up and down move the pointer, wrap at the ends and
@@ lines 13 to 112 @@
   13 // When on L135S080, see
   14 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s080
   15 // for what to write in this file.
+  16 // 'public' so JUnit can run it. 'public' allows access from any code.
+  17 public class L135OptionMenuTest {
+  18
+  19     L135MenuItem red = new L135MenuItem("R", "Red");
+  20     L135MenuItem blue = new L135MenuItem("B", "Blue");
+  21     L135MenuRow allianceRow = new L135MenuRow("Alliance", red, blue);
+  22     L135MenuItem nothing = new L135MenuItem("N", "Nothing");
+  23     L135MenuItem forward24 = new L135MenuItem("F24", "Forward 24");
+  24     L135MenuItem forwardThenStrafe = new L135MenuItem("FS", "Forward Then Strafe");
+  25     L135MenuRow autoRow = new L135MenuRow("Auto", nothing, forward24, forwardThenStrafe);
+  26     L135OptionMenu optionMenu = new L135OptionMenu(allianceRow, autoRow);
+  27
+  28     @Test
+  29     // 'public' so JUnit can run it. 'public' allows access from any code.
+  30     public void theMenuStartsWithThePointerOnTheFirstRow() {
+  31         String[] expected = {"R-N", "> Alliance: <Red>", "  Auto: <Nothing>"};
+  32         String[] lines = optionMenu.lines();
+  33         assertArrayEquals(expected, lines);
+  34     }
+  35
+  36     @Test
+  37     // 'public' so JUnit can run it. 'public' allows access from any code.
+  38     public void rightOffersInTheRowThePointerIsOn() {
+  39         optionMenu.right();
+  40         String[] expected = {"R-N", "> Alliance: Blue? A:OK B:Back", "  Auto: <Nothing>"};
+  41         String[] lines = optionMenu.lines();
+  42         assertArrayEquals(expected, lines);
+  43     }
+  44
+  45     @Test
+  46     // 'public' so JUnit can run it. 'public' allows access from any code.
+  47     public void selectChangesTheSummary() {
+  48         optionMenu.right();
+  49         optionMenu.select();
+  50         String summary = optionMenu.summary();
+  51         assertEquals("B-N", summary);
+  52     }
+  53
+  54     @Test
+  55     // 'public' so JUnit can run it. 'public' allows access from any code.
+  56     public void downMovesThePointerAndClearsTheOffer() {
+  57         optionMenu.right();
+  58         optionMenu.down();
+  59         String[] expected = {"R-N", "  Alliance: <Red>", "> Auto: <Nothing>"};
+  60         String[] lines = optionMenu.lines();
+  61         assertArrayEquals(expected, lines);
+  62     }
+  63
+  64     @Test
+  65     // 'public' so JUnit can run it. 'public' allows access from any code.
+  66     public void downWrapsFromTheLastRowToTheFirst() {
+  67         optionMenu.down();
+  68         optionMenu.down();
+  69         String[] expected = {"R-N", "> Alliance: <Red>", "  Auto: <Nothing>"};
+  70         String[] lines = optionMenu.lines();
+  71         assertArrayEquals(expected, lines);
+  72     }
+  73
+  74     @Test
+  75     // 'public' so JUnit can run it. 'public' allows access from any code.
+  76     public void upWrapsFromTheFirstRowToTheLast() {
+  77         optionMenu.up();
+  78         String[] expected = {"R-N", "  Alliance: <Red>", "> Auto: <Nothing>"};
+  79         String[] lines = optionMenu.lines();
+  80         assertArrayEquals(expected, lines);
+  81     }
+  82
+  83     @Test
+  84     // 'public' so JUnit can run it. 'public' allows access from any code.
+  85     public void leftWrapsToTheLastChoice() {
+  86         optionMenu.down();
+  87         optionMenu.left();
+  88         optionMenu.select();
+  89         String summary = optionMenu.summary();
+  90         assertEquals("R-FS", summary);
+  91     }
+  92
+  93     @Test
+  94     // 'public' so JUnit can run it. 'public' allows access from any code.
+  95     public void chosenGivesTheFullNameSelected() {
+  96         optionMenu.down();
+  97         optionMenu.right();
+  98         optionMenu.select();
+  99         String alliance = optionMenu.chosen("Alliance");
+ 100         assertEquals("Red", alliance);
+ 101         String auto = optionMenu.chosen("Auto");
+ 102         assertEquals("Forward 24", auto);
+ 103     }
+ 104
+ 105     @Test
+ 106     // 'public' so JUnit can run it. 'public' allows access from any code.
+ 107     public void startAtSelectsInTheNamedRow() {
+ 108         optionMenu.startAt("Alliance", "Blue");
+ 109         String alliance = optionMenu.chosen("Alliance");
+ 110         assertEquals("Blue", alliance);
+ 111     }
+ 112 }
```
