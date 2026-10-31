# L153FeedforwardOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111.2`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L153

```{code-block} diff
:class: answer-diff

--- mytry/L153FeedforwardOpMode.java  (before L153)
+++ mytry/L153FeedforwardOpMode.java  (after L153)
@@ lines 12 to 17 @@
   12 import com.pedropathing.math.Twist;
   13 import com.pedropathing.utils.Angle;
   14 import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
-     import com.qualcomm.robotcore.eventloop.opmode.Disabled;
   15
   16 import org.firstinspires.ftc.teamcode.base.CorbelsAuto;
   17 import org.firstinspires.ftc.teamcode.base.CorbelsMecanum;
@@ lines 130 to 146 @@
  130         // When on L153S050, see
  131         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l153.html#l153s050
  132         // for what to do here.
-             return Command.NOOP;
+ 133         Command turnRuns = sequential(
+ 134                 oneRun(Axis.TURN, 0.2), oneRun(Axis.TURN, 0.4),
+ 135                 oneRun(Axis.TURN, 0.6), oneRun(Axis.TURN, 0.8));
+ 136         Command forwardRuns = sequential(
+ 137                 oneRun(Axis.FORWARD, 0.2), oneRun(Axis.FORWARD, -0.4),
+ 138                 oneRun(Axis.FORWARD, 0.6), oneRun(Axis.FORWARD, -0.8));
+ 139         Command strafeRuns = sequential(
+ 140                 oneRun(Axis.STRAFE, 0.2), oneRun(Axis.STRAFE, -0.4),
+ 141                 oneRun(Axis.STRAFE, 0.6), oneRun(Axis.STRAFE, -0.8));
+ 142         Command runs = sequential(turnRuns, forwardRuns, strafeRuns, instant(this::publishTheFits));
+ 143         return runs;
  144     }
  145
  146     /** One run: drive at a fixed power, stop in time, hold until still, and keep what it found. */
@@ lines 265 to 284 @@
  265         // When on L153S060, see
  266         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l153.html#l153s060
  267         // for what to do here.
+ 268         double earlySpeed = earlySpeedSum / earlyLoops;
+ 269         double lateSpeed = lateSpeedSum / lateLoops;
+ 270         double volts = lateVoltsSum / lateLoops;
+ 271         boolean steady = Math.abs(lateSpeed - earlySpeed) < STEADY_FRACTION * lateSpeed;
+ 272         if (steady && axis == Axis.FORWARD) {
+ 273             forwardFit.add(volts, lateSpeed);
+ 274         } else if (steady && axis == Axis.STRAFE) {
+ 275             strafeFit.add(volts, lateSpeed);
+ 276         } else if (steady) {
+ 277             turnFit.add(volts, lateSpeed);
+ 278         }
+ 279         Tracker.publish("Run/volts", volts);
+ 280         Tracker.publish("Run/speed", lateSpeed);
+ 281         Tracker.publish("Run/steady", steady);
  282     }
  283
  284     /** What each fit found, for AdvantageScope. */
```
