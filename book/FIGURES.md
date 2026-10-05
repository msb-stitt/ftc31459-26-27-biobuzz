# Figures, taken and not taken

**Status: controlling.** The register every figure in the guide is listed in.

A figure that does not exist yet is **pencilled**: the page shows a visible box carrying the
figure's id and what the picture must show, so a reader can see the guide is unfinished there and a
writer can keep going. `tools/check_figures.py` checks both directions, and it is the only place the
number of pencilled figures is counted.

How to add one: put the box in the page and the row here, in the same commit.

```
:::{admonition} fig-robot-front
:class: pencil
What the picture must show, in one sentence.
:::
```

| id | What it must show | How it is obtained | Status |
| --- | --- | --- | --- |
| `fig-robot-front` | The robot from the front, on the floor, with both gamepads beside it, so a student can match what is in front of them to what the guide calls each part | Photo of the robot | pencilled |
| `fig-gamepad-sticks` | Gamepad 1 from above, with the left stick, the right stick and the A button labelled, and an arrow showing which way each y axis counts up | Photo of a gamepad | pencilled |
| `fig-l2a-folders` | The Project window with `org.firstinspires.ftc.teamcode` open, showing the `lessons` folder and the `mytry` folder with only `package-info.java` in it | Screenshot, taken 2026-10-02 as `scratch/l2a/01-open.png` and kept out of the branch | pencilled |
| `fig-l2a-copy-class` | The Copy Class box with New name `L2aSticksOpMode` and Destination package `org.firstinspires.ftc.teamcode.mytry` | Screenshot, taken 2026-10-02 as `scratch/l2a/03-copy-dialog.png`, with the clip `v-copy.mov` | pencilled |
| `fig-l2a-complete` | The list under `gamepad1.` after typing `left_st`, with `left_stick_y` highlighted | Screenshot, taken 2026-10-02 as `scratch/l2a/07-complete-pick.png`, with the clips `v-tab.mov` and `v-hover.mov` | pencilled |
| `fig-l2a-live` | AdvantageScope's Line Graph with `NT:sim/stick/leftY` drawn at -1 and `NT:/stick/leftY` drawn at 1 | Screenshot, taken 2026-10-02 as `scratch/l2a/16-live.png` | pencilled |
| `fig-l2a-log` | AdvantageScope's Line Graph of the flight log with `/stick/leftY` in Left Axis, drawn at 1 | Screenshot, taken 2026-10-02 as `scratch/l2a/18-log.png` | pencilled |
| `fig-wheel-names` | The robot from above with its nose marked, and each of the four wheels labelled front left, front right, back left and back right | Photo of the robot, labelled | pencilled |
| `fig-wheel-forward` | One wheel from the side, with an arrow on the top of the tyre showing which way it travels when that wheel is driving the robot forward | Photo of the robot, labelled | pencilled |
| `fig-stick-shaping` | Three graphs side by side, each with the stick from -1 to 1 along the bottom and the power out from -1 to 1 up the side: the raw stick straight, the deadbanded stick with a flat step at the middle, and the squared stick as an S | A drawing | pencilled |
| `fig-mecanum-x` | The robot from above with its nose marked, and the top roller of each of the four wheels drawn as a line at 45 degrees, so the four lines make an X across the robot | A drawing | pencilled |
| `fig-wheel-handover` | Two arrows reaching the same four wheels, one from the path follower and one from the driver's sticks, with the commanded-wheels switch between them showing which one gets through | A drawing | pencilled |
| `fig-pinpoint-mounting` | The Pinpoint on the robot from above, with the forward pod and the strafe pod drawn at right angles to each other, each labelled with the socket it plugs into, and the sticker side marked facing up | Photo of the robot, labelled | pencilled |
| `fig-odometry-step` | One loop of dead reckoning: the four wheel travels since the last loop, the forward and left arrows those add up to, the same pair turned to point along the robot's heading, and the old estimate with that pair added on the end | A drawing | pencilled |
| `fig-field-relative` | The field from above with the driver at the bottom, the robot turned a quarter turn, the stick's direction as an arrow on the field, and the same arrow drawn again on the robot showing what forward and sideways it turns into | A drawing | pencilled |
| `fig-speed-loop` | The loop drawn once round: the stick asking for 40 in/s, the feedforward guess at the power, the encoder's measured speed coming back, and the error being added in | A drawing | pencilled |
| `fig-l020-copy-class` | The Copy Class box with New name `L020SticksOpMode` and Destination package `org.firstinspires.ftc.teamcode.mytry`, with `mytry` in red | Screenshot, taken 2026-10-05 and cropped from the full screen | pencilled |
| `fig-l020-copied` | The Project window with `lessons` and the new `mytry` folder both open, `L020SticksOpMode` in each, and the copy open in the editor with its first line ending in `mytry;` | Screenshot, taken 2026-10-05 | pencilled |
| `fig-l020-complete` | The list under `gamepad1.` after typing `left_st`, with `left_stick_y` highlighted and marked `float` | Screenshot, taken 2026-10-05 and cropped from the full screen | pencilled |
| `fig-l020-hover` | The box over `left_stick_y` reading `public volatile float left_stick_y` and *left analog stick vertical axis* | Screenshot, taken 2026-10-05 and cropped from the full screen | pencilled |
| `fig-l020-tracker` | The list under `Track`, with `Tracker` from `org.firstinspires.ftc.teamcode.base` highlighted at the top and `Tracker` from `org.opencv.video` under it | Screenshot, taken 2026-10-05 and cropped from the full screen | pencilled |
| `fig-l020-live` | AdvantageScope's Line Graph connected to the simulator, with `NT:/stick/leftY` drawn at -1 and `NT:/stick/rightY` drawn at 0.5, and both names in Left Axis | Screenshot, taken 2026-10-05 with the sticks set by `--left_stick_y=-1 --right_stick_y=0.5` | pencilled |
| `fig-l020-log` | AdvantageScope's Line Graph of the flight log `L020SticksOpMode-….wpilog`, with `/stick/leftY` at -1 and `/stick/rightY` at 0.5 for the whole run | Screenshot, taken 2026-10-05 of `L020SticksOpMode-20261005-131029.wpilog` | pencilled |
| `fig-l030-complete` | The list under `-leftS`, offering the variable `leftStickY`, marked `double` | Screenshot, taken 2026-10-05 and cropped from the full screen | pencilled |
| `fig-l030-live` | AdvantageScope's Line Graph connected to the simulator, with `NT:/stick/leftY` drawn at -1 and `NT:/speed/left` drawn at 1, and `speed/right` reading -0.500 beside `stick/rightY` at 0.500 in the list | Screenshot, taken 2026-10-05 with the sticks set by `--left_stick_y=-1 --right_stick_y=0.5` | pencilled |
| `fig-l040-field` | AdvantageScope's 2D Field connected to the simulator, with the robot `NT:sim/Pose` near the left wall after curving left from its start, `speed/left` reading 0.250 and `speed/right` 0.350 | Screenshot, taken 2026-10-05 with the sticks set by `--left_stick_y=-0.25 --right_stick_y=-0.35` | pencilled |
| `fig-l040-hardware` | The list under `hardware.front`, offering `frontLeft` and `frontRight`, each marked `DcMotorEx` | Screenshot, taken 2026-10-05 and cropped from the full screen | pencilled |
| `fig-l040-setpower` | The list under `hardware.frontLeft.setP`, with `setPower (double power)` second | Screenshot, taken 2026-10-05 and cropped from the full screen | pencilled |
| `fig-l050-direction` | The list under `hardware.mecanumConfig.frontLeftD`, offering `frontLeftDirection` and `frontLeftDirection.get()`, the second marked `Direction` | Screenshot, taken 2026-10-05 and cropped from the full screen | pencilled |
| `fig-l060-deadband` | AdvantageScope's Line Graph with `NT:/speed/left` at 0.03 and `NT:/power/left` at 0, and the list showing `power/left` and `power/right` at 0, all four `sim/wheels` at 0, and `speed/right` at -0.040 | Screenshot, taken 2026-10-05 with the sticks set by `--left_stick_y=-0.03 --right_stick_y=0.04` | pencilled |
| `fig-l060-squared` | AdvantageScope's Line Graph with `NT:/speed/left` at 0.5 and `NT:/power/left` at 0.25, and the list showing `power/right` at -0.250 beside `speed/right` at -0.500 | Screenshot, taken 2026-10-05 with the sticks set by `--left_stick_y=-0.5 --right_stick_y=0.5` | pencilled |
| `fig-l070-button` | The list under `gamepad1.a`, offering `a` marked `boolean` first, then `aWasPressed()` and `aWasReleased()` | Screenshot, taken 2026-10-05 in Android Studio while typing L070S030 | pencilled |
| `fig-l070-pressed` | AdvantageScope's Line Graph with `NT:/button/a` at true and `NT:/button/event` at "A pressed" in **Discrete Fields**, drawn as two bars along the bottom of the graph, and the list showing `button/a` as a green dot and `button/event` as `A pressed` | Screenshot, taken 2026-10-05 with A held by `--a=true` | pencilled |
| `fig-l070-sideways` | AdvantageScope's list with the `stick` folder open, showing `leftX` at 0.500 and `rightX` at -0.250, `leftY` and `rightY` at 0, and `speed` and `power` at 0 | Screenshot, taken 2026-10-05 with the sticks set by `--left_stick_x=0.5 --right_stick_x=-0.25` | pencilled |
| `fig-l080-scaled` | AdvantageScope's Line Graph with `NT:/power/left` at 1 and `NT:/power/right` at 0.6 on **Left Axis**, and the list showing `arcade/forward` at 1 and `arcade/turn` at -0.250 | Screenshot, taken 2026-10-05 with the sticks set by `--left_stick_y=-1 --right_stick_x=0.5`, after L080S050 | pencilled |
| `fig-l080-forward-turn` | AdvantageScope's list with the `arcade` folder open, showing `forward` at 0.250 and `turn` at -0.250, beside `stick/leftY` at -0.500 and `stick/rightX` at 0.500 | Screenshot, taken 2026-10-05 with the sticks set by `--left_stick_y=-0.5 --right_stick_x=0.5` | pencilled |
| `fig-l080-too-much` | AdvantageScope's Line Graph with `NT:/power/left` at 1.25 and `NT:/power/right` at 0.75 on **Left Axis**, and the list showing `arcade/forward` at 1 and `arcade/turn` at -0.250 | Screenshot, taken 2026-10-05 with the sticks set by `--left_stick_y=-1 --right_stick_x=0.5`, before L080S050 | pencilled |
| `fig-l090-slide` | AdvantageScope's 2D Field with the robot `NT:sim/Pose` slid to its own left along the bottom edge, its nose still pointing the same way at 180.00°, and the list showing `arcade/strafe` at 0.250 and the four wheel powers at -0.250, 0.250, 0.250 and -0.250 | Screenshot, taken 2026-10-05 with the stick set by `--left_stick_x=-0.5` | pencilled |
| `fig-l090-corner` | AdvantageScope's 2D Field and list with the left stick in the forward-left corner: `arcade/forward` and `arcade/strafe` at 1, `power/frontRight` and `power/backLeft` at 1, `power/frontLeft` and `power/backRight` at 0, and the robot's heading still 180.00° | Screenshot, taken 2026-10-05 with the stick set by `--left_stick_y=-1 --left_stick_x=-1` | pencilled |
| `fig-l090-wheel-pushes` | The robot from above, three times: driving forward, sliding left and turning left. On each wheel, an arrow for the way it spins and an arrow at 45 degrees for the way it pushes the robot, and in the middle one arrow for where the four pushes add up to | A drawing | pencilled |
| `fig-l110-slide` | AdvantageScope's 2D Field with the robot `NT:sim/Pose` slid to its own left along the bottom edge, its nose still pointing the same way at 180.00°, and the list showing `arcade/strafe` at 0.250, as in L090, with the path follower holding the drivetrain | Screenshot, taken 2026-10-05 with the stick set by `--left_stick_x=-0.5` | pencilled |
| `fig-l120-shadow-still` | AdvantageScope's list with the `Localizer` folder open: `pinPoint/y_in` climbing past 600 while the robot drives forward, and `driveWheelEncoders` still at x 72, y 10.5 and heading 90, where it started | Screenshot, taken 2026-10-05 with the stick set by `--left_stick_y=-0.5` | pencilled |
| `fig-l120-import` | Android Studio with `HardwareWheelSource` underlined in red and the popup offering **Import class... org.firstinspires.ftc.teamcode.base.odometry.HardwareWheelSource?** with ⌥↩ | Screenshot, taken 2026-10-05 | pencilled |
| `fig-l125-drive24` | AdvantageScope's 2D Field with the robot stopped 24 inches out from the back wall, its arrow pointing up the field, and the list showing `pinPoint/y_in` at 34.499, `x_in` at 72 and `heading_deg` at 90 | Screenshot, taken 2026-10-05 | pencilled |
| `fig-l125-import-static` | Android Studio with `sequential`, `follow` and `line` underlined in red and the popup offering **Import static method... com.pedropathing.api.Paths.line()?** with ⌥↩ | Screenshot, taken 2026-10-05 | pencilled |
| `fig-l130-two-legs` | AdvantageScope's 2D Field with the robot stopped right of the middle of the field, its arrow still pointing up the field, and the list showing `pinPoint/x_in` at 95.999, `y_in` at 72.001 and `heading_deg` at 90 | Screenshot, taken 2026-10-05 | pencilled |
| `fig-advantagescope-sim` | AdvantageScope with a 2D field showing `sim/Pose`, the topic list open beside it, and a graph of the four `sim/wheels` values | Screenshot of AdvantageScope | pencilled |
| `fig-panels-graph` | The Panels Graph panel with `stick/left_raw` and `stick/left_shaped` both plotted while the stick is pushed slowly to full, so the gap between the two curves is visible | Screenshot of Panels | pencilled |
