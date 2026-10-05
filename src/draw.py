"""Status: controlling, for the figures it draws.

Draws the book's figures that are drawings, and the drawn stand-ins for the ones
that are meant to be photos, into figures/ beside this file as PNG.

    python draw.py

Every number in a drawing is the code's: the 0.05 deadband and the sign-keeping
square of L3a and L3b, the four mixing sums and the rotation of L5 and L8, the
field rotation of L11, and L16's 40 in/s, its feedforward of
Constants.powerPerInchPerSecond and its correction of 0.008 per in/s. A stand-in
says on its face that it stands in for a photo still to be taken.
"""

from pathlib import Path

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt  # noqa: E402
from matplotlib.patches import (Circle, FancyArrowPatch, FancyBboxPatch, Polygon,  # noqa: E402
                                Rectangle)
import numpy as np  # noqa: E402

OUT = Path(__file__).resolve().parent / "figures"
DPI = 200
INK = "#222222"
RED = "#c0392b"
BLUE = "#2c6fb7"
GREEN = "#2e8b57"
GREY = "#888888"
LIGHT = "#eeeeee"

DEADBAND = 0.05                        # L3a and L3b
POWER_PER_IPS = 0.016695978563625216   # pedro/Constants.powerPerInchPerSecond
VELOCITY_KP = 0.008                    # LessonsDriveTrain.VELOCITY_KP
MAX_IPS = 40                           # L16VelocityDriveOpMode.MAX_IPS


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


def robot_top(ax, cx=0, cy=0, size=4.0, heading_deg=90, label_nose=True, body=LIGHT):
    """A square robot from above; heading 90 points the nose up the page."""
    h = np.radians(heading_deg)
    fwd = np.array([np.cos(h), np.sin(h)])
    left = np.array([-np.sin(h), np.cos(h)])
    c = np.array([cx, cy])
    s = size / 2
    corners = [c + s * (fwd + left), c + s * (fwd - left), c + s * (-fwd - left),
               c + s * (-fwd + left)]
    ax.add_patch(Polygon(corners, closed=True, facecolor=body, edgecolor=INK, linewidth=2))
    nose = [c + s * fwd * 1.0 + 0.35 * s * left, c + s * fwd * 1.35, c + s * fwd * 1.0 - 0.35 * s * left]
    ax.add_patch(Polygon(nose, closed=True, facecolor=RED, edgecolor=RED))
    if label_nose:
        p = c + s * fwd * 1.6
        ax.text(*p, "nose", ha="center", va="center", fontsize=11, color=RED)
    return c, fwd, left, s


# ---------------------------------------------------------------- drawings

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


def mecanum_x():
    fig, ax = plt.subplots(figsize=(6, 6.4))
    c, fwd, left, s = robot_top(ax, size=4)
    names = {"front left": (-1, 1), "front right": (1, 1), "back left": (-1, -1),
             "back right": (1, -1)}
    for name, (sx, sy) in names.items():
        wx, wy = sx * 2.35, sy * 1.45
        ax.add_patch(Rectangle((wx - 0.35, wy - 0.7), 0.7, 1.4, facecolor="#555555",
                               edgecolor=INK))
        # The top roller as a 45 degree line, each pointing in towards the middle,
        # so the four make an X across the robot.
        d = np.array([-sx, -sy]) / np.sqrt(2) * 0.55
        ax.plot([wx - d[0], wx + d[0]], [wy - d[1], wy + d[1]], color="#f1c40f", linewidth=4,
                solid_capstyle="round")
        ax.text(wx, wy + sy * 1.05, name, ha="center", va="center", fontsize=10)
    ax.plot([-2.35, 2.35], [1.45, -1.45], color="#f1c40f", linewidth=1, linestyle=":")
    ax.plot([-2.35, 2.35], [-1.45, 1.45], color="#f1c40f", linewidth=1, linestyle=":")
    ax.text(0, -3.4, "the top roller of each wheel, at 45 degrees: together they make an X",
            ha="center", fontsize=10)
    ax.set_xlim(-3.6, 3.6)
    ax.set_ylim(-3.8, 3.8)
    ax.set_aspect("equal")
    ax.axis("off")
    save(fig, "fig-mecanum-x")


def box(ax, xy, w, h, text, color=LIGHT, fontsize=11, edge=INK):
    ax.add_patch(FancyBboxPatch((xy[0] - w / 2, xy[1] - h / 2), w, h,
                                boxstyle="round,pad=0.05,rounding_size=0.15",
                                facecolor=color, edgecolor=edge, linewidth=1.8))
    ax.text(*xy, text, ha="center", va="center", fontsize=fontsize)


def wheel_handover():
    fig, ax = plt.subplots(figsize=(10, 5))
    box(ax, (1.6, 3.6), 3.0, 1.0, "Path follower\n(drive)", "#dbe8f7")
    box(ax, (1.6, 1.2), 3.0, 1.0, "Driver's sticks\n(sticks, then\nsetCommandedWheels)", "#fde2d2",
        fontsize=10)
    box(ax, (8.5, 2.4), 2.2, 1.6, "four wheels", "#e6e6e6", fontsize=12)
    # The switch: commanded wheels set, so the sticks get through.
    ax.add_patch(Circle((5.6, 2.4), 0.12, color=INK))
    ax.plot([5.6, 4.6], [2.4, 1.2], color=RED, linewidth=4)
    ax.plot([4.6, 4.6], [3.6, 3.6], marker="o", color=INK)
    ax.plot([4.6, 4.6], [1.2, 1.2], marker="o", color=INK)
    arrow(ax, (3.15, 3.6), (4.5, 3.6), color=BLUE)
    arrow(ax, (3.15, 1.2), (4.5, 1.2), color=RED)
    arrow(ax, (5.7, 2.4), (7.35, 2.4), color=RED)
    ax.text(5.6, 3.25, "commanded wheels set?", ha="center", fontsize=11)
    ax.text(5.6, 0.45, "set: the sticks get through\nreleased: the follower does", ha="center",
            fontsize=10)
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 4.6)
    ax.axis("off")
    save(fig, "fig-wheel-handover")


def odometry_step():
    # One loop's wheel travels, in inches, and a heading of 30 degrees.
    fl, fr, bl, br = 1.0, 2.0, 2.0, 1.0
    fwd = (fl + fr + bl + br) / 4
    lft = (-fl + fr + bl - br) / 4
    h = np.radians(30)
    dx = fwd * np.cos(h) - lft * np.sin(h)
    dy = fwd * np.sin(h) + lft * np.cos(h)
    fig, axes = plt.subplots(1, 4, figsize=(16, 4.6))
    titles = ["1. Each wheel's travel", "2. What they add up to", "3. Turned by the heading",
              "4. Added to the old estimate"]
    for ax, t in zip(axes, titles):
        ax.set_title(t, fontsize=12)
        ax.set_aspect("equal")
        ax.axis("off")
    ax = axes[0]
    robot_top(ax, size=2.4, label_nose=False)
    for (name, v, (x, y)) in [("FL", fl, (-1.45, 0.9)), ("FR", fr, (1.45, 0.9)),
                              ("BL", bl, (-1.45, -0.9)), ("BR", br, (1.45, -0.9))]:
        arrow(ax, (x, y - 0.35 * v), (x, y + 0.35 * v), color=BLUE, head=14)
        ax.text(x + (0.55 if x > 0 else -0.55), y, f"{name}\n{v:g} in", ha="center",
                va="center", fontsize=9)
    ax.set_xlim(-2.4, 2.4)
    ax.set_ylim(-2.2, 2.2)
    ax = axes[1]
    robot_top(ax, size=2.4, label_nose=False)
    arrow(ax, (0, 0), (0, fwd), color=GREEN)
    arrow(ax, (0, 0), (-lft, 0), color=RED)
    ax.text(-1.35, 1.75, f"forward (FL+FR+BL+BR)/4 = {fwd:g} in", fontsize=9, ha="left",
            va="bottom", color=GREEN)
    ax.text(-1.35, -1.55, f"strafe (-FL+FR+BL-BR)/4 = {lft:g} in", fontsize=9, ha="left",
            va="top", color=RED)
    ax.set_xlim(-2.4, 2.4)
    ax.set_ylim(-2.2, 2.2)
    ax = axes[2]
    ax.annotate("", xy=(2, 0), xytext=(0, 0), arrowprops=dict(arrowstyle="->", color=GREY))
    ax.annotate("", xy=(0, 2), xytext=(0, 0), arrowprops=dict(arrowstyle="->", color=GREY))
    ax.text(2.05, 0, "field x", fontsize=9, va="center")
    ax.text(0, 2.08, "field y", fontsize=9, ha="center")
    fw = np.array([np.cos(h), np.sin(h)]) * fwd
    lf = np.array([-np.sin(h), np.cos(h)]) * lft
    arrow(ax, (0, 0), tuple(fw), color=GREEN)
    arrow(ax, tuple(fw), tuple(fw + lf), color=RED)
    arrow(ax, (0, 0), (dx, dy), color=INK, style="-|>")
    ax.text(0.5, -0.45, "heading 30 degrees", fontsize=9)
    ax.text(dx + 0.05, dy + 0.1, f"({dx:.2f}, {dy:.2f}) in", fontsize=9)
    ax.set_xlim(-0.6, 2.6)
    ax.set_ylim(-0.8, 2.4)
    ax = axes[3]
    old = np.array([10.0, 20.0])
    ax.plot(*old, "o", color=GREY, markersize=9)
    ax.text(old[0] - 0.1, old[1] - 0.3, "old estimate\n(10, 20) in", ha="right", fontsize=9)
    arrow(ax, tuple(old), tuple(old + [dx, dy]), color=INK)
    new = old + [dx, dy]
    ax.plot(*new, "o", color=BLUE, markersize=9)
    ax.text(new[0] + 0.1, new[1] + 0.1, f"new estimate\n({new[0]:.2f}, {new[1]:.2f}) in",
            fontsize=9)
    ax.set_xlim(8.2, 12.6)
    ax.set_ylim(18.8, 22.6)
    save(fig, "fig-odometry-step")


def field_relative():
    fig, ax = plt.subplots(figsize=(7, 7.6))
    ax.add_patch(Rectangle((-5, -5), 10, 10, facecolor="#f7f7f7", edgecolor=INK, linewidth=2))
    ax.text(0, -5.6, "driver", ha="center", fontsize=12)
    ax.add_patch(Circle((0, -5.25), 0.18, color=INK))
    # Field x is away from the driver, so up the page; field y is then to the left.
    arrow(ax, (-4.4, -4.4), (-4.4, -3.2), color=GREY, head=12)
    arrow(ax, (-4.4, -4.4), (-5.6 + 0.0, -4.4), color=GREY, head=12)
    ax.text(-4.25, -3.2, "field x", fontsize=9)
    ax.text(-5.6, -4.15, "field y", fontsize=9)
    # The stick, pushed away from the driver, drawn on the field.
    arrow(ax, (2.8, -3.6), (2.8, -1.4), color=BLUE, width=4)
    ax.text(3.0, -2.6, "stick:\naway from\nthe driver", fontsize=10, color=BLUE)
    # The robot turned a quarter turn: heading 90 degrees faces field y, to the left.
    c, f, l, s = robot_top(ax, cx=-0.5, cy=0.6, size=2.6, heading_deg=180)
    arrow(ax, tuple(c), tuple(c + np.array([0, 2.2])), color=BLUE, width=4)
    ax.text(c[0] + 0.15, c[1] + 2.25, "the same arrow on the robot:\nforward 0, sideways to its "
            "right 1", fontsize=10, color=BLUE)
    ax.text(c[0], c[1] - 1.9, "robot turned a quarter turn\n(heading 90 degrees)", ha="center",
            fontsize=10)
    ax.set_xlim(-6, 6)
    ax.set_ylim(-6.1, 5.4)
    ax.set_aspect("equal")
    ax.axis("off")
    save(fig, "fig-field-relative")


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


# ---------------------------------------------------------------- stand-ins

def panels_graph():
    t = np.linspace(0, 3, 600)
    raw = np.clip(t / 2.5, 0, 1)
    shaped = squared(deadband(raw))
    fig, ax = plt.subplots(figsize=(9, 4.6))
    ax.set_facecolor("#1e1e24")
    ax.plot(t, raw, color="#5dade2", linewidth=2.5, label="stick/left_raw")
    ax.plot(t, shaped, color="#f5b041", linewidth=2.5, label="stick/left_shaped")
    ax.fill_between(t, shaped, raw, color="#ffffff", alpha=0.08)
    ax.set_xlabel("time, s")
    ax.set_ylabel("value")
    ax.set_ylim(-0.05, 1.08)
    ax.grid(alpha=0.2)
    ax.legend(loc="upper left")
    ax.set_title("Graph: the stick pushed slowly to full, with L3b's shaping", fontsize=12)
    stand_in(ax, "a Panels screenshot", x=0.98, y=0.04, ha="right", color="#bbbbbb")
    save(fig, "fig-panels-graph")


def robot_front():
    fig, ax = plt.subplots(figsize=(9, 5))
    ax.axhline(0, color=INK, linewidth=2)
    ax.add_patch(Rectangle((-2.2, 0.35), 4.4, 2.2, facecolor=LIGHT, edgecolor=INK, linewidth=2))
    for x in (-1.9, 1.9):
        ax.add_patch(Rectangle((x - 0.35, 0), 0.7, 0.75, facecolor="#555555", edgecolor=INK))
    ax.add_patch(Rectangle((-0.6, 1.2), 1.2, 0.8, facecolor="#cfd8dc", edgecolor=INK))
    ax.text(0, 1.6, "Control Hub", ha="center", va="center", fontsize=9)
    ax.text(0, 2.85, "the robot, from the front", ha="center", fontsize=11)
    for x, name in ((3.6, "gamepad 1"), (5.4, "gamepad 2")):
        ax.add_patch(FancyBboxPatch((x - 0.7, 0.05), 1.4, 0.55,
                                    boxstyle="round,pad=0.05,rounding_size=0.2",
                                    facecolor="#444444", edgecolor=INK))
        ax.text(x, 0.85, name, ha="center", fontsize=10)
    ax.set_xlim(-3, 6.5)
    ax.set_ylim(-0.6, 3.4)
    ax.set_aspect("equal")
    ax.axis("off")
    stand_in(ax)
    save(fig, "fig-robot-front")


def gamepad_sticks():
    fig, ax = plt.subplots(figsize=(9, 6))
    ax.add_patch(FancyBboxPatch((-4, -1.6), 8, 3.4, boxstyle="round,pad=0.1,rounding_size=1.2",
                                facecolor="#444444", edgecolor=INK))
    for x, name in ((-2.2, "left stick"), (1.0, "right stick")):
        ax.add_patch(Circle((x, -0.4), 0.7, facecolor="#222222", edgecolor="#999999"))
        ax.add_patch(Circle((x, -0.4), 0.35, facecolor="#666666"))
        ax.text(x, 0.55, name, ha="center", fontsize=11, color="white")
        arrow(ax, (x + 1.0, 0.2), (x + 1.0, -1.0), color="#82e0aa", head=14)
        ax.text(x + 1.15, -0.4, "y counts\nup", fontsize=9, color="#82e0aa", va="center")
    ax.add_patch(Circle((3.3, 0.9), 0.28, facecolor="#27ae60"))
    ax.text(3.3, 0.9, "A", ha="center", va="center", color="white", fontsize=11,
            fontweight="bold")
    ax.text(3.3, 1.35, "A button", ha="center", fontsize=10, color="white")
    ax.text(0, -2.3, "the driver holds it from this side", ha="center", fontsize=11)
    ax.text(0, 2.2, "a stick pushed away from the driver reads -1, pulled back +1",
            ha="center", fontsize=10)
    ax.set_xlim(-4.6, 4.6)
    ax.set_ylim(-3.0, 2.6)
    ax.set_aspect("equal")
    ax.axis("off")
    stand_in(ax)
    save(fig, "fig-gamepad-sticks")


def wheel_names():
    fig, ax = plt.subplots(figsize=(6, 6.4))
    robot_top(ax, size=4)
    for name, (x, y) in {"front left": (-2.35, 1.45), "front right": (2.35, 1.45),
                         "back left": (-2.35, -1.45), "back right": (2.35, -1.45)}.items():
        ax.add_patch(Rectangle((x - 0.35, y - 0.7), 0.7, 1.4, facecolor="#555555", edgecolor=INK))
        ax.text(x, y + (1.05 if y > 0 else -1.05), name, ha="center", va="center", fontsize=11)
    ax.set_xlim(-3.6, 3.6)
    ax.set_ylim(-3.6, 3.8)
    ax.set_aspect("equal")
    ax.axis("off")
    stand_in(ax)
    save(fig, "fig-wheel-names")


def wheel_forward():
    fig, ax = plt.subplots(figsize=(8, 5))
    ax.axhline(0, color=INK, linewidth=2)
    ax.add_patch(Rectangle((-3.2, 1.0), 6.4, 1.2, facecolor=LIGHT, edgecolor=INK, linewidth=2))
    ax.add_patch(Polygon([(3.2, 1.25), (3.75, 1.6), (3.2, 1.95)], facecolor=RED))
    ax.text(3.9, 1.6, "nose", fontsize=11, color=RED, va="center")
    ax.add_patch(Circle((1.4, 0.9), 0.9, facecolor="#555555", edgecolor=INK, linewidth=2))
    ax.add_patch(FancyArrowPatch((0.75, 1.55), (2.05, 1.55), connectionstyle="arc3,rad=-0.45",
                                 arrowstyle="-|>", mutation_scale=18, color="#f1c40f",
                                 linewidth=3))
    arrow(ax, (-2.5, 2.7), (0.5, 2.7), color=RED)
    ax.text(-1.0, 2.9, "robot driving forward", ha="center", fontsize=10, color=RED)
    ax.text(1.4, 2.95, "the top of the tyre\nmoves towards the nose", ha="left", fontsize=10)
    ax.set_xlim(-3.6, 5.2)
    ax.set_ylim(-0.6, 3.6)
    ax.set_aspect("equal")
    ax.axis("off")
    stand_in(ax)
    save(fig, "fig-wheel-forward")


def pinpoint_mounting():
    fig, ax = plt.subplots(figsize=(7, 7))
    robot_top(ax, size=5, label_nose=True)
    ax.add_patch(Rectangle((-0.55, -0.55), 1.1, 1.1, facecolor="#cfd8dc", edgecolor=INK))
    ax.text(0, 0.05, "Pinpoint\nsticker side up", ha="center", va="center", fontsize=8)
    ax.add_patch(Rectangle((-1.75, -0.15), 0.4, 1.0, facecolor="#555555", edgecolor=INK))
    ax.text(-1.55, 1.15, "forward pod\nrolls along\nx socket", ha="center", fontsize=9)
    arrow(ax, (-1.35, 0.35), (-0.55, 0.35), color=BLUE, head=10, width=1.5)
    ax.add_patch(Rectangle((0.4, -1.75), 1.0, 0.4, facecolor="#555555", edgecolor=INK))
    ax.text(0.9, -2.15, "strafe pod rolls across\ny socket", ha="center", fontsize=9)
    arrow(ax, (0.9, -1.35), (0.3, -0.55), color=BLUE, head=10, width=1.5)
    ax.set_xlim(-3.4, 3.4)
    ax.set_ylim(-3.4, 3.9)
    ax.set_aspect("equal")
    ax.axis("off")
    stand_in(ax)
    save(fig, "fig-pinpoint-mounting")


def wheel_pushes():
    # L090's four lines: fl = f - s - t, fr = f + s + t, bl = f + s - t, br = f - s + t.
    # Spinning forward, front left and back right push forward and right, front right and
    # back left push forward and left; spinning backward, each pushes the other way.
    pushes = {"front left": (1, 1), "front right": (-1, 1), "back left": (-1, 1),
              "back right": (1, 1)}
    where = {"front left": (-1, 1), "front right": (1, 1), "back left": (-1, -1),
             "back right": (1, -1)}
    motions = [("driving forward", 1, 0, 0), ("sliding left", 0, 1, 0),
               ("turning left", 0, 0, 1)]
    fig, axes = plt.subplots(1, 3, figsize=(15, 5.6))
    for ax, (title, f, s, t) in zip(axes, motions):
        robot_top(ax, size=4)
        power = {"front left": f - s - t, "front right": f + s + t, "back left": f + s - t,
                 "back right": f - s + t}
        for name, (sx, sy) in where.items():
            wx, wy = sx * 2.35, sy * 1.45
            ax.add_patch(Rectangle((wx - 0.35, wy - 0.7), 0.7, 1.4, facecolor="#555555",
                                   edgecolor=INK))
            p = power[name]
            arrow(ax, (wx, wy - 0.55 * p), (wx, wy + 0.55 * p), color=BLUE, head=14)
            ux, uy = np.array(pushes[name]) / np.sqrt(2) * p
            ox = wx + sx * 0.9
            arrow(ax, (ox - 0.5 * ux, wy - 0.5 * uy), (ox + 0.5 * ux, wy + 0.5 * uy), color=RED,
                  head=14)
        if t:
            # Along the bottom, left to right: counter-clockwise seen from above.
            ax.add_patch(FancyArrowPatch((-0.7, -0.4), (0.7, -0.4), connectionstyle="arc3,rad=0.9",
                                         arrowstyle="-|>", mutation_scale=22, color=GREEN,
                                         linewidth=3))
        else:
            arrow(ax, (0, 0), (-1.2 * s, 1.2 * f), color=GREEN, head=22, width=3.5)
        ax.set_title(title, fontsize=14)
        ax.set_xlim(-4.0, 4.0)
        ax.set_ylim(-2.4, 3.8)
        ax.set_aspect("equal")
        ax.axis("off")
    fig.text(0.5, 0.06, "blue: which way each wheel spins    red: which way it pushes the robot    "
             "green: what the four pushes add up to", ha="center", fontsize=11)
    save(fig, "fig-l090-wheel-pushes")


DRAWINGS = [stick_shaping, mecanum_x, wheel_handover, odometry_step, field_relative, speed_loop,
            wheel_pushes]
STAND_INS = [panels_graph, robot_front, gamepad_sticks, wheel_names, wheel_forward,
             pinpoint_mounting]

if __name__ == "__main__":
    for draw in DRAWINGS + STAND_INS:
        draw()
    print(f"drew {len(DRAWINGS) + len(STAND_INS)} figures into {OUT}")
