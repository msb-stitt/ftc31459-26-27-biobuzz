# StickShaper

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L065

```{code-block} diff
:class: answer-diff

--- mytry/l065/StickShaper.java  (before L065)
+++ mytry/l065/StickShaper.java  (after L065)
@@ lines 1 to 5 @@
+   1 package org.firstinspires.ftc.teamcode.mytry.l065;
+   2
    3 /**
    4  * Shapes one stick's reading: a deadband, then linear, squared or cubed.
    5  * The object holds the band and the shape, so the code that asks for a shaped
@@ lines 10 to 77 @@
   10 // When on L065S030, see
   11 // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l065.html#l065s030
   12 // for what to write in this file.
+  13 // 'public' so later lessons, in other packages, can use it. 'public' allows access from any code.
+  14 public class StickShaper {
+  15
+  16     // 'public' so later lessons, in other packages, can use it. 'public' allows access from any code.
+  17     public enum Shape {
+  18         LINEAR,
+  19         SQUARED,
+  20         CUBED
+  21     }
+  22
+  23     double band;
+  24     Shape shape = Shape.SQUARED;
+  25
+  26     // 'public' so later lessons, in other packages, can use it. 'public' allows access from any code.
+  27     public StickShaper(double bandToSetToZero) {
+  28         band = bandToSetToZero;
+  29     }
+  30
+  31     // 'public' so later lessons, in other packages, can use it. 'public' allows access from any code.
+  32     public void setBand(double bandToSetToZero) {
+  33         band = bandToSetToZero;
+  34     }
+  35
+  36     // 'public' so later lessons, in other packages, can use it. 'public' allows access from any code.
+  37     public void setShape(Shape newShape) {
+  38         shape = newShape;
+  39     }
+  40
+  41     // 'public' so later lessons, in other packages, can use it. 'public' allows access from any code.
+  42     public double shaped(double value) {
+  43         double outsideTheBand = deadband(value, band);
+  44         double shapedValue = outsideTheBand;
+  45         switch (shape) {
+  46             case LINEAR:
+  47                 break;
+  48             case SQUARED:
+  49                 shapedValue = signedSquared(outsideTheBand);
+  50                 break;
+  51             case CUBED:
+  52                 shapedValue = cubed(outsideTheBand);
+  53                 break;
+  54         }
+  55         return shapedValue;
+  56     }
+  57
+  58     double deadband(double value, double band) {
+  59         double alwaysPositiveMagnitude = Math.abs(value);
+  60         boolean isCloseToZero = alwaysPositiveMagnitude < band;
+  61         if (isCloseToZero) {
+  62             return 0.0;
+  63         }
+  64         return value;
+  65     }
+  66
+  67     double signedSquared(double value) {
+  68         double alwaysPositiveValue = Math.abs(value);
+  69         double valueSquaredKeepingTheSign = value * alwaysPositiveValue;
+  70         return valueSquaredKeepingTheSign;
+  71     }
+  72
+  73     double cubed(double value) {
+  74         double valueCubed = value * value * value;
+  75         return valueCubed;
+  76     }
+  77 }
```
