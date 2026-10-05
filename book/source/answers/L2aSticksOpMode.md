# L2aSticksOpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l010`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L2aSticksOpMode.java`

## L2a

### TODO 1

Before L2a:

```java
        // TODO 1: read all four stick axes into four named doubles. Negate each
        //         y axis, so that pushing away from the driver is positive.
```

After L2a:

```java
        double leftSpeed = -gamepad1.left_stick_y;
        double rightSpeed = -gamepad1.right_stick_y;
        double leftSideways = gamepad1.left_stick_x;
        double rightSideways = gamepad1.right_stick_x;
```

### TODO 2

Before L2a:

```java
        // TODO 2: log all four, so Panels can draw them:
        //         Tracker.publish("stick/leftY", leftSpeed);
        //         then stick/leftX, stick/rightY and stick/rightX.
```

After L2a:

```java
        Tracker.publish("stick/leftY", leftSpeed);
        Tracker.publish("stick/leftX", leftSideways);
        Tracker.publish("stick/rightY", rightSpeed);
        Tracker.publish("stick/rightX", rightSideways);
```

### TODO 3

Before L2a:

```java
        // TODO 3: read gamepad1.a into a boolean, and log it as
        //         "driver pressed A". A boolean logs the same way a double does.
```

After L2a:

```java
        boolean buttonA = gamepad1.a;
        Tracker.publish("driver pressed A", buttonA);
```

### TODO 4

Before L2a:

```java
        // TODO 4: add one to loopCount, read getRuntime() into a double, and log
        //         them as "lesson/loop_count" and "lesson/seconds_running". An int
        //         and a double, so three of Java's kinds of number are now logged.
```

After L2a:

```java
        loopCount = loopCount + 1;
        double secondsRunning = getRuntime();
        Tracker.publish("lesson/loop_count", loopCount);
        Tracker.publish("lesson/seconds_running", secondsRunning);
```

### TODO 5

Before L2a:

```java
        // TODO 5: say when the button changes. If the boolean is not the same as
        //         previousButtonA, log "button A pressed" when it is now true and
        //         "button A released" when it is now false, as
        //         "lesson/event". A String logs the same way. Then remember this
        //         loop's value in previousButtonA for the next one.
        //         Works when: LessonsTest.l2a_logsEveryStickAndTheAButton and
        //         LessonsTest.l2a_saysWhenTheButtonIsPressedAndReleased pass.
```

After L2a:

```java
        if (buttonA != previousButtonA) {
            if (buttonA) {
                Tracker.publish("lesson/event", "button A pressed");
            } else {
                Tracker.publish("lesson/event", "button A released");
            }
        }
        previousButtonA = buttonA;
```
