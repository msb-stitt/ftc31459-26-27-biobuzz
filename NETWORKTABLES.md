# NetworkTables on the robot

AdvantageScope connects to the robot the way it connects to the simulator: live, over
NetworkTables 4, on port 5810. The server is `Nt4Server` in corbelsflightlog, plain Java with no
libraries, and it sends everything the flight log records under the same names the log uses.

## Turning it on

It is off unless an OpMode turns it on. An OpMode calls `Tracker.serveLive()` after `initBefore()`,
and every value the flight log records from then on, and the last value of everything recorded
before, goes out live. `Tracker.begin` switches it off again for the next OpMode. The server itself
starts on the first call and runs until the Robot Controller app stops, so AdvantageScope stays
connected from one run to the next.

**For practice only.** FTC rule R704 forbids third-party telemetry over Wi-Fi at competitions, so no
competition OpMode calls `serveLive()`. No lesson calls it either.

`example/NtCheckOpMode`, **NetworkTables check** on the Driver Station, turns it on and drives no
wheels.

## Connecting AdvantageScope to the robot

With the laptop on the robot's Wi-Fi:

- **AdvantageScope > Settings…**, and set **Robot Address** to `192.168.43.1`.
- **File > Connect to Robot > NetworkTables 4**.

The menu items were read out of AdvantageScope 26.0.2's menu bar on 2026-10-03, not tried against a
robot.

## The robot test

Not yet run on a robot. Each step says what to see when it works.

- **Build and install.** Build the app from the `nt-server` branch and install it on the Control
  Hub as usual. It builds; whether it installs and starts has not been seen.
- **Run the check.** On the Driver Station choose **NetworkTables check** under TeleOp, then **INIT**
  and **▶**. The Driver Station shows `NetworkTables on, port 5810`, `AdvantageScope connections:
  0`, and the seconds counting up. If it shows `NetworkTables OFF`, the line above it says why.
- **Probe the server.** From the laptop on the robot's Wi-Fi, `curl -i http://192.168.43.1:5810/`
  prints `HTTP/1.1 200 OK`. This is the request AdvantageScope sends before it connects.
- **Connect AdvantageScope**, as above. The window title changes to `192.168.43.1`, the Driver
  Station shows `AdvantageScope connections: 2` (NetworkTables 4.1 opens a second connection for
  its clock), and `ntcheck/` appears in the list of fields.
- **Watch it move.** Drag `ntcheck/sine` onto a **Line Graph**: a wave, one cycle every 4 s,
  matching the `sine` value on the Driver Station. Push the left stick forward and
  `ntcheck/leftY` goes to 1. Hold A and `ntcheck/a` goes true and `ntcheck/word` reads `A held`.
- **See the robot.** Add a **2D Field**, choose this season's field and drag `Robot/Pose` onto it.
  Push the robot by hand and it moves on the field.
- **Reconnect.** Stop the OpMode and start it again. AdvantageScope stays connected, and the values
  start again from 0 seconds.
- **The log matches.** Pull the run's flight log from `http://192.168.43.1:8080/corbelsflightlog`
  and open it in AdvantageScope. `ntcheck/sine` and the rest hold what was seen live.
- **Loop time.** With AdvantageScope connected, the Driver Station's loop time stays where it was
  before connecting; graph `loop/period_ms` live to see it.

## What to record

For each step, whether it did what it says. And, from `adb shell getprop ro.product.cpu.abilist`,
what the Control Hub reports, which settles a question native ntcore raised and this server does not
depend on.
