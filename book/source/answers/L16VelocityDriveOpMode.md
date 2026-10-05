# L16VelocityDriveOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l010`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L16VelocityDriveOpMode.java`

## L16

### TODO 1 (L16)

Before L16:

```java
        // TODO 1 (L16): read the sticks as a SPEED, not a power. Full forward asks
        //         for MAX_IPS inches per second; full turn, MAX_TURN_RADPS radians
        //         per second. Use drivetrain.deadband(value, 0.05) as usual, and
        //         remember the minus signs from L5.
        double forwardSpeedInPerS = 0;
        double strafeLeftSpeedInPerS = 0;
        double turnCcwSpeedRadPerS = 0;
```

After L16:

```java
        // 1. The sticks ask for a speed, not a power.
        double forwardSpeedInPerS = drivetrain.deadband(-gamepad1.left_stick_y, 0.05) * MAX_IPS;
        double strafeLeftSpeedInPerS = drivetrain.deadband(-gamepad1.left_stick_x, 0.05) * MAX_IPS;
        double turnCcwSpeedRadPerS =
                drivetrain.deadband(-gamepad1.right_stick_x, 0.05) * MAX_TURN_RADPS;
```

### TODO 2 (L16)

Before L16:

```java
        // TODO 2 (L16): work out how fast each wheel has to travel for the robot to
        //         move like that. WheelTargets.forMecanum(forward, strafeLeft,
        //         turnCcw, radius) does the arithmetic, and the radius is
        //         Constants.turnRadiusInches.
        double[] target = new double[4];
```

After L16:

```java
        // 2. What each wheel must do for the robot to move like that.
        double[] target = WheelTargets.forMecanum(forwardSpeedInPerS, strafeLeftSpeedInPerS,
                turnCcwSpeedRadPerS, Constants.turnRadiusInches);
```

### TODO 3 (L16)

Before L16:

```java
        // TODO 3 (L16): ask the drivetrain for those four speeds, with
        //         drivetrain.setCommandedWheelSpeeds(...). It measures, guesses and
        //         corrects, and publishes wheel/... for every wheel.
```

After L16:

```java
        // 3. Ask the drivetrain for those speeds. It measures, guesses and corrects,
        // and publishes wheel/... for every wheel.
        drivetrain.setCommandedWheelSpeeds(target[0], target[1], target[2], target[3]);
```

It also changes 1 run(s) of comment lines, which are not shown.
