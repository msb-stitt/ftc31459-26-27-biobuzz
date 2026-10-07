# L110FollowerDriveTrain

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l010`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L110FollowerDriveTrain.java`

## L110

### A change

Before L110:

```java
(nothing)
```

After L110:

```java
        wheels[FL] = forward - strafe - turn;
        wheels[FR] = forward + strafe + turn;
        wheels[BL] = forward + strafe - turn;
        wheels[BR] = forward - strafe + turn;
```
