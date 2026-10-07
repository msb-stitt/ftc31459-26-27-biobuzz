# L150RobotRelativeButtonOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l010`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L150RobotRelativeButtonOpMode.java`

## L150

### A change

Before L150:

```java
        drivetrain.sticks(robotForward, robotStrafe, turn);
```

After L150:

```java
        boolean robotRelative = gamepad1.right_bumper;
        Tracker.publish("drive/robotRelative", robotRelative);
        if (robotRelative) {
            drivetrain.sticks(forward, strafe, turn);
        } else {
            drivetrain.sticks(robotForward, robotStrafe, turn);
        }
```
