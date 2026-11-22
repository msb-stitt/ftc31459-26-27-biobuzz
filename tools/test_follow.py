"""Status: controlling. follow.py's arithmetic, with no simulator: python3 tools/test_follow.py.

Copied from ftc-claude's `tools/video/test_follow.py` at `b257083`."""

import math
import unittest

import follow
from follow import Around, Fig8, Gains, command, point_ahead, sticks, slew


def lesson_speeds(st, mode):
    """What the lesson's OpMode makes of the sticks, as forward and leftward in/s and turn rad/s:
    L040's, L080's, L090's and StarterBot TeleOp's answers, with follow.py's measured speeds at a
    power of 1."""
    def shaped(v):  # deadband 0.05, then signedSquared; StarterBot TeleOp takes the stick as it is
        if mode == "starterbot":
            return v
        return 0.0 if abs(v) < 0.05 else v * abs(v)
    if mode == "tank":
        left, right = -st["left_stick_y"], -st["right_stick_y"]
        f, s, t = (left + right) / 2, 0.0, (right - left) / 2
    else:
        f = shaped(-st["left_stick_y"])
        s = shaped(-st.get("left_stick_x", 0.0))
        t = shaped(-st["right_stick_x"])
        biggest = max(1.0, abs(f) + abs(s) + abs(t))
        f, s, t = f / biggest, s / biggest, t / biggest
    return f * follow.FORWARD_IPS, s * follow.STRAFE_IPS, t * follow.TURN_RADPS


def drive(mode, path, pose, g, dt=0.01, hold=0.0):
    """Steps a robot through the path from sticks(), slewed, and returns the poses."""
    held, poses, t = {}, [], 0.0
    while t < path.duration + hold:
        held = slew(held, sticks(pose, path.reference(t), mode, g), dt, g.slew)
        f, s, w = lesson_speeds(held, mode)
        x, y, h = pose
        pose = (x + (f * math.cos(h) - s * math.sin(h)) * dt,
                y + (f * math.sin(h) + s * math.cos(h)) * dt, h + w * dt)
        poses.append((t, pose))
        t += dt
    return poses


START = (61.5, 0.0, math.pi)  # sim/Pose's start, in inches


class PointAheadTest(unittest.TestCase):

    def test_the_inverse_gives_the_point_its_asked_velocity_at_eight_headings(self):
        g = Gains(k=0.0)
        for i in range(8):
            h = i * math.pi / 4
            pose = (10.0, -5.0, h)
            p = point_ahead(pose, g.ahead)
            v, _, w = command(pose, (p, (3.0, -7.0), 0.0), "tank", g)
            # M(h) (v, w), the point's velocity
            vx = math.cos(h) * v - g.ahead * math.sin(h) * w
            vy = math.sin(h) * v + g.ahead * math.cos(h) * w
            self.assertAlmostEqual(vx, 3.0, places=9, msg=f"heading {h}")
            self.assertAlmostEqual(vy, -7.0, places=9, msg=f"heading {h}")

    def test_a_target_ahead_drives_forward_and_one_to_the_left_turns_left(self):
        g = Gains()
        pose = (0.0, 0.0, 0.0)
        v, _, w = command(pose, ((20.0, 0.0), (0.0, 0.0), 0.0), "tank", g)
        self.assertGreater(v, 0.0)
        self.assertAlmostEqual(w, 0.0)
        _, _, w = command(pose, ((6.0, 10.0), (0.0, 0.0), 0.0), "tank", g)
        self.assertGreater(w, 0.0)


class SticksTest(unittest.TestCase):

    def test_forward_pushes_each_lessons_forward_stick_forward(self):
        g = Gains()
        ref = ((20.0, 0.0), (0.0, 0.0), 0.0)
        for mode in follow.MODES:
            st = sticks((0.0, 0.0, 0.0), ref, mode, g)
            self.assertLess(st["left_stick_y"], 0.0, mode)
            f, s, t = lesson_speeds(st, mode)
            self.assertGreater(f, 0.0, mode)

    def test_no_wheel_is_asked_for_more_than_the_limit(self):
        g = Gains(limit=0.6)
        ref = ((500.0, 300.0), (0.0, 0.0), 2.0)
        for mode in follow.MODES:
            f, s, t = lesson_speeds(sticks((0.0, 0.0, 0.0), ref, mode, g), mode)
            wheel = abs(f / follow.FORWARD_IPS) + abs(s / follow.STRAFE_IPS) \
                + abs(t / follow.TURN_RADPS)
            self.assertLessEqual(wheel, 0.6 + 1e-9, mode)

    def test_holonomic_slides_left_for_a_target_to_its_left(self):
        for mode in follow.HOLONOMIC:
            st = sticks((0.0, 0.0, 0.0), ((0.0, 20.0), (0.0, 0.0), 0.0), mode, Gains())
            f, s, t = lesson_speeds(st, mode)
            self.assertGreater(s, 0.0, mode)
            self.assertLess(st["left_stick_x"], 0.0, mode)

    def test_starterbot_takes_the_power_as_its_stick(self):
        ref = ((5.0, 0.0), (0.0, 0.0), 0.0)
        robot, starter = (sticks((0.0, 0.0, 0.0), ref, m, Gains()) for m in ("robot", "starterbot"))
        self.assertAlmostEqual(starter["left_stick_y"], -robot["left_stick_y"] ** 2)


class Figure8Test(unittest.TestCase):

    def test_the_8_starts_and_ends_at_the_centre_at_rest(self):
        path = Fig8((55.5, 0.0), math.pi)
        for t in (path.leg_s, path.duration):
            p, v, _ = path.reference(t)
            self.assertAlmostEqual(math.hypot(*p), 0.0, places=9)
            self.assertAlmostEqual(math.hypot(*v), 0.0, places=9)

    def check_tracks(self, mode, ahead_or_middle, within, limit=1.0, leg_s=2.0, eight_s=8.0):
        g = Gains(limit=limit)
        start = point_ahead(START, g.ahead) if ahead_or_middle == "ahead" else START[:2]
        path = Fig8(start, START[2], leg_s=leg_s, eight_s=eight_s)
        curve = path.curve()
        worst = 0.0
        for t, pose in drive(mode, path, START, g):
            tracked = point_ahead(pose, g.ahead) if ahead_or_middle == "ahead" else pose[:2]
            worst = max(worst, follow.distance_to(tracked, curve))
            if mode in follow.HOLONOMIC:
                self.assertLess(abs(follow.wrap(pose[2] - START[2])), math.radians(2))
        self.assertLess(worst, within, f"{mode}: {worst:.2f} in from the route")

    # Slowly, within 1 in: a reversed turn sign drives the 8 backwards within 8 in, and fails here.
    def check_tracks_slowly(self, mode, ahead_or_middle):
        self.check_tracks(mode, ahead_or_middle, 1.0, limit=0.6, leg_s=4.0, eight_s=20.0)

    def test_tank_steers_the_point_ahead_around_the_8_slowly(self):
        self.check_tracks_slowly("tank", "ahead")

    def test_arcade_steers_the_point_ahead_around_the_8_slowly(self):
        self.check_tracks_slowly("arcade", "ahead")

    def test_holonomic_slides_its_middle_around_the_8_slowly_holding_its_heading(self):
        self.check_tracks_slowly("robot", "middle")

    def test_starterbot_slides_its_middle_around_the_8_slowly_holding_its_heading(self):
        self.check_tracks_slowly("starterbot", "middle")

    def test_each_mode_drives_the_take_within_10_in(self):  # lv.ask.spike.fast, the defaults
        for mode, ahead_or_middle in (("tank", "ahead"), ("arcade", "ahead"), ("robot", "middle"),
                                      ("starterbot", "middle")):
            with self.subTest(mode):
                self.check_tracks(mode, ahead_or_middle, 10.0)


class AroundTest(unittest.TestCase):

    def test_each_shape_ends_at_its_first_corner_at_rest(self):
        for shape in follow.SIDES:
            path = Around(START[:2], START[2], shape)
            p, v, _, _ = path.reference(path.duration)
            first = path.reference(path.parts[0][0])[0]
            self.assertAlmostEqual(math.hypot(p[0] - first[0], p[1] - first[1]), 0.0, 6, shape)
            self.assertAlmostEqual(math.hypot(*v), 0.0, 6, shape)

    def test_the_heading_faces_the_way_the_shape_goes(self):
        for shape in follow.SIDES:
            path = Around(START[:2], START[2], shape)
            for i in range(1, 400):
                t = path.parts[0][0] + (path.duration - path.parts[0][0]) * i / 400
                _, v, h, _ = path.reference(t)
                if math.hypot(*v) > 1.0:
                    self.assertAlmostEqual(follow.wrap(h - math.atan2(v[1], v[0])), 0.0, 6,
                                           f"{shape} at {t:.2f} s")

    def test_holonomic_drives_each_shape_within_6_in_facing_the_way_it_goes(self):
        g = Gains()
        for shape in follow.SIDES:
            path = Around(START[:2], START[2], shape)
            curve = path.curve()
            poses = drive("robot", path, START, g, hold=1.0)
            worst = max(follow.distance_to(pose[:2], curve) for _, pose in poses)
            self.assertLess(worst, 6.0, shape)
            t, pose = poses[-1]
            want = path.reference(path.duration)[2]
            self.assertLess(abs(follow.wrap(pose[2] - want)), 0.1, shape)


if __name__ == "__main__":
    unittest.main()
