# L12RobotRelativeButtonOpMode

The blanks in this file, filled in from `solutions-03`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons/L12RobotRelativeButtonOpMode.java`

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

## TODO (L12)

What the lesson leaves blank:

```java
        // TODO (L12): if gamepad1.right_bumper is held, drive robot relative
        //       (drivetrain.sticks); otherwise field relative
        //       (drivetrain.fieldRelative, with follower.pose().heading() first).
        //       Held, not toggled -- ask a driver why.
        //       Log it too: Tracker.publish("drive/robotRelative", gamepad1.right_bumper);
```

What the solutions line has there:

```java
        if (gamepad1.right_bumper) {
            drivetrain.sticks(forwardSpeed, strafeLeftSpeed, turnCcwSpeed);
        } else {
            drivetrain.fieldRelative(follower.pose().heading(),
                    forwardSpeed, strafeLeftSpeed, turnCcwSpeed);
        }
        Tracker.publish("drive/robotRelative", gamepad1.right_bumper);
```

The two lines also differ in 1 run(s) of comment lines, which are not
blanks and are not shown.
