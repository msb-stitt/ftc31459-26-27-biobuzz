# L020SticksOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-rules.1`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L020SticksOpMode.java`

## L020

### A change

Before L020:

```java
(nothing)
```

After L020:

```java
import org.firstinspires.ftc.teamcode.base.Tracker;
```

### A change

Before L020:

```java
(nothing)
```

After L020:

```java
        double leftStickY = gamepad1.left_stick_y;
```

### A change

Before L020:

```java
(nothing)
```

After L020:

```java
        Tracker.publish("stick/leftY", leftStickY);
```

### A change

Before L020:

```java
(nothing)
```

After L020:

```java
        double rightStickY = gamepad1.right_stick_y;
        Tracker.publish("stick/rightY", rightStickY);
```
