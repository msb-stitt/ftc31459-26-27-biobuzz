# L9Drive24OpMode

What each lesson's patch does to this file, in the order the lessons come, from
applying `solutions/` on `solutions-try`:

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/mytry/L9Drive24OpMode.java`

## L9

### TODO 

Before L9:

```java
        // TODO: return a sequence with one step: follow a straight line from
        //       start to end, holding the heading it starts at.
        //           return sequential(follow(follower, line(start, end).constant(start)));
        //       .constant(pose) takes the heading from a pose. The number form,
        //       .constant(0), is radians, so .constant(90) is not 90 degrees.
        //       Use .constant(), not .linear() -- Pedro 3.0.1 issues #176 and #181.
        return Command.NOOP;
```

After L9:

```java
        return sequential(
                follow(follower, line(start, end).constant(start))
        );
```
