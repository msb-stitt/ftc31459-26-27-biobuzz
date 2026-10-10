# L160HeadingHoldOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-rules.1`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L160HeadingHoldOpMode.java`

## L160

### a change

Before L160:

```java
(nothing)
```

After L160:

```java
import org.firstinspires.ftc.teamcode.base.HeadingHold;
```

### a change

Before L160:

```java
(nothing)
```

After L160:

```java
import org.firstinspires.ftc.teamcode.pedro.Constants;
```

### a change

Before L160:

```java
(nothing)
```

After L160:

```java
    HeadingHold hold = new HeadingHold(Constants.foresightConfig.headingFeedback.get());
```

### a change

Before L160:

```java
(nothing)
```

After L160:

```java
        turn = hold.turn(follower, turn);
        Tracker.publish("heading/holding", hold.target() != null);
```
