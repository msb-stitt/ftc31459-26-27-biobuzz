"""Status: controlling, for not-found.png.

Draws the picture on 404.html, the student in a purple cap, sad, on the grey square, in the
lessons' 3D style: lit as book/tools/draw.py lights them, the camera in front of the student, a
little to their left and above, and the head turned almost to the camera.

    book/.venv/bin/python cloudflare/not_found.py

It writes not-found.png beside it, which assemble.py copies into the site.
"""

import math
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE.parent / "book" / "tools"))
import draw  # noqa: E402
from draw import (PEN, PAPER, Sketch, Iso, ellipse, landing, stroke_shadows, HEAD_R,  # noqa: E402
                  CAP, BRIM, CAP_TILT, STAND_HALF)

CAM_AZ, CAM_EL = -45.0, 16.0      # the camera's direction from the student, in degrees
YAW = -70.0                       # the way the student faces, in degrees
HEAD_TURN = 22.0                  # the head turned this far toward the camera, in degrees
BRIM_OUT, BRIM_LIFT = 1.0, 10.0   # the brim's reach in head radii, and its tip up, in degrees


def add(*vs):
    return tuple(sum(c) for c in zip(*vs))


def mul(k, v):
    return tuple(k * c for c in v)


def dot(a, b):
    return sum(x * y for x, y in zip(a, b))


def unit(v):
    return mul(1 / math.sqrt(dot(v, v)), v)


def toward(az, el):
    a, e = math.radians(az), math.radians(el)
    return (math.cos(e) * math.cos(a), math.cos(e) * math.sin(a), math.sin(e))


TOWARD = toward(CAM_AZ, CAM_EL)
CAM_RIGHT = (-math.sin(math.radians(CAM_AZ)), math.cos(math.radians(CAM_AZ)), 0.0)
CAM_UP = toward(CAM_AZ + 180.0, 90.0 - CAM_EL)


class Body:
    """A frame for the student: f forward, r to their right, up, from the feet's middle o."""

    def __init__(self, o, yaw_deg):
        a = math.radians(yaw_deg)
        self.o = o
        self.f = (math.cos(a), math.sin(a), 0.0)
        self.r = (self.f[1], -self.f[0], 0.0)

    def at(self, side, fwd, up):
        return add(self.o, mul(side, self.r), mul(fwd, self.f), (0.0, 0.0, up))


# the sad pose, in the body's (right, forward, up) units
JOINTS = dict(hips=(0, 0, 1.15), neck=(0, 0.1, 1.98), lankle=(-0.12, -0.05, 0.07),
              rankle=(0.12, -0.05, 0.07), lhand=(-0.24, 0.04, 1.3), rhand=(0.24, 0.04, 1.3))


def head_in_cap(v, head, f, r):
    """The head's sphere in a cap whose brim points along f; returns whether the brim is on the
    near side, a function that draws it, and the crown's outline."""
    t = CAP_TILT
    fwd = add(mul(math.cos(t), f), (0, 0, math.sin(t)))
    up = add(mul(-math.sin(t), f), (0, 0, math.cos(t)))
    h = 0.15
    centre, rb = add(head, mul(h * HEAD_R, up)), HEAD_R * math.sqrt(1 - h * h)

    def band(phi):
        return add(centre, mul(rb * math.cos(phi), r), mul(rb * math.sin(phi), fwd))

    span = [math.radians(d) for d in range(25, 156, 5)]
    lift = math.radians(BRIM_LIFT)

    def edge(a):
        ext = BRIM_OUT * HEAD_R * math.sin(math.pi * (a - span[0]) / (span[-1] - span[0]))
        d = unit(add(band(a), mul(-1, centre)))
        return add(band(a), mul(ext * math.cos(lift), d), mul(ext * math.sin(lift), up))

    brim = [v.page(band(a)) for a in reversed(span)] + [v.page(edge(a)) for a in span]

    def draw_brim():
        v.s.line(brim, PEN, 2.4, 0.9, fill=BRIM, closed=True)

    near = dot(fwd, TOWARD) > 0
    if not near:
        draw_brim()
    c, rr = v.page(head), HEAD_R * v.k
    v.s.line(ellipse(c, rr), PEN, 3.4, 0.95, fill=PAPER, closed=True)
    steps = [math.radians(d) for d in range(0, 360, 2)]
    seen = [dot(add(band(a), mul(-1, head)), TOWARD) >= 0 for a in steps]
    first = next(i for i in range(len(steps)) if seen[i] and not seen[i - 1])
    arc = []
    for i in range(len(steps)):
        j = (first + i) % len(steps)
        if not seen[j]:
            break
        arc.append(v.page(band(steps[j])))
    lean = add(up, mul(-dot(up, TOWARD), TOWARD))
    top = v.page(add(head, mul(HEAD_R / math.sqrt(dot(lean, lean)), lean)))

    def angle(p):
        return math.atan2(p[1] - c[1], p[0] - c[0])

    a0, a1, at_top = angle(arc[-1]), angle(arc[0]), angle(top)
    ccw = (a1 - a0) % (2 * math.pi)
    if (at_top - a0) % (2 * math.pi) > ccw:
        ccw -= 2 * math.pi
    rim = [(c[0] + rr * math.cos(a0 + ccw * i / 40), c[1] + rr * math.sin(a0 + ccw * i / 40))
           for i in range(1, 40)]
    return near, draw_brim, arc + rim


def sad_face(v, head, f, r):
    """Eyes, brows tipped up in the middle, a frown and a tear, on the head's sphere."""
    def pt(across, down):
        d = add(mul(math.cos(down) * math.cos(across), f), mul(math.cos(down) * math.sin(across), r),
                (0, 0, -math.sin(down)))
        return v.page(add(head, mul(HEAD_R, d)))

    def curve(pts, width=3.0):
        v.s.line([pt(a, d) for a, d in pts], PEN, width, 0.95)

    ed = -0.05
    for a in (-0.38, 0.38):
        v.s.dot(pt(a, ed), 0.022 * v.k)
        curve([(a * 0.35, ed - 0.2), (a * 1.45, ed - 0.12)], 2.6)
    curve([(-0.26 + 0.52 * i / 10, 0.42 - 0.12 * math.sin(math.pi * i / 10)) for i in range(11)])
    v.s.line([pt(-0.4, ed + 0.06), pt(-0.42, ed + 0.26)], "#5aa0e6", 3.6)


def foot(b, ankle):
    """The foot under the ankle: the ankle sits over its back third, the toes forward."""
    heel = add(b.at(ankle[0], ankle[1] - 0.06, 0.0), (0, 0, 0.012))
    toes = add(b.at(ankle[0] * 1.15, ankle[1] + 0.2, 0.0), (0, 0, 0.012))
    c = mul(0.5, add(heel, toes))
    along = unit(add(toes, mul(-1, heel)))
    across = (along[1], -along[0], 0.0)
    half = 0.5 * math.dist(heel, toes)
    return [add(c, mul(half * math.cos(u), along), mul(0.075 * math.sin(u), across),
                (0, 0, 0.03 * max(math.sin(u * 0.5 + 1.0), 0)))
            for u in (2 * math.pi * i / 36 for i in range(36))]


def student(v, b):
    j = {k: b.at(*p) for k, p in JOINTS.items()}
    head = add(j["neck"], (0, 0, 0.38))
    shoulder = add(j["neck"], (0, 0, -0.08))
    limbs = [(j["hips"], j["lankle"]), (j["hips"], j["rankle"]), (j["neck"], j["hips"]),
             (shoulder, j["lhand"]), (shoulder, j["rhand"])]
    s, hw = v.s, STAND_HALF
    square = [v.page(add(b.o, (dx, dy, 0))) for dx, dy in ((-hw, -hw), (hw, -hw), (hw, hw),
                                                           (-hw, hw))]
    s.line(square, None, fill="#e4e5e5", closed=True)
    s.marker(square, "#e4e5e5", nib=12, opacity=0.5, layers=1)
    s.line(square, PEN, 2.0, 0.85, closed=True)
    floor = []
    for a, c in limbs:
        floor += stroke_shadows(v, a, c, 7)
    hl = landing(head)
    floor.append([v.page((hl[0] + HEAD_R * 1.25 * math.cos(u), hl[1] + HEAD_R * math.sin(u), 0))
                  for u in (i * math.pi / 12 for i in range(24))])
    for k in ("lhand", "rhand"):
        hp = landing(j[k])
        floor.append([v.page((hp[0] + 0.08 * math.cos(u), hp[1] + 0.08 * math.sin(u), 0))
                      for u in (i * math.pi / 8 for i in range(16))])
    s.cast(floor, sigma=3, alpha=0.5)
    for k in ("lankle", "rankle"):
        s.line([v.page(q) for q in foot(b, JOINTS[k])], PEN, 2.6, 0.95, fill=PAPER, closed=True)
    for a, c in limbs:
        s.ink_line(v.page(a), v.page(c), width=4.2, over=2, passes=1, wobble=0.8)
    turned = Body(b.o, YAW + HEAD_TURN)
    near, draw_brim, crown = head_in_cap(v, head, turned.f, turned.r)
    s.line(crown, PEN, 2.6, 0.95, fill=CAP, closed=True)
    sad_face(v, head, turned.f, turned.r)
    if near:
        draw_brim()
    for k in ("lhand", "rhand"):
        s.line(ellipse(v.page(j[k]), 0.075 * v.k, 0.065 * v.k), PEN, 2.6, 0.95, fill=PAPER,
               closed=True)


def main():
    draw.OUT = HERE
    s = Sketch(460, 540, 11)
    student(Iso(s, (290, 460), 165, right=CAM_RIGHT, up=CAM_UP), Body((0.0, 0.0, 0.0), YAW))
    s.save("not-found")


if __name__ == "__main__":
    main()
