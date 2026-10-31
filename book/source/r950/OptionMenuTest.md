# OptionMenuTest

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111.2`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L093

```{code-block} diff
:class: answer-diff

--- mytry/l093/OptionMenuTest.java  (before L093)
+++ mytry/l093/OptionMenuTest.java  (after L093)
@@ lines 1 to 12 @@
+   1 package org.firstinspires.ftc.teamcode.mytry.l093;
+   2
+   3 import static org.junit.Assert.assertEquals;
+   4
+   5 import org.junit.Before;
+   6 import org.junit.Test;
+   7
+   8 import java.util.ArrayList;
+   9
   10 /**
   11  * Tests for {@link OptionMenu}: the summary first, then each row's line with
   12  * a pointer; up and down move the pointer, wrap at the ends and clear an
@@ lines 15 to 104 @@
   15 // When on L093S070, see
   16 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l093.html#l093s070
   17 // for what to write in this file.
+  18 // 'public' so JUnit can run it. 'public' allows access from any code.
+  19 public class OptionMenuTest {
+  20
+  21     MenuItem red = new MenuItem("R", "Red");
+  22     MenuItem blue = new MenuItem("B", "Blue");
+  23     MenuRow allianceRow = new MenuRow("Alliance");
+  24     MenuItem nothing = new MenuItem("N", "Nothing");
+  25     MenuItem forward24 = new MenuItem("F24", "Forward 24");
+  26     MenuItem forwardThenStrafe = new MenuItem("FS", "Forward Then Strafe");
+  27     MenuRow autoRow = new MenuRow("Auto");
+  28     OptionMenu optionMenu = new OptionMenu();
+  29
+  30     @Before
+  31     // 'public' so JUnit can run it. 'public' allows access from any code.
+  32     public void addTheRows() {
+  33         allianceRow.add(red);
+  34         allianceRow.add(blue);
+  35         autoRow.add(nothing);
+  36         autoRow.add(forward24);
+  37         autoRow.add(forwardThenStrafe);
+  38         optionMenu.add(allianceRow);
+  39         optionMenu.add(autoRow);
+  40     }
+  41
+  42     @Test
+  43     // 'public' so JUnit can run it. 'public' allows access from any code.
+  44     public void theMenuStartsWithThePointerOnTheFirstRow() {
+  45         assertLines("R-N", "> Alliance: <Red>", "  Auto: <Nothing>");
+  46     }
+  47
+  48     @Test
+  49     // 'public' so JUnit can run it. 'public' allows access from any code.
+  50     public void selectChangesTheSummary() {
+  51         allianceRow.goRight();
+  52         allianceRow.select();
+  53         String summary = optionMenu.getSummary();
+  54         assertEquals("B-N", summary);
+  55     }
+  56
+  57     @Test
+  58     // 'public' so JUnit can run it. 'public' allows access from any code.
+  59     public void rightOffersInTheRowThePointerIsOn() {
+  60         optionMenu.goRight();
+  61         assertLines("R-N", "> Alliance: Blue? A:OK B:Back", "  Auto: <Nothing>");
+  62     }
+  63
+  64     @Test
+  65     // 'public' so JUnit can run it. 'public' allows access from any code.
+  66     public void downMovesThePointerAndClearsTheOffer() {
+  67         optionMenu.goRight();
+  68         optionMenu.goDown();
+  69         assertLines("R-N", "  Alliance: <Red>", "> Auto: <Nothing>");
+  70     }
+  71
+  72     @Test
+  73     // 'public' so JUnit can run it. 'public' allows access from any code.
+  74     public void downWrapsFromTheLastRowToTheFirst() {
+  75         optionMenu.goDown();
+  76         optionMenu.goDown();
+  77         assertLines("R-N", "> Alliance: <Red>", "  Auto: <Nothing>");
+  78     }
+  79
+  80     @Test
+  81     // 'public' so JUnit can run it. 'public' allows access from any code.
+  82     public void upWrapsFromTheFirstRowToTheLast() {
+  83         optionMenu.goUp();
+  84         assertLines("R-N", "  Alliance: <Red>", "> Auto: <Nothing>");
+  85     }
+  86
+  87     @Test
+  88     // 'public' so JUnit can run it. 'public' allows access from any code.
+  89     public void leftAndSelectWorkInTheRowThePointerIsOn() {
+  90         optionMenu.goDown();
+  91         optionMenu.goLeft();
+  92         optionMenu.select();
+  93         assertLines("R-FS", "  Alliance: <Red>", "> Auto: <Forward Then Strafe>");
+  94     }
+  95
+  96     void assertLines(String summaryLine, String allianceLine, String autoLine) {
+  97         ArrayList<String> expected = new ArrayList<>();
+  98         expected.add(summaryLine);
+  99         expected.add(allianceLine);
+ 100         expected.add(autoLine);
+ 101         ArrayList<String> lines = optionMenu.getLines();
+ 102         assertEquals(expected, lines);
+ 103     }
+ 104 }
```
