# L110FollowerOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-rules.1`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L110FollowerOpMode.java`

## L110

### a change

Before L110:

```java
(nothing)
```

After L110:

```java
    L110FollowerDriveTrain drivetrain;
```

### a change

Before L110:

```java
(nothing)
```

After L110:

```java
        drivetrain = new L110FollowerDriveTrain(hardware);
```

### a change

Before L110:

```java
        initAfter();
```

After L110:

```java
        initAfter(drivetrain);
```

### a change

Before L110:

```java
(nothing)
```

After L110:

```java
        drivetrain.sticks(forward, strafe, turn);
```
