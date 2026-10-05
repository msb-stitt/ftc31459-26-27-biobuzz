# Cheat sheet

Every table here is read straight out of the lesson code, so it says what the code says.

## What to run

One OpMode name for the Driver Station, one command for the laptop. Each command runs only
that lesson's own tests, which is what makes a red one easy to read. A dash means no test
names that lesson: its check is on the floor.

| Lesson | On the robot | On the laptop |
| --- | --- | --- |
| L2a | `L2a Sticks` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l2a*'` |
| L2b | `L2b Tank` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l2b*'` |
| L3a | `L3a Deadband` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l3a*'` |
| L3b | `L3b Squared` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l3b*'` |
| L4 | `L4 Arcade` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l4*'` |
| L5 | `L5 Holonomic` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l5*'` |
| L6 | `L6 Follower Wheels` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*L6FollowerDriveTrainTest*'` |
| L8 | `L8 Compare Localizers` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l8*'` |
| L9 | `L9 Drive 24` (Autonomous) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l9*'` |
| L10 | `L10 Forward Then Strafe` (Autonomous) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l10*'` |
| L11 | `L11 Field Relative` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l11*'` |
| L12 | `L12 Robot Relative Button` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l12*'` |
| L13 | `L13 Heading Hold` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l13*'` |
| L14 | `L14 Drive To Pose` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l14*'` |
| L15 | `L15 Combined` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l15*'` |
| L16 | `L16 Velocity Drive` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsTest.l16*'` |
| L17a | `L17a Measure ticks per inch` (TeleOp) | `./gradlew :TeamCode:testDebugUnitTest --tests '*LessonsDriveTrainTest.theTicksDoorReportsWhatEachEncoderCounted'` |
| L17b | `L17b Measure turn radius` (TeleOp) | -- |
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

## Who writes what in the shared drivetrain

Every lesson adds to one class, and nothing is written twice. A method written in an early
lesson is the one a later lesson calls.

| Lesson | What it writes in `LessonsDriveTrain` |
| --- | --- |
| L2b | `writeWheels()`, `driveWheelsNow()` |
| L3a and L3b | `deadband()`, `squared()`, `fieldRelative()` |
| L4 | `normalized()` |
| L6 | `mix()`, `setCommandedWheels()`, `releaseCommandedWheels()`, `commandedWheelsAreSet()`, `drive()` |
| L16 | `setCommandedWheelSpeeds()` |
| L17a and L17b | `wheelTicks()` |

## What shows up in Panels

A key ending in `...` has a name added to the end of it, one per wheel or per localizer.

| Log key | Published by |
| --- | --- |
| `Localizer/driveWheelEncoders/...` | `L120CompareLocalizersOpMode`, `L15CombinedOpMode`, `L180CombinedOpMode`, `L8CompareLocalizersOpMode` |
| `arcade/forward` | `L080ArcadeOpMode`, `L090MecanumOpMode`, `L110FollowerOpMode`, `L120CompareLocalizersOpMode`, `L140FieldRelativeOpMode`, `L150RobotRelativeButtonOpMode`, `L160HeadingHoldOpMode`, `L170DriveToPoseOpMode`, `L180CombinedOpMode` |
| `arcade/strafe` | `L090MecanumOpMode`, `L110FollowerOpMode`, `L120CompareLocalizersOpMode`, `L140FieldRelativeOpMode`, `L150RobotRelativeButtonOpMode`, `L160HeadingHoldOpMode`, `L170DriveToPoseOpMode`, `L180CombinedOpMode` |
| `arcade/turn` | `L080ArcadeOpMode`, `L090MecanumOpMode`, `L110FollowerOpMode`, `L120CompareLocalizersOpMode`, `L140FieldRelativeOpMode`, `L150RobotRelativeButtonOpMode`, `L160HeadingHoldOpMode`, `L170DriveToPoseOpMode`, `L180CombinedOpMode` |
| `button/a` | `L070ButtonsOpMode`, `L080ArcadeOpMode`, `L090MecanumOpMode`, `L110FollowerOpMode`, `L120CompareLocalizersOpMode`, `L140FieldRelativeOpMode`, `L150RobotRelativeButtonOpMode`, `L160HeadingHoldOpMode`, `L170DriveToPoseOpMode`, `L180CombinedOpMode` |
| `button/event` | `L070ButtonsOpMode`, `L080ArcadeOpMode`, `L090MecanumOpMode`, `L110FollowerOpMode`, `L120CompareLocalizersOpMode`, `L140FieldRelativeOpMode`, `L150RobotRelativeButtonOpMode`, `L160HeadingHoldOpMode`, `L170DriveToPoseOpMode`, `L180CombinedOpMode` |
| `command/forward` | `L4ArcadeOpMode`, `L5HolonomicOpMode`, `L6WheelsFollowerOpMode` |
| `command/forward_ips` | `L16VelocityDriveOpMode`, `L190VelocityDriveOpMode` |
| `command/left` | `L5HolonomicOpMode`, `L6WheelsFollowerOpMode` |
| `command/left_ips` | `L16VelocityDriveOpMode`, `L190VelocityDriveOpMode` |
| `command/turn_ccw` | `L4ArcadeOpMode`, `L5HolonomicOpMode`, `L6WheelsFollowerOpMode` |
| `command/turn_radps` | `L16VelocityDriveOpMode`, `L190VelocityDriveOpMode` |
| `drive/aiming` | `L15CombinedOpMode` |
| `drive/mode` | `L14DriveToPoseOpMode`, `L15CombinedOpMode`, `L170DriveToPoseOpMode`, `L180CombinedOpMode` |
| `drive/robotRelative` | `L12RobotRelativeButtonOpMode`, `L150RobotRelativeButtonOpMode`, `L160HeadingHoldOpMode`, `L170DriveToPoseOpMode`, `L180CombinedOpMode` |
| `drive/target_deg` | `L15CombinedOpMode` |
| `driver pressed A` | `L2aSticksOpMode`, `L2bTankOpMode` |
| `heading/deg` | `L13HeadingHoldOpMode` |
| `heading/holding` | `L13HeadingHoldOpMode`, `L160HeadingHoldOpMode`, `L170DriveToPoseOpMode`, `L180CombinedOpMode` |
| `heading/target_deg` | `L180CombinedOpMode` |
| `lesson/event` | `L2aSticksOpMode`, `L2bTankOpMode` |
| `lesson/loop_count` | `L2aSticksOpMode` |
| `lesson/seconds_running` | `L2aSticksOpMode` |
| `measure/inches` | `L17aMeasureTicksPerInchOpMode` |
| `measure/radians` | `L17bMeasureTurnRadiusOpMode` |
| `measure/ticks` | `L17aMeasureTicksPerInchOpMode` |
| `measure/ticksPerInch` | `L17aMeasureTicksPerInchOpMode` |
| `measure/turnRadius_in` | `L17bMeasureTurnRadiusOpMode` |
| `measure/wheel_inches` | `L17bMeasureTurnRadiusOpMode` |
| `power/backLeft` | `L090MecanumOpMode` |
| `power/backRight` | `L090MecanumOpMode` |
| `power/frontLeft` | `L090MecanumOpMode` |
| `power/frontRight` | `L090MecanumOpMode` |
| `power/left` | `L060ShapingOpMode`, `L070ButtonsOpMode`, `L080ArcadeOpMode` |
| `power/right` | `L060ShapingOpMode`, `L070ButtonsOpMode`, `L080ArcadeOpMode` |
| `speed/left` | `L030SpeedsOpMode`, `L040TankOpMode`, `L050BlocksOpMode`, `L060ShapingOpMode`, `L070ButtonsOpMode` |
| `speed/right` | `L030SpeedsOpMode`, `L040TankOpMode`, `L050BlocksOpMode`, `L060ShapingOpMode`, `L070ButtonsOpMode` |
| `stick/leftX` | `L070ButtonsOpMode`, `L080ArcadeOpMode`, `L090MecanumOpMode`, `L110FollowerOpMode`, `L120CompareLocalizersOpMode`, `L140FieldRelativeOpMode`, `L150RobotRelativeButtonOpMode`, `L160HeadingHoldOpMode`, `L170DriveToPoseOpMode`, `L180CombinedOpMode`, `L190VelocityDriveOpMode`, `L2aSticksOpMode`, `L2bTankOpMode` |
| `stick/leftY` | `L020SticksOpMode`, `L030SpeedsOpMode`, `L040TankOpMode`, `L050BlocksOpMode`, `L060ShapingOpMode`, `L070ButtonsOpMode`, `L080ArcadeOpMode`, `L090MecanumOpMode`, `L110FollowerOpMode`, `L120CompareLocalizersOpMode`, `L140FieldRelativeOpMode`, `L150RobotRelativeButtonOpMode`, `L160HeadingHoldOpMode`, `L170DriveToPoseOpMode`, `L180CombinedOpMode`, `L190VelocityDriveOpMode`, `L2aSticksOpMode`, `L2bTankOpMode` |
| `stick/left_raw` | `L3aDeadbandOpMode`, `L3bSquaredOpMode` |
| `stick/left_shaped` | `L3aDeadbandOpMode`, `L3bSquaredOpMode` |
| `stick/rightX` | `L070ButtonsOpMode`, `L080ArcadeOpMode`, `L090MecanumOpMode`, `L110FollowerOpMode`, `L120CompareLocalizersOpMode`, `L140FieldRelativeOpMode`, `L150RobotRelativeButtonOpMode`, `L160HeadingHoldOpMode`, `L170DriveToPoseOpMode`, `L180CombinedOpMode`, `L190VelocityDriveOpMode`, `L2aSticksOpMode`, `L2bTankOpMode` |
| `stick/rightY` | `L020SticksOpMode`, `L030SpeedsOpMode`, `L040TankOpMode`, `L050BlocksOpMode`, `L060ShapingOpMode`, `L070ButtonsOpMode`, `L080ArcadeOpMode`, `L090MecanumOpMode`, `L110FollowerOpMode`, `L120CompareLocalizersOpMode`, `L140FieldRelativeOpMode`, `L150RobotRelativeButtonOpMode`, `L160HeadingHoldOpMode`, `L170DriveToPoseOpMode`, `L180CombinedOpMode`, `L2aSticksOpMode`, `L2bTankOpMode` |
| `stick/right_raw` | `L3aDeadbandOpMode`, `L3bSquaredOpMode` |
| `stick/right_shaped` | `L3aDeadbandOpMode`, `L3bSquaredOpMode` |
| `wheel/...` | `LessonsDriveTrain` |

Generated by applying `solutions/` on `solutions-l010`, every lesson in order.
