# L195TicksDriveTrain

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-rules`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L195TicksDriveTrain.java`

## L195

### A change

Before L195:

```java
(nothing)
```

After L195:

```java
        ticks[FL] = hardware.frontLeft.getCurrentPosition();
        ticks[FR] = hardware.frontRight.getCurrentPosition();
        ticks[BL] = hardware.backLeft.getCurrentPosition();
        ticks[BR] = hardware.backRight.getCurrentPosition();
```
