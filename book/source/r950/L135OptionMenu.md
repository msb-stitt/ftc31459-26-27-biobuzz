# L135OptionMenu

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l135-menu-classes`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L135

```{code-block} diff
:class: answer-diff

--- mytry/L135OptionMenu.java  (before L135)
+++ mytry/L135OptionMenu.java  (after L135)
@@ lines 1 to 5 @@
+   1 package org.firstinspires.ftc.teamcode.mytry;
+   2
    3 /**
    4  * The rows of a menu, and which row the pointer is on. Up and down move the
    5  * pointer, wrap at the ends, and clear the offer in the row it leaves. Left,
@@ lines 10 to 99 @@
   10 // When on L135S030, see
   11 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l135.html#l135s030
   12 // for what to write in this file.
+  13 class L135OptionMenu {
+  14
+  15     L135MenuRow[] rows;
+  16     int pointedRow = 0;
+  17
+  18     L135OptionMenu(L135MenuRow... rows) {
+  19         this.rows = rows;
+  20     }
+  21
+  22     void up() {
+  23         back();
+  24         pointedRow = pointedRow - 1;
+  25         if (pointedRow < 0) {
+  26             pointedRow = rows.length - 1;
+  27         }
+  28     }
+  29
+  30     void down() {
+  31         back();
+  32         pointedRow = pointedRow + 1;
+  33         if (pointedRow == rows.length) {
+  34             pointedRow = 0;
+  35         }
+  36     }
+  37
+  38     void left() {
+  39         rows[pointedRow].left();
+  40     }
+  41
+  42     void right() {
+  43         rows[pointedRow].right();
+  44     }
+  45
+  46     void select() {
+  47         rows[pointedRow].select();
+  48     }
+  49
+  50     void back() {
+  51         rows[pointedRow].back();
+  52     }
+  53
+  54     String chosen(String rowName) {
+  55         L135MenuRow row = rowNamed(rowName);
+  56         L135MenuItem selectedItem = row.selected();
+  57         String fullName = selectedItem.fullName();
+  58         return fullName;
+  59     }
+  60
+  61     void startAt(String rowName, String fullName) {
+  62         L135MenuRow row = rowNamed(rowName);
+  63         row.startAt(fullName);
+  64     }
+  65
+  66     String summary() {
+  67         String summary = "";
+  68         for (int i = 0; i < rows.length; i++) {
+  69             L135MenuItem selectedItem = rows[i].selected();
+  70             if (i > 0) {
+  71                 summary = summary + "-";
+  72             }
+  73             summary = summary + selectedItem.shortName();
+  74         }
+  75         return summary;
+  76     }
+  77
+  78     String[] lines() {
+  79         String[] lines = new String[rows.length + 1];
+  80         lines[0] = summary();
+  81         for (int i = 0; i < rows.length; i++) {
+  82             String pointer = "  ";
+  83             if (i == pointedRow) {
+  84                 pointer = "> ";
+  85             }
+  86             lines[i + 1] = pointer + rows[i].line();
+  87         }
+  88         return lines;
+  89     }
+  90
+  91     L135MenuRow rowNamed(String rowName) {
+  92         for (int i = 0; i < rows.length; i++) {
+  93             if (rows[i].name().equals(rowName)) {
+  94                 return rows[i];
+  95             }
+  96         }
+  97         throw new IllegalArgumentException("No row named " + rowName);
+  98     }
+  99 }
```
