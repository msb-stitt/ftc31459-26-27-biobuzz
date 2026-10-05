# L12RobotRelativeButtonOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l010`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L12RobotRelativeButtonOpMode.java`

## L12

### TODO (L12)

Before L12:

```java
        // TODO (L12): if gamepad1.right_bumper is held, drive robot relative
        //       (drivetrain.sticks); otherwise field relative
        //       (drivetrain.fieldRelative, with follower.pose().heading() first).
        //       Held, not toggled -- ask a driver why.
        //       Log it too: Tracker.publish("drive/robotRelative", gamepad1.right_bumper);
```

After L12:

```java
        if (gamepad1.right_bumper) {
            drivetrain.sticks(forwardSpeed, strafeLeftSpeed, turnCcwSpeed);
        } else {
            drivetrain.fieldRelative(follower.pose().heading(),
                    forwardSpeed, strafeLeftSpeed, turnCcwSpeed);
        }
        Tracker.publish("drive/robotRelative", gamepad1.right_bumper);
```

It also changes 1 run(s) of comment lines, which are not shown.
