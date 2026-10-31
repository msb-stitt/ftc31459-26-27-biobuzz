# MenuButtonsTest

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L093

```{code-block} diff
:class: answer-diff

--- mytry/l093/MenuButtonsTest.java  (before L093)
+++ mytry/l093/MenuButtonsTest.java  (after L093)
@@ lines 1 to 12 @@
+   1 package org.firstinspires.ftc.teamcode.mytry.l093;
+   2
+   3 import static org.junit.Assert.assertEquals;
+   4
+   5 import com.qualcomm.robotcore.hardware.Gamepad;
+   6
+   7 import org.junit.Before;
+   8 import org.junit.Test;
+   9
   10 /**
   11  * Tests for {@link MenuButtons}: a press of the D-pad, A or B on a gamepad
   12  * moves the menu once.
@@ lines 14 to 82 @@
   14 // When on L093S090, see
   15 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l093.html#l093s090
   16 // for what to write in this file.
+  17 // 'public' so JUnit can run it. 'public' allows access from any code.
+  18 public class MenuButtonsTest {
+  19
+  20     MenuItem red = new MenuItem("R", "Red");
+  21     MenuItem blue = new MenuItem("B", "Blue");
+  22     MenuRow allianceRow = new MenuRow("Alliance");
+  23     MenuItem nothing = new MenuItem("N", "Nothing");
+  24     MenuItem forward24 = new MenuItem("F24", "Forward 24");
+  25     MenuRow autoRow = new MenuRow("Auto");
+  26     OptionMenu optionMenu = new OptionMenu();
+  27     MenuButtons menuButtons = new MenuButtons(optionMenu);
+  28     Gamepad gamepad = new Gamepad();
+  29
+  30     @Before
+  31     // 'public' so JUnit can run it. 'public' allows access from any code.
+  32     public void addTheRows() {
+  33         allianceRow.add(red);
+  34         allianceRow.add(blue);
+  35         autoRow.add(nothing);
+  36         autoRow.add(forward24);
+  37         optionMenu.add(allianceRow);
+  38         optionMenu.add(autoRow);
+  39     }
+  40
+  41     @Test
+  42     // 'public' so JUnit can run it. 'public' allows access from any code.
+  43     public void theDpadAndAWorkTheMenu() {
+  44         Gamepad rightHeld = new Gamepad();
+  45         rightHeld.dpad_right = true;
+  46         press(rightHeld);
+  47         Gamepad aHeld = new Gamepad();
+  48         aHeld.a = true;
+  49         press(aHeld);
+  50         Gamepad downHeld = new Gamepad();
+  51         downHeld.dpad_down = true;
+  52         press(downHeld);
+  53         press(rightHeld);
+  54         press(aHeld);
+  55         String summary = optionMenu.getSummary();
+  56         assertEquals("B-F24", summary);
+  57     }
+  58
+  59     @Test
+  60     // 'public' so JUnit can run it. 'public' allows access from any code.
+  61     public void bClearsTheOffer() {
+  62         Gamepad rightHeld = new Gamepad();
+  63         rightHeld.dpad_right = true;
+  64         press(rightHeld);
+  65         Gamepad bHeld = new Gamepad();
+  66         bHeld.b = true;
+  67         press(bHeld);
+  68         Gamepad aHeld = new Gamepad();
+  69         aHeld.a = true;
+  70         press(aHeld);
+  71         String summary = optionMenu.getSummary();
+  72         assertEquals("R-N", summary);
+  73     }
+  74
+  75     void press(Gamepad held) {
+  76         gamepad.copy(held);
+  77         menuButtons.loop(gamepad);
+  78         Gamepad letGo = new Gamepad();
+  79         gamepad.copy(letGo);
+  80         menuButtons.loop(gamepad);
+  81     }
+  82 }
```
