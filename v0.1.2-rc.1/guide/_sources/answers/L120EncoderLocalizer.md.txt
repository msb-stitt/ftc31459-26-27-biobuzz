# L120EncoderLocalizer

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l010.1`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L120EncoderLocalizer.java`

## L120

### A change

Before L120:

```java
(nothing)
```

After L120:

```java
        if (lastWheels == null) {
            lastWheels = wheels.clone();
            lastRawHeading = rawHeading;
            return;
        }

        double frontLeft = wheels[0] - lastWheels[0];
        double frontRight = wheels[1] - lastWheels[1];
        double backLeft = wheels[2] - lastWheels[2];
        double backRight = wheels[3] - lastWheels[3];

        double forward = (frontLeft + frontRight + backLeft + backRight) / 4;
        double strafe = (-frontLeft + frontRight + backLeft - backRight) / 4;
```

### A change

Before L120:

```java
(nothing)
```

After L120:

```java
        double heading = rawHeading + headingOffset;
        x += forward * Math.cos(heading) - strafe * Math.sin(heading);
        y += forward * Math.sin(heading) + strafe * Math.cos(heading);
```
