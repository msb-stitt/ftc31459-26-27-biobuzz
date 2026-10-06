# L190VelocityDriveOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l010`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L190VelocityDriveOpMode.java`

## L190

### A change

Before L190:

```java
(nothing)
```

After L190:

```java
import org.firstinspires.ftc.teamcode.base.WheelTargets;
import org.firstinspires.ftc.teamcode.pedro.Constants;
```

### A change

Before L190:

```java
(nothing)
```

After L190:

```java
    L190SpeedDriveTrain drivetrain;
```

### A change

Before L190:

```java
(nothing)
```

After L190:

```java
        drivetrain = new L190SpeedDriveTrain(hardware);
```

### A change

Before L190:

```java
        initAfter();
```

After L190:

```java
        initAfter(drivetrain);
```

### A change

Before L190:

```java
        double forwardInPerS = 0;
        double strafeInPerS = 0;
        double turnRadPerS = 0;
```

After L190:

```java
        double forwardInPerS = deadband(-leftStickY, band) * MAX_IPS;
        double strafeInPerS = deadband(-leftStickX, band) * MAX_IPS;
        double turnRadPerS = deadband(-rightStickX, band) * MAX_TURN_RADPS;
```

### A change

Before L190:

```java
(nothing)
```

After L190:

```java
        double[] target = WheelTargets.forMecanum(forwardInPerS, strafeInPerS, turnRadPerS, Constants.turnRadiusInches);
        drivetrain.setCommandedWheelSpeeds(target[0], target[1], target[2], target[3]);
```
