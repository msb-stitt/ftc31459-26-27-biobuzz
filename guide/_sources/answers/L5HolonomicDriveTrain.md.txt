# L5HolonomicDriveTrain

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-try`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L5HolonomicDriveTrain.java`

## L5

### A change

Before L5:

```java
    public void sticks(double forwardSpeed, double turnCcwSpeed) {
        double leftSpeed = forwardSpeed - turnCcwSpeed;
        double rightSpeed = forwardSpeed + turnCcwSpeed;
        driveWheelsNow(leftSpeed, rightSpeed, leftSpeed, rightSpeed);
```

After L5:

```java
    public void sticks(double forwardSpeed, double strafeLeftSpeed, double turnCcwSpeed) {
        double frontLeftPower = forwardSpeed - strafeLeftSpeed - turnCcwSpeed;
        double frontRightPower = forwardSpeed + strafeLeftSpeed + turnCcwSpeed;
        double backLeftPower = forwardSpeed + strafeLeftSpeed - turnCcwSpeed;
        double backRightPower = forwardSpeed - strafeLeftSpeed + turnCcwSpeed;
        driveWheelsNow(frontLeftPower, frontRightPower, backLeftPower, backRightPower);
```

It also changes 6 run(s) of comment lines, which are not shown.
