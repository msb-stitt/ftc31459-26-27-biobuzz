# Glossary

Every word here is one the task pages use, and each entry names the page that introduces it. Where
the guide could have invented a friendlier word and did not, the reason is that the code, Pedro's
documentation and the Driver Station all use this one.

**OpMode**
: One program the Driver Station can run. Each lesson is an OpMode, and its name in the list is the
  one in its `@TeleOp` or `@Autonomous` line. [L020](l020.md)

**Simulator**
: A robot that exists only in the laptop. It runs the same OpMode, so code can be tried before the
  real robot is near. [L020](l020.md)

**AdvantageScope**
: The program that shows what the code published. It can show it live or from a flight log, as
  numbers, as graphs, or as a robot on the field. [The cheat sheet](cheatsheet.md) lists every name
  the lessons publish. [L020](l020.md)

**Flight log**
: The file a run writes. It holds every number the code published, so you can look at the run
  later. [L020](l020.md)

**TeleOp and Autonomous**
: The two kinds of OpMode. A TeleOp reads the gamepads; an autonomous runs on its own. L125 and
  L130 are the two autonomous lessons. [L125](l125.md)

**Driver Station**
: The phone or tablet that holds the gamepads and picks the OpMode. Lines printed with
  `Tracker.printToDs` show up there. [L050](l050.md)

**Drivetrain**
: The four motors and the code that decides what power each one gets. L040 sets the four powers
  itself. From L110 on, a lesson's drivetrain is a class of its own. [L090](l090.md)

**Deadband**
: A band of stick readings near the middle that count as let go. Without one the robot creeps when
  nobody is touching it. [L060](l060.md)

**Method**
: A named piece of code you can use again. It takes numbers in and can give one back.
  [L060](l060.md)

**Squaring a stick**
: Making a small push mean a smaller power, by multiplying the stick by itself and putting the sign
  back. [L060](l060.md)

**Scaling the powers**
: Bringing two or four wheel powers down together when the biggest one is over 1. The robot still
  goes where it was asked. It only goes slower. [L080](l080.md)

**Strafe**
: Driving sideways without turning. [L090](l090.md)

**Mixing**
: Turning forward, sideways and turn into four wheel powers with four sums. [L090](l090.md)

**Mecanum**
: A wheel with rollers set at 45 degrees on its rim, which lets the robot drive sideways. The four
  wheels' rollers make an X seen from above. [L090](l090.md)

**Commanded wheels**
: Four powers a lesson has set itself. The path follower cannot overrule them. It gets the wheels
  back when the lesson hands them back. [L110](l110.md)

**Array**
: Several numbers in a row, in one variable. `wheels[FL]` is one of them. [L110](l110.md)

**Follower**
: Pedro's code that drives the robot along a path, or holds it at a pose. From L110 on it holds the
  drivetrain and the lessons reach past it. [L110](l110.md)

**Pose**
: Where the robot is and which way it faces: x, y and a heading. A pose is a place on the field, not
  a distance from the robot. [L040](l040.md)

**Heading**
: Which way the robot is facing, as an angle. Counter-clockwise counts up, and the code keeps it in
  radians even where a page says degrees. [L090](l090.md)

**Localizer**
: Whatever works out the pose. A robot can run more than one at a time. That is what L120 is for.
  [L120](l120.md)

**Shadow localizer**
: A second localizer that is read and logged but never steers anything. It is how two answers get
  compared on one run. [L120](l120.md)

**Encoder**
: The counter inside a drive motor that counts how far the motor has turned. [L120](l120.md)

**Dead reckoning**
: Working out where you are by adding up every step you took, with nothing to check it against. The
  drive encoders do it; the Pinpoint does it better. [L120](l120.md)

**Pinpoint**
: The odometry computer. It has its own two measuring wheels, and it works out the pose. The
  follower believes this one. L195 measures against it. [L100](l100.md)

**Path**
: A line or curve the follower drives along, with a heading to hold while it does.
  [L125](l125.md)

**Hold**
: Telling the follower to sit at one pose and stay there. If something pushes the robot, the
  follower puts it back. [L130](l130.md)

**Field relative**
: Sticks that mean the field. Push away from you and the robot moves away from you, whichever way
  its nose points. [L140](l140.md)

**Robot relative**
: Sticks that mean the robot's own directions. Better for lining up against something.
  [L150](l150.md)

**Heading hold**
: Keeping the nose where the driver left it, and giving it straight back the moment the turn stick
  moves. [L160](l160.md)

**Instant command**
: A command that does its work and finishes in the same breath, rather than running for a while. A
  button press is usually one. [L170](l170.md)

**Feedforward**
: A guess at the power a wanted speed needs, made before anything is measured. Gets most of the way
  there at once. [L190](l190.md)

**Feedback**
: A correction worked out from two speeds: the one asked for and the one measured. It cleans up
  what the guess got wrong. [L190](l190.md)

**Ticks**
: What an encoder counts as a motor turns. A tick is a fixed fraction of a turn, so ticks become
  inches once you know how many make one. [L195](l195.md)

**Ticks per inch**
: How many ticks a wheel counts for an inch of travel. Measured, not looked up. [L195](l195.md)

**Turn radius**
: How far a wheel rolls each time the robot spins one radian. Measure from the middle of the robot
  to a wheel, once across and once along, and add the two. It turns a spin speed into a wheel
  speed. [L195](l195.md)

**Coast and brake**
: What a motor does at zero power. Braking holds the robot still; coasting lets the wheels roll,
  which is what a measurement you push by hand needs. [L195](l195.md)
