# L15CombinedOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l010`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L15CombinedOpMode.java`

## L15

### TODO 1 (L15)

Before L15:

```java
        // TODO 1 (L15): add your encoder localizer as "driveWheelEncoders", as in
        //         L8, so the two localizers plot under the same names as they did
        //         there.
```

After L15:

```java
        shadowLocalizers.add("driveWheelEncoders",
                new MecanumEncoderLocalizer(new HardwareWheelSource(hardware)));
```

It also changes 1 run(s) of comment lines, which are not shown.
