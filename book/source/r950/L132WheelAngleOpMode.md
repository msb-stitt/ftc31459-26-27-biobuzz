# L132WheelAngleOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L132

```{code-block} diff
:class: answer-diff

--- mytry/L132WheelAngleOpMode.java  (before L132)
+++ mytry/L132WheelAngleOpMode.java  (after L132)
@@ lines 1 to 10 @@
    1 package org.firstinspires.ftc.teamcode.mytry;
    2
    3 import com.pedropathing.api.PoseFactory;
+   4 import static com.pedropathing.ivy.commands.Commands.instant;
+   5 import static com.pedropathing.ivy.commands.Commands.waitUntil;
+   6 import static com.pedropathing.ivy.groups.Groups.sequential;
+   7
    8 import com.pedropathing.ivy.Command;
    9 import com.pedropathing.ivy.Scheduler;
   10 import com.pedropathing.ivy.commands.Commands;
@@ lines 98 to 145 @@
   98         // When on L132S100, see
   99         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s100
  100         // for what to do here.
+ 101         Command strafeRun = sequential(
+ 102                 instant(this::startTheStrafeRun), waitUntil(this::slidTwoFeet),
+ 103                 instant(this::stopTheRun), waitUntil(this::standingStill));
  104
  105         // When on L132S120, see
  106         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s120
  107         // for what to do here.
-             return Command.NOOP;
+ 108         Command mixedRun = sequential(
+ 109                 instant(this::startTheMixedRun), waitUntil(this::turnedHalfway),
+ 110                 instant(this::stopTheRun), waitUntil(this::standingStill));
+ 111
+ 112         Command runs = sequential(strafeRun, mixedRun, instant(this::publishTheFits));
+ 113         return runs;
  114     }
  115
  116     // When on L132S100, see
  117     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s100
  118     // for what to do here.
+ 119     /** Slide left at 0.4 power, adding to the strafe run's sums. */
+ 120     void startTheStrafeRun() {
+ 121         fitting = strafeFit;
+ 122         follower.manual(0.0, 0.4, 0.0);
+ 123     }
+ 124
+ 125     /** True once the Pinpoint has seen the robot slide 24 inches. */
+ 126     boolean slidTwoFeet() {
+ 127         return strafeFit.robotStrafe_in >= 24.0;
+ 128     }
  129
  130     // When on L132S120, see
  131     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s120
  132     // for what to do here.
+ 133     /** Drive forward, slide left and turn at once, adding to the mixed run's sums. */
+ 134     void startTheMixedRun() {
+ 135         fitting = mixedFit;
+ 136         follower.manual(0.3, 0.3, 0.3);
+ 137     }
+ 138
+ 139     /** True once the Pinpoint has seen the robot turn half a circle. */
+ 140     boolean turnedHalfway() {
+ 141         return mixedFit.robotTurn_rad >= Math.PI;
+ 142     }
  143
  144     /** All four wheels to zero, so the robot stops. */
  145     void stopTheRun() {
@@ lines 159 to 171 @@
  159         // When on L132S100, see
  160         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s100
  161         // for what to do here.
+ 162         Tracker.publish("Fit/strafeRun/cotAlpha", strafeFit.cotAlpha());
  163
  164         // When on L132S120, see
  165         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s120
  166         // for what to do here.
+ 167         Tracker.publish("Fit/mixedRun/cotAlpha", mixedFit.cotAlpha());
+ 168         Tracker.publish("Fit/mixedRun/turnRadius_in", mixedFit.turnRadius_in());
  169     }
  170
  171     /**
```
