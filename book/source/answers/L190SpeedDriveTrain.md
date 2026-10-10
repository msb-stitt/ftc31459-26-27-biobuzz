# L190SpeedDriveTrain

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-rules.1`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L190SpeedDriveTrain.java`

## L190

### A change

Before L190:

```java
(nothing)
```

After L190:

```java
import org.firstinspires.ftc.teamcode.pedro.Constants;
```

### A change

Before L190:

```java
(nothing)
```

After L190:

```java
        double[] measured = measuredSpeeds.all();
        double[] powers = new double[4];
        for (int i = 0; i < 4; i++) {
            double feedforward = Constants.powerPerInchPerSecond * wanted[i];
            double feedback = VELOCITY_KP * (wanted[i] - measured[i]);
            powers[i] = clampToPower(feedforward + feedback);
        }
        setCommandedWheels(powers[FL], powers[FR], powers[BL], powers[BR]);
        publishWheelSpeeds(wanted, measured, powers);
```
