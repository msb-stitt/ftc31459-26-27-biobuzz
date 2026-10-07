# L170DriveToPoseOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l010.1`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L170DriveToPoseOpMode.java`

## L170

### A change

Before L170:

```java
(nothing)
```

After L170:

```java
import com.pedropathing.ivy.commands.Commands;
```

### A change

Before L170:

```java
(nothing)
```

After L170:

```java
        buttons.whenPressed(() -> gamepad1.y, Commands.instant(() -> {
            follower.hold(target);
            drivingItself = true;
        }));
```

### A change

Before L170:

```java
(nothing)
```

After L170:

```java
        if (drivingItself) {
            if (!driverWantsControl) {
                Tracker.publish("drive/mode", "AUTO");
                drivetrain.releaseCommandedWheels();
                return;
            }
            drivingItself = false;
            follower.manual(0, 0, 0);
        }
        Tracker.publish("drive/mode", "DRIVER");
```
