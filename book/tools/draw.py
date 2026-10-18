"""Status: controlling, for the figures it draws.

Draws the book's figures as PNG into a folder of figures, which is corbels-lfs-lessons' figures/
at the ref keep.toml's [media] names. Moved here from gh-pages' src/ on 2026-10-11.

    python tools/run.py draw DIR

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
- A method is a lavender box on its object's note, touching the note's left edge. What the FTC
  platform provides is drawn inside a grey box, labeled the FTC platform.
- Handwriting is Marker Felt and code is Menlo, both in macOS, so the drawings are drawn on a Mac.
  The script stops when either font is missing rather than draw in another.
"""

import math
import random
import sys
from pathlib import Path

import matplotlib

matplotlib.use("Agg")
from matplotlib.collections import LineCollection, PolyCollection  # noqa: E402
from matplotlib.colors import to_rgba  # noqa: E402
from matplotlib.font_manager import FontProperties  # noqa: E402
import matplotlib.pyplot as plt  # noqa: E402
from matplotlib.patches import Circle, FancyArrowPatch, FancyBboxPatch, PathPatch, Polygon  # noqa: E402,E501
from matplotlib.path import Path as MplPath  # noqa: E402
from matplotlib.textpath import TextPath  # noqa: E402
from matplotlib.transforms import Affine2D  # noqa: E402
import numpy as np  # noqa: E402

OUT = Path(sys.argv[1]) if len(sys.argv) > 1 else None  # set before drawing; see the end
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
MARKER_LAVENDER = "#b99be6"  # a method's box, on its object's note
MARKER_PINK = "#f4a6c4"     # a String note
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
    """An agg_filter that blurs what it is given by sigma drawing pixels, and fades it to alpha,
    so shapes drawn solid and overlapping make one even shadow."""

    def __init__(self, sigma, alpha=1.0):
        self.sigma, self.alpha = sigma, alpha

    def __call__(self, image, dpi):
        out, r = blur(image, self.sigma * dpi / 100)
        out[..., 3] *= self.alpha
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

    def cast(self, shapes, sigma=10, alpha=0.3):
        """One soft shadow made of many shapes."""
        if shapes:
            self.add(PolyCollection(shapes, facecolors="#3a3a3a", edgecolors="none")
                     ).set_agg_filter(Soft(sigma, alpha))

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


def gamepad(s, cx, cy, w, h, shine=0.44):
    shape = gamepad_outline(cx, cy, w, h)
    s.shadow([(x + 0.02 * w, y + 0.05 * h) for x, y in shape], sigma=0.02 * w)
    s.marker(shape, GREY3, nib=0.03 * w, opacity=0.65)
    s.shade(shape, color=GREY4, nib=0.025 * w, within=rect(cx - 0.6 * w, cy, 1.2 * w, h))
    s.highlight((cx - 0.36 * w, cy - shine * h), (cx + 0.15 * w, cy - shine * h), width=4)
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
    s = Sketch(1300, 720, 23)
    cx, cy = 650, 430  # low enough that the pulled-back labels clear the notch
    gamepad(s, cx, cy, 1000, 430, shine=0.475)
    for x, name, side in ((400, "left stick", 1), (900, "right stick", -1)):  # arrows between them
        y = 360
        s.marker(ellipse((x, y), 62), GREY4, nib=14, opacity=0.8)
        s.marker(ellipse((x, y), 32), GREY2, nib=10, opacity=0.9)
        s.ink_circle((x, y), 62, width=2.2)
        s.ink_circle((x, y), 32, width=1.6)
        s.text((x, y - 84), name, 28, PAPER)
        s.marker_arrow((x + side * 95, y - 8), (x + side * 95, y - 95), MARKER_GREEN, nib=7)
        s.lines((x + side * 112, y - 70), ["pushed away", "-1.0"], 22, color=PAPER,
                anchor="start" if side > 0 else "end")
        s.marker_arrow((x + side * 95, y + 8), (x + side * 95, y + 95), MARKER_GREEN, nib=7)
        s.lines((x + side * 112, y + 42), ["pulled back", "+1.0"], 22, color=PAPER,
                anchor="start" if side > 0 else "end")
    s.marker(ellipse((1040, 300), 30), MARKER_GREEN, nib=10, opacity=0.9)
    s.ink_circle((1040, 300), 30, width=2.0)
    s.text((1040, 310), "A", 30)
    s.text((1040, 254), "A button", 26, PAPER)
    s.text((cx, 80), "a stick pushed away from the driver reads -1.0, pulled back +1.0", 28)
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


def gamepad_notes(s):
    """gamepad1's label, its Gamepad note, and the float note left_stick_y points to."""
    sticky(s, 390, 130, 500, 400, MARKER_BLUE, "Gamepad", tilt=-1.0, lift=40)
    dot_arrow(s, label(s, 110, 185, "gamepad1"), (384, 212))
    end = label(s, 412, 270, "left_stick_y", tilt=-1.0, size=26)
    sticky(s, 625, 335, 225, 160, MARKER_YELLOW, "float", "-0.7", tilt=1.5, lift=26,
           value_size=48)
    dot_arrow(s, end, (662, 332), bend=-0.3)


def platform(s, x, y, w, h, name_x=None):
    """The grey box of what the FTC platform provides, its name along the bottom, centered on
    name_x where arrows cross the middle."""
    box = rect(x, y, w, h)
    s.marker(box, GREY1, nib=22, opacity=0.25, layers=1)
    s.ink_poly(box, width=1.8)
    s.text((name_x or x + w / 2, y + h - 30), "the FTC platform", 32, anchor="middle")


def method_box(s, x, y, w, h):
    """A method's lavender box, its left edge on the note's."""
    shape = rect(x, y, w, h)
    s.marker(shape, MARKER_LAVENDER, nib=14, opacity=0.8)
    s.ink_poly(shape, width=1.8)


def tracker_note(s):
    """Tracker's label and note, inside the platform, with publish's box on the note."""
    sticky(s, 330, 90, 440, 260, MARKER_BLUE, "Tracker", tilt=-0.5, lift=34)
    dot_arrow(s, label(s, 80, 98, "Tracker"), (324, 122))


def l020_platform():
    s = Sketch(1000, 680, 41)
    platform(s, 60, 80, 880, 540)
    gamepad_notes(s)
    s.save("fig-l020-platform")


def l020_tracker_note():
    s = Sketch(880, 480, 47)
    platform(s, 40, 40, 800, 400)
    tracker_note(s)
    method_box(s, 330, 170, 220, 80)
    s.text((350, 220), "publish()", 26, anchor="start", code=True)
    s.save("fig-l020-tracker-note")


def l020_publish():
    s = Sketch(1600, 1100, 53)
    s.text((800, 50), 'Tracker.publish("stick/leftY", leftStickY);', 30, code=True)
    platform(s, 40, 80, 800, 420, name_x=190)
    tracker_note(s)
    method_box(s, 330, 170, 360, 80)
    s.text((345, 220), "publish(", 26, anchor="start", code=True)
    first = label(s, 480, 190, "", size=24)
    s.text((548, 222), ",", 26, anchor="start", code=True)
    second = label(s, 570, 190, "", size=24)
    s.text((640, 220), ")", 26, anchor="start", code=True)
    sticky(s, 80, 620, 340, 170, MARKER_PINK, "String", '"stick/leftY"', tilt=-1.0, lift=26,
           value_size=34)
    dot_arrow(s, first, (330, 614), bend=0.15)
    # publish gets a copy of leftStickY's number on a note of its own: passing by value, which the
    # page models and does not explain
    sticky(s, 300, 840, 240, 180, MARKER_YELLOW, "double", "-0.7", tilt=-1.0, value_size=48)
    dot_arrow(s, label(s, 40, 1000, "leftStickY"), (294, 960), bend=-0.2)
    sticky(s, 760, 620, 240, 180, MARKER_YELLOW, "double", tilt=1.0)
    dot_arrow(s, second, (840, 614), bend=-0.15)
    for (x, y, size), grey in zip(((556, 860, 36), (620, 830, 40), (684, 800, 44), (748, 770, 48)),
                                  ("#e2e3e4", GREY1, "#b6b9bb", GREY2)):  # lightest farthest back
        s.text((x, y), "-0.7", size, grey)
    s.text((850, 748), "-0.7", 48)
    s.marker_arrow((600, 960), (780, 852), MARKER_ORANGE, nib=9, bend=0.15)
    # the log, outside the platform: publish writes the name and the number as one row
    log = rect(1020, 320, 520, 200)
    s.line(log, None, fill="#ffffff", closed=True)
    s.ink_poly(log, width=2.2)
    s.ink_line((1020, 400), (1540, 400), width=1.6, over=4, passes=1)
    s.ink_line((1280, 320), (1280, 520), width=1.6, over=4, passes=1)
    s.text((1280, 300), "the AdvantageScope log", 30)
    s.text((1150, 372), "name", 26)
    s.text((1410, 372), "value", 26)
    s.text((1150, 460), "stick/leftY", 24, code=True)
    s.text((1410, 462), "-0.7", 34)
    s.marker_arrow((700, 210), (1012, 440), MARKER_ORANGE, nib=9, bend=-0.2)
    s.lines((1200, 600), ["publish writes the name", "and the number in the log"], 28,
            anchor="start")
    s.save("fig-l020-publish")


def l020_variable():
    s = Sketch(900, 360, 43)
    s.text((450, 50), "double leftStickY", 30, code=True)
    sticky(s, 450, 110, 260, 200, MARKER_YELLOW, "double", tilt=-1.5)
    dot_arrow(s, label(s, 80, 175, "leftStickY"), (444, 208))
    s.save("fig-l020-variable")


def l020_sticky():
    s = Sketch(1400, 880, 31)
    s.text((700, 64), "double leftStickY = gamepad1.left_stick_y;", 30, code=True)
    gamepad_notes(s)
    sticky(s, 450, 600, 260, 200, MARKER_YELLOW, "double", tilt=-1.5)
    dot_arrow(s, label(s, 110, 665, "leftStickY"), (444, 698))
    # a copy of -0.7 leaves the float's value and is landing on the double's note
    for (x, y, size), grey in zip(((684, 524, 40), (662, 580, 44), (638, 634, 48), (614, 686, 52)),
                                  ("#e2e3e4", GREY1, "#b6b9bb", GREY2)):  # lightest farthest back
        s.text((x, y), "-0.7", size, grey)
    s.text((588, 732), "-0.7", 54)
    s.marker_arrow((745, 458), (676, 694), MARKER_ORANGE, nib=9, bend=-0.15)
    s.lines((900, 640), ["The equals symbol '='", "tells the program to do", "an assignment action."],
            30, anchor="start")
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


def axis(s, o, end, name, xy, anchor="middle", color=MARKER_ORANGE):
    """An axis drawn from o to end, its name written at xy."""
    s.marker_arrow(o, end, color, nib=8)
    s.text(xy, name, 30, anchor=anchor)


def stick_to_robot():
    # The stick's axes, as the SDK reads them, and Pedro's robot axes, as its mecanum mix and
    # Twist use them: +x forward, +y left, counter-clockwise positive.
    s = Sketch(1700, 1060, 29)
    s.text((430, 70), "the stick", 34)
    s.text((1230, 70), "Pedro's robot", 34)
    cx, cy = 430, 420
    gamepad(s, cx, cy, 660, 350)
    for x, name in ((cx - 150, "left"), (cx + 150, "right")):
        y = cy - 30
        s.marker(ellipse((x, y), 50), GREY4, nib=12, opacity=0.8)
        s.marker(ellipse((x, y), 26), GREY2, nib=9, opacity=0.9)
        s.ink_circle((x, y), 50, width=2.2)
        s.ink_circle((x, y), 26, width=1.6)
        s.marker_arrow((x, y), (x + 110, y), MARKER_GREEN, nib=7)
        s.marker_arrow((x, y), (x, y + 110), MARKER_GREEN, nib=7)
        s.text((x + 124, y + 10), "+x", 28, PAPER, anchor="start")
        s.text((x - 16, y + 120), "+y", 28, PAPER, anchor="end")
        s.text((x, y - 72), name, 26, PAPER)
    s.lines((cx, 690), ["+x: pushed right", "+y: pulled back, toward the driver"], 28)

    rx, ry, side = 1230, 440, 240
    frame_top(s, rx - side / 2, ry - side / 2, side, side, nose=False)
    tri = [(rx - 50, ry - side / 2), (rx, ry - side / 2 - 60), (rx + 50, ry - side / 2)]
    s.marker(tri, MARKER_RED, nib=12, opacity=0.8)
    s.ink_poly(tri, width=2.2)
    axis(s, (rx, ry), (rx, ry - 280), "+x: forward, out the nose", (rx, ry - 304))
    axis(s, (rx, ry), (rx - 280, ry), "+y: left", (rx - 290, ry - 26), "start")
    turn = [(rx + 200 * math.cos(math.radians(a)), ry - 200 * math.sin(math.radians(a)))
            for a in range(90, 156, 5)]   # from +x, where heading 0 is, toward +y
    s.line(turn, MARKER_ORANGE, 8, 0.85)
    s.line(turn, PEN, 1.6, 0.8)
    s.marker_arrow(turn[-2], turn[-1], MARKER_ORANGE, nib=8)
    s.lines((rx - 170, ry - 200), ["+ turn:", "counter-clockwise"], 28, anchor="end")
    s.lines((rx, 690), ["position and velocity along these axes:", "twist vx forward, vy left,",
                        "omega counter-clockwise"], 28)

    s.text((850, 900), "turning a stick into a velocity is a change of axes:", 30)
    s.text((850, 960), "forward = -left_stick_y    strafe = -left_stick_x    turn = -right_stick_x",
           24, code=True)
    s.save("fig-stick-to-robot")


def pedro_field():
    # Pedro's field axes, from its Coordinates page: (0, 0) in a corner, x and y 0 in to 144 in,
    # heading 0 along +x and counter-clockwise positive. The red drive team stands on the left, as
    # L140 puts the driver, and the blue drive team on the right.
    s = Sketch(1300, 1060, 31)
    fx, fy, fs = 190, 120, 760
    s.shadow(rect(fx + 10, fy + 14, fs, fs), sigma=10, alpha=0.25)
    s.marker(rect(fx, fy, fs, fs), GREY1, nib=30, opacity=0.35)
    s.highlight((fx + 20, fy + 16), (fx + fs - 40, fy + 16))
    s.ink_poly(rect(fx, fy, fs, fs))
    for k in (1, 2, 3, 4, 5):
        s.construct((fx + fs * k / 6, fy), (fx + fs * k / 6, fy + fs))
        s.construct((fx, fy + fs * k / 6), (fx + fs, fy + fs * k / 6))
    s.text((110, fy + fs / 2), "red drive team", 32, MARKER_RED, rot=-90)
    s.dot((140, fy + fs / 2), 10, MARKER_RED)
    s.text((fx + fs + 80, fy + fs / 2), "blue drive team", 32, BLUE, rot=90)
    s.dot((fx + fs + 50, fy + fs / 2), 10, BLUE)
    o = (fx, fy + fs)
    s.dot(o, 12)
    s.text((fx - 10, fy + fs + 46), "(0, 0)", 24, anchor="end", code=True)
    s.text((fx + fs + 10, fy + fs + 46), "(144, 0)", 24, anchor="start", code=True)
    s.text((fx - 10, fy - 20), "(0, 144)", 24, anchor="end", code=True)
    s.text((fx + fs + 10, fy - 20), "(144, 144) in", 24, anchor="start", code=True)
    axis(s, o, (o[0] + 300, o[1]), "+x", (o[0] + 320, o[1] + 10), "start")
    axis(s, o, (o[0], o[1] - 300), "+y", (o[0] + 18, o[1] - 300), "start")

    c, r = (fx + fs * 0.3, fy + fs * 0.28), 120
    s.ink_circle(c, 40, width=1.8)
    for deg, name, xy, anchor in ((0, "0°, 0", (c[0] + r + 14, c[1] + 10), "start"),
                                  (90, "90°, π/2", (c[0], c[1] - r - 14), "middle"),
                                  (180, "180°, π", (c[0] - r - 14, c[1] + 10), "end"),
                                  (270, "270°, 3π/2", (c[0], c[1] + r + 40), "middle")):
        a = math.radians(deg)
        s.marker_arrow(c, (c[0] + r * math.cos(a), c[1] - r * math.sin(a)), GREY3, nib=4)
        s.text(xy, name, 26, anchor=anchor)
    turn = [(c[0] + 70 * math.cos(math.radians(a)), c[1] - 70 * math.sin(math.radians(a)))
            for a in range(0, 76, 5)]    # from 0, along +x
    s.line(turn, MARKER_ORANGE, 7, 0.85)
    s.marker_arrow(turn[-2], turn[-1], MARKER_ORANGE, nib=7)
    s.text((c[0], c[1] + r + 90), "heading, counter-clockwise +", 26)

    rc, side = (fx + fs * 0.68, fy + fs * 0.64), 150
    frame_top(s, rc[0] - side / 2, rc[1] - side / 2, side, side, nose=False)
    tri = [(rc[0] - 34, rc[1] - side / 2), (rc[0], rc[1] - side / 2 - 40),
           (rc[0] + 34, rc[1] - side / 2)]
    s.marker(tri, MARKER_RED, nib=10, opacity=0.8)
    s.ink_poly(tri, width=2.2)
    s.marker_arrow(rc, (rc[0], rc[1] - 170), MARKER_RED, nib=6)
    s.marker_arrow(rc, (rc[0] - 170, rc[1]), MARKER_RED, nib=6)
    s.text((rc[0] + 12, rc[1] - 180), "robot +x", 24, anchor="start")
    s.text((rc[0] - 176, rc[1] - 16), "robot +y", 24, anchor="start")
    s.lines((rc[0], rc[1] + 124), ["this robot has a 90° heading", "in the field coordinate system,",
                                    "so the robot's +x", "is the field's +y"], 24)

    s.lines((650, 980), ["pose x, y in inches and heading in radians;",
                         "follower.velocity() along the field's x and y"], 28)
    s.save("fig-pedro-field")


# -- E035: from the number line to the stick's axes and the robot's speeds, one idea a figure.
# No heading, turn or strafe; z is drawn only to show that the stick's axes are flipped.

def e035_numberline():
    s = Sketch(1300, 330, 41)
    y, left, step = 170, 170, 160
    s.construct((60, y), (1240, y))
    s.marker_arrow((left - 70, y), (left + 6 * step + 80, y), AXIS_X, nib=7)
    for k in range(-3, 4):
        x = left + (k + 3) * step
        s.ink_line((x, y - 16), (x, y + 16), width=2.2, over=3, passes=1)
        s.text((x, y + 62), f"{k:+d}" if k else "0", 34)
    s.text((left + 6 * step + 110, y + 16), "+x", 60, anchor="start")
    s.text((650, 80), "numbers grow to the right", 32)
    s.save("fig-e035-numberline")



# Onshape's views. Isometric, z up: +x to the lower right, +y to the upper right, +z up the page.
# Top: +x to the right, +y up the page.
ISO_RIGHT = (1 / math.sqrt(2), 1 / math.sqrt(2), 0.0)
ISO_UP = (-1 / math.sqrt(6), 1 / math.sqrt(6), 2 / math.sqrt(6))
TOP_RIGHT, TOP_UP = (1.0, 0.0, 0.0), (0.0, 1.0, 0.0)
AXIS_X, AXIS_Y, AXIS_Z = "#d8432c", "#3f9f45", "#2f6fd0"   # Onshape's red, green and blue
SHEET = "#bfe0f0"                                           # a clear plastic sheet


class Iso:
    """Points in space, drawn at o on the page, k drawing pixels to a unit, in the view that right
    and up name; turn moves a point before it is drawn, as turning a sheet over moves everything
    printed on it."""

    def __init__(self, s, o, k, turn=lambda q: q, right=ISO_RIGHT, up=ISO_UP):
        self.s, self.o, self.k, self.turn = s, o, k, turn
        self.right, self.up = right, up

    def p(self, q):
        return self.page(self.turn(q))

    def page(self, q):
        """q drawn where it is, not turned."""
        return (self.o[0] + self.k * sum(a * b for a, b in zip(q, self.right)),
                self.o[1] - self.k * sum(a * b for a, b in zip(q, self.up)))

    def word(self, c, a, b, text, h, color=PEN):
        """text printed flat in the plane through c, reading along a, its top toward b."""
        path = TextPath((0, 0), text, size=1, prop=self.s.hand)
        box = path.get_extents()
        mx, my = (box.x0 + box.x1) / 2, (box.y0 + box.y1) / 2
        pts = [self.p(tuple(ci + (gx - mx) * h * ai + (gy - my) * h * bi
                            for ci, ai, bi in zip(c, a, b))) for gx, gy in path.vertices]
        self.s.add(PathPatch(MplPath(pts, path.codes), facecolor=color, edgecolor="none",
                             transform=self.s.ax.transData))


X3, Y3, Z3 = (1, 0, 0), (0, 1, 0), (0, 0, 1)


def sheet_axes(v, z=True, z_under=False, drop=True, upright=False):
    """x and y from -3 to +3, with a clear sheet ruled in units over the corner where both are
    positive, and perhaps z standing up out of it. z_under draws z first, so a sheet turned over
    lies on top of it; upright prints its letters and numbers unturned where they have moved to."""
    edge = 3.5
    plain = Iso(v.s, v.o, v.k, right=v.right, up=v.up)

    def word(c, a, b, text, h):
        if upright:
            plain.word(v.turn(c), a, b, text, h)
        else:
            v.word(c, a, b, text, h)
    sheet = [(0, 0, 0), (edge, 0, 0), (edge, edge, 0), (0, edge, 0)]

    def z_axis():
        v.s.marker_arrow(v.p((0, 0, 0)), v.p((0, 0, 2.6)), AXIS_Z, nib=8)
        word((0.3, 0, 2.75), X3, Z3, "z", 0.42)

    if z and z_under:
        z_axis()
    if drop:
        v.s.shadow([(x + 6, y + 18) for x, y in map(v.p, sheet)], sigma=10, alpha=0.10)
    v.s.line([v.p(q) for q in sheet], None, fill=SHEET, fill_alpha=0.45, closed=True)
    for n in (1, 2, 3):                                  # the unit lines
        v.s.line([v.p((n, 0, 0)), v.p((n, edge, 0))], GREY2, 1.4, 0.7)
        v.s.line([v.p((0, n, 0)), v.p((edge, n, 0))], GREY2, 1.4, 0.7)
    v.s.line([v.p(q) for q in sheet], GREY3, 2.0, 0.8, closed=True)
    for a, b in (((1.4, 3.3), (3.1, 3.3)), ((3.3, 2.0), (3.3, 3.1))):   # glare by a corner
        v.s.line([v.p((*a, 0)), v.p((*b, 0))], "#ffffff", 5, 0.8)
    v.s.marker_arrow(v.p((-3.3, 0, 0)), v.p((3.4, 0, 0)), AXIS_X, nib=7)
    v.s.marker_arrow(v.p((0, -3.3, 0)), v.p((0, 3.4, 0)), AXIS_Y, nib=7)
    for n in (-3, -2, -1, 1, 2, 3):
        name = f"+{n}" if n > 0 else f"-{-n}"
        v.s.ink_line(v.p((n, -0.13, 0)), v.p((n, 0.13, 0)), width=2.0, over=2, passes=1)
        v.s.ink_line(v.p((-0.13, n, 0)), v.p((0.13, n, 0)), width=2.0, over=2, passes=1)
        word((n, -0.42, 0), X3, Y3, name, 0.3)
        word((0.45 if upright else -0.5, n, 0), X3, Y3, name, 0.3)
    v.s.dot(v.p((0, 0, 0)), 7)
    word((-0.38, -0.38, 0), X3, Y3, "0", 0.3)
    word((3.75, 0.0, 0), X3, Y3, "x", 0.42)
    word((0.8, 3.6, 0) if upright else (0.0, 3.75, 0), X3, Y3, "y", 0.42)
    if z and not z_under:
        z_axis()


# The room the 3D drawings stand in: a floor at z = -4, a light above, in front of and to the right
# of the viewer, and a student standing at (1.5, -5.5, 0) on a pedestal, looking at the sheet. Only
# the sheet and what is printed on it turns over.
FLOOR = -4.0
_LIT = tuple(a + 0.6 * b + 0.6 * c for a, b, c in
             zip((1 / math.sqrt(3), -1 / math.sqrt(3), 1 / math.sqrt(3)), ISO_RIGHT, ISO_UP))
_SWING = math.radians(-30)         # turned about z, further behind the viewer: shadows 30° up more
LIGHT = (math.cos(_SWING) * _LIT[0] - math.sin(_SWING) * _LIT[1],
         math.sin(_SWING) * _LIT[0] + math.cos(_SWING) * _LIT[1], _LIT[2])
STAND, STAND_HALF, STAND_TOP = (1.5, -5.5), 0.5, 0.0


def landing(q):
    """Where the light past q lands: on the pedestal's top, or else on the floor."""
    for h, box in ((STAND_TOP, STAND_HALF), (FLOOR, None)):
        if q[2] <= h:
            continue
        t = (q[2] - h) / LIGHT[2]
        x, y = q[0] - LIGHT[0] * t, q[1] - LIGHT[1] * t
        if box is None or (abs(x - STAND[0]) <= box and abs(y - STAND[1]) <= box):
            return (x, y, h)
    return None


def hull(pts):
    """The convex hull of page points."""
    pts = sorted(set(pts))
    if len(pts) < 3:
        return pts

    def side(o, a, b):
        return (a[0] - o[0]) * (b[1] - o[1]) - (a[1] - o[1]) * (b[0] - o[0])

    lower, upper = [], []
    for p in pts:
        while len(lower) >= 2 and side(lower[-2], lower[-1], p) <= 0:
            lower.pop()
        lower.append(p)
    for p in reversed(pts):
        while len(upper) >= 2 and side(upper[-2], upper[-1], p) <= 0:
            upper.pop()
        upper.append(p)
    return lower[:-1] + upper[:-1]


def stroke_shadows(v, a, b, width, n=24):
    """A world segment's shadow, as page quads, split by the plane each piece lands on."""
    out = {STAND_TOP: [], FLOOR: []}
    pts = [landing(tuple(a[i] + (b[i] - a[i]) * j / n for i in range(3))) for j in range(n + 1)]
    for p0, p1 in zip(pts, pts[1:]):
        if p0 is None or p1 is None or p0[2] != p1[2]:
            continue
        (x0, y0), (x1, y1) = v.page(p0), v.page(p1)
        d = math.hypot(x1 - x0, y1 - y0) or 1
        nx, ny = -(y1 - y0) / d * width / 2, (x1 - x0) / d * width / 2
        out[p0[2]].append([(x0 + nx, y0 + ny), (x1 + nx, y1 + ny), (x1 - nx, y1 - ny),
                           (x0 - nx, y0 - ny)])
    return out


STUDENT_FEET = STAND_TOP
_SX, _SY = STAND
STUDENT = [((_SX - 0.2, _SY, STUDENT_FEET), (_SX, _SY, STUDENT_FEET + 1.15)),         # legs
           ((_SX + 0.2, _SY, STUDENT_FEET), (_SX, _SY, STUDENT_FEET + 1.15)),
           ((_SX, _SY, STUDENT_FEET + 1.15), (_SX, _SY, STUDENT_FEET + 2.05)),        # body
           ((_SX, _SY, STUDENT_FEET + 1.95), (_SX - 0.35, _SY + 0.15, STUDENT_FEET + 1.25)),  # arms
           ((_SX, _SY, STUDENT_FEET + 1.95), (_SX + 0.35, _SY + 0.15, STUDENT_FEET + 1.25))]
HEAD, HEAD_R = (_SX, _SY, STUDENT_FEET + 2.4), 0.32


def quadrant_grid(v):
    """The z = 0 plane ruled in units where x is positive and y negative, out to the pedestal."""
    for x in range(0, 4):
        v.s.line([v.page((x, 0, 0)), v.page((x, -6, 0))], GREY3, 1.4, 0.6)
    for y in range(-1, -7, -1):
        v.s.line([v.page((0, y, 0)), v.page((3, y, 0))], GREY3, 1.4, 0.6)


def room(v, light=(), lines=(), solids=()):
    """The floor's shadows of the clear shapes in light, the lines, the solid shapes, the
    pedestal and the student, and a plumb line from 0 down to the floor; returns the shadows on
    the pedestal's top. The things themselves go on top, drawn by the caller."""
    v.s.line([v.page((0, 0, 0)), v.page((0, 0, FLOOR))], GREY3, 1.2, 0.6)
    floor, top = [], []
    v.s.cast([[v.page(landing(q)) for q in shape] for shape in light], sigma=5, alpha=0.2)
    for a, b in lines:
        floor += stroke_shadows(v, a, b, 6)[FLOOR]
    floor += [[v.page(landing(q)) for q in shape] for shape in solids]
    (cx, cy), hw = STAND, STAND_HALF
    box = [(cx + dx, cy + dy, h) for dx in (-hw, hw) for dy in (-hw, hw) for h in (FLOOR, STAND_TOP)]
    floor.append(hull([v.page(landing(q) if q[2] > FLOOR else q) for q in box]))
    for a, b in STUDENT:
        cast = stroke_shadows(v, a, b, 7)
        floor += cast[FLOOR]
        top += cast[STAND_TOP]
    head = landing(HEAD)
    disk = [v.page((head[0] + HEAD_R * 1.25 * math.cos(u), head[1] + HEAD_R * math.sin(u),
                    head[2])) for u in (i * math.pi / 12 for i in range(24))]
    (floor if head[2] == FLOOR else top).append(disk)
    v.s.cast(floor, sigma=3, alpha=0.5)
    return top


CAP, BRIM = "#7b5ea7", "#55407a"
CAP_TILT = math.radians(25)        # the brim, tilted up from level, facing +y


def pedestal(v, top):
    """The pedestal, from the floor up to the student's feet, and the shadows top on it."""
    (cx, cy), hw, h0, h1 = STAND, STAND_HALF, FLOOR, STAND_TOP
    faces = [([(cx + hw, cy - hw, h0), (cx + hw, cy + hw, h0), (cx + hw, cy + hw, h1),
               (cx + hw, cy - hw, h1)], "#b9bcbe"),                       # +x, half lit
             ([(cx - hw, cy - hw, h0), (cx + hw, cy - hw, h0), (cx + hw, cy - hw, h1),
               (cx - hw, cy - hw, h1)], "#8d9194"),                       # -y, in its own shade
             ([(cx - hw, cy - hw, h1), (cx + hw, cy - hw, h1), (cx + hw, cy + hw, h1),
               (cx - hw, cy + hw, h1)], "#e4e5e5")]                       # top, lit
    for face, color in faces:
        pts = [v.page(q) for q in face]
        v.s.line(pts, None, fill=color, closed=True)
        v.s.marker(pts, color, nib=12, opacity=0.5, layers=1)
        v.s.line(pts, PEN, 2.0, 0.85, closed=True)
    v.s.cast(top, sigma=2, alpha=0.45)


def student(v, look):
    """The student on the pedestal in a cap, looking at look, a point in space."""
    for a, b in STUDENT:
        v.s.ink_line(v.page(a), v.page(b), width=4.2, over=2, passes=1, wobble=0.8)
    v.s.construct(v.page(HEAD), v.page(look))                 # where the student is looking
    head_in_cap(v)


def head_in_cap(v):
    """The head, a sphere, in a cap: the crown is the sphere above a band circle tilted back with
    the brim, and the brim lies flat in the band's plane, out in front. Only the half of the crown
    facing the viewer shows; the brim, on the far side, shows past the head."""
    def at(c, *terms):
        return tuple(ci + sum(k * w[i] for k, w in terms) for i, ci in enumerate(c))

    def dot(a, b):
        return sum(x * y for x, y in zip(a, b))

    toward = (1 / math.sqrt(3), -1 / math.sqrt(3), 1 / math.sqrt(3))   # toward the viewer
    up = (0.0, -math.sin(CAP_TILT), math.cos(CAP_TILT))     # the crown's top, tilted back
    fwd = (0.0, math.cos(CAP_TILT), math.sin(CAP_TILT))     # the brim's way, tilted up
    side = (1.0, 0.0, 0.0)
    h = 0.15                                                # the band, 0.15 radii above middle
    centre, rb = at(HEAD, (h * HEAD_R, up)), HEAD_R * math.sqrt(1 - h * h)

    def band(phi, r=rb):
        return at(centre, (r * math.cos(phi), side), (r * math.sin(phi), fwd))

    span = [math.radians(d) for d in range(35, 146, 5)]
    brim = [band(a) for a in reversed(span)] + [
        band(a, rb + 0.65 * HEAD_R * math.sin(math.pi * (a - span[0]) / (span[-1] - span[0])))
        for a in span]
    v.s.line([v.page(q) for q in brim], PEN, 2.4, 0.9, fill=BRIM, closed=True)

    c, r = v.page(HEAD), HEAD_R * v.k
    v.s.line(ellipse(c, r), PEN, 3.4, 0.95, fill=PAPER, closed=True)
    steps = [math.radians(d) for d in range(0, 360, 2)]
    seen = [dot(tuple(a - b for a, b in zip(band(a), HEAD)), toward) >= 0 for a in steps]
    first = next(i for i in range(len(steps)) if seen[i] and not seen[i - 1])
    arc = []
    for i in range(len(steps)):                   # the band's near half, in order
        j = (first + i) % len(steps)
        if not seen[j]:
            break
        arc.append(v.page(band(steps[j])))
    lean = tuple(a - dot(up, toward) * b for a, b in zip(up, toward))
    top = v.page(at(HEAD, (HEAD_R / math.sqrt(dot(lean, lean)), lean)))

    def angle(p):
        return math.atan2(p[1] - c[1], p[0] - c[0])

    a0, a1, at_top = angle(arc[-1]), angle(arc[0]), angle(top)
    ccw = (a1 - a0) % (2 * math.pi)
    if (at_top - a0) % (2 * math.pi) > ccw:      # round the outline the other way, by the top
        ccw -= 2 * math.pi
    rim = [(c[0] + r * math.cos(a0 + ccw * i / 40), c[1] + r * math.sin(a0 + ccw * i / 40))
           for i in range(1, 40)]
    v.s.line(arc + rim, PEN, 2.6, 0.95, fill=CAP, closed=True)


def sheet_in_room(v, z_under=False, upright=False):
    """sheet_axes in the room, its shadows cast by the light."""
    edge = 3.5
    corners = [v.turn(q) for q in ((0, 0, 0), (edge, 0, 0), (edge, edge, 0), (0, edge, 0))]
    lines = [(v.turn((-3.3, 0, 0)), v.turn((3.4, 0, 0))),
             (v.turn((0, -3.3, 0)), v.turn((0, 3.4, 0))),
             (v.turn((0, 0, 0)), v.turn((0, 0, 2.6)))]
    top = room(v, light=[corners], lines=lines)
    pedestal(v, top)
    quadrant_grid(v)
    sheet_axes(v, z_under=z_under, drop=False, upright=upright)
    student(v, v.turn((STAND[0], 1.75, 0)))                  # gazing along x = 1.5


def e035_xy():
    s = Sketch(1000, 1000, 43)
    sheet_axes(Iso(s, (430, 560), 110, right=TOP_RIGHT, up=TOP_UP), z=False)
    s.lines((760, 820), ["+x to the right,", "+y up the page"], 30)
    s.save("fig-e035-xy")


def e035_xyz():
    s = Sketch(1450, 1000, 47)
    sheet_in_room(Iso(s, (760, 330), 92))
    s.lines((1100, 900), ["the sheet laid flat, as Onshape shows it:", "+z points up, out of it"],
            28)
    s.save("fig-e035-xyz")


def e035_flip():
    s = Sketch(1500, 1180, 53)
    v = Iso(s, (660, 430), 92)
    s.line([v.p((-5.2, 0, 0)), v.p((6.4, 0, 0))], GREY3, 1.6, 0.7)   # the line it turns on
    sheet_in_room(v)
    turn = [v.p((4.9, 3.5 * math.cos(math.radians(a)), 3.5 * math.sin(math.radians(a))))
            for a in range(6, 175, 6)]           # the far edge's path, up, over and toward you
    s.line(turn, MARKER_ORANGE, 9, 0.85)
    s.line(turn, PEN, 1.6, 0.8)
    s.marker_arrow(turn[-3], turn[-1], MARKER_ORANGE, nib=9)
    s.text((750, 80), "a clear sheet, ready to flip over, top to bottom", 34)
    s.lines((1130, 1090), ["it turns on the x axis:", "the far edge comes up and over, toward you"],
            26)
    s.save("fig-e035-flip")


def iso_stick(v, c):
    """One stick on a gamepad lying flat, seen in the same view."""
    ring = [v.p((c[0] + 0.42 * math.cos(a), c[1] + 0.42 * math.sin(a), 0))
            for a in (i * math.pi / 24 for i in range(48))]
    cap = [v.p((c[0] + 0.22 * math.cos(a), c[1] + 0.22 * math.sin(a), 0.3))
           for a in (i * math.pi / 24 for i in range(48))]
    v.s.marker(ring, GREY4, nib=10, opacity=0.8)
    v.s.line(ring, PEN, 2.0, 0.9, closed=True)
    v.s.line([v.p((c[0], c[1], 0)), v.p((c[0], c[1], 0.3))], GREY1, 8, 0.9)
    v.s.marker(cap, GREY2, nib=8, opacity=0.95)
    v.s.line(cap, PEN, 1.8, 0.9, closed=True)


def e035_flipped():
    s = Sketch(2300, 1220, 54)
    flipped = Iso(s, (560, 380), 82, turn=lambda q: (q[0], -q[1], -q[2]))
    sheet_in_room(flipped, z_under=True, upright=True)
    pad = Iso(s, (1500, 380), 82)          # the left stick at 0, so the student stands as before
    w, h = 7.4, 3.2                        # the gamepad's width and depth, in the sheet's units
    middle = (1.85, -0.5)                  # the gamepad's middle, from the left stick
    flat = [(middle[0] + u * w / 1000, middle[1] - v * h / 430)
            for u, v in gamepad_outline(0, 0, 1000, 430)]
    arrows = [((0.5, 0, 0.3), (2.2, 0, 0.3)), ((0, -0.5, 0.3), (0, -2.9, 0.3))]
    top = room(pad, lines=arrows, solids=[[(x, y, z) for x, y in flat] for z in (0, -0.45)])
    pedestal(pad, top)
    quadrant_grid(pad)
    outline = [pad.p((x, y, 0)) for x, y in flat]      # gamepad_outline's page, laid flat
    under = [pad.p((x, y, -0.45)) for x, y in flat]    # its bottom, for its sides
    for i in range(len(flat)):                         # its sides, then its top over them
        j = (i + 1) % len(flat)
        s.line([outline[i], outline[j], under[j], under[i]], GREY4, 1.0, 1.0, fill=GREY4,
               closed=True)
    s.line(under, PEN, 2.4, 0.9, closed=True)
    s.line(outline, None, fill="#9a9ea1", closed=True)
    s.marker(outline, GREY3, nib=18, opacity=0.65)
    s.line(outline, PEN, 2.4, 0.9, closed=True)
    iso_stick(pad, (3.7, 0))
    iso_stick(pad, (0, 0))
    s.marker_arrow(pad.p(arrows[0][0]), pad.p(arrows[0][1]), AXIS_X, nib=7)
    s.marker_arrow(pad.p(arrows[1][0]), pad.p(arrows[1][1]), AXIS_Y, nib=7)
    pad.word((2.65, 0, 0.3), X3, Y3, "+x", 0.42)
    pad.word((0.6, -3.2, 0.3), X3, Y3, "+y", 0.42)
    student(pad, (STAND[0], middle[1], 0))
    s.text((560, 70), "the sheet, flipped over", 34)
    s.text((1700, 70), "a gamepad's left stick, the same way round", 34)
    s.lines((1150, 1140), ["The axes on the sticks on gamepads are flipped upside down from what",
                          "people normally expect."], 28)
    s.save("fig-e035-flipped")


def stick_top(s, c, rad=150):
    """One stick from above, big."""
    s.shadow(ellipse((c[0] + 10, c[1] + 14), rad), sigma=10, alpha=0.25)
    s.marker(ellipse(c, rad), GREY4, nib=24, opacity=0.8)
    s.marker(ellipse(c, rad * 0.5), GREY2, nib=14, opacity=0.9)
    s.highlight((c[0] - rad * 0.5, c[1] - rad * 0.6), (c[0] + rad * 0.1, c[1] - rad * 0.8))
    s.ink_circle(c, rad)
    s.ink_circle(c, rad * 0.5, width=1.8)


def e035_stick():
    s = Sketch(1000, 1000, 59)
    c = (430, 500)
    stick_top(s, c)
    s.marker_arrow(c, (c[0] + 260, c[1]), AXIS_X, nib=8)
    s.marker_arrow(c, (c[0], c[1] + 260), AXIS_Y, nib=8)
    s.text((c[0] + 290, c[1] + 12), "+x", 34, anchor="start")
    s.text((c[0] - 24, c[1] + 270), "+y", 34, anchor="end")
    s.marker_arrow((c[0] + 230, c[1] - 60), (c[0] + 230, c[1] - 300), MARKER_ORANGE, nib=8)
    s.lines((c[0] + 260, c[1] - 260), ["pushed away", "-1.0"], 32, anchor="start")
    s.marker_arrow((c[0] + 230, c[1] + 60), (c[0] + 230, c[1] + 300), MARKER_ORANGE, nib=8)
    s.lines((c[0] + 260, c[1] + 220), ["pulled back", "+1.0"], 32, anchor="start")
    s.text((c[0], 940), "you, the driver, are down here", 30)
    s.text((c[0], 70), "the left stick, from above", 34)
    s.save("fig-e035-stick")


def e035_robot():
    s = Sketch(1000, 1000, 61)
    c, side = (560, 560), 360
    robot_inside(s, c[0], c[1], side, nose=False)
    tri = [(c[0] - 55, c[1] - side / 2), (c[0], c[1] - side / 2 - 70), (c[0] + 55, c[1] - side / 2)]
    s.marker(tri, MARKER_RED, nib=14, opacity=0.8)
    s.ink_poly(tri, width=2.2)
    s.text((c[0] - 70, c[1] - side / 2 - 20), "nose", 32, MARKER_RED, "end")
    axis(s, c, (c[0], c[1] - 400), "+x: forward, out the nose", (c[0] + 20, c[1] - 420), "start",
         color=AXIS_X)
    axis(s, c, (c[0] - 400, c[1]), "+y: left", (c[0] - 400, c[1] - 26), "start", color=AXIS_Y)
    s.text((500, 960), "the robot, from above", 34)
    s.save("fig-e035-robot")


def e035_speeds():
    s = Sketch(1100, 820, 67)
    c, side = (550, 420), 360
    wheels = robot_inside(s, c[0], c[1], side)
    for name in ("left", "right"):
        x = wheels[f"front {name}"][0]
        out = -1 if name == "left" else 1
        s.marker_arrow((x + out * 130, c[1] + 120), (x + out * 130, c[1] - 160), MARKER_RED, nib=9)
        s.lines((x + out * 160, c[1] - 120), [f"{name}Speed", "1.0"], 30,
                anchor="end" if out < 0 else "start", code=False)
    s.lines((550, 730), ["forward is +, backward is -,", "and 1.0 is full speed"], 30)
    s.save("fig-e035-speeds")


def e035_switch():
    s = Sketch(1900, 800, 71)
    c = (260, 420)
    stick_top(s, c, 120)
    s.marker_arrow((c[0], c[1] - 20), (c[0], c[1] - 230), MARKER_GREEN, nib=8)
    s.text((c[0], 110), "pushed away", 32)
    s.text((c[0], 640), "leftStickY: -1.0", 30, code=True)
    s.marker_arrow((470, 420), (620, 420), MARKER_ORANGE, nib=9)
    box = rect(650, 360, 600, 110)
    s.marker(box, GREY1, nib=20, opacity=0.5)
    s.ink_poly(box)
    s.text((950, 428), "double leftSpeed = -leftStickY;", 26, code=True)
    s.text((950, 540), "the minus sign turns it around", 30)
    s.marker_arrow((1280, 420), (1430, 420), MARKER_ORANGE, nib=9)
    rc, side = (1640, 440), 260
    wheels = robot_inside(s, rc[0], rc[1], side)
    x = wheels["front left"][0] - 100
    s.marker_arrow((x, rc[1] + 90), (x, rc[1] - 130), MARKER_RED, nib=9)
    s.text((rc[0], 690), "leftSpeed: 1.0, full speed forward", 30, code=False)
    s.save("fig-e035-switch")


PLOTS = [stick_shaping, speed_loop, panels_graph]
PICTURES = [mecanum_x, wheel_handover, odometry_step, field_relative, wheel_pushes, robot_front,
            gamepad_sticks, wheel_names, wheel_forward, pinpoint_mounting, l020_platform,
            l020_variable, l020_sticky, l020_tracker_note, l020_publish, l040_sticky,
            stick_to_robot, pedro_field, e035_numberline, e035_xy, e035_xyz, e035_flip,
            e035_flipped, e035_stick, e035_robot, e035_speeds, e035_switch]

if __name__ == "__main__":
    if OUT is None:
        raise SystemExit("name the folder to draw into: python tools/run.py draw DIR")
    for draw in PLOTS + PICTURES:
        draw()
    print(f"drew {len(PLOTS) + len(PICTURES)} figures into {OUT}")
