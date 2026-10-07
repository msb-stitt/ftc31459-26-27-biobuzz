# L160HeadingHoldOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l010`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L160HeadingHoldOpMode.java`

## L160

### A change

Before L160:

```java
(nothing)
```

After L160:

```java
import org.firstinspires.ftc.teamcode.base.HeadingHold;
```

### A change

Before L160:

```java
(nothing)
```

After L160:

```java
import org.firstinspires.ftc.teamcode.pedro.Constants;
```

### A change

Before L160:

```java
(nothing)
```

After L160:

```java
    HeadingHold hold = new HeadingHold(Constants.foresightConfig.headingFeedback.get());
```

### A change

Before L160:

```java
(nothing)
```

After L160:

```java
        turn = hold.turn(follower, turn);
        Tracker.publish("heading/holding", hold.target() != null);
```
