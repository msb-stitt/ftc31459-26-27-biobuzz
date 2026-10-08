# L180CombinedOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l010.1`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L180CombinedOpMode.java`

## L180

### A change

Before L180:

```java
(nothing)
```

After L180:

```java
import org.firstinspires.ftc.teamcode.base.odometry.HardwareWheelSource;
```

### A change

Before L180:

```java
(nothing)
```

After L180:

```java
        shadowLocalizers.add("driveWheelEncoders",
                new L120EncoderLocalizer(new HardwareWheelSource(hardware)));
```
