# L130ForwardThenStrafeOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-rules`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L130ForwardThenStrafeOpMode.java`

## L130

### A change

Before L130:

```java
(nothing)
```

After L130:

```java
import static com.pedropathing.ivy.pedro.PedroCommands.hold;
```

### A change

Before L130:

```java
                follow(follower, line(start, corner).constant(start))
```

After L130:

```java
                follow(follower, line(start, corner).constant(start)),
```

### A change

Before L130:

```java
(nothing)
```

After L130:

```java
                follow(follower, line(corner, end).constant(start)),
                hold(follower, end)
```
