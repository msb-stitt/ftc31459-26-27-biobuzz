# L135MenuButtonsTest

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l135-menu-classes`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L135

```{code-block} diff
:class: answer-diff

--- mytry/L135MenuButtonsTest.java  (before L135)
+++ mytry/L135MenuButtonsTest.java  (after L135)
@@ lines 1 to 11 @@
+   1 package org.firstinspires.ftc.teamcode.mytry;
+   2
+   3 import static org.junit.Assert.assertEquals;
+   4
+   5 import com.qualcomm.robotcore.hardware.Gamepad;
+   6
+   7 import org.junit.Test;
+   8
    9 /**
   10  * Tests for {@link L135MenuButtons}: a press of the D-pad, A or B on a
   11  * gamepad moves the menu once.
@@ lines 13 to 70 @@
   13 // When on L135S110, see
   14 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s110
   15 // for what to write in this file.
+  16 // 'public' so JUnit can run it. 'public' allows access from any code.
+  17 public class L135MenuButtonsTest {
+  18
+  19     L135MenuItem red = new L135MenuItem("R", "Red");
+  20     L135MenuItem blue = new L135MenuItem("B", "Blue");
+  21     L135MenuRow allianceRow = new L135MenuRow("Alliance", red, blue);
+  22     L135MenuItem nothing = new L135MenuItem("N", "Nothing");
+  23     L135MenuItem forward24 = new L135MenuItem("F24", "Forward 24");
+  24     L135MenuRow autoRow = new L135MenuRow("Auto", nothing, forward24);
+  25     L135OptionMenu optionMenu = new L135OptionMenu(allianceRow, autoRow);
+  26     L135MenuButtons menuButtons = new L135MenuButtons(optionMenu);
+  27     Gamepad gamepad = new Gamepad();
+  28
+  29     @Test
+  30     // 'public' so JUnit can run it. 'public' allows access from any code.
+  31     public void theDpadAndAWorkTheMenu() {
+  32         Gamepad rightHeld = new Gamepad();
+  33         rightHeld.dpad_right = true;
+  34         press(rightHeld);
+  35         Gamepad aHeld = new Gamepad();
+  36         aHeld.a = true;
+  37         press(aHeld);
+  38         Gamepad downHeld = new Gamepad();
+  39         downHeld.dpad_down = true;
+  40         press(downHeld);
+  41         press(rightHeld);
+  42         press(aHeld);
+  43         String summary = optionMenu.summary();
+  44         assertEquals("B-F24", summary);
+  45     }
+  46
+  47     @Test
+  48     // 'public' so JUnit can run it. 'public' allows access from any code.
+  49     public void bClearsTheOffer() {
+  50         Gamepad rightHeld = new Gamepad();
+  51         rightHeld.dpad_right = true;
+  52         press(rightHeld);
+  53         Gamepad bHeld = new Gamepad();
+  54         bHeld.b = true;
+  55         press(bHeld);
+  56         Gamepad aHeld = new Gamepad();
+  57         aHeld.a = true;
+  58         press(aHeld);
+  59         String summary = optionMenu.summary();
+  60         assertEquals("R-N", summary);
+  61     }
+  62
+  63     void press(Gamepad held) {
+  64         gamepad.copy(held);
+  65         menuButtons.loop(gamepad);
+  66         Gamepad letGo = new Gamepad();
+  67         gamepad.copy(letGo);
+  68         menuButtons.loop(gamepad);
+  69     }
+  70 }
```
