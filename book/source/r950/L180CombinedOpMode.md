# L180CombinedOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-rules.1`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L180CombinedOpMode.java`

## L180

### a change

Before L180:

```java
(nothing)
```

After L180:

```java
import org.firstinspires.ftc.teamcode.base.odometry.HardwareWheelSource;
```

### a change

Before L180:

```java
(nothing)
```

After L180:

```java
        shadowLocalizers.add("driveWheelEncoders",
                new L120EncoderLocalizer(new HardwareWheelSource(hardware)));
```
