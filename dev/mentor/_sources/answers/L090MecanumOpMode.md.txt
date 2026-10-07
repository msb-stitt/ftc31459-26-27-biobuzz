# L090MecanumOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l010.1`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L090MecanumOpMode.java`

## L090

### A change

Before L090:

```java
(nothing)
```

After L090:

```java
        double strafe = squared(deadband(-leftStickX, band));
        Tracker.publish("arcade/strafe", strafe);
```

### A change

Before L090:

```java
(nothing)
```

After L090:

```java
        double frontLeftPower = forward - strafe - turn;
        double frontRightPower = forward + strafe + turn;
        double backLeftPower = forward + strafe - turn;
        double backRightPower = forward - strafe + turn;
        double biggest = Math.max(1, Math.max(Math.max(Math.abs(frontLeftPower), Math.abs(frontRightPower)),
                Math.max(Math.abs(backLeftPower), Math.abs(backRightPower))));
        frontLeftPower = frontLeftPower / biggest;
        frontRightPower = frontRightPower / biggest;
        backLeftPower = backLeftPower / biggest;
        backRightPower = backRightPower / biggest;
        Tracker.publish("power/frontLeft", frontLeftPower);
        Tracker.publish("power/frontRight", frontRightPower);
        Tracker.publish("power/backLeft", backLeftPower);
        Tracker.publish("power/backRight", backRightPower);

        hardware.frontLeft.setPower(frontLeftPower);
        hardware.frontRight.setPower(frontRightPower);
        hardware.backLeft.setPower(backLeftPower);
        hardware.backRight.setPower(backRightPower);
```
