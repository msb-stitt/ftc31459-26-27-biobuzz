# L060ShapingOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-rules.1`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L060ShapingOpMode.java`

## L060

### A change

Before L060:

```java
(nothing)
```

After L060:

```java
        double band = 0.05;
        double leftPower = deadband(leftSpeed, band);
        leftPower = signedSquared(leftPower);
        Tracker.publish("power/left", leftPower);
        double rightPower = deadband(rightSpeed, band);
        rightPower = signedSquared(rightPower);
        Tracker.publish("power/right", rightPower);
```

### A change

Before L060:

```java
        hardware.frontLeft.setPower(leftSpeed);
        hardware.backLeft.setPower(leftSpeed);
```

After L060:

```java
        hardware.frontLeft.setPower(leftPower);
        hardware.backLeft.setPower(leftPower);
```

### A change

Before L060:

```java
        hardware.frontRight.setPower(rightSpeed);
        hardware.backRight.setPower(rightSpeed);
```

After L060:

```java
        hardware.frontRight.setPower(rightPower);
        hardware.backRight.setPower(rightPower);
```

### A change

Before L060:

```java
(nothing)
```

After L060:

```java
    double deadband(double value, double band) {
        double alwaysPositiveMagnitude = Math.abs(value);
        boolean isCloseToZero = alwaysPositiveMagnitude < band;
        if (isCloseToZero) {
            return 0.0;
        }
        return value;
    }
```

### A change

Before L060:

```java
(nothing)
```

After L060:

```java
    double signedSquared(double value) {
        return value * Math.abs(value);
    }
```
