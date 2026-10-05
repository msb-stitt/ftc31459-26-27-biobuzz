# L6FollowerDriveTrain

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l010`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L6FollowerDriveTrain.java`

## L6

### TODO 1

Before L6:

```java
        // TODO 1: wrap the three numbers in a new DrivePowers(...) in its own
        //         variable, hand that to mix() into a second variable, hand that
        //         to normalized() into a third, and pass the four slots to
        //         setCommandedWheels(). One call to a line; nothing nested.
        //         Going through mix() is the point: the sticks and the follower
        //         then agree about which wheel does what.
```

After L6:

```java
        DrivePowers drivePowers = new DrivePowers(forwardSpeed, strafeLeftSpeed, turnCcwSpeed);
        double[] mixed = mix(drivePowers);
        double[] scaled = normalized(mixed);
        setCommandedWheels(scaled[FL], scaled[FR], scaled[BL], scaled[BR]);
```

### TODO 2

Before L6:

```java
        // TODO 2: fill each slot in, one line each, naming it with FL, FR, BL or
        //         BR. The four sums are the same ones L5HolonomicDriveTrain uses.
```

After L6:

```java
        wheels[FL] = forwardSpeed - strafeLeftSpeed - turnCcwSpeed;
        wheels[FR] = forwardSpeed + strafeLeftSpeed + turnCcwSpeed;
        wheels[BL] = forwardSpeed + strafeLeftSpeed - turnCcwSpeed;
        wheels[BR] = forwardSpeed - strafeLeftSpeed + turnCcwSpeed;
```
