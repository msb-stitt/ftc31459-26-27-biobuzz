"""Status: controlling, for the figures it draws.

Draws the book's figures into figures/ beside this file as PNG.

    python draw.py

Three kinds of figure. The plots, stick_shaping and speed_loop, are plain matplotlib, so a value
can be read off them. panels_graph is a stand-in for a Panels screenshot still to be taken, and
says so on its face. Every other figure is a drawing, in the marker-sketch style below, and is the
drawing the book uses.

Every number in a figure is the code's: the 0.05 deadband and the sign-keeping square of L060, the
four mixing sums of L090, the field rotation of L140, and L190's 40 in/s, its feedforward of
Constants.powerPerInchPerSecond and its correction of 0.008 per in/s.

The marker-sketch style, as the lesson outline sets it:

- Ink lines are drawn twice, fast: each runs a little past its ends, wobbles and bows.
- Color is laid down as broad parallel marker strokes, slanted, clipped to the shape, overlapping
  where two strokes cross. A darker pass on one side is the shadow side; a white stroke is the
  shine.
- Thin blue construction lines run past the drawing, as if it were laid out first.
- Red is the nose and the way the robot drives; orange a motion; blue a wheel's spin; green what
  the pushes add up to.
- A variable is a sticky note, its color its type, its value written on it; its name is a white
  label with an arrow to the note. A note's corner curls up and casts a soft shadow.
- Handwriting is Marker Felt and code is Menlo, both in macOS, so the drawings are drawn on a Mac.
  The script stops when either font is missing rather than draw in another.
"""

import math
import random
from pathlib import Path

import matplotlib

matplotlib.use("Agg")
from matplotlib.collections import LineCollection  # noqa: E402
from matplotlib.colors import to_rgba  # noqa: E402
from matplotlib.font_manager import FontProperties  # noqa: E402
import matplotlib.pyplot as plt  # noqa: E402
from matplotlib.patches import Circle, FancyArrowPatch, FancyBboxPatch, Polygon  # noqa: E402
import numpy as np  # noqa: E402

OUT = Path(__file__).resolve().parent / "figures"
DPI = 200
HAND_FONT = "/System/Library/Fonts/MarkerFelt.ttc"
CODE_FONT = "/System/Library/Fonts/Menlo.ttc"
for font in (HAND_FONT, CODE_FONT):
    if not Path(font).exists():
        raise SystemExit(f"{font} is missing: the drawings are drawn on a Mac")
INK = "#222222"
RED = "#c0392b"
BLUE = "#2c6fb7"
GREEN = "#2e8b57"
GREY = "#888888"
LIGHT = "#eeeeee"

DEADBAND = 0.05                        # L060
POWER_PER_IPS = 0.016695978563625216   # pedro/Constants.powerPerInchPerSecond
VELOCITY_KP = 0.008                    # LessonsDriveTrain.VELOCITY_KP
MAX_IPS = 40                           # L190VelocityDriveOpMode.MAX_IPS


def deadband(v):
    return np.where(np.abs(v) < DEADBAND, 0.0, v)


def squared(v):
    return np.sign(v) * v * v


def save(fig, name):
    OUT.mkdir(exist_ok=True)
    fig.savefig(OUT / f"{name}.png", dpi=DPI, bbox_inches="tight", facecolor="white")
    plt.close(fig)


def arrow(ax, start, end, color=INK, width=2.5, style="-|>", head=18, **kw):
    ax.add_patch(FancyArrowPatch(start, end, arrowstyle=style, mutation_scale=head,
                                 color=color, linewidth=width, **kw))


def stand_in(ax, what="a photo", x=0.5, y=0.02, ha="center", color=GREY):
    ax.text(x, y, f"Stand-in drawing for {what} still to be taken", transform=ax.transAxes,
            ha=ha, va="bottom", fontsize=10, color=color, style="italic")


def box(ax, xy, w, h, text, color=LIGHT, fontsize=11, edge=INK):
    ax.add_patch(FancyBboxPatch((xy[0] - w / 2, xy[1] - h / 2), w, h,
                                boxstyle="round,pad=0.05,rounding_size=0.15",
                                facecolor=color, edgecolor=edge, linewidth=1.8))
    ax.text(*xy, text, ha="center", va="center", fontsize=fontsize)


# ---------------------------------------------------------------- plots

def stick_shaping():
    x = np.linspace(-1, 1, 801)
    fig, axes = plt.subplots(1, 3, figsize=(12, 4))
    for ax, y, title in [(axes[0], x, "Raw stick"),
                         (axes[1], deadband(x), f"Deadbanded (band {DEADBAND})"),
                         (axes[2], squared(deadband(x)), "Deadbanded, then squared")]:
        ax.plot(x, y, color=BLUE, linewidth=2.5)
        ax.axhline(0, color=GREY, linewidth=0.8)
        ax.axvline(0, color=GREY, linewidth=0.8)
        ax.set_xlim(-1.05, 1.05)
        ax.set_ylim(-1.05, 1.05)
        ax.set_aspect("equal")
        ax.set_title(title, fontsize=13)
        ax.set_xlabel("stick, -1 to 1")
        ax.grid(alpha=0.3)
    axes[0].set_ylabel("power out, -1 to 1")
    inset = axes[1].inset_axes([0.58, 0.06, 0.38, 0.38])
    xi = np.linspace(-0.12, 0.12, 401)
    inset.plot(xi, deadband(xi), color=BLUE, linewidth=2)
    inset.axvspan(-DEADBAND, DEADBAND, color=RED, alpha=0.15)
    inset.set_xticks([-0.05, 0.05])
    inset.set_xticklabels(["-0.05", "0.05"], fontsize=7)
    inset.set_yticks([])
    inset.set_title("near the middle", fontsize=8)
    save(fig, "fig-stick-shaping")


def speed_loop():
    fig, ax = plt.subplots(figsize=(12, 5))
    ff = POWER_PER_IPS * MAX_IPS
    box(ax, (1.2, 3.2), 2.0, 1.0, f"stick full forward\nwanted {MAX_IPS} in/s", "#fde2d2")
    box(ax, (4.4, 4.2), 2.6, 0.9, f"feedforward\n{POWER_PER_IPS:.4f} x {MAX_IPS} = {ff:.3f}",
        "#dbe8f7", fontsize=10)
    box(ax, (4.4, 2.0), 2.6, 0.9, f"error x {VELOCITY_KP}\n(wanted - measured)", "#e3f1e3",
        fontsize=10)
    ax.add_patch(Circle((7.0, 3.2), 0.32, facecolor="white", edgecolor=INK, linewidth=2))
    ax.text(7.0, 3.2, "+", ha="center", va="center", fontsize=18)
    box(ax, (9.0, 3.2), 1.8, 1.0, "power\nto the motor", "#eeeeee")
    box(ax, (9.0, 0.8), 1.8, 0.9, "encoder:\nmeasured in/s", "#eeeeee", fontsize=10)
    arrow(ax, (2.2, 3.4), (3.1, 4.1))
    arrow(ax, (2.2, 3.0), (3.1, 2.1))
    arrow(ax, (5.7, 4.1), (6.75, 3.4))
    arrow(ax, (5.7, 2.1), (6.75, 3.0))
    arrow(ax, (7.32, 3.2), (8.1, 3.2))
    arrow(ax, (9.0, 2.7), (9.0, 1.25))
    arrow(ax, (8.1, 0.8), (4.4, 0.8), color=GREEN)
    arrow(ax, (4.4, 0.8), (4.4, 1.55), color=GREEN)
    ax.text(6.2, 0.45, "the measured speed comes back", fontsize=10, color=GREEN, ha="center")
    ax.set_xlim(0, 10.2)
    ax.set_ylim(0, 4.9)
    ax.axis("off")
    save(fig, "fig-speed-loop")


def panels_graph():
    t = np.linspace(0, 3, 600)
    raw = np.clip(t / 2.5, 0, 1)
    shaped = squared(deadband(raw))
    fig, ax = plt.subplots(figsize=(9, 4.6))
    ax.set_facecolor("#1e1e24")
    ax.plot(t, raw, color="#5dade2", linewidth=2.5, label="speed/left")
    ax.plot(t, shaped, color="#f5b041", linewidth=2.5, label="power/left")
    ax.fill_between(t, shaped, raw, color="#ffffff", alpha=0.08)
    ax.set_xlabel("time, s")
    ax.set_ylabel("value")
    ax.set_ylim(-0.05, 1.08)
    ax.grid(alpha=0.2)
    ax.legend(loc="upper left")
    ax.set_title("Graph: the stick pushed slowly to full, with L060's shaping", fontsize=12)
    stand_in(ax, "a Panels screenshot", x=0.98, y=0.04, ha="right", color="#bbbbbb")
    save(fig, "fig-panels-graph")


# ---------------------------------------------------------------- the marker-sketch style

PAPER = "#fbfaf6"
PEN = "#1d1d1f"
CONSTRUCT = "#9fb4c8"
GREY1, GREY2, GREY3, GREY4 = "#c9cbcc", "#a4a7a9", "#6e7275", "#3e4245"
MARKER_RED = "#d8432c"      # the nose, and the way the robot drives
MARKER_ORANGE = "#f39a1e"   # a motion, or a copy
MARKER_YELLOW = "#ffd84a"   # a double or float note
MARKER_GREEN = "#8fd16a"    # a boolean note
MARKER_BLUE = "#7fb6f0"     # an object's note
PX = 0.72                   # points to a drawing pixel, at 100 drawing pixels to the inch


def rect(x, y, w, h):
    return [(x, y), (x + w, y), (x + w, y + h), (x, y + h)]


def ellipse(c, rx, ry=None, n=72):
    ry = rx if ry is None else ry
    return [(c[0] + rx * math.cos(2 * math.pi * i / n), c[1] + ry * math.sin(2 * math.pi * i / n))
            for i in range(n)]


def arc(c, r, a0, a1, n=24):
    """Points on a circle from a0 to a1 degrees, clockwise on the page as the angle grows."""
    return [(c[0] + r * math.cos(math.radians(a0 + (a1 - a0) * i / n)),
             c[1] + r * math.sin(math.radians(a0 + (a1 - a0) * i / n))) for i in range(n + 1)]


def quad(p0, c, p1, n=12):
    """Points along a quadratic curve from p0 to p1, p0 left out."""
    return [((1 - t) ** 2 * p0[0] + 2 * (1 - t) * t * c[0] + t * t * p1[0],
             (1 - t) ** 2 * p0[1] + 2 * (1 - t) * t * c[1] + t * t * p1[1])
            for t in (i / n for i in range(1, n + 1))]


def _side(a, b, p):
    return (b[0] - a[0]) * (p[1] - a[1]) - (b[1] - a[1]) * (p[0] - a[0])


def clip_convex(poly, window):
    """The part of poly inside the convex polygon window (Sutherland and Hodgman)."""
    area = sum(p[0] * q[1] - q[0] * p[1] for p, q in zip(window, window[1:] + window[:1]))
    sign = 1 if area > 0 else -1
    out = list(poly)
    for a, b in zip(window, window[1:] + window[:1]):
        given, out = out, []
        for p, q in zip(given[-1:] + given[:-1], given):
            dp, dq = sign * _side(a, b, p), sign * _side(a, b, q)
            if (dp >= 0) != (dq >= 0):
                t = dp / (dp - dq)
                out.append((p[0] + (q[0] - p[0]) * t, p[1] + (q[1] - p[1]) * t))
            if dq >= 0:
                out.append(q)
        if not out:
            break
    return out


def blur(image, sigma):
    """A Gaussian blur of an RGBA image, padded by three sigma all round."""
    r = int(3 * sigma) + 1
    k = np.exp(-0.5 * (np.arange(-r, r + 1) / sigma) ** 2)
    k /= k.sum()
    image = np.pad(image, ((r, r), (r, r), (0, 0)))
    for axis in (0, 1):
        pad = [(0, 0)] * 3
        pad[axis] = (r, r)
        image = np.lib.stride_tricks.sliding_window_view(np.pad(image, pad), 2 * r + 1,
                                                         axis=axis) @ k
    return image, r


class Soft:
    """An agg_filter that blurs what it is given by sigma drawing pixels."""

    def __init__(self, sigma):
        self.sigma = sigma

    def __call__(self, image, dpi):
        out, r = blur(image, self.sigma * dpi / 100)
        return out, -r, -r


class Sketch:
    """A drawing w by h drawing pixels, y counting down the page as in an SVG."""

    def __init__(self, w, h, seed):
        self.r = random.Random(seed)
        self.fig = plt.figure(figsize=(w / 100, h / 100), dpi=100)
        self.ax = self.fig.add_axes((0, 0, 1, 1))
        self.ax.set_xlim(0, w)
        self.ax.set_ylim(h, 0)
        self.ax.axis("off")
        self.hand = FontProperties(fname=HAND_FONT)
        self.code = FontProperties(fname=CODE_FONT)
        self.turn = (0.0, 0.0, 0.0)   # the center and degrees of the note being drawn
        self.z = 0

    def j(self, a):
        return self.r.uniform(-a, a)

    def at(self, p):
        cx, cy, deg = self.turn
        if not deg:
            return p
        c, s = math.cos(math.radians(deg)), math.sin(math.radians(deg))
        x, y = p[0] - cx, p[1] - cy
        return (cx + x * c - y * s, cy + x * s + y * c)

    def add(self, artist):
        self.z += 1
        artist.set_zorder(self.z)
        self.ax.add_artist(artist)
        return artist

    def clip(self, shape):
        return Polygon([self.at(p) for p in shape], closed=True, transform=self.ax.transData)

    def line(self, pts, color=PEN, width=2.6, alpha=1.0, fill=None, fill_alpha=1.0, closed=False):
        return self.add(Polygon([self.at(p) for p in pts], closed=closed,
                                facecolor=to_rgba(fill, fill_alpha) if fill else "none",
                                edgecolor=to_rgba(color, alpha) if color else "none",
                                linewidth=width * PX, capstyle="round", joinstyle="round"))

    # -- ink: a line drawn fast, running past its ends, a little bowed
    def ink_line(self, p0, p1, width=2.6, over=10, passes=2, color=PEN, wobble=1.6):
        (x0, y0), (x1, y1) = p0, p1
        n = math.hypot(x1 - x0, y1 - y0) or 1
        ux, uy = (x1 - x0) / n, (y1 - y0) / n
        for k in range(passes):
            o0, o1 = over * self.r.uniform(0.3, 1.0), over * self.r.uniform(0.3, 1.0)
            a = (x0 - ux * o0 + self.j(wobble), y0 - uy * o0 + self.j(wobble))
            b = (x1 + ux * o1 + self.j(wobble), y1 + uy * o1 + self.j(wobble))
            bow = self.j(wobble * 1.5)
            m = ((a[0] + b[0]) / 2 - uy * bow, (a[1] + b[1]) / 2 + ux * bow)
            self.line([a] + quad(a, m, b), color, width * (1.0 if k == 0 else 0.55),
                      0.92 if k == 0 else 0.6)

    def ink_poly(self, pts, **kw):
        for a, b in zip(pts, pts[1:] + pts[:1]):
            self.ink_line(a, b, **kw)

    def ink_circle(self, c, rad, width=2.6, passes=2):
        for k in range(passes):
            start = self.r.uniform(0, 2 * math.pi)
            sweep = 2 * math.pi + self.r.uniform(0.15, 0.45)
            pts = []
            for i in range(73):
                t = start + sweep * i / 72
                rr = rad + self.j(1.2) + k * 1.5
                pts.append((c[0] + rr * math.cos(t), c[1] + rr * math.sin(t)))
            self.line(pts, PEN, width * (1.0 if k == 0 else 0.5), 0.9 if k == 0 else 0.55)

    def construct(self, p0, p1):
        self.ink_line(p0, p1, width=0.9, over=40, passes=1, color=CONSTRUCT, wobble=0.5)

    # -- marker: parallel broad strokes laid inside a shape, overlapping where they cross
    def marker(self, shape, color, angle=-28, nib=16, opacity=0.55, layers=2, gap=0.8,
               within=None):
        if within:
            shape = clip_convex(shape, within)
        if len(shape) < 3:
            return
        xs, ys = [p[0] for p in shape], [p[1] for p in shape]
        cx, cy = (min(xs) + max(xs)) / 2, (min(ys) + max(ys)) / 2
        reach = math.hypot(max(xs) - min(xs), max(ys) - min(ys)) / 2 + nib
        a = math.radians(angle)
        strokes, widths, colors = [], [], []
        for _ in range(layers):
            ux, uy = math.cos(a), math.sin(a)
            nx, ny = -uy, ux
            s = -reach + self.j(nib / 2)
            while s < reach:
                n = reach * self.r.uniform(0.85, 1.05)
                px, py = cx + nx * s, cy + ny * s
                strokes.append([self.at((px - ux * n, py - uy * n)),
                                self.at((px + ux * n, py + uy * n))])
                widths.append(nib * self.r.uniform(0.9, 1.1) * PX)
                colors.append(to_rgba(color, opacity * self.r.uniform(0.8, 1.0)))
                s += nib * gap * self.r.uniform(0.85, 1.15)
            a += math.radians(4)
        lines = LineCollection(strokes, linewidths=widths, colors=colors, capstyle="butt")
        lines.set_clip_path(self.clip(shape))
        self.add(lines)

    def shade(self, shape, color=GREY3, angle=-28, nib=14, within=None):
        """A darker pass over one side of a shape, the shadow side."""
        self.marker(shape, color, angle=angle, nib=nib, opacity=0.45, layers=1, gap=1.1,
                    within=within)

    def shadow(self, shape, sigma=6, alpha=0.32):
        """A soft grey shadow."""
        self.line(shape, None, fill="#3a3a3a", fill_alpha=alpha, closed=True).set_agg_filter(
            Soft(sigma))

    def highlight(self, p0, p1, width=5):
        self.ink_line(p0, p1, width=width, over=-12, passes=1, color="#ffffff", wobble=0.8)

    def text(self, xy, s, size=26, color=PEN, anchor="middle", rot=0, code=False):
        x, y = self.at(xy)
        self.z += 1
        self.ax.text(x, y, s, fontproperties=self.code if code else self.hand, fontsize=size * PX,
                     color=color, ha={"middle": "center", "start": "left", "end": "right"}[anchor],
                     va="baseline", rotation=-(rot + self.turn[2]), rotation_mode="anchor",
                     zorder=self.z)

    def lines(self, xy, rows, size=26, gap=1.25, **kw):
        """Text a row at a time, each its own baseline, gap sizes apart."""
        for i, row in enumerate(rows):
            self.text((xy[0], xy[1] + i * size * gap), row, size, **kw)

    def marker_line(self, p0, p1, color, nib=9, bend=0.0):
        """A broad marker stroke with an ink line along it; gives back its curve."""
        (x0, y0), (x1, y1) = p0, p1
        n = math.hypot(x1 - x0, y1 - y0)
        ux, uy = (x1 - x0) / n, (y1 - y0) / n
        c = ((x0 + x1) / 2 - uy * bend * n, (y0 + y1) / 2 + ux * bend * n)
        pts = [p0] + quad(p0, c, p1, 24)
        self.line(pts, color, nib, 0.85)
        self.line(pts, PEN, 1.6, 0.8)
        return c

    def marker_arrow(self, p0, p1, color, nib=9, bend=0.0):
        """A marker stroke with an ink edge and an inked head."""
        c = self.marker_line(p0, p1, color, nib, bend)
        x1, y1 = p1
        tx, ty = x1 - c[0], y1 - c[1]
        tl = math.hypot(tx, ty)
        tx, ty = tx / tl, ty / tl
        h = nib * 2.6
        left = (x1 - tx * h - ty * h * 0.6, y1 - ty * h + tx * h * 0.6)
        right = (x1 - tx * h + ty * h * 0.6, y1 - ty * h - tx * h * 0.6)
        self.line([left, (x1 + tx * 4, y1 + ty * 4), right], PEN, 2.2, 0.9, fill=color,
                  closed=True)

    def dot(self, c, r=6, color=PEN):
        self.line(ellipse(c, r, n=24), None, fill=color, closed=True)

    def save(self, name):
        OUT.mkdir(exist_ok=True)
        self.fig.savefig(OUT / f"{name}.png", dpi=DPI, facecolor=PAPER)
        plt.close(self.fig)


# -- the robot's parts, drawn from above with the nose up the page

def frame_top(s, x0, y0, w, h, nose=True):
    k = w / 400
    s.shadow(rect(x0 + 10 * k, y0 + 14 * k, w, h), sigma=10 * k, alpha=0.25)
    s.marker(rect(x0, y0, w, h), GREY1, nib=24 * k)
    s.shade(rect(x0 + w * 0.6, y0, w * 0.4, h), nib=14 * k)
    s.highlight((x0 + 14 * k, y0 + 16 * k), (x0 + 14 * k, y0 + h - 30 * k))
    s.highlight((x0 + 20 * k, y0 + 14 * k), (x0 + w - 40 * k, y0 + 14 * k))
    s.ink_poly(rect(x0, y0, w, h))
    if nose:
        cx = x0 + w / 2
        tip = y0 - 70 * k
        tri = [(cx - 55 * k, y0), (cx, tip), (cx + 55 * k, y0)]
        s.marker(tri, MARKER_RED, nib=14 * k, opacity=0.8)
        s.ink_poly(tri, width=2.2)
        s.text((cx, tip - 20 * k), "nose", 32 * min(1.0, k * 1.4), MARKER_RED)


def wheel_top(s, cx, cy, ww, wh):
    x, y = cx - ww / 2, cy - wh / 2
    s.marker(rect(x, y, ww, wh), GREY3, nib=14 * ww / 66, opacity=0.65)
    s.shade(rect(x + ww * 0.55, y, ww * 0.45, wh), color=GREY4, nib=14 * ww / 66)
    for i in range(1, 6):      # tread
        s.ink_line((x + 8, y + i * wh / 6), (x + ww - 8, y + i * wh / 6 - 10 * ww / 66),
                   width=1.2, over=2, passes=1)
    s.highlight((x + 10, y + 12), (x + 10, y + wh - 16), width=4)
    s.ink_poly(rect(x, y, ww, wh), width=2.2, over=7)


def robot_inside(s, cx, cy, side, nose=True):
    """A square frame with a wheel inside each corner, the wheel centers on a square, so the
    diagonals through them cross at 45 degrees. Gives back each wheel's center."""
    frame_top(s, cx - side / 2, cy - side / 2, side, side, nose)
    d = side * 0.335
    centers = {"front left": (cx - d, cy - d), "front right": (cx + d, cy - d),
               "back left": (cx - d, cy + d), "back right": (cx + d, cy + d)}
    for c in centers.values():
        wheel_top(s, c[0], c[1], side * 0.15, side * 0.32)
    return centers


def gamepad_outline(cx, cy, w, h):
    """A gamepad from above: a flat top with two grips hanging below."""
    tl, tr = (cx - 0.42 * w, cy - 0.5 * h), (cx + 0.42 * w, cy - 0.5 * h)
    rg, rn = (cx + 0.36 * w, cy + 0.5 * h), (cx + 0.14 * w, cy + 0.1 * h)
    ln, lg = (cx - 0.14 * w, cy + 0.1 * h), (cx - 0.36 * w, cy + 0.5 * h)
    return ([tl] + quad(tl, (cx, cy - 0.58 * h), tr) + quad(tr, (cx + 0.6 * w, cy), rg)
            + quad(rg, (cx + 0.2 * w, cy + 0.62 * h), rn) + quad(rn, (cx, cy), ln)
            + quad(ln, (cx - 0.2 * w, cy + 0.62 * h), lg) + quad(lg, (cx - 0.6 * w, cy), tl))


def gamepad(s, cx, cy, w, h):
    shape = gamepad_outline(cx, cy, w, h)
    s.shadow([(x + 0.02 * w, y + 0.05 * h) for x, y in shape], sigma=0.02 * w)
    s.marker(shape, GREY3, nib=0.03 * w, opacity=0.65)
    s.shade(shape, color=GREY4, nib=0.025 * w, within=rect(cx - 0.6 * w, cy, 1.2 * w, h))
    s.highlight((cx - 0.36 * w, cy - 0.44 * h), (cx + 0.15 * w, cy - 0.44 * h), width=4)
    s.line(shape, PEN, 2.4, 0.9, closed=True)


# -- sticky notes and labels, for a variable and its value

def note_shape(x, y, w, h, lift):
    """A note whose bottom-right corner lifts off the page, its bottom edge bowed."""
    a, b = (x + w, y + h - lift), (x + w - lift, y + h + 2)
    return ([(x, y), (x + w, y), a] + quad(a, (x + w - lift * 0.15, y + h - lift * 0.25), b)
            + quad(b, (x + w * 0.5, y + h + 7), (x, y + h)))


def curl(s, shape, x, y, w, h, bands=16):
    """Darker toward the bottom-right corner, from halfway down the diagonal."""
    for i in range(bands):
        c0, c1 = 2 * (0.55 + 0.45 * i / bands), 2 * (0.55 + 0.45 * (i + 1) / bands)
        strip = [(x + (c0 + 1) * w, y - h), (x - w, y + (c0 + 1) * h),
                 (x - w, y + (c1 + 1) * h), (x + (c1 + 1) * w, y - h)]
        band = s.line(strip, None, fill="#000000", fill_alpha=0.22 * (i + 0.5) / bands,
                      closed=True)
        band.set_clip_path(s.clip(shape))


def sticky(s, x, y, w, h, color, type_name, value=None, tilt=-2.0, lift=34, value_size=54):
    s.turn = (x + w / 2, y + h / 2, tilt)
    a, b = (x + w + 14, y + h - lift + 14), (x + w - lift + 10, y + h + 24)
    s.shadow([(x + 4, y + 8), (x + w + 4, y + 8), a] + quad(a, (x + w + 10, y + h + 22), b)
             + quad(b, (x + w * 0.5, y + h + 18), (x + 6, y + h + 8)))
    shape = note_shape(x, y, w, h, lift)
    s.line(shape, None, fill=color, fill_alpha=0.55, closed=True)
    s.marker(shape, color, nib=18, opacity=0.6)
    s.marker(shape, "#000000", nib=10, opacity=0.06, layers=1, within=rect(x, y, w, h * 0.2))
    curl(s, shape, x, y, w, h)
    a, b = (x + w, y + h - lift), (x + w - lift, y + h + 2)
    under = [a] + quad(a, (x + w - lift * 0.15, y + h - lift * 0.25), b) + quad(
        b, (x + w - lift * 0.55, y + h - lift * 0.55), a)
    s.line(under, PEN, 1.4, 0.7, fill="#fffbe8", closed=True)
    s.highlight((x + 12, y + 10), (x + w * 0.7, y + 10), width=4)
    e = (x + w - 6, y + h * 0.35)
    s.line([e] + quad(e, (x + w - 8, y + h - lift * 1.2), (x + w - lift * 0.6, y + h - lift * 0.6)),
           "#ffffff", 4, 0.7)
    s.line(shape, PEN, 2.2, 0.85, closed=True)
    s.ink_line((x, y), (x + w, y), width=1.2, over=6, passes=1)
    s.text((x + 14, y + 30), type_name, 22, anchor="start", code=True)
    if value is not None:
        s.text((x + w * 0.46, y + h * 0.5 + value_size * 0.45), value, value_size)
    s.turn = (0.0, 0.0, 0.0)


def label(s, x, y, name, tilt=0.0, size=28):
    """A name: a white label a little curled at its right end. Gives back the dot its arrow
    leaves from."""
    w, h = 52 + len(name) * size * 0.8 * 0.61, size * 1.7
    s.turn = (x + w / 2, y + h / 2, tilt)
    a, b, c = (x + w - 10, y), (x + w - 4, y + h), (x, y + h)
    shape = [(x, y), a] + quad(a, (x + w + 4, y + h * 0.5), b) + [c] + quad(
        c, (x - 3, y + h * 0.5), (x, y))
    s.shadow([(px + 5, py + 8) for px, py in shape])
    s.line(shape, None, fill="#ffffff", closed=True)
    s.marker(shape, GREY1, nib=10, opacity=0.35, layers=1, within=rect(x + w * 0.72, y, w, h))
    s.line(shape, PEN, 2.2, 0.9, closed=True)
    s.text((x + 12, y + h * 0.68), name, size * 0.8, anchor="start", code=True)
    s.turn = (0.0, 0.0, 0.0)
    return (x + w - 16, y + h / 2)


def dot_arrow(s, start, end, bend=0.0):
    s.dot(start)
    s.marker_arrow(start, end, PEN, nib=3, bend=bend)


# ---------------------------------------------------------------- the pictures

def wheel_names():
    s = Sketch(900, 940, 11)
    x0, y0, w, h = 250, 240, 400, 440
    s.construct((x0 - 140, y0), (x0 + w + 140, y0))
    s.construct((x0 - 140, y0 + h), (x0 + w + 140, y0 + h))
    s.construct((450, 100), (450, 800))
    frame_top(s, x0, y0, w, h)
    wheels = {"front left": (x0 - 72, y0 + 10, -3), "front right": (x0 + w + 6, y0 + 10, 3),
              "back left": (x0 - 72, y0 + h - 160, -3),
              "back right": (x0 + w + 6, y0 + h - 160, 3)}
    for name, (wx, wy, rot) in wheels.items():
        wheel_top(s, wx + 33, wy + 75, 66, 150)
        ty = wy - 26 if wy < 400 else wy + 150 + 48
        s.text((wx + 33, ty), name, 30, rot=rot)
    s.save("fig-wheel-names")


def wheel_forward():
    s = Sketch(1200, 600, 7)
    s.construct((40, 520), (1160, 520))      # the ground
    s.construct((640, 140), (640, 560))      # the axle, up and down
    s.construct((80, 400), (1000, 400))      # the axle, along
    bx, by, bw, bh = 110, 250, 820, 150
    s.marker(ellipse((560, 528), 440, 14), GREY2, nib=12, opacity=0.5, layers=1)
    s.marker(rect(bx, by, bw, bh), GREY1, nib=22)
    s.shade(rect(bx, by + bh * 0.62, bw, bh * 0.38))
    s.highlight((bx + 20, by + 12), (bx + bw - 30, by + 12))
    s.ink_poly(rect(bx, by, bw, bh))
    nose = [(930, 280), (1010, 325), (930, 370)]
    s.marker(nose, MARKER_RED, nib=14, opacity=0.8)
    s.ink_poly(nose, width=2.2)
    s.text((1030, 336), "nose", 30, MARKER_RED, "start")
    c, rad = (640, 400), 118
    s.marker(ellipse(c, rad), GREY3, nib=20, opacity=0.6)
    s.shade(ellipse((c[0] + 45, c[1] + 45), rad), color=GREY4, within=ellipse(c, rad))
    s.marker(ellipse(c, 38), GREY1, nib=10, opacity=0.8)
    s.ink_circle(c, rad)
    s.ink_circle(c, 38, width=1.8)
    s.line(arc(c, 100, 225, 276), "#ffffff", 6, 0.85)
    s.marker_arrow((557, 281), (723, 281), MARKER_ORANGE, nib=11, bend=-0.3)
    s.lines((780, 150), ["the top of the tire", "moves toward the nose"], 28, gap=1.3,
            anchor="start")
    s.construct((770, 196), (720, 250))
    s.marker_arrow((160, 150), (560, 150), MARKER_RED, nib=12)
    s.text((360, 118), "robot driving forward", 30, MARKER_RED)
    s.save("fig-wheel-forward")


def mecanum_x():
    s = Sketch(900, 960, 3)
    cx, cy, side = 450, 500, 520
    centers = robot_inside(s, cx, cy, side)
    fl, fr, bl, br = (centers[k] for k in ("front left", "front right", "back left", "back right"))
    s.construct(fl, br)
    s.construct(fr, bl)
    for name, (wx, wy) in centers.items():
        # The top roller, at 45 degrees, lies along the line through the robot's middle.
        dx, dy = (cx - wx) / abs(cx - wx), (cy - wy) / abs(cy - wy)
        n = 44
        s.marker_line((wx - dx * n, wy - dy * n), (wx + dx * n, wy + dy * n), MARKER_ORANGE, nib=12)
        top = wy < cy
        s.text((wx, cy - side / 2 - 24 if top else cy + side / 2 + 46), name, 30)
    s.text((cx, 920), "the top roller of each wheel, at 45 degrees: together they make an X", 28)
    s.save("fig-mecanum-x")


def wheel_pushes():
    # L090's four lines: fl = f - s - t, fr = f + s + t, bl = f + s - t, br = f - s + t.
    # Spinning forward, front left and back right push forward and right, front right and
    # back left push forward and left; spinning backward, each pushes the other way.
    # Up the page is forward, so a push forward is a negative y.
    pushes = {"front left": (1, -1), "front right": (-1, -1), "back left": (-1, -1),
              "back right": (1, -1)}
    motions = [("driving forward", 1, 0, 0), ("sliding left", 0, 1, 0),
               ("turning left", 0, 0, 1)]
    s = Sketch(1800, 840, 5)
    side = 360
    for i, (title, f, sl, t) in enumerate(motions):
        cx, cy = 300 + 600 * i, 450
        s.text((cx, 80), title, 36)
        centers = robot_inside(s, cx, cy, side)
        power = {"front left": f - sl - t, "front right": f + sl + t, "back left": f + sl - t,
                 "back right": f - sl + t}
        for name, (wx, wy) in centers.items():
            p = power[name]
            s.marker_arrow((wx, wy + 48 * p), (wx, wy - 48 * p), MARKER_BLUE, nib=7)
            ux, uy = (v / math.sqrt(2) * p for v in pushes[name])
            ox = wx + (1 if wx > cx else -1) * 120
            s.marker_arrow((ox - 40 * ux, wy - 40 * uy), (ox + 40 * ux, wy + 40 * uy), MARKER_RED,
                           nib=7)
        if t:
            # Along the bottom, left to right: counter-clockwise seen from above.
            s.marker_arrow((cx - 60, cy + 20), (cx + 60, cy + 20), MARKER_GREEN, nib=10, bend=0.5)
        else:
            s.marker_arrow((cx + 70 * sl, cy + 70 * f), (cx - 70 * sl, cy - 70 * f), MARKER_GREEN,
                           nib=10)
    s.text((900, 800), "blue: which way each wheel spins      red: which way it pushes the robot"
           "      green: what the four pushes add up to", 28)
    s.save("fig-l090-wheel-pushes")


def wheel_handover():
    s = Sketch(1300, 640, 9)

    def card(x, y, w, h, color, rows):
        s.shadow(rect(x + 8, y + 12, w, h), sigma=8, alpha=0.25)
        s.marker(rect(x, y, w, h), color, nib=18, opacity=0.4)
        s.highlight((x + 14, y + 12), (x + w - 30, y + 12), width=4)
        s.ink_poly(rect(x, y, w, h), width=2.2)
        top = y + h / 2 - (len(rows) - 1) * 17 + 9
        for i, (row, code) in enumerate(rows):
            s.text((x + w / 2, top + 34 * i), row, 22 if code else 28, code=code)

    card(60, 90, 340, 140, MARKER_BLUE, [("Path follower", False), ("drive", True)])
    card(60, 380, 340, 170, MARKER_ORANGE, [("Driver's sticks", False), ("sticks, then", True),
                                            ("setCommandedWheels", True)])
    card(1000, 230, 240, 160, GREY1, [("four wheels", False)])
    pivot, up, down = (780, 310), (640, 160), (640, 465)
    s.marker_arrow((400, 160), (628, 160), MARKER_BLUE, nib=9)
    s.marker_arrow((400, 465), (628, 465), MARKER_RED, nib=9)
    for c in (up, down):
        s.line(ellipse(c, 11, n=24), PEN, 2.2, 0.9, fill=PAPER, closed=True)
    # The switch: commanded wheels set, so the sticks get through.
    s.marker_line(pivot, (down[0] + 8, down[1] - 8), MARKER_RED, nib=14)
    s.dot(pivot, 13)
    s.marker_arrow((pivot[0] + 14, pivot[1]), (990, 310), MARKER_RED, nib=9)
    s.text((780, 60), "commanded wheels set?", 30)
    s.lines((780, 560), ["set: the sticks get through", "released: the follower does"], 26)
    s.save("fig-wheel-handover")


def odometry_step():
    # One loop's wheel travels, in inches, and a heading of 30 degrees.
    fl, fr, bl, br = 1.0, 2.0, 2.0, 1.0
    fwd = (fl + fr + bl + br) / 4
    lft = (-fl + fr + bl - br) / 4
    h = math.radians(30)
    dx = fwd * math.cos(h) - lft * math.sin(h)
    dy = fwd * math.sin(h) + lft * math.cos(h)
    s = Sketch(2000, 640, 13)
    titles = ["1. Each wheel's travel", "2. What they add up to", "3. Turned by the heading",
              "4. Added to the old estimate"]
    for i, title in enumerate(titles):
        s.text((250 + 500 * i, 60), title, 30)

    cx, cy = 250, 350
    centers = robot_inside(s, cx, cy, 220, nose=True)
    for (name, v), (wx, wy) in zip([("FL", fl), ("FR", fr), ("BL", bl), ("BR", br)],
                                   centers.values()):
        s.marker_arrow((wx, wy + 30 * v), (wx, wy - 30 * v), MARKER_BLUE, nib=7)
        right = wx > cx
        s.lines((wx + (1 if right else -1) * 62, wy - 6), [name, f"{v:.1f} in"], 22,
                anchor="start" if right else "end")

    cx = 750
    robot_inside(s, cx, cy, 220, nose=True)
    scale = 80                      # drawing pixels to the inch
    s.marker_arrow((cx, cy), (cx, cy - fwd * scale), MARKER_GREEN, nib=9)
    s.marker_arrow((cx, cy), (cx - lft * scale, cy), MARKER_RED, nib=9)
    s.text((cx, 150), f"forward (FL+FR+BL+BR)/4 = {fwd:.1f} in", 18, code=True)
    s.text((cx, 530), f"strafe (-FL+FR+BL-BR)/4 = {lft:.1f} in", 18, code=True)

    o = (1110, 500)
    scale = 170
    s.marker_arrow(o, (o[0] + 300, o[1]), GREY3, nib=4)
    s.marker_arrow(o, (o[0], o[1] - 300), GREY3, nib=4)
    s.text((o[0] + 300, o[1] + 34), "field x", 22)
    s.text((o[0] - 14, o[1] - 310), "field y", 22, anchor="end")
    fw = (math.cos(h) * fwd * scale, -math.sin(h) * fwd * scale)
    lf = (-math.sin(h) * lft * scale, -math.cos(h) * lft * scale)
    tip = (o[0] + fw[0], o[1] + fw[1])
    end = (o[0] + dx * scale, o[1] - dy * scale)
    s.marker_arrow(o, tip, MARKER_GREEN, nib=9)
    s.marker_arrow(tip, end, MARKER_RED, nib=9)
    s.marker_arrow(o, end, PEN, nib=3)
    s.line(arc(o, 90, -30, 0), CONSTRUCT, 1.6)
    s.text((o[0] + 100, o[1] - 14), "heading 30 degrees", 20, anchor="start")
    s.text((end[0] + 10, end[1] - 16), f"({dx:.2f}, {dy:.2f}) in", 18, anchor="start", code=True)

    old = (1640, 440)
    scale = 130
    new = (old[0] + dx * scale, old[1] - dy * scale)
    s.marker_arrow(old, new, PEN, nib=3)
    s.dot(old, 12, GREY3)
    s.dot(new, 12, MARKER_BLUE)
    s.text((old[0] - 10, old[1] + 50), "old estimate", 24, anchor="start")
    s.text((old[0] - 10, old[1] + 80), "(10.00, 20.00) in", 18, anchor="start", code=True)
    s.text((new[0] - 60, new[1] - 70), "new estimate", 24, anchor="start")
    s.text((new[0] - 60, new[1] - 40), f"({10 + dx:.2f}, {20 + dy:.2f}) in", 18, anchor="start",
           code=True)
    s.save("fig-odometry-step")


def field_relative():
    s = Sketch(1000, 940, 17)
    fx, fy, fs = 130, 90, 760
    s.shadow(rect(fx + 10, fy + 14, fs, fs), sigma=10, alpha=0.25)
    s.marker(rect(fx, fy, fs, fs), GREY1, nib=30, opacity=0.35)
    s.highlight((fx + 20, fy + 16), (fx + fs - 40, fy + 16))
    s.ink_poly(rect(fx, fy, fs, fs))
    s.text((70, fy + fs / 2), "driver", 32, rot=-90)
    s.dot((100, fy + fs / 2), 10)
    # As AdvantageScope's 2D Field draws it: field x to the right, away from the driver; y up.
    o = (fx + 50, fy + fs - 50)
    s.marker_arrow(o, (o[0] + 130, o[1]), GREY3, nib=4)
    s.marker_arrow(o, (o[0], o[1] - 130), GREY3, nib=4)
    s.text((o[0] + 140, o[1] + 8), "field x", 22, anchor="start")
    s.text((o[0], o[1] - 150), "field y", 22)
    # The stick, pushed away from the driver, drawn on the field.
    s.marker_arrow((200, 210), (440, 210), MARKER_BLUE, nib=12)
    s.text((200, 176), "stick: away from the driver", 28, anchor="start")
    # The robot at heading 90 degrees faces field y, up the page.
    c = (400, 500)
    frame_top(s, c[0] - 100, c[1] - 100, 200, 200)
    s.marker_arrow(c, (640, c[1]), MARKER_BLUE, nib=12)
    s.lines((560, 570), ["the same arrow on the robot:", "forward 0.0,",
                         "sideways to its right 1.0"], 26, anchor="start")
    s.lines((c[0], 680), ["robot facing up the field", "(heading 90 degrees)"], 26)
    s.save("fig-field-relative")


def robot_front():
    s = Sketch(1300, 620, 19)
    ground = 520
    s.construct((40, ground), (1260, ground))
    s.construct((400, 150), (400, 580))       # the robot's center line
    s.marker(ellipse((400, ground + 6), 300, 14), GREY2, nib=12, opacity=0.5, layers=1)
    bx, by, bw, bh = 150, 240, 500, 200
    s.marker(rect(bx, by, bw, bh), GREY1, nib=22)
    s.shade(rect(bx + bw * 0.62, by, bw * 0.38, bh))
    s.highlight((bx + 20, by + 12), (bx + bw - 30, by + 12))
    s.ink_poly(rect(bx, by, bw, bh))
    for wx in (175, 555):
        s.marker(rect(wx, 400, 70, ground - 400), GREY3, nib=14, opacity=0.65)
        s.shade(rect(wx + 38, 400, 32, ground - 400), color=GREY4)
        s.ink_poly(rect(wx, 400, 70, ground - 400), width=2.2, over=6)
    hub = rect(330, 290, 140, 90)
    s.marker(hub, "#d9dee0", nib=12, opacity=0.9)
    s.ink_poly(hub, width=2.0, over=5)
    s.text((400, 342), "Control Hub", 22)
    s.text((400, 200), "the robot, from the front", 30)
    for gx, name in ((880, "gamepad 1"), (1130, "gamepad 2")):
        gamepad(s, gx, ground - 50, 200, 90)
        s.text((gx, ground - 120), name, 28)
    s.save("fig-robot-front")


def gamepad_sticks():
    s = Sketch(1100, 760, 23)
    cx, cy = 550, 390
    gamepad(s, cx, cy, 820, 430)
    for x, name, side in ((370, "left stick", -1), (630, "right stick", 1)):
        y = 360
        s.marker(ellipse((x, y), 62), GREY4, nib=14, opacity=0.8)
        s.marker(ellipse((x, y), 32), GREY2, nib=10, opacity=0.9)
        s.ink_circle((x, y), 62, width=2.2)
        s.ink_circle((x, y), 32, width=1.6)
        s.text((x, y - 84), name, 28, PAPER)
        s.marker_arrow((x + side * 95, y - 50), (x + side * 95, y + 60), MARKER_GREEN, nib=7)
        s.lines((x + side * 112, y - 4), ["pulled back", "+1.0"], 22, color=PAPER,
                anchor="start" if side > 0 else "end")
    s.marker(ellipse((820, 260), 30), MARKER_GREEN, nib=10, opacity=0.9)
    s.ink_circle((820, 260), 30, width=2.0)
    s.text((820, 270), "A", 30)
    s.text((820, 214), "A button", 26, PAPER)
    s.text((cx, 80), "a stick pushed away from the driver reads -1.0, pulled back +1.0", 28)
    s.text((cx, 720), "the driver holds it from this side", 28)
    s.save("fig-gamepad-sticks")


def pinpoint_mounting():
    s = Sketch(900, 960, 29)
    cx, cy, side = 450, 520, 560
    s.construct((cx, cy - side / 2 - 40), (cx, cy + side / 2 + 40))
    s.construct((cx - side / 2 - 40, cy), (cx + side / 2 + 40, cy))
    frame_top(s, cx - side / 2, cy - side / 2, side, side)
    board = rect(cx - 62, cy - 62, 124, 124)
    s.marker(board, "#d9dee0", nib=12, opacity=0.95)
    s.ink_poly(board, width=2.2, over=6)
    s.text((cx, cy - 4), "Pinpoint", 24)
    s.lines((cx, cy + 24), ["sticker", "side up"], 16)
    wheel_top(s, cx - 180, cy - 30, 44, 110)          # rolls along the robot: forward
    s.lines((cx - 180, cy - 150), ["forward pod", "rolls along"], 24)
    s.text((cx - 180, cy + 70), "x socket", 22, code=False)
    s.marker_arrow((cx - 154, cy - 30), (cx - 70, cy - 30), MARKER_BLUE, nib=6)
    s.turn = (cx + 60, cy + 180, 90)                  # rolls across the robot: strafe
    wheel_top(s, cx + 60, cy + 180, 44, 110)
    s.turn = (0.0, 0.0, 0.0)
    s.lines((cx + 60, cy + 240), ["strafe pod rolls across", "y socket"], 24)
    s.marker_arrow((cx + 30, cy + 154), (cx + 30, cy + 70), MARKER_BLUE, nib=6)
    s.save("fig-pinpoint-mounting")


def l020_sticky():
    s = Sketch(1400, 880, 31)
    s.text((700, 64), "double leftStickY = gamepad1.left_stick_y;", 30, code=True)
    sticky(s, 390, 130, 500, 400, MARKER_BLUE, "Gamepad", tilt=-1.0, lift=40)
    dot_arrow(s, label(s, 110, 185, "gamepad1"), (384, 212))
    end = label(s, 412, 270, "left_stick_y", tilt=-1.0, size=26)
    sticky(s, 625, 335, 225, 160, MARKER_YELLOW, "float", "-0.7", tilt=1.5, lift=26,
           value_size=48)
    dot_arrow(s, end, (662, 332), bend=-0.3)
    sticky(s, 450, 600, 260, 200, MARKER_YELLOW, "double", "-0.7", tilt=-1.5)
    dot_arrow(s, label(s, 110, 665, "leftStickY"), (444, 698))
    s.marker_arrow((842, 470), (716, 668), MARKER_ORANGE, nib=9, bend=-0.3)
    s.lines((900, 640), ["= copies the value", "onto leftStickY's note"], 30, anchor="start")
    s.save("fig-l020-sticky")


def l040_sticky():
    s = Sketch(1500, 900, 37)
    s.lines((750, 60), ["hardware.frontLeft.setPower(leftSpeed);",
                        "hardware.backLeft.setPower(leftSpeed);"], 26, gap=1.4, code=True)
    sticky(s, 330, 170, 600, 560, MARKER_BLUE, "RobotHardware", tilt=-1.0, lift=44)
    dot_arrow(s, label(s, 60, 230, "hardware"), (324, 258))
    for i, (name, words) in enumerate((("frontLeft", "front left"), ("backLeft", "back left"))):
        y = 250 + 220 * i
        end = label(s, 352, y + 30, name, size=24)
        sticky(s, 610, y, 270, 160, MARKER_BLUE, "DcMotorEx", words, tilt=1.0 - 2.0 * i,
               lift=26, value_size=34)
        dot_arrow(s, end, (604, y + 52))
    s.text((370, 690), "and frontRight, backRight", 26, anchor="start")
    sticky(s, 1110, 420, 240, 180, MARKER_YELLOW, "double", "0.7", tilt=1.5)
    end = label(s, 1110, 300, "leftSpeed")
    dot_arrow(s, end, (end[0], 414))
    s.marker_arrow((1104, 470), (886, 330), MARKER_ORANGE, nib=9, bend=0.15)
    s.marker_arrow((1104, 520), (886, 550), MARKER_ORANGE, nib=9, bend=-0.1)
    s.lines((1000, 700), ["setPower hands 0.7", "to each motor"], 30, anchor="start")
    s.save("fig-l040-sticky")


PLOTS = [stick_shaping, speed_loop, panels_graph]
PICTURES = [mecanum_x, wheel_handover, odometry_step, field_relative, wheel_pushes, robot_front,
            gamepad_sticks, wheel_names, wheel_forward, pinpoint_mounting, l020_sticky, l040_sticky]

if __name__ == "__main__":
    for draw in PLOTS + PICTURES:
        draw()
    print(f"drew {len(PLOTS) + len(PICTURES)} figures into {OUT}")
