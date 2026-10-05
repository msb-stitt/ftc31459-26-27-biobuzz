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
| `fig-advantagescope-sim` | AdvantageScope with a 2D field showing `sim/Pose`, the topic list open beside it, and a graph of the four `sim/wheels` values | Screenshot of AdvantageScope | pencilled |
| `fig-panels-graph` | The Panels Graph panel with `stick/left_raw` and `stick/left_shaped` both plotted while the stick is pushed slowly to full, so the gap between the two curves is visible | Screenshot of Panels | pencilled |
