# L132CompareLocalizersOpMode

What each lesson's patch does to this file, in the order the lessons come, from applying
`solutions/` on `solutions-e111.2`. A line marked + is new, and a line marked - is gone; each number
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
@@ lines 77 to 84 @@
   77         // When on L132S050, see
   78         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s050
   79         // for what to do here.
+  80         Command compareWithPedrosEveryLoop = Commands.infinite(this::compareWithPedros);
+  81         Scheduler.schedule(compareWithPedrosEveryLoop);
   82
   83         startAfter();
   84     }
@@ lines 121 to 141 @@
  121         // When on L132S050, see
  122         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s050
  123         // for what to do here.
+ 124         Pose pedros = follower.pose();
+ 125         publishError("driveWheelEncoders", driveWheelEncoders.pose(), pedros);
+ 126         publishError("pinpointPods", pinpointPods.pose(), pedros);
  127
  128         // When on L132S060, see
  129         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s060
  130         // for what to do here.
+ 131         Twist podsSpeed = pinpointPods.twist();
+ 132         Twist pedrosSpeed = follower.localizer.twist();
+ 133         Tracker.publish("Speed/pinpointPods/forward_ips", podsSpeed.vx);
+ 134         Tracker.publish("Speed/pinPoint/forward_ips", pedrosSpeed.vx);
+ 135         Tracker.publish("Speed/pinpointPods/strafe_ips", podsSpeed.vy);
+ 136         Tracker.publish("Speed/pinPoint/strafe_ips", pedrosSpeed.vy);
+ 137         Tracker.publish("Speed/pinpointPods/turn_degps", Math.toDegrees(podsSpeed.omega));
+ 138         Tracker.publish("Speed/pinPoint/turn_degps", Math.toDegrees(pedrosSpeed.omega));
  139
  140         // When on L132S110, see
  141         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s110
@@ lines 145 to 159 @@
  145     // When on L132S050, see
  146     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s050
  147     // for what to do here.
+ 148     /** How far one of your localizers is from the robot's own, in x, y and heading. */
+ 149     void publishError(String name, Pose yours, Pose pedros) {
+ 150         double xError_in = yours.x() - pedros.x();
+ 151         double yError_in = yours.y() - pedros.y();
+ 152         double headingError_rad = Angle.normalizeSigned(yours.heading() - pedros.heading());
+ 153         Tracker.publish("Error/" + name + "/x_in", xError_in);
+ 154         Tracker.publish("Error/" + name + "/y_in", yError_in);
+ 155         Tracker.publish("Error/" + name + "/heading_deg", Math.toDegrees(headingError_rad));
+ 156     }
  157
  158     // 'public' to match the parent class. 'public' allows access from any code.
  159     @Override
@@ lines 161 to 173 @@
  161         // When on L132S030, see
  162         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s030
  163         // for what to do here.
+ 164         HardwareWheelSource wheelSource = new HardwareWheelSource(hardware);
+ 165         driveWheelEncoders = new L113EncoderLocalizer(wheelSource);
+ 166         shadowLocalizers.add("driveWheelEncoders", driveWheelEncoders);
+ 167
+ 168         PinpointSource pinpointSource = HardwarePinpointSource.find(hardwareMap);
+ 169         pinpointPods = new L120PinpointLocalizer(pinpointSource, Constants.localizerConfig);
+ 170         shadowLocalizers.add("pinpointPods", pinpointPods);
  171
  172         // When on L132S110, see
  173         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s110
```

## L132

```{code-block} diff
:class: answer-diff

--- mytry/L132CompareLocalizersOpMode.java  (before L132)
+++ mytry/L132CompareLocalizersOpMode.java  (after L132)
@@ lines 56 to 62 @@
   56     // When on L132S110, see
   57     // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s110
   58     // for what to do here.
+  59     L113EncoderLocalizer measuredWheels;
   60
   61     // 'public' to match the parent class. 'public' allows access from any code.
   62     @Override
@@ lines 141 to 147 @@
  141         // When on L132S110, see
  142         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s110
  143         // for what to do here.
+ 144         publishError("measuredWheels", measuredWheels.pose(), pedros);
  145     }
  146
  147     // When on L132S050, see
@@ lines 174 to 182 @@
  174         // When on L132S110, see
  175         // https://coding.stitt.cc/ftc-lessons-java/guide/tasks/l132.html#l132s110
  176         // for what to do here.
+ 177         measuredWheels = new L113EncoderLocalizer(wheelSource);
+ 178         measuredWheels.cotAlpha = 1.19;
+ 179         shadowLocalizers.add("measuredWheels", measuredWheels);
  180     }
  181
  182     // 'public' to match the parent class. 'public' allows access from any code.
```
