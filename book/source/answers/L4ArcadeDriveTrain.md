# L4ArcadeDriveTrain

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-l010`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L4ArcadeDriveTrain.java`

## L4

### A change

Before L4:

```java
    public void sticks(double leftSpeed, double rightSpeed) {
```

After L4:

```java
    public void sticks(double forwardSpeed, double turnCcwSpeed) {
        double leftSpeed = forwardSpeed - turnCcwSpeed;
        double rightSpeed = forwardSpeed + turnCcwSpeed;
```

It also changes 5 run(s) of comment lines, which are not shown.
