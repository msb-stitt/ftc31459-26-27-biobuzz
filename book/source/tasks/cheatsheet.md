# Cheat sheet

Every table here is read straight out of the lesson code, so it says what the code says.

## What to run

One OpMode name for the Driver Station, one command for the laptop. Each command runs only
that lesson's own tests, which is what makes a red one easy to read. A dash means no test
names that lesson: its check is on the floor.

| Lesson | On the robot | On the laptop |
| --- | --- | --- |
| L020 | `L020 Sticks` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l020*'` |
| L030 | `L030 Speeds` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l030*'` |
| L040 | `L040 Tank` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l040*'` |
| L050 | `L050 Blocks` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l050*'` |
| L060 | `L060 Shaping` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l060*'` |
| L070 | `L070 Buttons` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l070*'` |
| L080 | `L080 Arcade` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l080*'` |
| L090 | `L090 Mecanum` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l090*'` |
| L110 | `L110 Follower` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l110*'` |
| L120 | `L120 Compare Localizers` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l120_theEncoderLocalizerRunsAlongsideAndIsLogged' --tests '*L120EncoderLocalizerTest*'` |
| L125 | `L125 Drive 24` (Autonomous) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l125*'` |
| L130 | `L130 Forward Then Strafe` (Autonomous) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l130*'` |
| L140 | `L140 Field Relative` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l140*'` |
| L150 | `L150 Robot Relative Button` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l150*'` |
| L160 | `L160 Heading Hold` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l160*'` |
| L170 | `L170 Drive To Pose` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l170*'` |
| L180 | `L180 Combined` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l180*'` |
| L190 | `L190 Velocity Drive` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l190*'` |
| L195 | `L195 Measure ticks per inch` (TeleOp) `L195 Measure turn radius` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l195*'` |

## What shows up in AdvantageScope

A key ending in `...` has a name added to the end of it, one per wheel or per localizer.

| Log key | Published by |
| --- | --- |
| `Localizer/driveWheelEncoders/...` | `L120CompareLocalizersOpMode`, `L180CombinedOpMode` |
| `arcade/forward` | `L080ArcadeOpMode`, `L090MecanumOpMode`, `L110FollowerOpMode`, `L120CompareLocalizersOpMode`, `L140FieldRelativeOpMode`, `L150RobotRelativeButtonOpMode`, `L160HeadingHoldOpMode`, `L170DriveToPoseOpMode`, `L180CombinedOpMode` |
| `arcade/strafe` | `L090MecanumOpMode`, `L110FollowerOpMode`, `L120CompareLocalizersOpMode`, `L140FieldRelativeOpMode`, `L150RobotRelativeButtonOpMode`, `L160HeadingHoldOpMode`, `L170DriveToPoseOpMode`, `L180CombinedOpMode` |
| `arcade/turn` | `L080ArcadeOpMode`, `L090MecanumOpMode`, `L110FollowerOpMode`, `L120CompareLocalizersOpMode`, `L140FieldRelativeOpMode`, `L150RobotRelativeButtonOpMode`, `L160HeadingHoldOpMode`, `L170DriveToPoseOpMode`, `L180CombinedOpMode` |
| `button/a` | `L070ButtonsOpMode`, `L080ArcadeOpMode`, `L090MecanumOpMode`, `L110FollowerOpMode`, `L120CompareLocalizersOpMode`, `L140FieldRelativeOpMode`, `L150RobotRelativeButtonOpMode`, `L160HeadingHoldOpMode`, `L170DriveToPoseOpMode`, `L180CombinedOpMode` |
| `button/event` | `L070ButtonsOpMode`, `L080ArcadeOpMode`, `L090MecanumOpMode`, `L110FollowerOpMode`, `L120CompareLocalizersOpMode`, `L140FieldRelativeOpMode`, `L150RobotRelativeButtonOpMode`, `L160HeadingHoldOpMode`, `L170DriveToPoseOpMode`, `L180CombinedOpMode` |
| `command/forward_ips` | `L190VelocityDriveOpMode` |
| `command/left_ips` | `L190VelocityDriveOpMode` |
| `command/turn_radps` | `L190VelocityDriveOpMode` |
| `drive/mode` | `L170DriveToPoseOpMode`, `L180CombinedOpMode` |
| `drive/robotRelative` | `L150RobotRelativeButtonOpMode`, `L160HeadingHoldOpMode`, `L170DriveToPoseOpMode`, `L180CombinedOpMode` |
| `heading/holding` | `L160HeadingHoldOpMode`, `L170DriveToPoseOpMode`, `L180CombinedOpMode` |
| `heading/target_deg` | `L180CombinedOpMode` |
| `measure/inches` | `L195MeasureTicksPerInchOpMode` |
| `measure/radians` | `L195MeasureTurnRadiusOpMode` |
| `measure/ticks` | `L195MeasureTicksPerInchOpMode` |
| `measure/ticksPerInch` | `L195MeasureTicksPerInchOpMode` |
| `measure/turnRadius_in` | `L195MeasureTurnRadiusOpMode` |
| `measure/wheel_inches` | `L195MeasureTurnRadiusOpMode` |
| `power/backLeft` | `L090MecanumOpMode` |
| `power/backRight` | `L090MecanumOpMode` |
| `power/frontLeft` | `L090MecanumOpMode` |
| `power/frontRight` | `L090MecanumOpMode` |
| `power/left` | `L060ShapingOpMode`, `L070ButtonsOpMode`, `L080ArcadeOpMode` |
| `power/right` | `L060ShapingOpMode`, `L070ButtonsOpMode`, `L080ArcadeOpMode` |
| `speed/left` | `L030SpeedsOpMode`, `L040TankOpMode`, `L050BlocksOpMode`, `L060ShapingOpMode`, `L070ButtonsOpMode` |
| `speed/right` | `L030SpeedsOpMode`, `L040TankOpMode`, `L050BlocksOpMode`, `L060ShapingOpMode`, `L070ButtonsOpMode` |
| `stick/leftX` | `L070ButtonsOpMode`, `L080ArcadeOpMode`, `L090MecanumOpMode`, `L110FollowerOpMode`, `L120CompareLocalizersOpMode`, `L140FieldRelativeOpMode`, `L150RobotRelativeButtonOpMode`, `L160HeadingHoldOpMode`, `L170DriveToPoseOpMode`, `L180CombinedOpMode`, `L190VelocityDriveOpMode` |
| `stick/leftY` | `L020SticksOpMode`, `L030SpeedsOpMode`, `L040TankOpMode`, `L050BlocksOpMode`, `L060ShapingOpMode`, `L070ButtonsOpMode`, `L080ArcadeOpMode`, `L090MecanumOpMode`, `L110FollowerOpMode`, `L120CompareLocalizersOpMode`, `L140FieldRelativeOpMode`, `L150RobotRelativeButtonOpMode`, `L160HeadingHoldOpMode`, `L170DriveToPoseOpMode`, `L180CombinedOpMode`, `L190VelocityDriveOpMode` |
| `stick/rightX` | `L070ButtonsOpMode`, `L080ArcadeOpMode`, `L090MecanumOpMode`, `L110FollowerOpMode`, `L120CompareLocalizersOpMode`, `L140FieldRelativeOpMode`, `L150RobotRelativeButtonOpMode`, `L160HeadingHoldOpMode`, `L170DriveToPoseOpMode`, `L180CombinedOpMode`, `L190VelocityDriveOpMode` |
| `stick/rightY` | `L020SticksOpMode`, `L030SpeedsOpMode`, `L040TankOpMode`, `L050BlocksOpMode`, `L060ShapingOpMode`, `L070ButtonsOpMode`, `L080ArcadeOpMode`, `L090MecanumOpMode`, `L110FollowerOpMode`, `L120CompareLocalizersOpMode`, `L140FieldRelativeOpMode`, `L150RobotRelativeButtonOpMode`, `L160HeadingHoldOpMode`, `L170DriveToPoseOpMode`, `L180CombinedOpMode` |

Generated by applying `solutions/` on `solutions-l010`, every lesson in order.
