# L11FieldRelativeOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-try`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L11FieldRelativeOpMode.java`

## L11

### TODO (L11)

Before L11:

```java
        // TODO (L11): the same three sticks as L5, but through
        //       drivetrain.fieldRelative(...) instead of drivetrain.sticks(...).
        //       The heading goes in first, and the drivetrain cannot get it
        //       itself: follower.pose().heading() is where it comes from.
        //       Try L5's version with the robot turned 180 degrees first, so you
        //       can feel the difference.
```

After L11:

```java
        drivetrain.fieldRelative(follower.pose().heading(),
                -gamepad1.left_stick_y,      // away from the driver
                -gamepad1.left_stick_x,      // to the driver's left
                -gamepad1.right_stick_x);    // counter-clockwise
```

It also changes 1 run(s) of comment lines, which are not shown.
