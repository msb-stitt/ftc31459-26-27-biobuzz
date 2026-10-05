# The simulator

How to run a lesson on a laptop and watch the robot move in AdvantageScope.

## What it is

The lessons are real OpModes, and the simulator runs them with fake hardware. `OpModeHarness` gives
an OpMode four fake motors, a fake IMU and a fake battery, and `SimRobot` turns whatever power
reaches those motors into motion. Nothing here runs on the robot: it all lives in `TeamCode`'s test
sources, and WPILib's NetworkTables is a `testImplementation` dependency, so none of it can reach a
Control Hub. The NetworkTables server is the robot's own, `Nt4Server` from corbelsflightlog, with
the run's flight log mirrored onto it as on the robot; [NETWORKTABLES.md](./NETWORKTABLES.md) says
how the robot serves it.

Two ways to run it:

| Way | What it is for |
|---|---|
| `./gradlew :TeamCode:test` | Everything except the lessons package. They drive the sticks in code and assert where the robot ended up |
| `./gradlew :TeamCode:testLessons` | The lessons package alone. On the lessons line most of it fails, because the blanks are open |
| `./gradlew :TeamCode:simRun` | Watching. One OpMode, running until Ctrl-C, published to AdvantageScope |

## Watching a lesson

Start the simulator:

```
./gradlew :TeamCode:simRun --args="lessons.L040TankOpMode --left_stick_y=-1 --right_stick_y=-1"
```

The first argument is the OpMode's class name after `org.firstinspires.ftc.teamcode`, so a lesson is
`lessons.L040TankOpMode`, a student's copy of one is `mytry.L020SticksOpMode`, and a simulator teleop
is `base.SimOpModes$Tank`. Leaving it off is an
error, not a default. Every option after it is `--name=value` naming one of the 21 controls a
gamepad has, so `--left_stick_y=-1` is the left stick pushed fully forward and `--a=true` is the A
button held. A control is held from before the run starts, so `--a=true` fires `aWasPressed()` on
the first loop and not again.

`--args="--help"` prints all of it, including which field names are refused and why.

To push the sticks yourself instead, plug a gamepad in and use `--pad`. `--pad-check` reports what a
gamepad is doing without running an OpMode, so it can run beside one.

It prints where the flight log goes and then runs, serving NetworkTables 4 on port 5810:

```
Flight log: <the repository's top folder>
lessons.L040TankOpMode running. Connect AdvantageScope to 127.0.0.1 as NetworkTables 4, and Ctrl-C to stop.
```

Point AdvantageScope at `127.0.0.1` and it will find the topics under `sim/`. In AdvantageScope
26.0.2 that is **File > Connect to Simulator > NetworkTables 4**. Choose it before starting `simRun`
and the window waits, titled `127.0.0.1 (Searching)`, until the server comes up. Add a 2D field and
give it `sim/Pose`, and the robot drives up the field. The **Field** list keeps the FTC fields in
their own group below the FRC ones; **2026-2027 Field** is this season's, and choosing it shows a
notice that FTC fields are experimental. Watched on 2026-09-28, and the menu path and the field on
2026-10-01.

An autonomous can be over before a field is set up by hand: `lessons.L125Drive24OpMode` drives its
24 in and holds in about 1 s. Its flight log replays the run. Stop `simRun` with Ctrl-C, open the
`.wpilog` it wrote into the repository's top folder in AdvantageScope, and drag `Robot/Pose` onto the field. `Robot/Path` dragged
beside it and switched to **Trajectory** from its icon draws the line the robot was told to follow.
Watched on 2026-10-01 against `solutions-03`, as `L9Drive24OpMode`.

Ctrl-C stops it. The flight log goes in the repository's top folder, where `.gitignore` keeps it
out of git, and `simRun` names that folder before anything else, as `Flight log: <folder>`.

## What gets published

What the simulated robot is doing is under `sim/`.

| Topic | What it is |
|---|---|
| `sim/Pose` | A `struct:Pose2d`: x, y in metres from the centre of the field, and heading in radians |
| `sim/Mode` | What Pedro's follower says it is doing, as its own name for it |
| `sim/wheels/frontLeft` and the other three | What reached that motor, -1 to 1 |
| `sim/stick/leftY`, `leftX`, `rightY`, `rightX` | What the driver is holding |
| `sim/vel/forward_ips`, `strafe_ips`, `omega_radps` | How fast the robot is going, in its own frame |

Every value the OpMode publishes through `Tracker` goes out too, under the name the flight log gives
it, so `Tracker.publish("stick/leftY", leftSpeed)` is `/stick/leftY` live and in the log.
`sim/stick/leftY` is the stick as the gamepad gives it, and `/stick/leftY` is what the lesson made
of it. Watched on 2026-10-02 with L2a's solution and the left stick forward: -1 and 1.

The pose is a struct because AdvantageScope wants one. A bare `double[]` of x, y and heading is what
it calls the legacy numeric array format: it draws that too, warns about it in 2026 and removes it
in 2027. The flight log has always written `struct:Pose2d`, so the topic now carries the same three
little-endian doubles, and the schema that says what they are goes out beside it.

The flight log's other struct topics are still missing: no `Speeds`, no `Twist`, no `Path`, no
`AimPose`. The three `sim/vel` numbers say what `Twist` would have said. That is
`sim.struct.topics` in `open-work.md`.

## Which teleop a test drives

The simulator's own tests drive `SimOpModes.Tank` and `SimOpModes.Driven`, which are built only out
of `base`. `SimOpModes.Edges` and `SimOpModes.Slow` are there for running by hand: one prints a
button and its press edge, the other burns 25 ms in every loop to show a slow OpMode still moves the
robot at field speed. That is deliberate: on the lessons branch the lessons are blanks, and a test
of the simulator that drove one would fail there, where a failure outside the `lessons` package is a
defect rather than the point.

The one test that asks a real lesson to move the robot is
`LessonsTest.l040_bothSticksForwardDriveTheSimulatedRobotForward`, and it lives in the `lessons`
package, where it failing until L040 is typed is expected.

## Where a failing test leaves its log

A test that fails names its flight log in the failure message:

```
drove forward, and got a fair way: 54.75097300967889
AdvantageScope can open what ran:
  /var/folders/.../corbelsflightlog-test2706371408438583527/L040TankOpMode-20260927-032443.wpilog
```

Open that file in AdvantageScope and the run is there up to the moment the assertion went wrong.

A test class gets that by adding one line:

```java
@Rule
public final SimLogs logs = new SimLogs();
```

`SimMotionTest` and `SimPublisherTest` have it. Without the rule a failing test still writes a log,
but it never reaches `stop()`, so the file is closed by nobody and holds 0 bytes.

## What the model does, and what it does not

The whole of the physics is three numbers and a lag. Each loop, the four wheel powers are mixed
back into forward, strafe and turn; each of those chases its commanded speed with a 0.15 s time
constant; and the result is integrated into a pose.

| Axis | Flat out |
|---|---|
| Forward | 64.4 in/s |
| Strafe | 43.6 in/s |
| Turn | 4.0 rad/s |

The fake IMU's yaw is how far the simulated robot has turned since it was put down, so a
localizer's `setPose` does not move it, as on the robot. The motors' encoders count nothing.

So the simulator has no slip, no scrub, no battery sag, no floor, no field wall, no motor wired
backwards and no Pinpoint. It cannot tell you a path is too fast for the tyres, and it will happily
drive through the perimeter. **When the simulator and the robot disagree, the robot is right.**

An autonomous starts where its own `startPose()` says. A teleop starts where the last autonomous
finished, which `OpModeStorage.autonomousEndPose` carries between them; with no autonomous before
it, that is the middle of the near wall facing up the field. Nothing starts at Pedro's origin any
more, which is a field corner and outside the perimeter frame.

Time is simulated in a test and measured in a run. `OpModeHarness.loop()` advances `stepMs` and
reads no wall clock, so a test integrates the same motion every time. `simRun` calls `loop(long)`
with the real milliseconds its last pass took, bounded at 100 ms, so the robot moves at the speed it
would move on the field however slow the OpMode's own loop is. Everything periodic runs on a 10 ms
grid measured from the clock read at startup, rather than sleeping a fixed time after each pass.

## Why the NetworkTables version is not the current one

WPILib publishes `ntcore-java` at 2026.2.2, but every `-jni` artifact stops at 2025.3.2, and
`SimPublisherTest`'s client, which checks the server against WPILib's own reading of the protocol,
needs the native library. So both halves are pinned to 2025.3.2 in `TeamCode/build.gradle`,
and both move together when the 2026 natives appear.

Loading that native out of a plain Maven jar takes some care, and `NtNatives` is where it happens.
`RuntimeLoader` and `CombinedRuntimeLoader` both fail, each in its own way, and that class's javadoc
records what they print so nobody tries them again.

## The conversion that is temporary

`base/FieldPose.java` is a copy of `FlightLog.fieldPose` in corbelsflightlog, which converts Pedro's
inches into metres from the centre of the field and applies `fieldQuarterTurns`. The library's copy
is written and unreleased. When it releases, the dependency moves to that version,
`base/FieldPose.java` is deleted, and `SimPublisher` calls the library. `FieldPoseTest` is what
keeps the two agreeing meanwhile.
