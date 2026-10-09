# L080ArcadeOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-rules`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L080ArcadeOpMode.java`

## L080

### A change

Before L080:

```java
(nothing)
```

After L080:

```java
        double band = 0.05;
        double forward = deadband(-leftStickY, band);
        forward = signedSquared(forward);
        Tracker.publish("arcade/forward", forward);
        double turn = deadband(-rightStickX, band);
        turn = signedSquared(turn);
        Tracker.publish("arcade/turn", turn);
```

### A change

Before L080:

```java
(nothing)
```

After L080:

```java
        double leftPower = forward - turn;
        double rightPower = forward + turn;
        double leftSize = Math.abs(leftPower);
        double rightSize = Math.abs(rightPower);
        double biggest = Math.max(leftSize, rightSize);
        biggest = Math.max(1.0, biggest);
        leftPower = leftPower / biggest;
        rightPower = rightPower / biggest;
        Tracker.publish("power/left", leftPower);
        Tracker.publish("power/right", rightPower);

        hardware.frontLeft.setPower(leftPower);
        hardware.backLeft.setPower(leftPower);
        hardware.frontRight.setPower(rightPower);
        hardware.backRight.setPower(rightPower);
```
