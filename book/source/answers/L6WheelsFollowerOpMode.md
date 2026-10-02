# L6WheelsFollowerOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-try`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L6WheelsFollowerOpMode.java`

## L6

### A change

Before L6:

```java
@TeleOp(name = "L5 Holonomic", group = "Lessons")
```

After L6:

```java
@TeleOp(name = "L6 Follower Wheels", group = "Lessons")
```

### A change

Before L6:

```java
    private L5HolonomicDriveTrain drivetrain;
```

After L6:

```java
    private L6FollowerDriveTrain drivetrain;
```

### A change

Before L6:

```java
        drivetrain = new L5HolonomicDriveTrain(hardware);
        initAfter();
```

After L6:

```java
        drivetrain = new L6FollowerDriveTrain(hardware);
        initAfter(drivetrain);
```

It also changes 3 run(s) of comment lines, which are not shown.
