# l157-StickShaper

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111.2`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L157

```{code-block} diff
:class: answer-diff

--- mytry/l157/StickShaper.java  (before L157)
+++ mytry/l157/StickShaper.java  (after L157)
@@ lines 22 to 28 @@
   22
   23     double band;
   24     Shape shape = Shape.SQUARED;
+  25     boolean respread;
   26
   27     // 'public' so later lessons, in other packages, can use it. 'public' allows access from any code.
   28     public StickShaper(double bandToSetToZero) {
@@ lines 40 to 55 @@
   40     }
   41
   42     // 'public' so later lessons, in other packages, can use it. 'public' allows access from any code.
+  43     public void setRespread(boolean respreadPastTheBand) {
+  44         respread = respreadPastTheBand;
+  45     }
+  46
+  47     // 'public' so later lessons, in other packages, can use it. 'public' allows access from any code.
   48     public double shaped(double value) {
   49         double outsideTheBand = deadband(value, band);
+  50         if (respread) {
+  51             outsideTheBand = respreadPastTheBand(outsideTheBand, band);
+  52         }
   53         double shapedValue = outsideTheBand;
   54         switch (shape) {
   55             case LINEAR:
@@ lines 73 to 84 @@
   73         return value;
   74     }
   75
+  76     double respreadPastTheBand(double value, double band) {
+  77         double pastTheBand = Math.abs(value) - band;
+  78         double spreadFromZeroToOne = pastTheBand / (1.0 - band);
+  79         return Math.signum(value) * spreadFromZeroToOne;
+  80     }
+  81
   82     double signedSquared(double value) {
   83         double alwaysPositiveValue = Math.abs(value);
   84         double valueSquaredKeepingTheSign = value * alwaysPositiveValue;
```
