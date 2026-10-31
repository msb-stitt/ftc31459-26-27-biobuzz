# MenuRow

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111.1`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L093

```{code-block} diff
:class: answer-diff

--- mytry/l093/MenuRow.java  (before L093)
+++ mytry/l093/MenuRow.java  (after L093)
@@ lines 1 to 7 @@
+   1 package org.firstinspires.ftc.teamcode.mytry.l093;
+   2
+   3 import java.util.ArrayList;
+   4
    5 /**
    6  * One row of an {@link OptionMenu}: its name and the choices added to it.
    7  * Left and right offer the choice before or after, and wrap at the ends. A
@@ lines 13 to 78 @@
   13 // When on L093S040, see
   14 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l093.html#l093s040
   15 // for what to write in this file.
+  16 // 'public' so later lessons, in other packages, can use it. 'public' allows access from any code.
+  17 public class MenuRow {
+  18
+  19     // 'private' so only this class can use it. 'private' allows access from this class only.
+  20     private String name;
+  21     // 'private' so only this class can use it. 'private' allows access from this class only.
+  22     private ArrayList<MenuItem> items = new ArrayList<>();
+  23     // 'private' so only this class can use it. 'private' allows access from this class only.
+  24     private int selectedIndex = 0;
+  25     // 'private' so only this class can use it. 'private' allows access from this class only.
+  26     private int offeredIndex = 0;
+  27
+  28     // 'public' so later lessons, in other packages, can use it. 'public' allows access from any code.
+  29     public MenuRow(String name) {
+  30         this.name = name;
+  31     }
+  32
+  33     // 'public' so later lessons, in other packages, can use it. 'public' allows access from any code.
+  34     public void add(MenuItem item) {
+  35         items.add(item);
+  36     }
+  37
+  38     void goRight() {
+  39         int lastIndex = items.size() - 1;
+  40         offeredIndex = offeredIndex + 1;
+  41         if (offeredIndex > lastIndex) {
+  42             offeredIndex = 0;
+  43         }
+  44     }
+  45
+  46     void goLeft() {
+  47         int lastIndex = items.size() - 1;
+  48         offeredIndex = offeredIndex - 1;
+  49         if (offeredIndex < 0) {
+  50             offeredIndex = lastIndex;
+  51         }
+  52     }
+  53
+  54     void select() {
+  55         selectedIndex = offeredIndex;
+  56     }
+  57
+  58     void goBack() {
+  59         offeredIndex = selectedIndex;
+  60     }
+  61
+  62     // 'public' so later lessons, in other packages, can use it. 'public' allows access from any code.
+  63     public MenuItem getSelected() {
+  64         MenuItem selectedItem = items.get(selectedIndex);
+  65         return selectedItem;
+  66     }
+  67
+  68     String getLine() {
+  69         MenuItem offeredItem = items.get(offeredIndex);
+  70         String fullName = offeredItem.getFullName();
+  71         String choiceShown = "<" + fullName + ">";
+  72         if (offeredIndex != selectedIndex) {
+  73             choiceShown = fullName + "? A:OK B:Back";
+  74         }
+  75         String rowLine = name + ": " + choiceShown;
+  76         return rowLine;
+  77     }
+  78 }
```
