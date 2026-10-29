# L135MenuRow

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-l135-menu-classes.2`. A line marked + is new, and a line marked - is
gone; each number is the line's place in the file after the lesson.

## L135

```{code-block} diff
:class: answer-diff

--- mytry/L135MenuRow.java  (before L135)
+++ mytry/L135MenuRow.java  (after L135)
@@ lines 1 to 5 @@
+   1 package org.firstinspires.ftc.teamcode.mytry;
+   2
    3 /**
    4  * One row of an {@link L135OptionMenu}: its name and its choices. Left and right
    5  * offer the choice before or after, and wrap at the ends. A selects the choice
@@ lines 10 to 76 @@
   10 // When on L135S050, see
   11 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s050
   12 // for what to write in this file.
+  13 class L135MenuRow {
+  14
+  15     String name;
+  16     L135MenuItem[] items;
+  17     int selectedIndex = 0;
+  18     int offeredIndex = 0;
+  19
+  20     L135MenuRow(String name, L135MenuItem... items) {
+  21         this.name = name;
+  22         this.items = items;
+  23     }
+  24
+  25     String name() {
+  26         return name;
+  27     }
+  28
+  29     void right() {
+  30         int lastIndex = items.length - 1;
+  31         offeredIndex = offeredIndex + 1;
+  32         if (offeredIndex > lastIndex) {
+  33             offeredIndex = 0;
+  34         }
+  35     }
+  36
+  37     void left() {
+  38         int lastIndex = items.length - 1;
+  39         offeredIndex = offeredIndex - 1;
+  40         if (offeredIndex < 0) {
+  41             offeredIndex = lastIndex;
+  42         }
+  43     }
+  44
+  45     void select() {
+  46         selectedIndex = offeredIndex;
+  47     }
+  48
+  49     void back() {
+  50         offeredIndex = selectedIndex;
+  51     }
+  52
+  53     L135MenuItem selected() {
+  54         L135MenuItem selectedItem = items[selectedIndex];
+  55         return selectedItem;
+  56     }
+  57
+  58     void startAt(String fullName) {
+  59         for (int i = 0; i < items.length; i++) {
+  60             if (items[i].fullName().equals(fullName)) {
+  61                 selectedIndex = i;
+  62                 offeredIndex = i;
+  63             }
+  64         }
+  65     }
+  66
+  67     String line() {
+  68         L135MenuItem offeredItem = items[offeredIndex];
+  69         String choiceShown = "<" + offeredItem.fullName() + ">";
+  70         if (offeredIndex != selectedIndex) {
+  71             choiceShown = offeredItem.fullName() + "? A:OK B:Back";
+  72         }
+  73         String rowLine = name + ": " + choiceShown;
+  74         return rowLine;
+  75     }
+  76 }
```
