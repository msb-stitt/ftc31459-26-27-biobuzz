# L3aDeadbandOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-try`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L3aDeadbandOpMode.java`

## L3a

### TODO 1 (L3a)

Before L3a:

```java
    // TODO 1 (L3a): pick the number. A stick let go reads a few hundredths, so
    //         0.05 is a good first guess; 0 leaves the creep exactly where it was.
    private static final double DEADBAND = 0;
```

After L3a:

```java
    private static final double DEADBAND = 0.05;
```

### TODO 2 (L3a)

Before L3a:

```java
        // TODO 2 (L3a): put each raw stick through drivetrain.deadband(), with
        //         DEADBAND as the band both times, and hand the two to
        //         drivetrain.sticks().
        double leftSpeed = 0;
        double rightSpeed = 0;
```

After L3a:

```java
        double leftSpeed = drivetrain.deadband(leftRawSpeed, DEADBAND);
        double rightSpeed = drivetrain.deadband(rightRawSpeed, DEADBAND);
        drivetrain.sticks(leftSpeed, rightSpeed);
```
