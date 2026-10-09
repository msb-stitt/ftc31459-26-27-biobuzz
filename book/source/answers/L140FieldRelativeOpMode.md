# L140FieldRelativeOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-rules`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L140FieldRelativeOpMode.java`

## L140

### A change

Before L140:

```java
        drivetrain.sticks(forward, strafe, turn);
```

After L140:

```java
        double heading = follower.pose().heading();
        double robotForward = forward * Math.cos(heading) + strafe * Math.sin(heading);
        double robotStrafe = -forward * Math.sin(heading) + strafe * Math.cos(heading);
        drivetrain.sticks(robotForward, robotStrafe, turn);
```
