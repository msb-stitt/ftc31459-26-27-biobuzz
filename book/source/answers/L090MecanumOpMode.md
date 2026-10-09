# L090MecanumOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-rules`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L090MecanumOpMode.java`

## L090

### A change

Before L090:

```java
(nothing)
```

After L090:

```java
        double strafe = deadband(-leftStickX, band);
        strafe = signedSquared(strafe);
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
        double frontLeftSize = Math.abs(frontLeftPower);
        double frontRightSize = Math.abs(frontRightPower);
        double backLeftSize = Math.abs(backLeftPower);
        double backRightSize = Math.abs(backRightPower);
        double biggest = Math.max(frontLeftSize, frontRightSize);
        biggest = Math.max(biggest, backLeftSize);
        biggest = Math.max(biggest, backRightSize);
        biggest = Math.max(1.0, biggest);
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
