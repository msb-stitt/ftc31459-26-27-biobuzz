# L4ArcadeOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l010`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L4ArcadeOpMode.java`

## L4

### TODO 

Before L4:

```java
        // TODO: read the two sticks and hand them to drivetrain.sticks(forwardSpeed, turnCcwSpeed).
        //       forward comes from the left stick's y axis, turn from the right
        //       stick's x axis, and BOTH need a minus sign -- the note above says
        //       why the turn one does. Log them as command/forward and
        //       command/turn_ccw so Panels can draw them.
        double forwardSpeed = 0;
        double turnCcwSpeed = 0;
```

After L4:

```java
        double forwardSpeed = -gamepad1.left_stick_y;
        double turnCcwSpeed = -gamepad1.right_stick_x;
```

### A change

Before L4:

```java
(nothing)
```

After L4:

```java

        Tracker.publish("command/forward", forwardSpeed);
        Tracker.publish("command/turn_ccw", turnCcwSpeed);
```

It also changes 1 run(s) of comment lines, which are not shown.
