# L5HolonomicOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l010`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L5HolonomicOpMode.java`

## L5

### A change

Before L5:

```java
@TeleOp(name = "L4 Arcade", group = "Lessons")
```

After L5:

```java
@TeleOp(name = "L5 Holonomic", group = "Lessons")
```

### A change

Before L5:

```java
    private L4ArcadeDriveTrain drivetrain;
```

After L5:

```java
    private L5HolonomicDriveTrain drivetrain;
```

### A change

Before L5:

```java
        drivetrain = new L4ArcadeDriveTrain(hardware);
```

After L5:

```java
        drivetrain = new L5HolonomicDriveTrain(hardware);
```

### A change

Before L5:

```java
(nothing)
```

After L5:

```java
        double strafeLeftSpeed = -gamepad1.left_stick_x;
```

### A change

Before L5:

```java
        drivetrain.sticks(forwardSpeed, turnCcwSpeed);
```

After L5:

```java
        drivetrain.sticks(forwardSpeed, strafeLeftSpeed, turnCcwSpeed);
```

### A change

Before L5:

```java
(nothing)
```

After L5:

```java
        Tracker.publish("command/left", strafeLeftSpeed);
```

It also changes 3 run(s) of comment lines, which are not shown.
