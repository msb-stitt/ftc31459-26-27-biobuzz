# OptionMenu

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111.1`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L093

```{code-block} diff
:class: answer-diff

--- mytry/l093/OptionMenu.java  (before L093)
+++ mytry/l093/OptionMenu.java  (after L093)
@@ lines 1 to 7 @@
+   1 package org.firstinspires.ftc.teamcode.mytry.l093;
+   2
+   3 import java.util.ArrayList;
+   4
    5 /**
    6  * The rows of a menu, and which row the pointer is on. Up and down move the
    7  * pointer, wrap at the ends, and clear the offer in the row it leaves. Left,
@@ lines 12 to 92 @@
   12 // When on L093S070, see
   13 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l093.html#l093s070
   14 // for what to write in this file.
+  15 // 'public' so later lessons, in other packages, can use it. 'public' allows access from any code.
+  16 public class OptionMenu {
+  17
+  18     // 'private' so only this class can use it. 'private' allows access from this class only.
+  19     private ArrayList<MenuRow> rows = new ArrayList<>();
+  20     // 'private' so only this class can use it. 'private' allows access from this class only.
+  21     private int pointedIndex = 0;
+  22
+  23     // 'public' so later lessons, in other packages, can use it. 'public' allows access from any code.
+  24     public void add(MenuRow row) {
+  25         rows.add(row);
+  26     }
+  27
+  28     void goUp() {
+  29         goBack();
+  30         pointedIndex = pointedIndex - 1;
+  31         if (pointedIndex < 0) {
+  32             pointedIndex = rows.size() - 1;
+  33         }
+  34     }
+  35
+  36     void goDown() {
+  37         goBack();
+  38         pointedIndex = pointedIndex + 1;
+  39         if (pointedIndex == rows.size()) {
+  40             pointedIndex = 0;
+  41         }
+  42     }
+  43
+  44     void goRight() {
+  45         MenuRow pointedRow = rows.get(pointedIndex);
+  46         pointedRow.goRight();
+  47     }
+  48
+  49     void goLeft() {
+  50         MenuRow pointedRow = rows.get(pointedIndex);
+  51         pointedRow.goLeft();
+  52     }
+  53
+  54     void select() {
+  55         MenuRow pointedRow = rows.get(pointedIndex);
+  56         pointedRow.select();
+  57     }
+  58
+  59     void goBack() {
+  60         MenuRow pointedRow = rows.get(pointedIndex);
+  61         pointedRow.goBack();
+  62     }
+  63
+  64     String getSummary() {
+  65         String summary = "";
+  66         String between = "";
+  67         for (MenuRow row : rows) {
+  68             MenuItem selectedItem = row.getSelected();
+  69             String shortName = selectedItem.getShortName();
+  70             summary = summary + between + shortName;
+  71             between = "-";
+  72         }
+  73         return summary;
+  74     }
+  75
+  76     ArrayList<String> getLines() {
+  77         ArrayList<String> lines = new ArrayList<>();
+  78         String summary = getSummary();
+  79         lines.add(summary);
+  80         MenuRow pointedRow = rows.get(pointedIndex);
+  81         for (MenuRow row : rows) {
+  82             String pointer = "  ";
+  83             if (row == pointedRow) {
+  84                 pointer = "> ";
+  85             }
+  86             String rowLine = row.getLine();
+  87             String pointedLine = pointer + rowLine;
+  88             lines.add(pointedLine);
+  89         }
+  90         return lines;
+  91     }
+  92 }
```
