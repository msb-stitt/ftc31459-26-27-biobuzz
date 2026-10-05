# L8CompareLocalizersOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l010`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L8CompareLocalizersOpMode.java`

## L8

### TODO 1 (L8)

Before L8:

```java
        // TODO 1 (L8): run your localizer alongside the robot's own, named
        //         "driveWheelEncoders" because that is which encoders it reads:
        //         shadowLocalizers.add("driveWheelEncoders",
        //                 new MecanumEncoderLocalizer(new HardwareWheelSource(hardware)));
```

After L8:

```java
        shadowLocalizers.add("driveWheelEncoders",
                new MecanumEncoderLocalizer(new HardwareWheelSource(hardware)));
```

### TODO 2 (L8)

Before L8:

```java
        // TODO 2 (L8): holonomic driving, same as L5 -- but through the drivetrain
        //         the follower is holding now, the way L6 handed it over. One call
        //         to drivetrain.sticks() with the three sticks.
```

After L8:

```java
        drivetrain.sticks(
                -gamepad1.left_stick_y, -gamepad1.left_stick_x, -gamepad1.right_stick_x);
```

It also changes 1 run(s) of comment lines, which are not shown.
