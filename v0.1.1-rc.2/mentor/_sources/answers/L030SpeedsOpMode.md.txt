# L030SpeedsOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l010`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L030SpeedsOpMode.java`

## L030

### A change

Before L030:

```java
(nothing)
```

After L030:

```java
        double leftSpeed = -leftStickY;
        Tracker.publish("speed/left", leftSpeed);
```

### A change

Before L030:

```java
(nothing)
```

After L030:

```java
        double rightSpeed = -rightStickY;
        Tracker.publish("speed/right", rightSpeed);
```
