# L070ButtonsOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-rules`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L070ButtonsOpMode.java`

## L070

### A change

Before L070:

```java
(nothing)
```

After L070:

```java
    boolean previousButtonA;
```

### A change

Before L070:

```java
(nothing)
```

After L070:

```java
        double leftStickX = gamepad1.left_stick_x;
        Tracker.publish("stick/leftX", leftStickX);
        double rightStickX = gamepad1.right_stick_x;
        Tracker.publish("stick/rightX", rightStickX);
```

### A change

Before L070:

```java
(nothing)
```

After L070:

```java
        boolean buttonA = gamepad1.a;
        Tracker.publish("button/a", buttonA);
```

### A change

Before L070:

```java
(nothing)
```

After L070:

```java
        if (buttonA != previousButtonA) {
            if (buttonA) {
                Tracker.publish("button/event", "A pressed");
            } else {
                Tracker.publish("button/event", "A released");
            }
        }
        previousButtonA = buttonA;
```
