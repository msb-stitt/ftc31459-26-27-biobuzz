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
        ax.set_xlabel("stick, -1.0 to 1.0")
        ax.grid(alpha=0.3)
    axes[0].set_ylabel("power out, -1.0 to 1.0")
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


def screen_device(s, x, y, w, h, bezel):
    """A device seen from the front: a light case, bezel wide, around a dark screen."""
    s.shadow([(px + 6, py + 10) for px, py in rect(x, y, w, h)], sigma=8)
    s.marker(rect(x, y, w, h), GREY1, nib=12, opacity=0.9)
    s.ink_poly(rect(x, y, w, h), width=2.2, over=5)
    sx, sy, sw, sh = x + bezel, y + bezel, w - 2 * bezel, h - 2 * bezel
    s.marker(rect(sx, sy, sw, sh), "#3c3f42", nib=12, opacity=0.9)
    s.highlight((sx + 0.12 * sw, sy + 0.18 * sh), (sx + 0.55 * sw, sy + 0.18 * sh), width=4)
    s.ink_poly(rect(sx, sy, sw, sh), width=1.8, over=3)


def robot_front():
    s = Sketch(1800, 620, 19)
    ground = 520
    s.construct((40, ground), (1760, ground))
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
    s.marker(rect(860, ground - 24, 120, 24), GREY3, nib=10, opacity=0.8)    # the Driver Station's stand
    s.ink_poly(rect(860, ground - 24, 120, 24), width=2.0, over=4)
    screen_device(s, 820, ground - 140, 200, 118, 14)
    s.text((920, ground - 175), "Driver Station", 28)
    gamepad(s, 1210, ground - 50, 200, 90)
    s.text((1210, ground - 120), "gamepad", 28)
    screen_device(s, 1400, ground - 215, 280, 190, 14)                    # the laptop's lid
    s.marker(rect(1375, ground - 25, 330, 25), GREY1, nib=10, opacity=0.9)  # and its base
    s.shade(rect(1375, ground - 12, 330, 12), color=GREY3)
    s.ink_poly(rect(1375, ground - 25, 330, 25), width=2.2, over=5)
    s.text((1540, ground - 250), "laptop", 28)
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

    def word(self, c, a, b, text, h, color=PEN, code=False, anchor="middle"):
        """text printed flat in the plane through c, reading along a, its top toward b; anchor
        "start" or "end" puts that end of it at c."""
        path = TextPath((0, 0), text, size=1, prop=self.s.code if code else self.s.hand)
        box = path.get_extents()
        mx = {"start": box.x0, "end": box.x1}.get(anchor, (box.x0 + box.x1) / 2)
        my = (box.y0 + box.y1) / 2
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


# The room the 3D drawings stand in: a floor at z = 0, a light above, in front of and to the right
# of the viewer, and a student standing at (1.5, -5.5, 0) on a pedestal, a square on the floor,
# looking at the sheet. Only the sheet and what is printed on it turns over.
FLOOR = 0.0
_LIT = tuple(a + 0.6 * b + 0.6 * c for a, b, c in
             zip((1 / math.sqrt(3), -1 / math.sqrt(3), 1 / math.sqrt(3)), ISO_RIGHT, ISO_UP))
_SWING = math.radians(-30)         # turned about z, further behind the viewer: shadows 30° up more
LIGHT = (math.cos(_SWING) * _LIT[0] - math.sin(_SWING) * _LIT[1],
         math.sin(_SWING) * _LIT[0] + math.cos(_SWING) * _LIT[1], _LIT[2])
STAND, STAND_HALF, STAND_TOP = (1.5, -5.5), 0.5, 0.0


def landing(q):
    """Where the light past q lands on the floor: q itself on the floor, None under it."""
    if q[2] < FLOOR - 1e-9:
        return None
    t = max(q[2] - FLOOR, 0) / LIGHT[2]
    return (q[0] - LIGHT[0] * t, q[1] - LIGHT[1] * t, FLOOR)


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
    """A world segment's shadow on the floor, as page quads."""
    out = []
    pts = [landing(tuple(a[i] + (b[i] - a[i]) * j / n for i in range(3))) for j in range(n + 1)]
    for p0, p1 in zip(pts, pts[1:]):
        if p0 is None or p1 is None:
            continue
        (x0, y0), (x1, y1) = v.page(p0), v.page(p1)
        d = math.hypot(x1 - x0, y1 - y0) or 1
        nx, ny = -(y1 - y0) / d * width / 2, (x1 - x0) / d * width / 2
        out.append([(x0 + nx, y0 + ny), (x1 + nx, y1 + ny), (x1 - nx, y1 - ny),
                    (x0 - nx, y0 - ny)])
    return out


STUDENT_FEET = STAND_TOP
HEAD_R = 0.32


def figure(at):
    """The student's limbs, as segments, and the middle of the head, standing at at."""
    sx, sy = at
    return ([((sx - 0.2, sy, STUDENT_FEET), (sx, sy, STUDENT_FEET + 1.15)),           # legs
             ((sx + 0.2, sy, STUDENT_FEET), (sx, sy, STUDENT_FEET + 1.15)),
             ((sx, sy, STUDENT_FEET + 1.15), (sx, sy, STUDENT_FEET + 2.05)),          # body
             ((sx, sy, STUDENT_FEET + 1.95), (sx - 0.35, sy + 0.15, STUDENT_FEET + 1.25)),  # arms
             ((sx, sy, STUDENT_FEET + 1.95), (sx + 0.35, sy + 0.15, STUDENT_FEET + 1.25))],
            (sx, sy, STUDENT_FEET + 2.4))


def quadrant_grid(v):
    """The z = 0 plane ruled in units where x is positive and y negative, out to the pedestal."""
    for x in range(0, 4):
        v.s.line([v.page((x, 0, 0)), v.page((x, -6, 0))], GREY3, 1.4, 0.6)
    for y in range(-1, -7, -1):
        v.s.line([v.page((0, y, 0)), v.page((3, y, 0))], GREY3, 1.4, 0.6)


def room(v, light=(), lines=(), solids=(), at=STAND):
    """The pedestal at at, a square on the floor, then the floor's shadows of the clear shapes in
    light, the lines, the solid shapes and the student on it. The things themselves go on top,
    drawn by the caller."""
    (cx, cy), hw = at, STAND_HALF
    square = [v.page((cx + dx, cy + dy, FLOOR)) for dx, dy in
              ((-hw, -hw), (hw, -hw), (hw, hw), (-hw, hw))]
    v.s.line(square, None, fill="#e4e5e5", closed=True)
    v.s.marker(square, "#e4e5e5", nib=12, opacity=0.5, layers=1)
    v.s.line(square, PEN, 2.0, 0.85, closed=True)
    floor = []
    v.s.cast([[v.page(landing(q)) for q in shape] for shape in light], sigma=5, alpha=0.2)
    for a, b in lines:
        floor += stroke_shadows(v, a, b, 6)
    floor += [[v.page(p) for p in map(landing, shape) if p] for shape in solids]
    limbs, head = figure(at)
    for a, b in limbs:
        floor += stroke_shadows(v, a, b, 7)
    head = landing(head)
    floor.append([v.page((head[0] + HEAD_R * 1.25 * math.cos(u), head[1] + HEAD_R * math.sin(u),
                          head[2])) for u in (i * math.pi / 12 for i in range(24))])
    v.s.cast(floor, sigma=3, alpha=0.5)


CAP, BRIM = "#7b5ea7", "#55407a"
RED_CAP, BLUE_CAP = (MARKER_RED, "#9e2f1f"), (BLUE, "#1f4f85")   # in an alliance's area
CAP_TILT = math.radians(25)        # the brim, tilted up from level, facing +y


def student(v, look, at=STAND, cap=(CAP, BRIM)):
    """The student on the pedestal at at, in a cap, looking at look, a point in space."""
    limbs, head = figure(at)
    for a, b in limbs:
        v.s.ink_line(v.page(a), v.page(b), width=4.2, over=2, passes=1, wobble=0.8)
    v.s.construct(v.page(head), v.page(look))                 # where the student is looking
    head_in_cap(v, head, cap)


def head_in_cap(v, head, cap=(CAP, BRIM)):
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
    centre, rb = at(head, (h * HEAD_R, up)), HEAD_R * math.sqrt(1 - h * h)

    def band(phi, r=rb):
        return at(centre, (r * math.cos(phi), side), (r * math.sin(phi), fwd))

    span = [math.radians(d) for d in range(35, 146, 5)]
    brim = [band(a) for a in reversed(span)] + [
        band(a, rb + 0.65 * HEAD_R * math.sin(math.pi * (a - span[0]) / (span[-1] - span[0])))
        for a in span]
    v.s.line([v.page(q) for q in brim], PEN, 2.4, 0.9, fill=cap[1], closed=True)

    c, r = v.page(head), HEAD_R * v.k
    v.s.line(ellipse(c, r), PEN, 3.4, 0.95, fill=PAPER, closed=True)
    steps = [math.radians(d) for d in range(0, 360, 2)]
    seen = [dot(tuple(a - b for a, b in zip(band(a), head)), toward) >= 0 for a in steps]
    first = next(i for i in range(len(steps)) if seen[i] and not seen[i - 1])
    arc = []
    for i in range(len(steps)):                   # the band's near half, in order
        j = (first + i) % len(steps)
        if not seen[j]:
            break
        arc.append(v.page(band(steps[j])))
    lean = tuple(a - dot(up, toward) * b for a, b in zip(up, toward))
    top = v.page(at(head, (HEAD_R / math.sqrt(dot(lean, lean)), lean)))

    def angle(p):
        return math.atan2(p[1] - c[1], p[0] - c[0])

    a0, a1, at_top = angle(arc[-1]), angle(arc[0]), angle(top)
    ccw = (a1 - a0) % (2 * math.pi)
    if (at_top - a0) % (2 * math.pi) > ccw:      # round the outline the other way, by the top
        ccw -= 2 * math.pi
    rim = [(c[0] + r * math.cos(a0 + ccw * i / 40), c[1] + r * math.sin(a0 + ccw * i / 40))
           for i in range(1, 40)]
    v.s.line(arc + rim, PEN, 2.6, 0.95, fill=cap[0], closed=True)


def sheet_in_room(v, z_under=False, upright=False):
    """sheet_axes in the room, its shadows cast by the light."""
    edge = 3.5
    corners = [v.turn(q) for q in ((0, 0, 0), (edge, 0, 0), (edge, edge, 0), (0, edge, 0))]
    lines = [(v.turn((-3.3, 0, 0)), v.turn((3.4, 0, 0))),
             (v.turn((0, -3.3, 0)), v.turn((0, 3.4, 0))),
             (v.turn((0, 0, 0)), v.turn((0, 0, 2.6)))]
    room(v, light=[corners], lines=lines)
    quadrant_grid(v)
    sheet_axes(v, z_under=z_under, drop=False, upright=upright)
    student(v, v.turn((STAND[0], 1.75, 0)))                  # gazing along x = 1.5


def e035_xy():
    s = Sketch(1000, 1000, 43)
    sheet_axes(Iso(s, (430, 560), 110, right=TOP_RIGHT, up=TOP_UP), z=False)
    s.lines((760, 820), ["+x to the right,", "+y up the page"], 30)
    s.save("fig-e035-xy")


def e035_xyz():
    s = Sketch(1450, 830, 47)
    sheet_in_room(Iso(s, (760, 330), 92))
    s.text((1050, 760), "+x to the person's right, +y away from them, +z up", 30)
    s.save("fig-e035-xyz")


def e035_flip():
    s = Sketch(1500, 910, 53)
    v = Iso(s, (660, 430), 92)
    s.line([v.p((-5.2, 0, 0)), v.p((6.4, 0, 0))], GREY3, 1.6, 0.7)   # the line it turns on
    sheet_in_room(v)
    turn = [v.p((4.9, 3.5 * math.cos(math.radians(a)), 3.5 * math.sin(math.radians(a))))
            for a in range(6, 175, 6)]           # the far edge's path, up, over and toward you
    s.line(turn, MARKER_ORANGE, 9, 0.85)
    s.line(turn, PEN, 1.6, 0.8)
    s.marker_arrow(turn[-3], turn[-1], MARKER_ORANGE, nib=9)
    s.text((750, 80), "a clear sheet, ready to flip over, top to bottom", 34)
    s.lines((1130, 820), ["it turns on the x axis:", "the far edge comes up and over, toward you"],
            26)
    s.save("fig-e035-flip")


def iso_stick(v, c):
    """One stick on a gamepad lying flat, seen in the same view."""
    ring = [v.p((c[0] + 0.42 * math.cos(a), c[1] + 0.42 * math.sin(a), PAD_TOP))
            for a in (i * math.pi / 24 for i in range(48))]
    cap = [v.p((c[0] + 0.22 * math.cos(a), c[1] + 0.22 * math.sin(a), STICK_Z))
           for a in (i * math.pi / 24 for i in range(48))]
    v.s.marker(ring, GREY4, nib=10, opacity=0.8)
    v.s.line(ring, PEN, 2.0, 0.9, closed=True)
    v.s.line([v.p((c[0], c[1], PAD_TOP)), v.p((c[0], c[1], STICK_Z))], GREY1, 8, 0.9)
    v.s.marker(cap, GREY2, nib=8, opacity=0.95)
    v.s.line(cap, PEN, 1.8, 0.9, closed=True)


PAD_MIDDLE = (1.85, -0.5)                  # the gamepad's middle, from the left stick at 0
PAD_TOP = 0.45                             # the gamepad's top, its bottom on the floor
STICK_Z = PAD_TOP + 0.3                    # the top of a stick, where its arrows are drawn


def gamepad_in_room(pad, lines, grid=True, at=STAND):
    """A gamepad lying flat in the room, its left stick at 0, the pedestal at at, and the floor's
    shadows of it and of lines."""
    s = pad.s
    w, h = 7.4, 3.2                        # the gamepad's width and depth, in the sheet's units
    middle = PAD_MIDDLE
    flat = [(middle[0] + u * w / 1000, middle[1] - v * h / 430)
            for u, v in gamepad_outline(0, 0, 1000, 430)]
    room(pad, lines=lines, solids=[[(x, y, z) for x, y in flat] for z in (PAD_TOP, 0)], at=at)
    if grid:
        quadrant_grid(pad)
    outline = [pad.p((x, y, PAD_TOP)) for x, y in flat]    # gamepad_outline's page, laid flat
    under = [pad.p((x, y, 0)) for x, y in flat]            # its bottom, for its sides
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


def e035_flipped():
    s = Sketch(1500, 1940, 54)
    flipped = Iso(s, (864, 344), 150, turn=lambda q: (q[0], -q[1], -q[2]))
    sheet_in_room(flipped, z_under=True, upright=True)
    pad = Iso(s, (750, 1210), 150)         # the left stick at 0, so the student stands as before
    middle = PAD_MIDDLE
    arrows = [((0.5, 0, STICK_Z), (2.2, 0, STICK_Z)), ((0, -0.5, STICK_Z), (0, -2.9, STICK_Z))]
    gamepad_in_room(pad, arrows)
    s.marker_arrow(pad.p(arrows[0][0]), pad.p(arrows[0][1]), AXIS_X, nib=7)
    s.marker_arrow(pad.p(arrows[1][0]), pad.p(arrows[1][1]), AXIS_Y, nib=7)
    pad.word((2.65, 0, STICK_Z), X3, Y3, "+x", 0.42)
    pad.word((0.6, -3.2, STICK_Z), X3, Y3, "+y", 0.42)
    student(pad, (STAND[0], middle[1], 0))
    s.text((750, 80), "the sheet flipped over", 50)
    s.text((750, 960), "a gamepad's left stick, the same way round", 50)
    s.lines((750, 1830), ["The axes on the sticks on gamepads are flipped upside",
                          "down from what people normally expect."], 44)
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
    s.text((c[0], 70), "the left stick, from above", 34)
    s.save("fig-e035-stick")


def tank_top(s, c, k):
    """The tank of the 3D drawings from above, its nose up the page, k pixels to its unit. Gives
    back the x of the outside of its left and right wheels."""
    w, h = TANK_WIDE * k, TANK_LONG * k
    out = (TANK_WIDE / 2 + 0.04 + WHEEL_W / 2) * k
    for dx in (-out, out):
        for dy in (-WHEELS_X * k, WHEELS_X * k):
            wheel_top(s, c[0] + dx, c[1] + dy, WHEEL_W * k, 2 * WHEEL_R * k)
    frame_top(s, c[0] - w / 2, c[1] - h / 2, w, h, nose=False)
    top = c[1] - h / 2
    tri = [(c[0] - 0.32 * k, top), (c[0], top - 0.42 * k), (c[0] + 0.32 * k, top)]
    s.marker(tri, MARKER_RED, nib=14 * k / 200, opacity=0.8)
    s.ink_poly(tri, width=2.2)
    edge = out + WHEEL_W * k / 2
    return c[0] - edge, c[0] + edge


def e035_robot():
    s = Sketch(1000, 1000, 61)
    c, k = (560, 560), 200                 # the middle, and pixels to the robot's unit
    tank_top(s, c, k)
    s.text((c[0] - 80, c[1] - TANK_LONG * k / 2 - 20), "nose", 32, MARKER_RED, "end")
    axis(s, c, (c[0], c[1] - 400), "+x: forward, out the nose", (c[0] + 20, c[1] - 420), "start",
         color=AXIS_X)
    axis(s, c, (c[0] - 400, c[1]), "+y: left", (c[0] - 400, c[1] - 26), "start", color=AXIS_Y)
    s.text((500, 960), "the robot, from above", 34)
    s.save("fig-e035-robot")


def e035_speeds():
    s = Sketch(1100, 820, 67)
    c = (550, 400)
    edges = tank_top(s, c, 150)
    for name, x in zip(("left", "right"), edges):
        out = -1 if name == "left" else 1
        s.marker_arrow((x + out * 50, c[1] + 120), (x + out * 50, c[1] - 160), MARKER_RED, nib=9)
        s.lines((x + out * 80, c[1] - 120), [f"{name}Speed", "1.0"], 30,
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
    rc = (1640, 440)
    x = tank_top(s, rc, 110)[0] - 40
    s.marker_arrow((x, rc[1] + 90), (x, rc[1] - 130), MARKER_RED, nib=9)
    s.text((rc[0], 690), "leftSpeed: 1.0, full speed forward", 30, code=False)
    s.save("fig-e035-switch")



# -- E035 from where the student stands: each 2D drawing again in the room, the student on the
# pedestal behind it. The robot is turned into the room, its x onto the room's +y and its y onto
# the room's -x, so its x runs up the page to the right, its y up to the left, and the student
# behind it faces +y as in the drawings before.
AHEAD = (0.0, -5.5)                        # the student's place, behind 0 and facing +y, on
                                           # the line x = 0 they gaze along


def floor_grid(v, xs, ys):
    """The z = 0 plane ruled in units, x from xs[0] to xs[1] and y from ys[0] to ys[1]."""
    for x in range(xs[0], xs[1] + 1):
        v.s.line([v.page((x, ys[0], 0)), v.page((x, ys[1], 0))], GREY3, 1.4, 0.6)
    for y in range(ys[0], ys[1] + 1):
        v.s.line([v.page((xs[0], y, 0)), v.page((xs[1], y, 0))], GREY3, 1.4, 0.6)


def flat_axis(v, end, color, name, ticks=True):
    """An axis lying at z 0 from -end to end, ticked and numbered in units, named at its + end."""
    a = tuple(-0.97 * c for c in end)
    v.s.marker_arrow(v.p(a + (0,)), v.p(tuple(end) + (0,)), color, nib=7)
    along = (1, 0) if end[0] else (0, 1)
    for n in (-3, -2, -1, 1, 2, 3):
        c = (along[0] * n, along[1] * n)
        tick = (0.13 * along[1], 0.13 * along[0])
        v.s.ink_line(v.p((c[0] - tick[0], c[1] - tick[1], 0)),
                     v.p((c[0] + tick[0], c[1] + tick[1], 0)), width=2.0, over=2, passes=1)
        at = (n, -0.42, 0) if along[0] else (-0.5, n, 0)
        v.word(at, X3, Y3, f"+{n}" if n > 0 else f"-{-n}", 0.3)
    nx, ny = (end[0] + 0.45, 0) if along[0] else (0, end[1] + 0.4)
    v.word((nx, ny, 0), X3, Y3, name, 0.42)


def e035_numberline_3d():
    s = Sketch(1450, 770, 73)
    v = Iso(s, (820, 300), 92)
    line = ((-3.4, 0, 0), (3.5, 0, 0))
    room(v, lines=[line], at=AHEAD)
    floor_grid(v, (-3, 3), (-6, 1))
    flat_axis(v, (3.5, 0), AXIS_X, "+x")
    v.word((-0.0, -0.42, 0), X3, Y3, "0", 0.3)
    student(v, (0, 0, 0), AHEAD)
    s.text((1080, 690), "+x is to the person's right", 30)
    s.save("fig-e035-numberline-3d")


def e035_xy_3d():
    s = Sketch(1450, 820, 79)
    v = Iso(s, (820, 330), 92)
    lines = [((-3.4, 0, 0), (3.5, 0, 0)), ((0, -3.4, 0), (0, 3.5, 0))]
    room(v, lines=lines, at=AHEAD)
    floor_grid(v, (-3, 3), (-6, 3))
    sheet = [v.p(q) for q in ((0, 0, 0), (3.5, 0, 0), (3.5, 3.5, 0), (0, 3.5, 0))]
    v.s.line(sheet, None, fill=SHEET, fill_alpha=0.45, closed=True)
    v.s.line(sheet, GREY3, 2.0, 0.8, closed=True)
    for a, b in (((1.4, 3.3), (3.1, 3.3)), ((3.3, 2.0), (3.3, 3.1))):   # glare by a corner
        v.s.line([v.p((*a, 0)), v.p((*b, 0))], "#ffffff", 5, 0.8)
    flat_axis(v, (3.5, 0), AXIS_X, "x")
    flat_axis(v, (0, 3.5), AXIS_Y, "y")
    v.s.dot(v.p((0, 0, 0)), 7)
    v.word((-0.38, -0.38, 0), X3, Y3, "0", 0.3)
    student(v, (0, 0, 0), AHEAD)
    s.text((1080, 730), "+x to the person's right, +y away from them", 30)
    s.save("fig-e035-xy-3d")


def e035_stick_3d():
    s = Sketch(1600, 860, 83)
    pad = Iso(s, (700, 400), 92)
    axes = [((0.5, 0, STICK_Z), (2.2, 0, STICK_Z)), ((0, -0.5, STICK_Z), (0, -2.9, STICK_Z))]
    moves = [((-1.1, 0.4, STICK_Z), (-1.1, 2.4, STICK_Z)), ((-1.1, -0.4, STICK_Z), (-1.1, -1.9, STICK_Z))]
    gamepad_in_room(pad, axes + moves)
    s.marker_arrow(pad.p(axes[0][0]), pad.p(axes[0][1]), AXIS_X, nib=7)
    s.marker_arrow(pad.p(axes[1][0]), pad.p(axes[1][1]), AXIS_Y, nib=7)
    pad.word((2.65, 0, STICK_Z), X3, Y3, "+x", 0.42)
    pad.word((0.6, -3.2, STICK_Z), X3, Y3, "+y", 0.42)
    for (a, b), words in zip(moves, (("pushed away", "-1.0"), ("pulled back", "+1.0"))):
        s.marker_arrow(pad.p(a), pad.p(b), MARKER_ORANGE, nib=8)
        y = 1.9 if b[1] > 0 else -1.2                      # beside the arrow, clear of the head
        pad.word((-2.4, y, STICK_Z), X3, Y3, words[0], 0.34)
        pad.word((-2.4, y - 0.45, STICK_Z), X3, Y3, words[1], 0.34)
    student(pad, (STAND[0], PAD_MIDDLE[1], 0))
    s.text((800, 800), "the left stick, from where the person stands", 30)
    s.save("fig-e035-stick-3d")


def turned(q):
    """A point in the robot's coordinates, turned into the room's."""
    return (-q[1], q[0], q[2])


TANK_LONG, TANK_WIDE, TANK_LOW, TANK_HIGH = 2.2, 1.5, 0.15, 0.65      # the body, robot's units
WHEEL_R, WHEEL_W, WHEELS_X = 0.3, 0.24, 0.72                            # the wheels


def tank_parts(scale=1.0):
    """The tank in room coordinates, scale times the size: the body's corners, each wheel's two
    rims, left wheels first, and the nose."""
    hx, hy = TANK_LONG / 2, TANK_WIDE / 2
    body = [turned((x, y, z)) for x in (-hx, hx) for y in (-hy, hy) for z in (TANK_LOW, TANK_HIGH)]
    wheels = []
    for side in (1, -1):                                      # left, the far side, first
        for x in (WHEELS_X, -WHEELS_X):
            rims = [[turned((x + WHEEL_R * math.cos(a), side * y, WHEEL_R + WHEEL_R * math.sin(a)))
                     for a in (i * math.pi / 16 for i in range(32))]
                    for y in (hy + 0.04, hy + 0.04 + WHEEL_W)]
            wheels.append((side, rims))
    nose = [turned(q) for q in ((hx - 0.05, 0.32, TANK_HIGH), (hx + 0.42, 0, TANK_HIGH),
                                (hx - 0.05, -0.32, TANK_HIGH))]

    def big(q):
        return tuple(scale * c for c in q)
    return ([big(q) for q in body], [(side, [[big(q) for q in rim] for rim in rims])
                                     for side, rims in wheels], [big(q) for q in nose])


def tank_shadows(scale=1.0):
    """The tank's solid shapes, for the floor's shadows."""
    body, wheels, nose = tank_parts(scale)
    faces = [[body[i] for i in f] for f in ((0, 2, 6, 4), (1, 3, 7, 5), (0, 1, 3, 2), (4, 5, 7, 6),
                                             (0, 1, 5, 4), (2, 3, 7, 6))]
    return faces + [rim for _, rims in wheels for rim in rims] + [nose]


def wheel(v, rims, alpha=1.0):
    """A wheel: its tread, the hull of both rims, then the rim toward the viewer, and a hub."""
    near = rims[1] if rims[1][0][0] > rims[0][0][0] else rims[0]
    v.s.line(hull([v.p(q) for rim in rims for q in rim]), PEN, 2.0, 0.9, fill="#3c3f42",
             fill_alpha=alpha, closed=True)
    v.s.line([v.p(q) for q in near], PEN, 2.0, 0.9, fill="#5d6164", fill_alpha=alpha, closed=True)
    hub = tuple(sum(c) / len(near) for c in zip(*near))
    v.s.dot(v.p(hub), 5, GREY2)


def tank(v, scale=1.0, inside=lambda: None, alpha=0.45):
    """The tank, see-through unless alpha is 1, drawn far to near: the left wheels, the body, what
    inside draws, the nose, the right wheels."""
    body, wheels, nose = tank_parts(scale)
    for side, rims in wheels:
        if side == 1:
            wheel(v, rims, alpha)
    for f, color in (((0, 1, 3, 2), "#8d9194"),         # the back, toward the student, in shade
                     ((0, 1, 5, 4), "#b9bcbe"),         # the right side, half lit
                     ((1, 3, 7, 5), "#e4e5e5")):        # the top, lit
        pts = [v.p(body[i]) for i in f]
        v.s.line(pts, None, fill=color, fill_alpha=alpha, closed=True)
        v.s.line(pts, PEN, 2.0, 0.85, closed=True)
    inside()
    v.s.marker([v.p(q) for q in nose], MARKER_RED, nib=12, opacity=0.8)
    v.s.ink_poly([v.p(q) for q in nose], width=2.0)
    for side, rims in wheels:
        if side == -1:
            wheel(v, rims, alpha)


TANK_AXES = [((0, 0, 0), (3.3, 0, 0)), ((0, 0, 0), (0, 3.0, 0))]       # the robot's +x and +y


def robot_axes(v):
    """The robot's +x and +y from 0, at z 0, named."""
    for (a, b), color in zip(TANK_AXES, (AXIS_X, AXIS_Y)):
        v.s.marker_arrow(v.p(turned(a)), v.p(turned(b)), color, nib=7)
    v.word((0.3, 3.55, 0), X3, Y3, "+x: forward,", 0.42, anchor="start")
    v.word((0.3, 3.0, 0), X3, Y3, "out the nose", 0.42, anchor="start")
    v.word((-3.25, 0, 0), X3, Y3, "+y: left", 0.42, anchor="end")
    v.s.dot(v.p((0, 0, 0)), 7)


def tank_room(v, axes=True, at=AHEAD, speeds=False, left_only=False):
    """The see-through tank in the room with the student behind it, looking at its 0, and its +x
    and +y at z 0 if axes."""
    lines = [(turned(a), turned(b)) for a, b in TANK_AXES] if axes else []
    arrows = []
    if speeds:
        for side in ((1,) if left_only else (1, -1)):
            y = side * (TANK_WIDE / 2 + WHEEL_W + 0.45)
            arrows.append((side, turned((-0.9, y, WHEEL_R)), turned((1.5, y, WHEEL_R))))
    room(v, lines=lines + [(a, b) for _, a, b in arrows], solids=tank_shadows(), at=at)
    floor_grid(v, (-3, 2), (-6, 3))
    if axes:
        robot_axes(v)
    tank(v)
    for side, a, b in arrows:
        v.s.marker_arrow(v.p(a), v.p(b), MARKER_RED, nib=9)
        name = "leftSpeed" if side == 1 else "rightSpeed"
        v.word(turned((-1.45, side * (TANK_WIDE / 2 + WHEEL_W + 0.95), WHEEL_R)), X3, Y3,
               f"{name}  1.0", 0.3)                    # behind its arrow's tail, to the outside
    student(v, (0, 0, 0), at)


def e035_robot_3d():
    s = Sketch(1450, 800, 89)
    tank_room(Iso(s, (800, 380), 110))
    s.text((725, 70), "the robot with a person standing behind it", 30)
    s.save("fig-e035-robot-3d")


FRAME_UNIT = 0.55                        # one unit of the robot's grid, in the room's units
FRAME_EDGE = 3.5                         # the robot's grid runs from -FRAME_EDGE to +FRAME_EDGE


def grid_top(x, y):
    """A point of the robot's grid, in its units, on the tank's top, turned into the room's."""
    return turned((FRAME_UNIT * x, FRAME_UNIT * y, TANK_HIGH))


FRAME_SHEET = [grid_top(x, y) for x, y in ((-FRAME_EDGE, -FRAME_EDGE), (FRAME_EDGE, -FRAME_EDGE),
                                            (FRAME_EDGE, FRAME_EDGE), (-FRAME_EDGE, FRAME_EDGE))]


def robot_frame(v):
    """The xy grid of fig-e035-xy-3d on the tank's top, its 0 at the top's centre, a unit
    FRAME_UNIT, +x out the nose and +y to the left, on a ruled sheet 0.8 opaque; its words in
    fig-e035-xy-3d's proportions, printed square to the room."""
    u, e = FRAME_UNIT, FRAME_EDGE
    sheet = [v.p(q) for q in FRAME_SHEET]
    v.s.line(sheet, None, fill=SHEET, fill_alpha=0.8, closed=True)
    for n in range(-3, 4):
        v.s.line([v.p(grid_top(n, -e)), v.p(grid_top(n, e))], GREY3, 1.4, 0.6)
        v.s.line([v.p(grid_top(-e, n)), v.p(grid_top(e, n))], GREY3, 1.4, 0.6)
    v.s.line(sheet, GREY3, 2.0, 0.8, closed=True)
    for a, b in (((1.4, 3.3), (3.1, 3.3)), ((3.3, 2.0), (3.3, 3.1))):   # glare by a corner
        v.s.line([v.p(grid_top(*a)), v.p(grid_top(*b))], "#ffffff", 5, 0.8)
    for end, color in (((e, 0), AXIS_X), ((0, e), AXIS_Y)):
        v.s.marker_arrow(v.p(grid_top(-0.97 * end[0], -0.97 * end[1])), v.p(grid_top(*end)),
                         color, nib=7)
        along = (1, 0) if end[0] else (0, 1)
        for n in (-3, -2, -1, 1, 2, 3):
            c, t = (along[0] * n, along[1] * n), (0.13 * along[1], 0.13 * along[0])
            v.s.ink_line(v.p(grid_top(c[0] - t[0], c[1] - t[1])),
                         v.p(grid_top(c[0] + t[0], c[1] + t[1])), width=2.0, over=2, passes=1)
            at = grid_top(n, -0.42) if along[0] else grid_top(-0.5, n)
            v.word(at, X3, Y3, f"+{n}" if n > 0 else f"-{-n}", 0.3 * u)
    v.word(grid_top(e + 0.45, 0), X3, Y3, "+x", 0.42 * u)
    v.word(grid_top(0, e + 0.4), X3, Y3, "+y", 0.42 * u)
    v.s.dot(v.p(grid_top(0, 0)), 7)
    v.word(grid_top(-0.38, -0.38), X3, Y3, "0", 0.3 * u)


def e035_robot_frame_3d():
    s = Sketch(1450, 800, 107)
    v = Iso(s, (800, 380), 110)
    room(v, solids=tank_shadows() + [FRAME_SHEET], at=AHEAD)
    floor_grid(v, (-3, 2), (-6, 3))
    tank(v, alpha=1.0)
    robot_frame(v)                       # on the top, so over the near wheels and the nose
    student(v, turned((0, 0, TANK_HIGH)), AHEAD)
    s.text((725, 70), "the robot's reference frame, attached to the top of the robot", 30)
    s.save("fig-e035-robot-frame-3d")


def e035_speeds_3d():
    s = Sketch(1450, 800, 97)
    tank_room(Iso(s, (800, 380), 110), speeds=True)
    s.text((725, 70), "forward is +, and 1.0 is full speed", 30)
    s.save("fig-e035-speeds-3d")


def e035_switch_3d():
    """The stick pushed away, the line of code, and the left wheels going forward, stacked top to
    bottom."""
    s = Sketch(1500, 1920, 101)
    pad, tank_at = (620, 380), (820, 1420)
    arrows = [((750, 690), (750, 800)), ((750, 1080), (750, 1190))]
    code, minus, speed_words = (750, 900), (750, 1040), (1010, 1860)
    v = Iso(s, pad, 92)
    away = ((0, 0.4, STICK_Z), (0, 2.6, STICK_Z))
    gamepad_in_room(v, [away])
    s.marker_arrow(v.p(away[0]), v.p(away[1]), MARKER_ORANGE, nib=9)
    v.word((0, 4.2, STICK_Z), X3, Y3, "pushed away", 0.6)
    v.word((0, 3.3, STICK_Z), X3, Y3, "leftStickY: -1.0", 0.5, code=True)
    student(v, (STAND[0], PAD_MIDDLE[1], 0))
    for a, b in arrows:
        s.marker_arrow(a, b, MARKER_ORANGE, nib=12)
    box = rect(code[0] - 590, code[1] - 80, 1180, 140)
    s.marker(box, GREY1, nib=20, opacity=0.5)
    s.ink_poly(box)
    s.text((code[0], code[1] + 16), "double leftSpeed = -leftStickY;", 50, code=True)
    s.text(minus, "the minus sign turns it around", 60)
    tank_room(Iso(s, tank_at, 110), axes=False, speeds=True, left_only=True)
    s.text(speed_words, "leftSpeed: 1.0, full speed forward", 60)
    s.save("fig-e035-switch-3d")


# -- E035's drivetrain: the rear left wheel's drive shaft, gearbox, motor shaft and motor, inside
# the see-through tank at three times its size, each in the robot's units along the wheel's axle, from
# the wheel in toward the middle: (y where it starts, y where it ends, its half-width, its outline).
DRIVE = [("drive shaft", 0.79, 0.47, 0.05, "round", "#c9cccf", "#e4e5e5"),
         ("gear box", 0.47, 0.29, 0.15, "square", "#d9a521", "#efc75e"),
         ("motor shaft", 0.29, 0.03, 0.035, "round", "#c9cccf", "#e4e5e5"),
         ("motor", 0.03, -0.29, 0.15, "round", "#3c3f42", "#5d6164")]
DRIVE_SCALE = 3.0
SPIN_FROM = 160                            # degrees round the axle from the nose: an arrow's tail
BEHIND = (0.0, -8.5)                       # the student's place, back from the bigger tank
CALLOUTS = {"wheel": (True, -40, -150), "drive shaft": (False, -110, 150),   # above the part?
            "gear box": (True, -40, -150), "motor shaft": (True, 20, -110),   # and where its
            "motor": (False, None, None)}                       # name goes; None: straight back


def rear_left(q):
    """A point by the rear left wheel's axle, in the robot's units, turned and scaled into the
    room's."""
    return tuple(DRIVE_SCALE * c for c in turned((-WHEELS_X + q[0], q[1], WHEEL_R + q[2])))


def drive_part(v, y0, y1, half, shape, side_fill, end_fill):
    """One part on the axle: its two ends, the hull of them filled, then the end toward the
    viewer, which is the one nearer the robot's middle."""
    if shape == "round":
        ring = [(half * math.cos(a), half * math.sin(a))
                for a in (i * math.pi / 16 for i in range(32))]
    else:
        ring = [(-half, -half), (half, -half), (half, half), (-half, half)]
    ends = [[rear_left((x, y, z)) for x, z in ring] for y in (y0, y1)]
    v.s.line(hull([v.p(q) for end in ends for q in end]), PEN, 2.0, 0.9, fill=side_fill,
             closed=True)
    v.s.line([v.p(q) for q in ends[1]], PEN, 2.0, 0.9, fill=end_fill, closed=True)


def spin(v, y, r):
    """An orange arrow round the axle at y, r from it, over the top toward the nose: forward."""
    pts = [v.p(rear_left((r * math.cos(a), y, r * math.sin(a))))
           for a in (math.radians(d) for d in range(SPIN_FROM, 15, -5))]
    v.s.line(pts, MARKER_ORANGE, 7, 0.85)
    v.s.marker_arrow(pts[-3], pts[-1], MARKER_ORANGE, nib=8)


def e035_motor_wheel_gear_box():
    s = Sketch(1720, 1160, 103)
    v = Iso(s, (1020, 590), 100)
    wheel_y, wheel_r = TANK_WIDE / 2 + 0.04 + WHEEL_W, WHEEL_R + 0.06

    def drivetrain():
        for _, y0, y1, half, shape, side_fill, end_fill in DRIVE:
            drive_part(v, y0, y1, half, shape, side_fill, end_fill)
        spin(v, wheel_y, wheel_r)                                      # the wheel
        for name, y0, y1, half, _, _, _ in DRIVE:            # the motor and gearbox stand still
            if name.endswith("shaft"):
                spin(v, (y0 + y1) / 2, half + 0.06)

    room(v, solids=tank_shadows(DRIVE_SCALE), at=BEHIND)
    floor_grid(v, (-4, 4), (-9, 5))
    tank(v, DRIVE_SCALE, drivetrain)
    tail = math.radians(SPIN_FROM)                         # looking at the wheel arrow's tail
    student(v, rear_left((wheel_r * math.cos(tail), wheel_y, wheel_r * math.sin(tail))), BEHIND)
    names = [("wheel", TANK_WIDE / 2 + 0.04 + WHEEL_W / 2, WHEEL_R)]
    names += [(name, (y0 + y1) / 2, half) for name, y0, y1, half, *_ in DRIVE]
    for name, y, half in names:
        up, out, drop = CALLOUTS[name]
        tip = v.p(rear_left((0, y, half if up else -half)))
        at = ((tip[0] + out, tip[1] + drop) if out is not None
              else v.p(rear_left((-1.1, y, -half))))       # behind it, past the tank's back
        s.ink_line((at[0], at[1] + (12 if up else -34)), tip, width=1.6, over=0, passes=1)
        s.text(at, name, 30)
    s.text((850, 70), "the rear left wheel's drivetrain, its shafts and wheel turning forward", 30)
    s.save("fig-e035-motor-wheel-gear-box")


# -- L140's field: BIOBUZZ, 2026-27, in Pedro's coordinates, a unit one 24 in tile. Each place and
# size is FIRST's, from the Competition Manual TU06 section 9 and the Event Field Setup Guide V1.0;
# the frame's and the HIVES' places, and the FLOWERS' seams, are read off the manual's Figures 9-2
# and 9-4. Pedro's (0, 0) is the corner where the red wall meets the audience's, +x from the red
# wall toward the blue and +y away from the audience: Pedro's Coordinates page turns FTC's field
# coordinates into its own as (y + 72, 72 - x), and FTC's put +y from the red wall to the blue
# and +x toward the audience.

def inches(*q):
    return tuple(c / 24 for c in q)


FIELD_TAPE = {   # each LOADING ZONE, 11 in deep and 23 in wide, open on its wall's side
    MARKER_RED: [inches(0, 96.5), inches(11, 96.5), inches(11, 119.5), inches(0, 119.5)],
    BLUE: [inches(144, 24.5), inches(133, 24.5), inches(133, 47.5), inches(144, 47.5)]}
GARDENS = {MARKER_RED: rect(*inches(0.5, 0, 23, 2)), BLUE: rect(*inches(120.5, 142, 23, 2))}
ALLIANCE_AREAS = {MARKER_RED: rect(*inches(-54, 23.5, 54, 97)),
                  BLUE: rect(*inches(144, 23.5, 54, 97))}
FRAME_BASE = rect(*inches(72 - 49.46 / 2, 72 - 38.95 / 2, 49.46, 38.95))
HIVES = {MARKER_RED: 58.5, BLUE: 85.5}    # each HIVE's x, in in; 42.91 in long along y
FLOWERS = [inches(48, 142), inches(142, 96), inches(2, 48), inches(96, 2)]


def field_floor(v):
    """The field flat on the floor: tiles, tape, the frame's base, the HIVES and the FLOWERS."""
    floor = [v.p((x, y, 0)) for x, y in ((0, 0), (6, 0), (6, 6), (0, 6))]
    v.s.line(floor, None, fill=GREY1, fill_alpha=0.45, closed=True)
    v.s.marker(floor, GREY1, nib=30, opacity=0.3, layers=1)
    for n in range(1, 6):
        v.s.line([v.p((n, 0, 0)), v.p((n, 6, 0))], GREY3, 1.2, 0.5)
        v.s.line([v.p((0, n, 0)), v.p((6, n, 0))], GREY3, 1.2, 0.5)
    for color, shape in ALLIANCE_AREAS.items():
        v.s.line([v.p(q + (0,)) for q in shape], color, 6, 0.8, closed=True)
    for color, pts in FIELD_TAPE.items():
        v.s.line([v.p(q + (0,)) for q in pts], color, 7, 0.9)
    for color, shape in GARDENS.items():
        v.s.line([v.p(q + (0,)) for q in shape], color, 2, 0.9, fill=color, closed=True)
    v.s.line([v.p(q + (0,)) for q in FRAME_BASE], GREY4, 2.4, 0.8, fill=GREY2, fill_alpha=0.3,
             closed=True)
    for color, x in HIVES.items():
        hive = [v.p(q + (0,)) for q in rect(*inches(x - 10, 72 - 42.91 / 2, 20, 42.91))]
        v.s.marker(hive, color, nib=14, opacity=0.5)
        v.s.ink_poly(hive, width=2.0)
        for dy in (-9.42, 9.42):                    # its two CELLS, 18.84 in apart
            v.s.ink_poly([v.p(q + (0,)) for q in rect(*inches(x - 8, 72 + dy - 6.02, 16, 12.04))],
                         width=1.4)
    for c in FLOWERS:
        ring = [v.p((c[0] + 0.1 * math.cos(u), c[1] + 0.1 * math.sin(u), 0))
                for u in (i * math.pi / 12 for i in range(24))]
        v.s.line(ring, PEN, 2.0, 0.9, fill=MARKER_YELLOW, closed=True)


def field_walls(v, h=12.3 / 24):
    """The walls, 12.3 in tall, clear enough to see the floor through."""
    corners = ((0, 0), (6, 0), (6, 6), (0, 6))
    for a, b in zip(corners, corners[1:] + corners[:1]):
        face = [v.p(a + (0,)), v.p(b + (0,)), v.p(b + (h,)), v.p(a + (h,))]
        v.s.line(face, None, fill=GREY2, fill_alpha=0.18, closed=True)
        v.s.ink_line(v.p(a + (h,)), v.p(b + (h,)), width=2.0, over=4)
        v.s.ink_line(v.p(a + (0,)), v.p(b + (0,)), width=2.4, over=4)
    for a in corners:
        v.s.ink_line(v.p(a + (0,)), v.p(a + (h,)), width=1.6, over=2, passes=1)


def pedro_axes(v, zero=None):
    """Pedro's +x and +y from (0, 0); zero, if given, places the (0, 0) label there with an arrow
    to it, where the corner's own place is taken."""
    o = v.p((0, 0, 0))
    v.s.marker_arrow(o, v.p((2.6, 0, 0)), AXIS_X, nib=6)
    v.s.marker_arrow(o, v.p((0, 2.6, 0)), AXIS_Y, nib=6)
    v.s.dot(o, 9)
    labels = [((2.6, -0.3, 0), "+x"), ((-0.3, 2.6, 0), "+y")]
    if zero:
        v.s.text(zero, "(0, 0)", 28, anchor="end", code=True)
        p0, p1 = (zero[0] + 6, zero[1] - 10), (o[0] - 14, o[1] - 6)
        n = math.dist(p0, p1)
        ux, uy, h = (p1[0] - p0[0]) / n, (p1[1] - p0[1]) / n, 14
        v.s.line([p0, p1], GREY1, 3, 0.9)
        v.s.line([(p1[0] - ux * h - uy * h * 0.5, p1[1] - uy * h + ux * h * 0.5), p1,
                  (p1[0] - ux * h + uy * h * 0.5, p1[1] - uy * h - ux * h * 0.5)], GREY1, 3, 0.9)
    else:
        labels.append(((-0.5, -0.5, 0), "(0, 0)"))
    for q, name in labels:
        x, y = v.p(q)
        v.s.text((x, y + 10), name, 28, code=name.startswith("("))


def l140_field_top():
    s = Sketch(1300, 1100, 83)
    v = Iso(s, (285, 860), 120, right=TOP_RIGHT, up=TOP_UP)
    s.shadow([v.p(q) for q in ((0.1, -0.12, 0), (6.1, -0.12, 0), (6.1, 5.88, 0), (0.1, 5.88, 0))],
             sigma=10, alpha=0.2)
    field_floor(v)
    field_walls(v)
    pedro_axes(v)
    x, y = v.p((-1.12, 3, 0))
    s.text((x, y), "red drive team", 30, MARKER_RED, rot=-90)
    x, y = v.p((7.12, 3, 0))
    s.text((x, y), "blue drive team", 30, BLUE, rot=90)
    x, y = v.p((3, -0.55, 0))
    s.text((x, y), "audience", 30, GREY4)
    x, y = v.p((3, 3.1, 0))
    s.text((x - 40, y - 120), "hives", 26)
    for q, (dx, dy), anchor in (((2, 6), (0, -24), "middle"), ((6, 4), (24, 8), "start")):
        x, y = v.p(q + (0,))
        s.text((x + dx, y + dy), "flower", 24, anchor=anchor)
    x, y = v.p((6.05, 6.05, 0))
    s.text((x, y - 4), "(144, 144) in", 24, anchor="start", code=True)
    s.lines((650, 1010), ["BIOBUZZ from above, in Pedro's coordinates"], 30)
    s.save("fig-l140-field-top")


def drivers_view(name, turn, at, words, cap, zero=None):
    """The field in Onshape's isometric view, turned so the drive team's student stands near the
    viewer at at, looking across the field."""
    s = Sketch(1450, 1000, 89)
    v = Iso(s, (780, 410), 108, turn=turn)
    room(v, at=at)
    field_floor(v)
    field_walls(v)
    pedro_axes(v, zero)
    student(v, (at[0], 0.0, 0.0), at, cap)
    s.lines((725, 900), words, 30)
    s.save(name)


def l140_field_red():
    # Pedro's +x away from the red drive team and +y to their left.
    drivers_view("fig-l140-field-red", lambda q: (3 - q[1], q[0] - 3, q[2]), (0.5, -4.4),
                 ["from the red drive team's place:", "+x points away, +y to the left"], RED_CAP)


def l140_field_blue():
    # Pedro's +x toward the blue drive team and +y to their right.
    drivers_view("fig-l140-field-blue", lambda q: (q[1] - 3, 3 - q[0], q[2]), (0.5, -4.4),
                 ["from the blue drive team's place:", "+x points toward you, +y to the right"],
                 BLUE_CAP, zero=(600, 80))


# -- E141 and E142: vectors, then how L140's two lines turn the stick on the field into the robot's
# own forward and strafe. One heading and one stick throughout E142, L140's answer run on them in
# the test harness on 2026-10-25: heading 60°, left_stick_y -0.8 and left_stick_x -0.4, so
# forward 0.64 and strafe 0.16, and robotForward 0.4586 and robotStrafe -0.4743. A slanted vector
# is drawn with its triangle in the frame being talked about: the field's legs in grey, the
# robot's forward in red and its left in green.

ROT_HEADING = math.radians(60)
ROT_FORWARD, ROT_STRAFE = 0.8 ** 2, 0.4 ** 2                    # L060's sign-keeping square
ROT_F = (math.cos(ROT_HEADING), math.sin(ROT_HEADING))           # the robot's forward, on the field
ROT_L = (-math.sin(ROT_HEADING), math.cos(ROT_HEADING))          # the robot's left, on the field
ROT_PIECES = {   # each field part, in the robot's frame: (along forward, along left)
    "forward": (ROT_FORWARD * math.cos(ROT_HEADING), -ROT_FORWARD * math.sin(ROT_HEADING)),
    "strafe": (ROT_STRAFE * math.sin(ROT_HEADING), ROT_STRAFE * math.cos(ROT_HEADING))}
ROT_ROBOT_FORWARD = ROT_PIECES["forward"][0] + ROT_PIECES["strafe"][0]
ROT_ROBOT_STRAFE = ROT_PIECES["forward"][1] + ROT_PIECES["strafe"][1]
ROBOT_FORWARD, ROBOT_LEFT, FIELD_LEG = MARKER_RED, AXIS_Y, GREY3
NOSE = MARKER_YELLOW            # apart from the red of the robot's forward
FIELD_GRID, ROBOT_GRID = ((1.0, 0.0), (0.0, 1.0)), (ROT_F, ROT_L)
FORMULA = {("forward", 0): "forward·cos θ", ("forward", 1): "-forward·sin θ",
           ("strafe", 0): "strafe·sin θ", ("strafe", 1): "strafe·cos θ"}


def frame_grid(v, axes, step):
    """A light grid about the robot's center, ruled every step along the two axes given."""
    ex, ey = axes
    reach = 2400 / v.k
    for n in range(-int(reach / step), int(reach / step) + 1):
        for a, b in ((ex, ey), (ey, ex)):
            c = plus((0, 0), b, n * step)
            v.s.line([v.p(plus(c, a, -reach) + (0,)), v.p(plus(c, a, reach) + (0,))], GREY2, 1.1,
                     0.45)


def callout(s, at, rows, to, anchor="start", size=26):
    """Words at at, with a thin arrow from them to the point to."""
    s.lines(at, rows, size, anchor=anchor, code=True)
    s.marker_arrow((at[0], at[1] - size * 0.4), to, GREY2, nib=3)


def dotted(s, p0, p1, color=GREY2):
    """A light dotted arrow from p0 to p1."""
    n = math.dist(p0, p1)
    ux, uy = (p1[0] - p0[0]) / n, (p1[1] - p0[1]) / n
    for d in np.arange(0, n - 14, 14):
        s.line([(p0[0] + ux * d, p0[1] + uy * d), (p0[0] + ux * (d + 6), p0[1] + uy * (d + 6))],
               color, 3, 0.9)
    h = 14
    s.line([(p1[0] - ux * h - uy * h * 0.5, p1[1] - uy * h + ux * h * 0.5), p1,
            (p1[0] - ux * h + uy * h * 0.5, p1[1] - uy * h - ux * h * 0.5)], color, 3, 0.9)


def formula_words(s, rows):
    """Each (words, at, anchor, to): the words, and a dotted arrow from them to the point to."""
    for words, at, anchor, to in rows:
        s.text(at, words, 26, anchor=anchor, code=True)
        dotted(s, (at[0], at[1] + 10), to)


def plus(a, b, k=1.0):
    return (a[0] + k * b[0], a[1] + k * b[1])


def vec(v, a, b, color, nib=8):
    v.s.marker_arrow(v.p(a + (0,)), v.p(b + (0,)), color, nib=nib)


def field_legs(v, a, b, nib=5):
    """b - a as a triangle on the field's axes: along field x, then along field y."""
    corner = (b[0], a[1])
    for p, q in ((a, corner), (corner, b)):
        if math.dist(p, q) > 1e-6:
            v.s.marker_line(v.p(p + (0,)), v.p(q + (0,)), FIELD_LEG, nib=nib)
    return corner


def robot_legs(v, a, b, nib=5):
    """b - a as a triangle on the robot's axes: along its forward, then along its left."""
    d = (b[0] - a[0], b[1] - a[1])
    corner = plus(a, ROT_F, d[0] * ROT_F[0] + d[1] * ROT_F[1])
    v.s.marker_line(v.p(a + (0,)), v.p(corner + (0,)), ROBOT_FORWARD, nib=nib)
    v.s.marker_line(v.p(corner + (0,)), v.p(b + (0,)), ROBOT_LEFT, nib=nib)


def say(v, q, text, size=28, dx=0, dy=0, anchor="middle", code=False):
    x, y = v.p(q + (0,))
    v.s.text((x + dx, y + dy), text, size, anchor=anchor, code=code)


def grid_axes(v, w, h):
    floor_grid(v, (0, w), (0, h))
    v.s.marker_arrow(v.p((0, 0, 0)), v.p((w + 0.5, 0, 0)), AXIS_X, nib=6)
    v.s.marker_arrow(v.p((0, 0, 0)), v.p((0, h + 0.5, 0)), AXIS_Y, nib=6)
    say(v, (w + 0.75, -0.08), "x", 32)
    say(v, (-0.25, h + 0.5), "y", 32)
    for n in range(1, w + 1):
        say(v, (n, -0.4), str(n), 24)
    for n in range(1, h + 1):
        say(v, (-0.3, n - 0.08), str(n), 24)


def e141_notation():
    s = Sketch(1600, 1000, 113)
    v = Iso(s, (300, 760), 150, right=TOP_RIGHT, up=TOP_UP)
    grid_axes(v, 6, 4)
    field_legs(v, (0, 0), (3, 2), nib=10)
    vec(v, (0, 0), (3, 2), MARKER_ORANGE, nib=9)
    say(v, (1.5, 0.1), "3 along x", 28)
    say(v, (3.15, 1), "2 along y", 28, anchor="start")
    say(v, (3.1, 2.1), "(3, 2)", 34, anchor="start", code=True)
    s.lines((800, 900), ["an arrow is written (x, y): how far along x, then how far along y"], 30)
    s.save("fig-e141-notation")


def e141_scale():
    s = Sketch(1600, 1000, 127)
    v = Iso(s, (300, 760), 150, right=TOP_RIGHT, up=TOP_UP)
    grid_axes(v, 6, 4)
    field_legs(v, (0, 0), (6, 4), nib=6)
    vec(v, (0, 0), (6, 4), MARKER_BLUE, nib=14)
    field_legs(v, (0, 0), (3, 2), nib=10)
    vec(v, (0, 0), (3, 2), MARKER_ORANGE, nib=8)
    say(v, (3.1, 2.1), "(3, 2)", 30, anchor="start", code=True)
    say(v, (6.1, 4.1), "2 × (3, 2) = (2·3, 2·2) = (6, 4)", 30, anchor="end", dx=-30, dy=-10,
        code=True)
    s.lines((800, 880), ["twice as long, the same way: multiply x and y by 2"], 30)
    s.text((800, 940), "half as long: 0.5 × (3, 2) = (1.5, 1)", 26, code=True)
    s.save("fig-e141-scale")


def e141_add():
    s = Sketch(1600, 1000, 131)
    v = Iso(s, (300, 760), 150, right=TOP_RIGHT, up=TOP_UP)
    grid_axes(v, 6, 4)
    a, b = (3, 1), (1, 2)
    tip = plus(a, b)
    field_legs(v, (0, 0), a, nib=8)
    field_legs(v, a, tip, nib=8)
    vec(v, (0, 0), tip, MARKER_BLUE, nib=10)
    vec(v, (0, 0), a, MARKER_ORANGE, nib=8)
    vec(v, a, tip, MARKER_ORANGE, nib=8)
    say(v, (1.5, 0.5), "(3, 1)", 30, dy=30, dx=40, code=True)
    say(v, (3.6, 2.0), "(1, 2)", 30, anchor="start", code=True)
    say(v, (2.0, 1.5), "(4, 3)", 30, anchor="end", dx=-20, code=True)
    s.lines((800, 880), ["end to end, (3, 1) then (1, 2) reaches (4, 3)",
                         "or add the parts: (3 + 1, 1 + 2) = (4, 3)"], 30)
    s.save("fig-e141-add")


def unit_circle(v, r=1.0, axes=True):
    """A circle of radius 1 about 0, with the x and y axes through it."""
    v.s.ink_circle(v.p((0, 0, 0)), r * v.k, width=2.2)
    if axes:
        v.s.marker_arrow(v.p((-1.3, 0, 0)), v.p((1.4, 0, 0)), AXIS_X, nib=6)
        v.s.marker_arrow(v.p((0, -1.3, 0)), v.p((0, 1.3, 0)), AXIS_Y, nib=6)
        for q, name in (((1.52, -0.03, 0), "x"), ((0.1, 1.24, 0), "y")):
            v.s.text(v.p(q), name, 32)


def angle_arc(v, c, r, a0, a1, color=MARKER_ORANGE, nib=7):
    pts = [v.p((c[0] + r * math.cos(a), c[1] + r * math.sin(a), 0))
           for a in np.linspace(a0, a1, 24)]
    v.s.line(pts, color, nib, 0.85)
    v.s.line(pts, PEN, 1.6, 0.8)
    v.s.marker_arrow(pts[-2], pts[-1], color, nib=nib)


def e142_unit_circle():
    s = Sketch(1200, 1000, 97)
    v = Iso(s, (480, 460), 300, right=TOP_RIGHT, up=TOP_UP)
    unit_circle(v)
    c, sn = ROT_F
    s.construct(v.p((c, sn, 0)), v.p((c, 0, 0)))
    s.construct(v.p((c, sn, 0)), v.p((0, sn, 0)))
    s.marker_line(v.p((0, 0, 0)), v.p((c, 0, 0)), AXIS_X, nib=14)
    s.marker_line(v.p((0, 0, 0)), v.p((0, sn, 0)), AXIS_Y, nib=14)
    s.marker_arrow(v.p((0, 0, 0)), v.p((c, sn, 0)), MARKER_ORANGE, nib=7)
    s.dot(v.p((c, sn, 0)), 10)
    angle_arc(v, (0, 0), 0.22, 0, ROT_HEADING, GREY3, nib=4)
    say(v, (0.25, 0.12), "60°", 28, anchor="start")
    say(v, (0.06, -0.14), f"cos 60° = {c:.2f}", 28, anchor="start")
    say(v, (-0.06, sn / 2), f"sin 60° = {sn:.2f}", 28, anchor="end")
    say(v, (c, sn), f"({c:.2f}, {sn:.2f})", 26, dx=20, dy=-20, anchor="start", code=True)
    s.lines((600, 930), ["a point 1 from the middle, at 60°: its x is cos 60° and its y is sin 60°"],
            28)
    s.save("fig-e142-unit-circle")


def e142_radian():
    s = Sketch(1200, 1000, 101)
    v = Iso(s, (480, 460), 300, right=TOP_RIGHT, up=TOP_UP)
    unit_circle(v)
    s.marker_line(v.p((0, 0, 0)), v.p((1, 0, 0)), GREY2, nib=10)
    say(v, (0.5, -0.1), "1", 30)
    pts = [v.p((math.cos(a), math.sin(a), 0)) for a in np.linspace(0, ROT_HEADING, 30)]
    s.line(pts, MARKER_ORANGE, 16, 0.85)
    s.line(pts, PEN, 1.6, 0.8)
    s.marker_line(v.p((0, 0, 0)), v.p(ROT_F + (0,)), GREY2, nib=6)
    tick = (math.cos(1.0), math.sin(1.0))         # 1 radian along the edge
    s.ink_line(v.p((0.93 * tick[0], 0.93 * tick[1], 0)), v.p((1.07 * tick[0], 1.07 * tick[1], 0)),
               width=2.4, over=2, passes=1)
    say(v, (1.14 * tick[0], 1.14 * tick[1]), "1 around", 24, anchor="start")
    say(v, (1.12 * math.cos(0.4), 1.12 * math.sin(0.4)), f"{ROT_HEADING:.2f} around the edge", 28,
        anchor="start")
    s.lines((600, 900), [f"60° is {ROT_HEADING:.2f} of the radius around the edge: "
                         f"{ROT_HEADING:.2f} radians",
                         "all the way round is 360°, or 2π = 6.28 radians"], 28)
    s.save("fig-e142-radian")


def robot_at(s, c, side, heading):
    """The robot from above at page point c, its nose at heading, field +x to the right."""
    s.turn = (c[0], c[1], 90 - math.degrees(heading))
    frame_top(s, c[0] - side / 2, c[1] - side / 2, side, side, nose=False)
    tri = [(c[0] - side * 0.22, c[1] - side / 2), (c[0], c[1] - side * 0.78),
           (c[0] + side * 0.22, c[1] - side / 2)]
    s.marker(tri, NOSE, nib=10, opacity=0.8)
    s.ink_poly(tri, width=2.2)
    s.turn = (0.0, 0.0, 0.0)


def field_corner_axes(s, o):
    s.marker_arrow(o, (o[0] + 130, o[1]), GREY3, nib=4)
    s.marker_arrow(o, (o[0], o[1] - 130), GREY3, nib=4)
    s.text((o[0] + 140, o[1] + 8), "field x", 22, anchor="start")
    s.text((o[0], o[1] - 150), "field y", 22)


def e142_robot_axes():
    s = Sketch(1600, 1050, 103)
    v = Iso(s, (820, 700), 450, right=TOP_RIGHT, up=TOP_UP)
    frame_grid(v, FIELD_GRID, 0.25)
    field_corner_axes(s, (90, 960))
    c, sn = ROT_F
    s.construct(v.p((-1.2, 0, 0)), v.p((1.3, 0, 0)))
    callout(s, (720, 830), ["(0, 0)"], (812, 708), anchor="end")
    robot_at(s, v.p((0, 0, 0)), 90, ROT_HEADING)
    field_legs(v, (0, 0), ROT_F, nib=7)
    field_legs(v, (0, 0), ROT_L, nib=7)
    vec(v, (0, 0), ROT_F, ROBOT_FORWARD)
    vec(v, (0, 0), ROT_L, ROBOT_LEFT)
    angle_arc(v, (0, 0), 0.3, 0, ROT_HEADING, MARKER_ORANGE, nib=5)
    say(v, (0.33, 0.12), "60°", 26, anchor="start")
    say(v, (c, 0), f"cos 60° = {c:.2f}", 24, dy=40, anchor="end")
    say(v, (c, sn / 2), f"sin 60° = {sn:.2f}", 24, dx=14, anchor="start")
    say(v, (-sn, 0), f"-sin 60° = {-sn:.2f}", 24, dy=40, anchor="start")
    say(v, (-sn, c / 2), f"cos 60° = {c:.2f}", 24, dx=-14, anchor="end")
    say(v, ROT_F, f"forward ({c:.2f}, {sn:.2f})", 28, dy=-24, code=True)
    say(v, ROT_L, f"left ({-sn:.2f}, {c:.2f})", 28, dy=-24, code=True)
    s.lines((880, 920), ["the robot's own forward and left, each 1 long,",
                         "drawn with their x and y on the field;",
                         "the grid is the field's x and y, moved to the robot's center"], 28)
    s.save("fig-e142-robot-axes")


# The worked drawings share one scale and one place for the robot, so a piece is the same size in
# each of them.
WORK_O, WORK_K = (560, 700), 900


def worked(seed, o=WORK_O, k=WORK_K, grid=ROBOT_GRID, step=0.1):
    s = Sketch(1600, 1050, seed)
    v = Iso(s, o, k, right=TOP_RIGHT, up=TOP_UP)
    frame_grid(v, grid, step)
    field_corner_axes(s, (90, 980))
    back, ahead = 500 / k, 560 / k
    for axis, color, name in ((ROT_F, ROBOT_FORWARD, "robot forward"),
                              (ROT_L, ROBOT_LEFT, "robot left")):
        s.construct(v.p((-back * axis[0], -back * axis[1], 0)),
                    v.p((ahead * axis[0], ahead * axis[1], 0)))
        say(v, (ahead * axis[0], ahead * axis[1]), name, 24, dy=-14)
    robot_at(s, v.p((0, 0, 0)), 90, ROT_HEADING)
    return s, v


def piece_at(start, part):
    """Where a robot-frame part (along forward, along left) laid from start ends."""
    return plus(plus(start, ROT_F, part[0]), ROT_L, part[1])


def e142_stick_field():
    s, v = worked(137, grid=FIELD_GRID)
    tip = (ROT_FORWARD, ROT_STRAFE)
    callout(s, (470, 830), ["(0, 0)"], (552, 708), anchor="end")
    field_legs(v, (0, 0), tip, nib=8)
    vec(v, (0, 0), tip, MARKER_BLUE, nib=10)
    say(v, (ROT_FORWARD / 2, 0), f"forward {ROT_FORWARD:.2f} along field x", 26, dy=40)
    say(v, (ROT_FORWARD, ROT_STRAFE / 2), f"strafe {ROT_STRAFE:.2f} along field y", 26, dx=18,
        anchor="start")
    say(v, tip, f"({ROT_FORWARD:.2f}, {ROT_STRAFE:.2f}) on the field", 28, dy=-40,
        anchor="end", code=True)
    s.lines((900, 900), ["the stick asks for (0.64, 0.16) on the field:",
                         "0.64 away from the red driver, 0.16 to the red driver's left;",
                         "the grid is the field's x and y, moved to the robot's center"], 28)
    s.save("fig-e142-stick-field")


def split(seed, name, field, part, words, caption, tip_at, o=WORK_O, k=WORK_K, step=0.1):
    """One field part drawn as its two pieces in the robot's frame."""
    s, v = worked(seed, o, k, step=step)
    mid = plus((0, 0), ROT_F, part[0])
    vec(v, (0, 0), field, MARKER_BLUE, nib=10)
    vec(v, (0, 0), mid, ROBOT_FORWARD, nib=8)
    vec(v, mid, field, ROBOT_LEFT, nib=8)
    for q, text, kw in words(v, mid):
        say(v, q, text, **kw)
    at, anchor = tip_at
    callout(s, at, [f"({part[0]:.2f}, {part[1]:.2f})", "in the robot's frame"], v.p(field + (0,)),
            anchor)
    s.lines((900, 920), caption, 28)
    s.save(name)


def e142_forward_split():
    f, l = ROT_PIECES["forward"]

    def words(v, mid):
        angle_arc(v, (0, 0), 0.12, 0, ROT_HEADING, GREY3, nib=4)
        return [((0.13, 0.04), "60°", dict(size=24, anchor="start")),
                ((mid[0] / 2, mid[1] / 2), f"0.64 × cos 60° = {f:.2f} along robot forward",
                 dict(size=24, dx=-20, anchor="end", code=True)),
                (((mid[0] + ROT_FORWARD) / 2, mid[1] / 2), f"-0.64 × sin 60° = {l:.2f} along "
                 "robot left", dict(size=24, dx=24, dy=-10, anchor="start", code=True)),
                ((ROT_FORWARD / 2, 0), "(0.64, 0) on the field", dict(size=26, dy=44, code=True))]
    split(139, "fig-e142-forward-split", (ROT_FORWARD, 0.0), (f, l), words,
          [f"forward on the field is ({f:.2f}, {l:.2f}) in the robot's frame"],
          ((1190, 800), "start"))


def e142_strafe_split():
    f, l = ROT_PIECES["strafe"]

    def words(v, mid):
        return [((mid[0] / 2, mid[1] / 2), f"0.16 × sin 60° = {f:.2f} along robot forward",
                 dict(size=24, dx=30, anchor="start", code=True)),
                (((mid[0]) / 2, (mid[1] + ROT_STRAFE) / 2),
                 f"0.16 × cos 60° = {l:.2f} along robot left",
                 dict(size=24, dx=30, dy=-20, anchor="start", code=True)),
                ((0, ROT_STRAFE / 2), "(0, 0.16) on the field",
                 dict(size=26, dx=-30, anchor="end", code=True))]
    split(149, "fig-e142-strafe-split", (0.0, ROT_STRAFE), (f, l), words,
          [f"strafe on the field is ({f:.2f}, {l:.2f}) in the robot's frame",
           "drawn 3 times as large as the other pictures, so the pieces can be read"],
          ((700, 200), "end"), o=(760, 720), k=3 * WORK_K, step=0.1)


def chain(v, order):
    """The pieces in order, end to end from the robot; gives back each one's start and end."""
    at, ends = (0.0, 0.0), []
    for field_part, k in order:
        part = ROT_PIECES[field_part]
        step = (part[0], 0.0) if k == 0 else (0.0, part[1])
        end = piece_at(at, step)
        vec(v, at, end, ROBOT_FORWARD if k == 0 else ROBOT_LEFT, nib=8)
        ends.append((at, end, part[k]))
        at = end
    return ends


def piece_words(v, ends, places):
    """Each piece's value beside it, placed by (dx, dy, anchor)."""
    for (a, b, value), (dx, dy, anchor) in zip(ends, places):
        say(v, ((a[0] + b[0]) / 2, (a[1] + b[1]) / 2), f"{value:.2f}", 30, dx=dx, dy=dy,
            anchor=anchor, code=True)


ROW_X, ROW_Y, ROW_GAP = 300, 110, 260


def order_row(s, ends, words):
    """The pieces' order, written as a row at the top; gives back each entry's middle."""
    mids = []
    for i, ((a, b, value), word) in enumerate(zip(ends, words)):
        x = ROW_X + i * ROW_GAP
        color = ROBOT_FORWARD if word == "forward" else ROBOT_LEFT
        s.marker_line((x - 100, ROW_Y + 14), (x + 100, ROW_Y + 14), color, nib=8)
        s.text((x, ROW_Y), f"{value:.2f} {word}", 26, code=True)
        mids.append(x)
    return mids


REGROUP = [("forward", 0), ("forward", 1), ("strafe", 0), ("strafe", 1)]
REGROUPED = [("forward", 0), ("strafe", 0), ("forward", 1), ("strafe", 1)]


def piece_formulas(s, v, ends, order, places):
    """Each piece's value as the formula that made it, a dotted arrow to the piece's middle."""
    rows = []
    for (a, b, value), key, (at, anchor) in zip(ends, order, places):
        mid = v.p(((a[0] + b[0]) / 2, (a[1] + b[1]) / 2, 0))
        rows.append((f"{value:.2f} = {FORMULA[key]}", at, anchor, mid))
    formula_words(s, rows)


def regroup(name, formula):
    s, v = worked(151)
    tip = (ROT_FORWARD, ROT_STRAFE)
    vec(v, (0, 0), tip, GREY1, nib=6)
    ends = chain(v, REGROUP)
    if formula:
        piece_formulas(s, v, ends, REGROUP, (((560, 470), "end"), ((1010, 470), "start"),
                                             ((1260, 730), "start"), ((1260, 520), "start")))
    else:
        piece_words(v, ends, ((-20, 0, "end"), (30, -30, "start"), (24, 20, "start"),
                              (10, -34, "start")))
    mids = order_row(s, ends, ("forward", "left", "forward", "left"))
    s.marker_arrow((mids[1], ROW_Y - 40), (mids[2], ROW_Y - 40), MARKER_ORANGE, nib=6, bend=-0.15)
    s.marker_arrow((mids[2], ROW_Y + 40), (mids[1], ROW_Y + 40), MARKER_ORANGE, nib=6, bend=-0.15)
    s.lines((900, 880), ["the four pieces, end to end, reach the stick's tip.",
                         "swap the middle two, so the forward pieces come first"], 28)
    s.save(name)


def e142_regroup():
    regroup("fig-e142-regroup", False)


def e142_regroup_formula():
    regroup("fig-e142-regroup-formula", True)


def regrouped(name, formula):
    s, v = worked(157)
    tip = (ROT_FORWARD, ROT_STRAFE)
    vec(v, (0, 0), tip, GREY1, nib=6)
    ends = chain(v, REGROUPED)
    if formula:
        piece_formulas(s, v, ends, REGROUPED, (((470, 570), "end"), ((640, 330), "end"),
                                               ((1040, 400), "start"), ((1240, 680), "start")))
    else:
        piece_words(v, ends, ((-20, 0, "end"), (-20, 0, "end"), (30, -30, "start"),
                              (30, 56, "start")))
    order_row(s, ends, ("forward", "forward", "left", "left"))
    s.lines((900, 880), ["in the new order, the same four pieces reach the same tip:",
                         "both forward pieces, then both left pieces"], 28)
    s.save(name)


def e142_regrouped():
    regrouped("fig-e142-regrouped", False)


def e142_regrouped_formula():
    regrouped("fig-e142-regrouped-formula", True)


def added(name, formula):
    s, v = worked(163)
    tip = (ROT_FORWARD, ROT_STRAFE)
    mid = plus((0, 0), ROT_F, ROT_ROBOT_FORWARD)
    vec(v, (0, 0), tip, MARKER_BLUE, nib=10)
    vec(v, (0, 0), mid, ROBOT_FORWARD, nib=10)
    vec(v, mid, tip, ROBOT_LEFT, nib=10)
    (ff, fl), (sf, sl) = ROT_PIECES["forward"], ROT_PIECES["strafe"]
    if formula:
        formula_words(s, [
            (f"{ROT_ROBOT_FORWARD:.2f} = forward·cos θ + strafe·sin θ", (630, 450), "end",
             v.p((mid[0] / 2, mid[1] / 2, 0))),
            (f"{ROT_ROBOT_STRAFE:.2f} = -forward·sin θ + strafe·cos θ", (1000, 360), "start",
             v.p(((mid[0] + tip[0]) / 2, (mid[1] + tip[1]) / 2, 0)))])
    else:
        say(v, (mid[0] / 2, mid[1] / 2), f"{ff:.2f} + {sf:.2f} = {ROT_ROBOT_FORWARD:.2f}", 30,
            dx=-20, anchor="end", code=True)
        say(v, ((mid[0] + tip[0]) / 2, (mid[1] + tip[1]) / 2),
            f"{fl:.2f} + {sl:.2f} = {ROT_ROBOT_STRAFE:.2f}", 30, dx=24, anchor="start", code=True)
    callout(s, (1210, 610), [f"({ROT_ROBOT_FORWARD:.2f}, {ROT_ROBOT_STRAFE:.2f})",
                             "in the robot's frame"], v.p(tip + (0,)))
    s.lines((900, 850), [
        f"robotForward =  forward·cos θ + strafe·sin θ = {ROT_ROBOT_FORWARD:.2f}",
        f"robotStrafe  = -forward·sin θ + strafe·cos θ = {ROT_ROBOT_STRAFE:.2f}"], 24, code=True)
    s.lines((900, 960), ["the robot drives (0.46, -0.47) in its own frame,",
                         "and goes where the stick points"], 28)
    s.save(name)


def e142_add():
    added("fig-e142-add", False)


def e142_add_formula():
    added("fig-e142-add-formula", True)


# The stick's request seen from the red drive team's place, on BIOBUZZ's field in tiles, a unit
# 3 tiles long, the robot at E142_AT.
E142_AT, E142_UNIT = (0.8, 1.3), 3.0


def e142_stick_red_3d():
    s = Sketch(1450, 1000, 167)
    turn = lambda q: (3 - q[1], q[0] - 3, q[2])                     # as fig-l140-field-red
    v = Iso(s, (760, 420), 118, turn=turn)
    at = (0.5, -4.4)
    room(v, at=at)
    field_floor(v)
    c = E142_AT
    side = 0.75 / 2                                                 # 18 in, in tiles
    corner = [plus(plus(c, ROT_F, side * a), ROT_L, side * b)
              for a, b in ((1, 1), (-1, 1), (-1, -1), (1, -1))]
    v.s.line([v.p(q + (0.01,)) for q in corner], PEN, 2.4, 0.9, fill=GREY1, closed=True)
    nose = [plus(plus(c, ROT_F, side), ROT_L, side * 0.45), plus(c, ROT_F, side * 1.6),
            plus(plus(c, ROT_F, side), ROT_L, -side * 0.45)]
    v.s.line([v.p(q + (0.01,)) for q in nose], PEN, 2.0, 0.9, fill=NOSE, closed=True)
    axes = (((1.0, 0.0), "field x"), ((0.0, 1.0), "field y"))
    for d, _ in axes:
        v.s.marker_arrow(v.p(c + (0.02,)), v.p(plus(c, d, 0.8) + (0.02,)), GREY3, nib=4)
    tip = plus(c, (ROT_FORWARD, ROT_STRAFE), E142_UNIT)
    corner = (tip[0], c[1])
    for a, b in ((c, corner), (corner, tip)):
        v.s.marker_line(v.p(a + (0.03,)), v.p(b + (0.03,)), FIELD_LEG, nib=7)
    v.s.marker_arrow(v.p(c + (0.04,)), v.p(tip + (0.04,)), MARKER_BLUE, nib=10)
    field_walls(v)                                  # after the robot, so it is behind the wall
    for d, name in axes:
        x, y = v.p(plus(c, d, 0.8) + (0.02,))
        s.text((x - 16, y - 18), name, 24, anchor="end")
    x, y = v.p(((c[0] + corner[0]) / 2, c[1], 0))
    s.text((x + 30, y + 44), "0.64 away", 28, anchor="start")
    x, y = v.p((tip[0], (c[1] + tip[1]) / 2, 0))
    s.text((x + 30, y + 16), "0.16 to the left", 28, anchor="start")
    student(v, turn(c + (0.0,)), at, RED_CAP)
    s.lines((725, 870), ["from the Red Alliance Station, the stick asks for",
                         "0.64 away and 0.16 to the left;",
                         "its x and y are the field's, moved to the robot's center"], 28)
    s.save("fig-e142-stick-red-3d")


# -- the control loops from L160 to L180, and auto's, as block diagrams. The rows were read off the
# block diagrams at https://claude.ai/artifact/8R28NuvjRNNb8Uat1TZDom, version 1792936034-ff97,
# in that page's units; loops() draws them LOOP_K times as large.

LOOP_K = 2.0
LOOP_INK = {"w": PEN, "fb": PEN, "w3": "#6e4bb0", "fb3": "#6e4bb0", "frame": "#1f8a72",
            "frame3": "#6e4bb0", "plant": GREY3}
LOOP_FILL = {"human": MARKER_ORANGE, "hw": GREY1, "lesson": MARKER_BLUE, "base": MARKER_GREEN,
             "lib": MARKER_LAVENDER}
LOOP_WORDS = {"lib-s": "#6e4bb0", "frame": "#1f8a72", "human-s": "#a8742a", "hw-s": GREY4}
HAND_ONLY = {"→": "to", "×": "x", " ●": "", "●": ""}   # Marker Felt has none of these


def loop_head(s, a, b, color):
    """A filled head at b, pointing from a."""
    tx, ty = b[0] - a[0], b[1] - a[1]
    n = math.hypot(tx, ty) or 1
    tx, ty, h = tx / n, ty / n, 14
    s.line([(b[0] - tx * h - ty * h * 0.5, b[1] - ty * h + tx * h * 0.5), b,
            (b[0] - tx * h + ty * h * 0.5, b[1] - ty * h - tx * h * 0.5)], color, 1.6,
           fill=color, closed=True)


def loop_wire(s, pts, color, dashed, arrow):
    """A wire, dashed where it carries what is measured, hopping where it crosses a wire."""
    out = []
    for p in pts:
        if p[0] != "hop":
            out.append(p)
            continue
        (x0, y0), (x1, y1) = out[-1], p[1:]
        c, r = ((x0 + x1) / 2, (y0 + y1) / 2), math.hypot(x1 - x0, y1 - y0) / 2
        a0 = math.atan2(y0 - c[1], x0 - c[0])
        out += [(c[0] + r * math.cos(a0 + math.pi * i / 8), c[1] + r * math.sin(a0 + math.pi * i / 8))
                for i in range(1, 9)]
    line = s.line(out, color, 3.2 if color != PEN else 2.6, 0.9)
    if dashed:
        line.set_linestyle((0, (5, 3)))
    if arrow:
        loop_head(s, out[-2], out[-1], color)


def loop_rows(s, rows, dy):
    k = LOOP_K
    at = lambda x, y: (x * k, (y + dy) * k)
    for row in rows:
        kind, cls = row[0], row[1] if len(row) > 1 else None
        if kind == "box" and cls in LOOP_FILL:
            x, y = at(*row[2:4])
            shape = rect(x, y, row[4] * k, row[5] * k)
            s.marker(shape, LOOP_FILL[cls], nib=14, opacity=0.45)
            s.ink_poly(shape, width=3.4 if len(row) > 6 else 1.8, over=5)
        elif kind == "box":
            x, y = at(*row[2:4])
            frame = s.line(rect(x, y, row[4] * k, row[5] * k), LOOP_INK[cls], 3.0, 0.9,
                           closed=True)
            frame.set_linestyle((0, (8, 5)))
        elif kind == "sum":
            c = at(*row[2:4])
            s.line(ellipse(c, 16 * k), LOOP_INK["w3" if cls else "w"], 2.6, 0.9, fill=PAPER,
                   closed=True)
        elif kind == "dot":
            s.dot(at(*row[1:3]), 4 * k, LOOP_INK["w3"])
        elif kind == "wire" and cls == "plant":
            s.line([at(*p) for p in row[2]], GREY3, 4.4, 0.9)
        elif kind == "wire":
            pts = [at(*p) if p[0] != "hop" else ("hop",) + at(*p[1:]) for p in row[2]]
            loop_wire(s, pts, LOOP_INK[cls], cls.startswith("fb"), row[3])
        elif kind == "text":
            words, color, size = row[4], LOOP_WORDS.get(row[5] if len(row) > 5 else "", PEN), \
                row[6] if len(row) > 6 else 0
            if cls == "m":
                s.text(at(*row[2:4]), words, 21, GREY4, anchor="start", code=True)
                continue
            for a, b in HAND_ONLY.items():
                words = words.replace(a, b)
            if cls == "n":
                color, size = (color if color != PEN else GREY4), 12
            s.text(at(*row[2:4]), words, (size or (13.5 if cls == "t" else 13)) * k, color,
                   anchor="start")


TELEOP_LOOPS = [
    ("box", "frame3", 430, 14, 520, 152),
    ("text", "t", 442, 34,
     "follower.update() after follower.hold(target): the pose loop (L170, L180)", "lib-s"),
    ("box", "lesson", 446, 52, 122, 62),
    ("text", "t", 456, 72, "target pose"),
    ("text", "m", 456, 90, "120, 72, 90°"),
    ("text", "n", 456, 106, "on Y"),
    ("wire", "w3", ((568, 83), (584, 83)), True),
    ("sum", "lib", 600, 83),
    ("text", "", 592, 88, "Σ", "", 16),
    ("text", "", 576, 76, "+", "", 13),
    ("text", "", 606, 114, "-", "", 15),
    ("text", "n", 446, 140, "pose error out of Σ", "lib-s"),
    ("wire", "w3", ((616, 83), (630, 83)), False),
    ("wire", "w3", ((630, 52), (630, 128)), False),
    ("wire", "w3", ((630, 52), (646, 52)), False),
    ("wire", "w3", ((630, 90), (646, 90)), False),
    ("wire", "w3", ((630, 128), (646, 128)), False),
    ("wire", "w3", ((640, 52), (646, 52)), True),
    ("wire", "w3", ((640, 90), (646, 90)), True),
    ("wire", "w3", ((640, 128), (646, 128)), True),
    ("box", "lib", 650, 40, 160, 26),
    ("text", "m", 658, 58, "forwardTranslational"),
    ("box", "lib", 650, 77, 160, 26),
    ("text", "m", 658, 95, "strafeTranslational"),
    ("box", "lib", 650, 115, 160, 26, True),
    ("text", "m", 658, 133, "headingFeedback"),
    ("wire", "w3", ((810, 53), (826, 53)), False),
    ("wire", "w3", ((810, 90), (826, 90)), False),
    ("wire", "w3", ((810, 128), (826, 128)), False),
    ("wire", "w3", ((826, 53), (826, 128)), False),
    ("wire", "w3", ((826, 90), (836, 90)), False),
    ("wire", "w3", ((832, 90), (840, 90)), True),
    ("box", "lib", 844, 66, 96, 48),
    ("text", "t", 852, 86, "DrivePowers"),
    ("text", "n", 852, 104, "fwd, strafe, turn"),
    ("wire", "w3", ((885, 114), (885, 292)), True),
    ("text", "n", 892, 190, "drive()"),
    ("text", "n", 892, 206, "while no stick"),
    ("text", "n", 892, 222, "is pushed"),
    ("text", "n", 652, 156, "tuned in ForesightConfig"),
    ("wire", "fb3", ((790, 585), (790, 453), ("hop", 790, 443), (790, 320), ("hop", 790, 310),
                    (790, 196), (600, 196), (600, 103)), True),
    ("text", "n", 610, 190, "projected pose: x, y, heading, from pose and velocity", "lib-s"),
    ("dot", 790, 585),
    ("box", "human", 16, 350, 100, 64),
    ("text", "t", 28, 376, "Driver"),
    ("text", "", 28, 394, "eyes, hands"),
    ("wire", "w", ((116, 382), (142, 382)), True),
    ("box", "hw", 146, 350, 100, 64),
    ("text", "t", 158, 376, "Gamepad"),
    ("text", "", 158, 394, "two sticks"),
    ("wire", "w", ((246, 382), (272, 382)), True),
    ("box", "lesson", 276, 342, 134, 80),
    ("text", "t", 288, 366, "Shape the sticks"),
    ("text", "m", 288, 386, "deadband"),
    ("text", "m", 288, 402, "signedSquared"),
    ("wire", "w", ((410, 356), (480, 356), (480, 315), (556, 315)), True),
    ("text", "n", 420, 308, "forward, strafe"),
    ("text", "n", 420, 324, "(field's)"),
    ("wire", "w", ((410, 406), (425, 406), (425, 418), (722, 418)), True),
    ("text", "n", 600, 412, "turn stick"),
    ("box", "lesson", 560, 283, 170, 64),
    ("text", "t", 572, 308, "Field → robot"),
    ("text", "m", 572, 328, "cos, sin of heading"),
    ("wire", "w", ((730, 315), (816, 315)), True),
    ("text", "n", 736, 307, "robot's"),
    ("box", "lib", 470, 250, 70, 26),
    ("text", "m", 482, 268, "pose ●"),
    ("wire", "fb", ((540, 263), (645, 263), (645, 279)), True),
    ("text", "n", 556, 258, "heading"),
    ("box", "frame", 440, 390, 340, 164),
    ("text", "t", 452, 544, "HeadingHold.turn(): the loop is closed here", "frame"),
    ("box", "base", 452, 450, 94, 44),
    ("text", "t", 462, 468, "target"),
    ("text", "n", 462, 485, "at let-go"),
    ("wire", "w", ((546, 472), (562, 472)), True),
    ("sum", "", 580, 472),
    ("text", "", 572, 477, "Σ", "", 16),
    ("text", "", 554, 464, "+", "", 13),
    ("text", "", 586, 503, "-", "", 15),
    ("wire", "w", ((596, 472), (610, 472)), True),
    ("box", "lib", 614, 446, 96, 52, True),
    ("text", "t", 624, 464, "Controller"),
    ("text", "m", 624, 480, "Pedro PIDF"),
    ("text", "m", 624, 494, "I, D, F = 0"),
    ("wire", "w", ((710, 472), (722, 472)), True),
    ("box", "base", 726, 402, 44, 88),
    ("text", "n", 732, 442, "stick"),
    ("text", "n", 732, 456, "or fix"),
    ("wire", "w", ((770, 448), (816, 448)), True),
    ("text", "n", 795, 440, "turn"),
    ("box", "base", 820, 296, 130, 174),
    ("text", "t", 832, 372, "sticks()"),
    ("text", "m", 832, 392, "mix, normalize"),
    ("text", "n", 832, 412, "CorbelsDriveTrain"),
    ("wire", "w", ((950, 385), (976, 385)), True),
    ("box", "lib", 980, 353, 104, 64),
    ("text", "t", 990, 378, "setPower ×4"),
    ("text", "n", 990, 396, "FTC SDK"),
    ("wire", "w", ((1084, 385), (1106, 385)), True),
    ("text", "n", 990, 440, "fraction of"),
    ("text", "n", 990, 455, "battery volts"),
    ("wire", "plant", ((1100, 340), (1236, 340), (1236, 624), (950, 624), (950, 548), (1100, 548),
                      (1100, 340)), False),
    ("text", "t", 1112, 334, "Plant", "hw-s"),
    ("text", "n", 812, 552, "out: pose, velocity"),
    ("box", "hw", 1110, 353, 114, 64),
    ("text", "t", 1120, 378, "Battery, motors"),
    ("text", "n", 1120, 396, "no speed loop"),
    ("wire", "w", ((1167, 417), (1167, 446)), True),
    ("box", "hw", 1110, 450, 114, 64),
    ("text", "t", 1120, 475, "Robot"),
    ("text", "n", 1120, 493, "mass, floor"),
    ("wire", "fb", ((1167, 514), (1167, 585), (1104, 585)), True),
    ("box", "hw", 960, 560, 140, 50),
    ("text", "t", 972, 582, "Pinpoint"),
    ("text", "n", 972, 600, "odometry pods, IMU"),
    ("wire", "fb", ((960, 585), (934, 585)), True),
    ("box", "lib", 800, 560, 130, 50),
    ("text", "t", 810, 582, "follower.update()"),
    ("text", "m", 810, 600, "pose, velocity ●"),
    ("wire", "fb", ((800, 585), (580, 585), (580, 492)), True),
    ("text", "n", 610, 578, "measured heading"),
    ("wire", "fb", ((1224, 482), (1232, 482), (1232, 670), (66, 670), (66, 418)), True),
    ("text", "n", 500, 662, "the driver watches the robot and moves the sticks again"),
    ("text", "t", 80, 695, "Loop 1: the driver", "human-s"),
    ("text", "t", 260, 695, "Loop 2: heading hold, our code through the hardware", "frame"),
    ("text", "t", 680, 695, "Loop 3: the pose, Pedro's follower in place of the sticks", "lib-s"),
]

AUTO_LOOPS = [
    ("box", "lesson", 16, 180, 180, 170),
    ("text", "t", 28, 204, "Auto's commands"),
    ("text", "m", 28, 230, "follow(line(…))"),
    ("text", "m", 28, 250, "follow(line(…))"),
    ("text", "m", 28, 270, "hold(end)"),
    ("text", "n", 28, 300, "StarterBotAuto,"),
    ("text", "n", 28, 317, "one after another,"),
    ("text", "n", 28, 334, "by Ivy's Scheduler"),
    ("wire", "w", ((196, 215), (215, 215), (215, 150), (242, 150)), True),
    ("text", "n", 200, 142, "path"),
    ("wire", "w", ((196, 315), (215, 315), (215, 381), (242, 381)), True),
    ("text", "n", 200, 400, "pose"),
    ("box", "frame3", 230, 14, 620, 290),
    ("text", "t", 242, 36, "Foresight.calculatePath(): following the curve", "lib-s"),
    ("box", "lib", 246, 50, 160, 210),
    ("text", "t", 256, 72, "closest point"),
    ("text", "t", 256, 89, "on the curve"),
    ("text", "n", 256, 110, "to the projected pose:"),
    ("text", "n", 256, 128, "distance left, the"),
    ("text", "n", 256, 146, "point, its direction,"),
    ("text", "n", 256, 164, "its heading"),
    ("text", "n", 256, 200, "a line is a curve:"),
    ("text", "n", 256, 218, "same loop, simpler"),
    ("text", "n", 256, 236, "geometry"),
    ("box", "lib", 256, 270, 92, 22),
    ("text", "m", 264, 285, "projected ●"),
    ("wire", "fb3", ((300, 270), (300, 264)), True),
    ("wire", "w3", ((406, 77), (466, 77)), True),
    ("text", "n", 412, 70, "distance"),
    ("box", "lib", 470, 50, 246, 76),
    ("text", "t", 480, 68, "drive vector"),
    ("text", "n", 480, 85, "feedforward of the target speed"),
    ("text", "n", 480, 100, "along the curve"),
    ("text", "m", 480, 117, "coast, brake: kV × targetVelocity"),
    ("wire", "w3", ((716, 80), (726, 80)), True),
    ("wire", "w3", ((406, 165), (472, 165)), True),
    ("text", "n", 412, 158, "the point"),
    ("sum", "lib", 490, 165),
    ("text", "", 482, 170, "Σ", "", 16),
    ("text", "", 466, 158, "+", "", 13),
    ("text", "", 497, 198, "-", "", 15),
    ("wire", "w3", ((506, 165), (526, 165)), True),
    ("box", "lib", 530, 140, 180, 50),
    ("text", "m", 538, 160, "forwardTranslational"),
    ("text", "m", 538, 178, "strafeTranslational"),
    ("text", "n", 560, 208, "sideways error only"),
    ("wire", "w3", ((710, 165), (726, 165)), True),
    ("box", "lib", 446, 204, 92, 22),
    ("text", "m", 454, 219, "projected ●"),
    ("wire", "fb3", ((490, 204), (490, 185)), True),
    ("wire", "w3", ((406, 250), (472, 250)), True),
    ("text", "n", 412, 243, "heading"),
    ("sum", "lib", 490, 250),
    ("text", "", 482, 255, "Σ", "", 16),
    ("text", "", 466, 243, "+", "", 13),
    ("text", "", 497, 283, "-", "", 15),
    ("wire", "w3", ((506, 250), (526, 250)), True),
    ("box", "lib", 530, 230, 180, 40, True),
    ("text", "m", 538, 255, "headingFeedback"),
    ("wire", "w3", ((710, 250), (726, 250)), True),
    ("box", "lib", 446, 276, 92, 22),
    ("text", "m", 454, 291, "projected ●"),
    ("wire", "fb3", ((490, 276), (490, 270)), True),
    ("text", "n", 548, 296, "each Controller: Pedro PIDF, I, D, F = 0"),
    ("box", "lib", 730, 50, 110, 230),
    ("text", "t", 740, 74, "share the"),
    ("text", "t", 740, 91, "power"),
    ("text", "n", 740, 116, "heading and"),
    ("text", "n", 740, 133, "sideways before"),
    ("text", "n", 740, 150, "drive, then"),
    ("text", "n", 740, 167, "field → robot"),
    ("wire", "w3", ((840, 165), (876, 165)), True),
    ("box", "frame3", 230, 320, 620, 180),
    ("text", "t", 242, 342, "Foresight.calculateHold(): closing on one pose", "lib-s"),
    ("box", "lesson", 246, 356, 160, 114),
    ("text", "t", 256, 378, "target pose"),
    ("text", "n", 256, 398, "hold(end), or the"),
    ("text", "n", 256, 415, "last path's end"),
    ("wire", "w3", ((406, 381), (472, 381)), True),
    ("text", "n", 412, 374, "x, y"),
    ("sum", "lib", 490, 381),
    ("text", "", 482, 386, "Σ", "", 16),
    ("text", "", 466, 374, "+", "", 13),
    ("text", "", 497, 414, "-", "", 15),
    ("wire", "w3", ((506, 381), (526, 381)), True),
    ("box", "lib", 530, 356, 180, 50),
    ("text", "m", 538, 376, "forwardTranslational"),
    ("text", "m", 538, 394, "strafeTranslational"),
    ("wire", "w3", ((710, 381), (726, 381)), True),
    ("box", "lib", 446, 410, 92, 22),
    ("text", "m", 454, 425, "projected ●"),
    ("wire", "fb3", ((490, 410), (490, 401)), True),
    ("wire", "w3", ((406, 456), (472, 456)), True),
    ("text", "n", 412, 449, "heading"),
    ("sum", "lib", 490, 456),
    ("text", "", 482, 461, "Σ", "", 16),
    ("text", "", 466, 449, "+", "", 13),
    ("wire", "w3", ((506, 456), (526, 456)), True),
    ("box", "lib", 530, 436, 180, 40, True),
    ("text", "m", 538, 461, "headingFeedback"),
    ("wire", "w3", ((710, 456), (726, 456)), True),
    ("text", "n", 418, 492, "- projected heading"),
    ("text", "n", 580, 494, "each Controller: Pedro PIDF, I, D, F = 0"),
    ("box", "lib", 730, 356, 110, 120),
    ("text", "t", 740, 380, "field → robot"),
    ("text", "n", 740, 402, "whole distance,"),
    ("text", "n", 740, 419, "no drive vector"),
    ("wire", "w3", ((840, 413), (876, 413)), True),
    ("text", "t", 866, 110, "Follower"),
    ("box", "lib", 880, 120, 60, 380),
    ("text", "t", 890, 160, "PATH"),
    ("text", "t", 890, 430, "HOLD"),
    ("wire", "fb3", ((910, 175), (910, 405)), True),
    ("text", "n", 948, 420, "path done:"),
    ("text", "n", 948, 437, "hold(endPose)"),
    ("wire", "w", ((940, 300), (976, 300)), True),
    ("box", "base", 980, 250, 120, 100),
    ("text", "t", 990, 274, "drive()"),
    ("text", "m", 990, 294, "mix, normalize"),
    ("text", "n", 990, 314, "FLOAT, not brake"),
    ("text", "n", 990, 334, "CorbelsDriveTrain"),
    ("wire", "w", ((1100, 300), (1116, 300)), True),
    ("box", "lib", 1120, 265, 100, 70),
    ("text", "t", 1130, 292, "setPower ×4"),
    ("text", "n", 1130, 312, "FTC SDK"),
    ("wire", "w", ((1220, 300), (1236, 300)), True),
    ("box", "plant", 1228, 252, 164, 340),
    ("text", "t", 1236, 246, "Plant", "hw-s"),
    ("box", "hw", 1240, 265, 140, 70),
    ("text", "t", 1250, 292, "Battery, motors"),
    ("text", "n", 1250, 312, "no speed loop"),
    ("wire", "w", ((1310, 335), (1310, 376)), True),
    ("box", "hw", 1240, 380, 140, 64),
    ("text", "t", 1250, 406, "Robot"),
    ("text", "n", 1250, 424, "mass, floor"),
    ("wire", "fb3", ((1310, 444), (1310, 526)), True),
    ("box", "hw", 1230, 530, 150, 50),
    ("text", "t", 1240, 552, "Pinpoint"),
    ("text", "n", 1240, 570, "odometry pods, IMU"),
    ("wire", "fb3", ((1230, 555), (1204, 555)), True),
    ("box", "lib", 1020, 530, 180, 50),
    ("text", "t", 1030, 552, "follower.update()"),
    ("text", "m", 1030, 570, "pose, velocity"),
    ("wire", "fb3", ((1020, 555), (824, 555)), True),
    ("box", "lib", 560, 530, 260, 50),
    ("text", "t", 570, 552, "projected pose ●"),
    ("text", "n", 570, 570, "stopping point: uses the velocity output too"),
    ("text", "n", 836, 546, "plant out: pose, velocity", "lib-s"),
    ("text", "t", 230, 620, "One loop at a time, closed through the hardware each follower.update(): "
     "the curve, then the pose. No driver.", "lib-s"),
]


def loops():
    s = Sketch(2800, 3100, 1600)
    s.text((32, 60), "Teleop, from L160 to L180", 40, anchor="start")
    loop_rows(s, TELEOP_LOOPS, 50)
    s.text((32, 1640), "Auto: following a path, then holding its end", 40, anchor="start")
    loop_rows(s, AUTO_LOOPS, 840)
    x = 32
    for name, fill in (("a person", "human"), ("hardware and physics", "hw"),
                       ("lesson code, the student types", "lesson"), ("our base/ code", "base"),
                       ("a library: Pedro, the FTC SDK", "lib")):
        box = rect(x, 3030, 36, 28)
        s.marker(box, LOOP_FILL[fill], nib=14, opacity=0.45)
        s.ink_poly(box, width=1.8, over=4)
        s.text((x + 50, 3054), name, 28, anchor="start")
        x += 560
    s.save("fig-control-loops")


PLOTS = [stick_shaping, speed_loop, panels_graph]
PICTURES = [mecanum_x, wheel_handover, odometry_step, field_relative, wheel_pushes, robot_front,
            gamepad_sticks, wheel_names, wheel_forward, pinpoint_mounting, l020_platform,
            l020_variable, l020_sticky, l020_tracker_note, l020_publish, l040_sticky,
            stick_to_robot, pedro_field, e035_numberline, e035_xy, e035_xyz, e035_flip,
            e035_flipped, e035_stick, e035_robot, e035_speeds, e035_switch, e035_numberline_3d,
            e035_xy_3d, e035_stick_3d, e035_robot_3d, e035_robot_frame_3d, e035_speeds_3d,
            e035_switch_3d, e035_motor_wheel_gear_box, l140_field_top, l140_field_red,
            l140_field_blue, e141_notation, e141_scale, e141_add, e142_unit_circle,
            e142_radian, e142_robot_axes, e142_stick_field, e142_stick_red_3d,
            e142_forward_split, e142_strafe_split, e142_regroup, e142_regroup_formula,
            e142_regrouped, e142_regrouped_formula, e142_add, e142_add_formula, loops]

if __name__ == "__main__":
    if OUT is None:
        raise SystemExit("name the folder to draw into: python tools/run.py draw DIR")
    for draw in PLOTS + PICTURES:
        draw()
    print(f"drew {len(PLOTS) + len(PICTURES)} figures into {OUT}")
