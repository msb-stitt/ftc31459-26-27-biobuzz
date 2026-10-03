# L13HeadingHoldOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-try`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L13HeadingHoldOpMode.java`

## L13

### TODO 1

Before L13:

```java
        // TODO 1: build a HeadingHold from the tuned controller:
        //         new HeadingHold(Constants.foresightConfig.headingFeedback.get())
```

After L13:

```java
        heading = new HeadingHold(Constants.foresightConfig.headingFeedback.get());
```

### TODO 2 (L13)

Before L13:

```java
        // TODO 2 (L13): ask heading.turn(follower, -gamepad1.right_stick_x) for the
        //         turn power, then drive field relative with it through
        //         drivetrain.fieldRelative(...). Log heading/holding and
        //         heading/deg so Panels can show the hold.
```

After L13:

```java
        double turnCcwSpeed = heading.turn(follower, -gamepad1.right_stick_x);
        drivetrain.fieldRelative(follower.pose().heading(),
                -gamepad1.left_stick_y, -gamepad1.left_stick_x, turnCcwSpeed);
        Tracker.publish("heading/holding", heading.target() != null);
        Tracker.publish("heading/deg", Math.toDegrees(follower.pose().heading()));
```

It also changes 1 run(s) of comment lines, which are not shown.
