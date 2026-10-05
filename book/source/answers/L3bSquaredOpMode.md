# L3bSquaredOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l010`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L3bSquaredOpMode.java`

## L3b

### A change

Before L3b:

```java
@TeleOp(name = "L3a Deadband", group = "Lessons")
```

After L3b:

```java
@TeleOp(name = "L3b Squared", group = "Lessons")
```

### A change

Before L3b:

```java
        double leftSpeed = drivetrain.deadband(leftRawSpeed, DEADBAND);
        double rightSpeed = drivetrain.deadband(rightRawSpeed, DEADBAND);
```

After L3b:

```java
        double leftDeadbanded = drivetrain.deadband(leftRawSpeed, DEADBAND);
        double rightDeadbanded = drivetrain.deadband(rightRawSpeed, DEADBAND);

        double leftSpeed = drivetrain.squared(leftDeadbanded);
        double rightSpeed = drivetrain.squared(rightDeadbanded);
```

It also changes 4 run(s) of comment lines, which are not shown.
