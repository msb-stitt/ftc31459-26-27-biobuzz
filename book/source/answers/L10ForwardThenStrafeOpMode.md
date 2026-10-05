# L10ForwardThenStrafeOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l010`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L10ForwardThenStrafeOpMode.java`

## L10

### A change

Before L10:

```java
(nothing)
```

After L10:

```java
import static com.pedropathing.ivy.pedro.PedroCommands.hold;
```

### A change

Before L10:

```java
@Autonomous(name = "L9 Drive 24", group = "Lessons")
```

After L10:

```java
@Autonomous(name = "L10 Forward Then Strafe", group = "Lessons")
```

### A change

Before L10:

```java
    private final Pose end = POSES.of(72, botStartYIn+24, 90);

```

After L10:

```java
    private final Pose corner = POSES.of(72, 72, 90);
    private final Pose end = POSES.of(96, 72, 90);
```

### A change

Before L10:

```java
                follow(follower, line(start, end).constant(start))
```

After L10:

```java
                follow(follower, line(start, corner).constant(start)),
                follow(follower, line(corner, end).constant(start)),
                hold(follower, end)
```

It also changes 2 run(s) of comment lines, which are not shown.
