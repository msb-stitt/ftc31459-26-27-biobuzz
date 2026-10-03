# L2bTankOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-try`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L2bTankOpMode.java`

## L2b

### A change

Before L2b:

```java
@TeleOp(name = "L2a Sticks", group = "Lessons")
```

After L2b:

```java
@TeleOp(name = "L2b Tank", group = "Lessons")
```

### A change

Before L2b:

```java
    /** How many times {@link #loop} has run. */
    private int loopCount;
```

After L2b:

```java
    private L2TankDriveTrain drivetrain;
```

### A change

Before L2b:

```java
(nothing)
```

After L2b:

```java
        drivetrain = new L2TankDriveTrain(hardware);
```

### A change

Before L2b:

```java
        loopCount = 0;
```

After L2b:

```java
(nothing)
```

### A change

Before L2b:

```java
        double leftSideways = gamepad1.left_stick_x;
        double rightSideways = gamepad1.right_stick_x;
```

After L2b:

```java
        drivetrain.sticks(leftSpeed, rightSpeed);
```

### A change

Before L2b:

```java
        Tracker.publish("stick/leftX", leftSideways);
```

After L2b:

```java
        Tracker.publish("stick/leftX", gamepad1.left_stick_x);
```

### A change

Before L2b:

```java
        Tracker.publish("stick/rightX", rightSideways);
```

After L2b:

```java
        Tracker.publish("stick/rightX", gamepad1.right_stick_x);
```

### A change

Before L2b:

```java

        loopCount = loopCount + 1;
        double secondsRunning = getRuntime();
        Tracker.publish("lesson/loop_count", loopCount);
        Tracker.publish("lesson/seconds_running", secondsRunning);
```

After L2b:

```java
(nothing)
```

### A change

Before L2b:

```java
(nothing)
```

After L2b:

```java
        drivetrain.stop();
```

It also changes 4 run(s) of comment lines, which are not shown.
