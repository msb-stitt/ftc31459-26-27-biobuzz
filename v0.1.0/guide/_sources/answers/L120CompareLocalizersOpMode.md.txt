# L120CompareLocalizersOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l010`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L120CompareLocalizersOpMode.java`

## L120

### A change

Before L120:

```java
(nothing)
```

After L120:

```java
import org.firstinspires.ftc.teamcode.base.odometry.HardwareWheelSource;
```

### A change

Before L120:

```java
(nothing)
```

After L120:

```java
        shadowLocalizers.add("driveWheelEncoders",
                new L120EncoderLocalizer(new HardwareWheelSource(hardware)));
```
