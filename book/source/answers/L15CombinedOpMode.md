# L15CombinedOpMode

The blanks in this file, filled in from `solutions-03`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L15CombinedOpMode.java`

## An unmarked difference

What the lesson leaves blank:

```java
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
```

What the solutions line has there:

```java
(nothing)
```

## An unmarked difference

What the lesson leaves blank:

```java
@Disabled
```

What the solutions line has there:

```java
(nothing)
```

## TODO 1 (L15)

What the lesson leaves blank:

```java
        // TODO 1 (L15): add your encoder localizer as "driveWheelEncoders", as in
        //         L8, so the two localizers plot under the same names as they did
        //         there.
```

What the solutions line has there:

```java
        shadowLocalizers.add("driveWheelEncoders",
                new MecanumEncoderLocalizer(new HardwareWheelSource(hardware)));
```

The two lines also differ in 1 run(s) of comment lines, which are not
blanks and are not shown.
