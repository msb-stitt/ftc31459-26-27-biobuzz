# L132CompareLocalizersOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111`. A line marked + is new, and a line marked - is gone; each number
is the line's place in the file after the lesson.

## L132

```{code-block} diff
:class: answer-diff

--- mytry/L132CompareLocalizersOpMode.java  (before L132)
+++ mytry/L132CompareLocalizersOpMode.java  (after L132)
@@ lines 4 to 12 @@
    4 import com.pedropathing.ivy.Scheduler;
    5 import com.pedropathing.ivy.commands.Commands;
    6 import com.pedropathing.localization.Localizer;
+   7 import com.pedropathing.math.Pose;
+   8 import com.pedropathing.math.Twist;
+   9 import com.pedropathing.utils.Angle;
   10 import com.qualcomm.robotcore.eventloop.opmode.Disabled;
   11 import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
   12
@@ lines 14 to 23 @@
   14 import org.firstinspires.ftc.teamcode.base.CorbelsTeleOp;
   15 import org.firstinspires.ftc.teamcode.base.StickShaper;
   16 import org.firstinspires.ftc.teamcode.base.Tracker;
+  17 import org.firstinspires.ftc.teamcode.base.odometry.HardwarePinpointSource;
+  18 import org.firstinspires.ftc.teamcode.base.odometry.HardwareWheelSource;
+  19 import org.firstinspires.ftc.teamcode.base.odometry.PinpointSource;
+  20 import org.firstinspires.ftc.teamcode.pedro.Constants;
   21
   22 /**
   23  * L132: drive around while three localizers disagree. Your localizer from L113
@@ lines 73 to 80 @@
   73         // When on L132S050, see
   74         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s050
   75         // for what to do here.
+  76         Command compareWithPedrosEveryLoop = Commands.infinite(this::compareWithPedros);
+  77         Scheduler.schedule(compareWithPedrosEveryLoop);
   78
   79         startAfter();
   80     }
@@ lines 117 to 151 @@
  117         // When on L132S050, see
  118         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s050
  119         // for what to do here.
+ 120         Pose pedros = follower.pose();
+ 121         publishError("driveWheelEncoders", driveWheelEncoders.pose(), pedros);
+ 122         publishError("pinpointPods", pinpointPods.pose(), pedros);
  123
  124         // When on L132S060, see
  125         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s060
  126         // for what to do here.
+ 127         Twist podsSpeed = pinpointPods.twist();
+ 128         Twist pedrosSpeed = follower.localizer.twist();
+ 129         Tracker.publish("Speed/pinpointPods/forward_ips", podsSpeed.vx);
+ 130         Tracker.publish("Speed/pinPoint/forward_ips", pedrosSpeed.vx);
+ 131         Tracker.publish("Speed/pinpointPods/strafe_ips", podsSpeed.vy);
+ 132         Tracker.publish("Speed/pinPoint/strafe_ips", pedrosSpeed.vy);
+ 133         Tracker.publish("Speed/pinpointPods/turn_degps", Math.toDegrees(podsSpeed.omega));
+ 134         Tracker.publish("Speed/pinPoint/turn_degps", Math.toDegrees(pedrosSpeed.omega));
  135     }
  136
  137     // When on L132S050, see
  138     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s050
  139     // for what to do here.
+ 140     /** How far one of your localizers is from the robot's own, in x, y and heading. */
+ 141     void publishError(String name, Pose yours, Pose pedros) {
+ 142         double xError_in = yours.x() - pedros.x();
+ 143         double yError_in = yours.y() - pedros.y();
+ 144         double headingError_rad = Angle.normalizeSigned(yours.heading() - pedros.heading());
+ 145         Tracker.publish("Error/" + name + "/x_in", xError_in);
+ 146         Tracker.publish("Error/" + name + "/y_in", yError_in);
+ 147         Tracker.publish("Error/" + name + "/heading_deg", Math.toDegrees(headingError_rad));
+ 148     }
  149
  150     // 'public' to match the parent class. 'public' allows access from any code.
  151     @Override
@@ lines 153 to 165 @@
  153         // When on L132S030, see
  154         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s030
  155         // for what to do here.
+ 156         HardwareWheelSource wheelSource = new HardwareWheelSource(hardware);
+ 157         driveWheelEncoders = new L113EncoderLocalizer(wheelSource);
+ 158         shadowLocalizers.add("driveWheelEncoders", driveWheelEncoders);
+ 159
+ 160         PinpointSource pinpointSource = HardwarePinpointSource.find(hardwareMap);
+ 161         pinpointPods = new L120PinpointLocalizer(pinpointSource, Constants.localizerConfig);
+ 162         shadowLocalizers.add("pinpointPods", pinpointPods);
  163     }
  164
  165     // 'public' to match the parent class. 'public' allows access from any code.
```
