"""StarterBot on the field, served by msb-stitt's Scrimmage fork for `SimRun` over halsim_ws.

The fork's counterpart to `mujocobiobuzz.link`: the same field and robot from
`field.with_robots`, its four wheels the fork's fast-mode motors named as `SimRun` names them,
its motion reported by the fork's drivebase plugin, and its launcher and intake by
`biobuzz_sim.plugins.Mechanisms`, the field and robot built from this folder's recipes.
"""

from __future__ import annotations

import logging
from pathlib import Path
from typing import Any

from mujocobiobuzz import field as fieldmodel

from biobuzz_sim.robots import MODELS, STARTERBOT

log = logging.getLogger(__name__)

IN = 0.0254
NAME = "37523"
PREFIX = "robot_"
# goBILDA's 312 rpm Yellow Jacket, its currents goBILDA's, not measured here.
GOBILDA_312 = {"stall_torque": 2.383, "stall_current": 9.2, "free_current": 0.25, "free_rpm": 312}
# SimRun's hardware names, spaces as underscores, and each wheel's joint.
WHEELS = (("Front_Left", "fl"), ("Front_Right", "fr"), ("Back_Left", "bl"), ("Back_Right", "br"))
WHEEL_RADIUS_M = 0.052
# Two of 37523, each put down clear of the hive's stand, one near each alliance's wall.
TWO = {NAME: (PREFIX, (72.0, 30.0, 0.0)), NAME + "b": ("robotb_", (72.0, 114.0, 0.0))}


def config(
    prefix: str = PREFIX, robot: str = NAME, start: tuple[float, float, float] = (72.0, 72.0, 0.0)
) -> dict[str, Any]:
    """The fork's configuration for one StarterBot under `prefix`, put down at `start`,
    (x in, y in, heading rad)."""
    joint = prefix + "starterbot_wheel_"
    return {
        "robot": {
            "body": prefix + "starterbot",
            "start_pose": [start[0] * IN, start[1] * IN, start[2]],
        },
        "motor": [
            {
                "name": n,
                "scrimmage": n,
                "joint": joint + j,
                "motor": GOBILDA_312,
                "count": 1,
                "gear_ratio": 1.0,
            }
            for n, j in WHEELS
        ],
        "plugin": [
            {
                "class": "scrimmage.drivebase:Drivebase",
                "device": "drive",
                "wheels": [joint + j for _, j in WHEELS],
                "wheel_radius": WHEEL_RADIUS_M,
            },
            {"class": "biobuzz_sim.plugins:Mechanisms", "prefix": prefix, "robot": robot},
        ],
    }


def actuator(hardware: str, prefix: str = PREFIX) -> str:
    """The model's own actuator for a wheel's motor, named by its hardware name with spaces."""
    return prefix + hardware.replace("_", " ")


def world(
    pollen: list[tuple[float, float]] | None = None,
    models: Path = MODELS,
    spec: Path = STARTERBOT,
    robots: dict[str, tuple[str, tuple[float, float, float]]] | None = None,
) -> Any:
    """The fork's world: the field, a POLLEN at each (x in, y in), and a StarterBot for each of
    `robots`, by its name, served at `/wpilibws/<name>`: its prefix and where it is put down,
    (x in, y in, heading rad), until its program sets its pose; one, 37523, at the field's centre,
    unless given. Each its parts' contacts `spec`'s, its recipe, and its launcher 37523's."""
    from mujocobiobuzz import balls
    from scrimmage.multi import MultiWorld

    placed = [
        (balls.POLLEN, f"pollen{i}", (x * IN, y * IN, balls.POLLEN.radius_m + 0.001))
        for i, (x, y) in enumerate(pollen or [])
    ]
    robots = robots or {NAME: (PREFIX, (72.0, 72.0, 0.0))}
    m = fieldmodel.with_robots(
        {prefix: (x * IN, y * IN, h) for prefix, (x, y, h) in robots.values()},
        models=models,
        balls=placed,
        spec=spec,
    )
    # The fork's motors drive the wheels' joints, back-EMF and all; the model's own wheel
    # actuators, which the link drives, would add their back-EMF again.
    for prefix, _ in robots.values():
        for hardware, _ in WHEELS:
            a = m.actuator(actuator(hardware, prefix)).id
            m.actuator_gainprm[a, :] = 0.0
            m.actuator_biasprm[a, :] = 0.0
    configs = {
        name: (config(prefix, start=start), models) for name, (prefix, start) in robots.items()
    }
    return MultiWorld(m, configs, timestep=0.001)


def main() -> None:
    """`python -m biobuzz_sim.fork [--port N] [--two] [--viewer] [--pollen X,Y]...
    [--video OUT.mp4]`: StarterBot served by the fork at `/wpilibws/37523`, for `SimRun` started
    with `SIMRUN_SCRIMMAGE=localhost:N/37523`, in real time until Ctrl-C or the viewer is closed.
    `--two` serves a second 37523 at `/wpilibws/37523b`, each put down apart until its program
    sets its pose.
    `--pollen` puts a POLLEN on the floor at (X in, Y in); `--video` draws the run to a video.
    `--viewer` draws it live; on macOS it runs under `mjpython`, which ships with MuJoCo."""
    import argparse
    import asyncio
    import signal

    from scrimmage.multi import MultiServer

    from biobuzz_sim.plugins import Mechanisms

    logging.basicConfig(level=logging.INFO, format="%(levelname)s %(message)s")
    p = argparse.ArgumentParser(description=main.__doc__)
    p.add_argument("--port", type=int, default=3300)
    p.add_argument("--two", action="store_true")
    p.add_argument("--viewer", action="store_true")
    p.add_argument("--pollen", action="append", default=[], metavar="X,Y")
    p.add_argument("--video", type=Path)
    a = p.parse_args()
    robots = TWO if a.two else None
    w = world([(float(x), float(y)) for x, y in (xy.split(",") for xy in a.pollen)], robots=robots)
    server = MultiServer(w, port=a.port, speed=1.0)
    drawn = None
    if a.video is not None:
        from mujocobiobuzz import video

        drawn = video.Video(w.model, a.video)
        server.on_answer = lambda _name, _t: drawn(w.data)

    async def run() -> None:
        task = asyncio.create_task(server.run())
        try:
            asyncio.get_running_loop().add_signal_handler(signal.SIGINT, server.done.set)
        except (ValueError, RuntimeError):
            pass  # under mjpython this is not the main thread; closing the viewer stops it
        viewer = None
        if a.viewer:
            import mujoco.viewer
            from mujocobiobuzz import video

            viewer = mujoco.viewer.launch_passive(
                w.model, w.data, show_left_ui=False, show_right_ui=False
            )
            viewer.cam.lookat[:] = video.LOOKAT
            viewer.cam.distance = video.DISTANCE_M
            viewer.cam.azimuth, viewer.cam.elevation = video.AZIMUTH_DEG, video.ELEVATION_DEG
        while not task.done() and not server.done.is_set():
            if viewer is not None:
                if not viewer.is_running():
                    break
                viewer.sync()
            await asyncio.sleep(0.03)
        server.done.set()
        await task

    try:
        asyncio.run(run())
    finally:
        if drawn is not None:
            drawn.close()
    steps = max(1, round(w.data.time / w.model.opt.timestep))
    for name, robot in w.robots.items():
        host = robot.plugins
        for plugin, seconds in zip(host.plugins, host.seconds, strict=True):
            log.info(
                "%s %s: %.1f us a step over %d steps",
                name,
                type(plugin).__name__,
                seconds / steps * 1e6,
                steps,
            )
        shooter = next(p.shooter for p in host.plugins if isinstance(p, Mechanisms))
        for s in shooter.shots:
            log.info(
                "%s shot at %.2f s: %.2f m/s, %.1f deg up, at %.0f ticks/s",
                name,
                s.t,
                s.speed,
                s.up_deg,
                s.flywheel_tps,
            )
        log.info("%s: %d shots", name, len(shooter.shots))


if __name__ == "__main__":
    main()
