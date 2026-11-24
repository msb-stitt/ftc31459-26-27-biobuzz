# /// script
# requires-python = ">=3.11"
# dependencies = ["pyntcore==2026.2.2"]
# ///
"""Status: controlling. Drives simRun's virtual gamepad 1 so the simulated robot follows a path.

Copied from ftc-claude's `tools/video/follow.py` at `b257083`, beside `vpad.py`, so projects
outside ftc-claude can use it.

A proportional controller: it reads the robot's pose from simRun's `sim/Pose` over NetworkTables 4,
and moves the sticks the way the lesson's OpMode reads them. Tank and arcade steer a point a set
distance ahead of the robot onto the path; holonomic steers the robot's middle and holds a heading.
ftc-claude's `.docs/2026-10-21-lesson-videos.md`, *The point ahead, for tank and arcade*, gives
the arithmetic.

Usage: follow.py MODE [--shape S] [--port N] [--nt-port N] [--log FILE] [--size IN] [--leg S]
                 [--eight S] [--around S] [--limit P]
  MODE    tank (L040), arcade (L080), robot (L090) or starterbot (StarterBot TeleOp): which
          sticks, and how the OpMode shapes them
  --port  simRun's SIMRUN_VIRTUAL_PADS, 5812 unless given
  --nt-port  simRun's SIMRUN_NT_PORT, where it serves sim/Pose, 5810 unless given
  --log   writes each loop's time, pose, reference and sticks as JSON lines
  --shape eight, unless given, or triangle, square, pentagon, hexagon or circle: a driver's lap,
          Around's, for a screenshot of the robot driven as a student would drive it
  --size  the 8's A, or the shape's corners from the centre, in inches; --leg, the seconds to the
          centre or the first corner; --eight and --around, the seconds around the 8 or the
          shape; --limit, the largest wheel power. Fig8's, Around's and Gains' defaults unless
          given.

The 8 is ftc-claude's `lv.ask.spike.path`: from where the robot starts to the field's centre,
then once around a lemniscate of Gerono. Distances are inches and angles radians, in `sim/Pose`'s
frame with its origin at the field's centre. Run it with simRun already running the lesson's
OpMode with --pad: `uv run tools/follow.py arcade`, which takes pyntcore from the header above.
"""

import argparse
import json
import math
import struct
import sys
import time

IN_PER_M = 1 / 0.0254

# Measured in simRun on 2026-10-21, ftc-claude's `lv.spike.scale`: the speed at a wheel power of 1.
FORWARD_IPS = 64.4   # every wheel at 1
STRAFE_IPS = 43.6    # L090's strafe power at 1
TURN_RADPS = 4.0     # the two sides at -1 and 1

MODES = ("tank", "arcade", "robot", "starterbot")
HOLONOMIC = ("robot", "starterbot")


class Gains:
    """The controller's settings."""

    def __init__(self, k=2.0, k_heading=3.0, ahead=6.0, limit=1.0, slew=2.5):
        self.k = k                  # 1/s: how fast a position error closes
        self.k_heading = k_heading  # 1/s: how fast a heading error closes, holonomic only
        self.ahead = ahead          # in: how far ahead of the robot's middle the steered point is
        self.limit = limit          # the largest wheel power asked for
        self.slew = slew            # stick travel per second; L020's RAMP moves a stick 1 in 0.4 s


# The path ---------------------------------------------------------------------------------------

def smooth_progress(t, duration):
    """0 to 1 over `duration`, starting and ending at rest; and its rate. Smoothstep."""
    u = min(max(t / duration, 0.0), 1.0)
    rate = 6 * u * (1 - u) / duration if 0 < t < duration else 0.0
    return u * u * (3 - 2 * u), rate


def leg(t, start, end, duration):
    """The point at time t on a straight line from start to end, and its velocity."""
    u, du = smooth_progress(t, duration)
    dx, dy = end[0] - start[0], end[1] - start[1]
    return (start[0] + u * dx, start[1] + u * dy), (du * dx, du * dy)


def figure8(t, size, duration):
    """Once around a lemniscate of Gerono centred on (0, 0), starting up and to the left.

    x = -size sin s, y = size / 2 sin 2s, with s going 0 to 2 pi along a smoothstep, so the
    point starts and ends at rest."""
    u, du = smooth_progress(t, duration)
    s, ds = 2 * math.pi * u, 2 * math.pi * du
    p = (-size * math.sin(s), size / 2 * math.sin(2 * s))
    v = (-size * math.cos(s) * ds, size * math.cos(2 * s) * ds)
    return p, v


class Fig8:
    """The leg from the start to the centre, then the 8. `reference(t)` is the point, its
    velocity and the heading to hold."""

    def __init__(self, start, heading, size=36.0, leg_s=2.0, eight_s=8.0):
        self.start, self.heading = start, heading
        self.size, self.leg_s, self.eight_s = size, leg_s, eight_s
        self.duration = leg_s + eight_s

    def reference(self, t):
        if t < self.leg_s:
            p, v = leg(t, self.start, (0.0, 0.0), self.leg_s)
        else:
            p, v = figure8(t - self.leg_s, self.size, self.eight_s)
        return p, v, self.heading

    def curve(self, n=2000):
        """Points along the leg and the 8, for measuring how far the robot was from them."""
        return [self.reference(self.duration * i / n)[0] for i in range(n + 1)]


SIDES = {"triangle": 3, "square": 4, "pentagon": 5, "hexagon": 6, "circle": 0}


def turn(t, h0, h1, duration):
    """The heading at time t, turning from h0 to h1 the short way, and its rate."""
    u, du = smooth_progress(t, duration)
    d = wrap(h1 - h0)
    return h0 + u * d, du * d


class Around:
    """A driver's lap: from the start to the shape's corner nearest it, holding the heading, then
    once around the shape anticlockwise, facing the way it goes. A polygon stops at each corner and
    turns on the spot; a circle turns as it goes. `reference(t)` is the point, its velocity, the
    heading to hold and its rate.

    The shape is centred on the field's centre, its corners `size` inches from it."""

    def __init__(self, start, heading, shape, size=36.0, leg_s=2.0, around_s=16.0, turn_s=1.0):
        self.size, self.n = size, SIDES[shape]
        a0 = math.atan2(start[1], start[0])
        if self.n:
            self.corners = [(size * math.cos(a0 + 2 * math.pi * k / self.n),
                             size * math.sin(a0 + 2 * math.pi * k / self.n))
                            for k in range(self.n + 1)]
            first = self.corners[0]
            faces = [math.atan2(b[1] - a[1], b[0] - a[0])
                     for a, b in zip(self.corners, self.corners[1:])]
        else:
            first = (size * math.cos(a0), size * math.sin(a0))
            faces = [a0 + math.pi / 2]
        self.a0 = a0
        # Each part: (seconds, a function of the time into it giving (p, v, h, rate))
        self.parts = [(leg_s, lambda t: leg(t, start, first, leg_s) + (heading, 0.0))]
        h = heading
        if self.n:
            side_s = around_s / self.n
            for k in range(self.n):
                c0, c1, f = self.corners[k], self.corners[k + 1], faces[k]
                self.parts.append((turn_s, lambda t, c=c0, h0=h, f=f:
                                   (c, (0.0, 0.0)) + turn(t, h0, f, turn_s)))
                self.parts.append((side_s, lambda t, c0=c0, c1=c1, f=f:
                                   leg(t, c0, c1, side_s) + (f, 0.0)))
                h = f
        else:
            self.parts.append((turn_s, lambda t, h0=h, f=faces[0]:
                               (first, (0.0, 0.0)) + turn(t, h0, f, turn_s)))
            self.parts.append((around_s, lambda t: self.circle(t, around_s)))
        self.duration = sum(d for d, _ in self.parts)

    def circle(self, t, duration):
        u, du = smooth_progress(t, duration)
        a, da = self.a0 + 2 * math.pi * u, 2 * math.pi * du
        p = (self.size * math.cos(a), self.size * math.sin(a))
        v = (-self.size * math.sin(a) * da, self.size * math.cos(a) * da)
        return p, v, a + math.pi / 2, da

    def reference(self, t):
        for d, f in self.parts:
            if t < d:
                return f(t)
            t -= d
        d, f = self.parts[-1]
        return f(d)

    def curve(self, n=2000):
        return [self.reference(self.duration * i / n)[0] for i in range(n + 1)]


# The controller ---------------------------------------------------------------------------------

def point_ahead(pose, ahead):
    """The point `ahead` inches in front of the robot's middle."""
    x, y, h = pose
    return x + ahead * math.cos(h), y + ahead * math.sin(h)


def wrap(a):
    """An angle in -pi to pi."""
    return math.atan2(math.sin(a), math.cos(a))


def command(pose, ref, mode, g):
    """What the robot should do: forward and leftward speed in in/s, and turn in rad/s, CCW.

    `ref` is the path's point, its velocity, the heading to hold, and that heading's rate, which
    a path that holds one heading leaves out."""
    (rx, ry), (rvx, rvy), rh = ref[:3]
    rw = ref[3] if len(ref) > 3 else 0.0
    x, y, h = pose
    c, s = math.cos(h), math.sin(h)
    if mode in HOLONOMIC:
        ux, uy = rvx + g.k * (rx - x), rvy + g.k * (ry - y)
        return c * ux + s * uy, -s * ux + c * uy, rw + g.k_heading * wrap(rh - h)
    px, py = point_ahead(pose, g.ahead)
    ux, uy = rvx + g.k * (rx - px), rvy + g.k * (ry - py)
    # M(h) inverted: the point's velocity to forward speed and turn rate.
    return c * ux + s * uy, 0.0, (-s * ux + c * uy) / g.ahead


def powers(cmd, mode, limit):
    """The lesson's powers for a command: tank's left and right, or forward, strafe and turn.

    Scaled together so no wheel is asked for more than `limit`, which keeps the direction."""
    forward, left, turn = cmd
    f, st, t = forward / FORWARD_IPS, left / STRAFE_IPS, turn / TURN_RADPS
    if mode == "tank":
        p = (f - t, f + t)
        biggest = max(abs(p[0]), abs(p[1]))
    elif mode == "arcade":
        p = (f, t)
        biggest = abs(f) + abs(t)
    else:
        p = (f, st, t)
        biggest = abs(f) + abs(st) + abs(t)
    scale = min(1.0, limit / biggest) if biggest > 0 else 1.0
    return tuple(v * scale for v in p)


def unsquare(v):
    """The stick that L080's and L090's signedSquared turns into v."""
    return math.copysign(math.sqrt(abs(v)), v)


def sticks(pose, ref, mode, g):
    """The sticks, by the FTC names, that drive the robot toward `ref`."""
    p = powers(command(pose, ref, mode, g), mode, g.limit)
    if mode == "tank":  # L040: each side's power is its stick, negated
        return {"left_stick_y": -p[0], "right_stick_y": -p[1]}
    if mode == "arcade":  # L080: forward from -left_stick_y, turn from -right_stick_x, squared
        return {"left_stick_y": -unsquare(p[0]), "right_stick_x": -unsquare(p[1])}
    if mode == "starterbot":  # StarterBot TeleOp: L090's sticks, not squared
        return {"left_stick_y": -p[0], "left_stick_x": -p[1], "right_stick_x": -p[2]}
    # L090: strafe, leftward, from -left_stick_x, squared like the others
    return {"left_stick_y": -unsquare(p[0]), "left_stick_x": -unsquare(p[1]),
            "right_stick_x": -unsquare(p[2])}


def slew(held, want, dt, rate):
    """Each stick moved toward its wanted value by at most `rate` per second."""
    step = rate * dt
    return {k: held.get(k, 0.0) + max(-step, min(step, v - held.get(k, 0.0)))
            for k, v in want.items()}


def distance_to(p, curve):
    return min(math.hypot(p[0] - q[0], p[1] - q[1]) for q in curve)


# The wrappers -----------------------------------------------------------------------------------

class SimPose:
    """`sim/Pose` from simRun, in inches from the field's centre and radians."""

    def __init__(self, port=5810, wait_s=30.0):
        import ntcore
        self._inst = ntcore.NetworkTableInstance.create()
        self._inst.startClient4("follow.py")
        self._inst.setServer("127.0.0.1", port)
        # Every 10 ms, simRun's loop; the default, 100 ms, leaves the robot a loop behind.
        self._sub = self._inst.getRawTopic("sim/Pose").subscribe(
            "struct:Pose2d", b"", ntcore.PubSubOptions(periodic=0.01))
        deadline = time.time() + wait_s
        while len(self._sub.get()) != 24:
            if time.time() > deadline:
                raise SystemExit("follow.py: no sim/Pose from simRun")
            time.sleep(0.1)

    def read(self):
        x, y, h = struct.unpack("<3d", self._sub.get())
        return x * IN_PER_M, y * IN_PER_M, h


def main(argv):
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    ap.add_argument("mode", choices=MODES)
    ap.add_argument("--shape", choices=("eight",) + tuple(SIDES), default="eight")
    ap.add_argument("--port", type=int, default=5812)
    ap.add_argument("--nt-port", type=int, default=5810)
    ap.add_argument("--log")
    ap.add_argument("--size", type=float)
    ap.add_argument("--leg", type=float)
    ap.add_argument("--eight", type=float)
    ap.add_argument("--around", type=float)
    ap.add_argument("--limit", type=float)
    args = ap.parse_args(argv)
    from vpad import Pads  # beside this file, which Python puts first on the path

    g = Gains(**({"limit": args.limit} if args.limit else {}))
    pose_in = SimPose(args.nt_port)
    pose = pose_in.read()
    start = pose[:2] if args.mode in HOLONOMIC else point_ahead(pose, g.ahead)
    if args.shape == "eight":
        path = Fig8(start, pose[2], **{k: v for k, v in (("size", args.size), ("leg_s", args.leg),
                                                        ("eight_s", args.eight)) if v})
    else:
        path = Around(start, pose[2], args.shape,
                      **{k: v for k, v in (("size", args.size), ("leg_s", args.leg),
                                           ("around_s", args.around)) if v})
    curve = path.curve()
    log = open(args.log, "w", buffering=1) if args.log else None  # by line, to watch as it runs
    held = {}
    worst_path = worst_ref = 0.0
    with Pads(args.port) as pads:
        t0 = last = time.time()
        while True:
            now = time.time()
            t, dt = now - t0, now - last
            last = now
            if t > path.duration + 0.5:
                break
            pose = pose_in.read()
            ref = path.reference(t)
            want = sticks(pose, ref, args.mode, g)
            held = slew(held, want, dt, g.slew)
            for k, v in held.items():
                pads.set(1, k, round(v, 4))
            tracked = pose[:2] if args.mode in HOLONOMIC else point_ahead(pose, g.ahead)
            worst_path = max(worst_path, distance_to(tracked, curve))
            worst_ref = max(worst_ref, math.hypot(tracked[0] - ref[0][0], tracked[1] - ref[0][1]))
            if log:
                log.write(json.dumps({"t": round(t, 3), "pose": pose, "ref": ref[0],
                                      "tracked": tracked, "sticks": held}) + "\n")
            time.sleep(max(0.0, 0.02 - (time.time() - now)))
        for k in held:
            pads.set(1, k, 0.0)
    if log:
        log.close()
    print(f"{args.mode}: the tracked point was at most {worst_path:.2f} in from the route "
          f"and {worst_ref:.2f} in from where it should have been")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
