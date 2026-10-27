# L140FieldRelativeOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l140-alliances`. A line marked + is new, and a line marked -
is gone; each number is the line's place in the file after the lesson.

## L140

```{code-block} diff
:class: answer-diff

--- mytry/L140FieldRelativeOpMode.java  (before L140)
+++ mytry/L140FieldRelativeOpMode.java  (after L140)
@@ lines 107 to 117 @@
  107         // When on L140S050, see
  108         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s050
  109         // for what to do here.
+ 110         String alliance = menu.chosen("Alliance");
+ 111         if (alliance.equals("Blue")) {
+ 112             fieldXSpeed = leftStickY;
+ 113             fieldYSpeed = leftStickX;
+ 114         }
  115
  116         fieldXSpeed = speedShaped(fieldXSpeed, bandToSetToZero);
  117         Tracker.publish("drivetrain/fieldXSpeed", fieldXSpeed);
@@ lines 125 to 138 @@
  125         // When on L140S040, see
  126         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l140.html#l140s040
  127         // for what to do here.
-             drivetrain.sticks(fieldXSpeed, fieldYSpeed, turnCcwSpeed);
+ 128         double heading_rad = follower.pose().heading();
+ 129         double sinHeading = Math.sin(heading_rad);
+ 130         double cosHeading = Math.cos(heading_rad);
+ 131         double robotForwardXSpeed = fieldXSpeed * cosHeading + fieldYSpeed * sinHeading;
+ 132         double robotStrafeYSpeed = -fieldXSpeed * sinHeading + fieldYSpeed * cosHeading;
+ 133         Tracker.publish("drivetrain/robotForwardXSpeed", robotForwardXSpeed);
+ 134         Tracker.publish("drivetrain/robotStrafeYSpeed", robotStrafeYSpeed);
+ 135         drivetrain.sticks(robotForwardXSpeed, robotStrafeYSpeed, turnCcwSpeed);
  136     }
  137
  138     // 'public' to match the parent class. 'public' allows access from any code.
```
