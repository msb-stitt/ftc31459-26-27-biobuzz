"""StarterBot's launcher and intake as a plugin for msb-stitt's Scrimmage fork, the sim server.

The fork moves the drivetrain itself; this does for the rest what `link.Stepper.step` does for
`mujocobiobuzz.link`, with the same classes, `launcher.HubVelocity` and `shot.Shooter`. Named in
a robot's configuration:

    [[plugin]]
    class = "biobuzz_sim.plugins:Mechanisms"
    prefix = "r37523_"
    robot = "37523"

`SimRun` sends the values it sends the link, `nt.POWERS`' intake topic, `nt.FEEDING` and
`nt.TARGET_TPS`, each as `<` and its topic on the halsim device `DEVICE`, and the gains the code
gave the launcher's motor as `<launcher_p`, `<launcher_i`, `<launcher_d` and `<launcher_f`. Each
physics step this sets the intake's and the windmill's motors, holds the flywheel as the hub does
and takes a shot when a ball touches it; each sync it sends the hub's measured speed back as
`>launcher_tps`.
"""

from __future__ import annotations

from typing import Any

import mujoco
from mujocobiobuzz import launcher, nt, shot

from biobuzz_sim.robots import FLYWHEELS, STARTERBOT

DEVICE = ("Biobuzz", "mechanisms")
GAINS = ("<launcher_p", "<launcher_i", "<launcher_d", "<launcher_f")


class Mechanisms:
    """One StarterBot's launcher and intake, under `prefix` in the world's model, its flywheel
    fitted to `robot`'s logs, its shot `robots/starterbot.json`'s."""

    def __init__(self, world: Any, prefix: str, robot: str) -> None:
        self.m, self.d, self.prefix = world.model, world.data, prefix
        if robot not in FLYWHEELS:
            raise ValueError(f"no launcher fitted for robot {robot!r}")
        launcher.fit(self.m, FLYWHEELS[robot], prefix)
        self.delay_s = FLYWHEELS[robot].delay_s
        fly = mujoco.mj_name2id(self.m, mujoco.mjtObj.mjOBJ_JOINT, prefix + "starterbot_flywheel")
        self.fly_q, self.fly_v = self.m.jnt_qposadr[fly], self.m.jnt_dofadr[fly]
        self.fly_act = mujoco.mj_name2id(self.m, mujoco.mjtObj.mjOBJ_ACTUATOR, prefix + "launcher")
        self.shooter = shot.Shooter(self.m, shot.ShotSpec.load(STARTERBOT), prefix=prefix)
        self.hub: launcher.HubVelocity | None = None

    def reset(self) -> None:
        self.hub = None

    def before_step(self, hal: Any) -> None:
        dev = hal.devices.get(DEVICE, {})
        values = {k[1:]: v for k, v in dev.items() if k.startswith("</")}
        nt.apply(self.m, self.d, nt.controls(values), self.prefix)
        if all(k in dev for k in GAINS):
            g = launcher.Gains(*(float(dev[k]) for k in GAINS))
            if self.hub is None:
                self.hub = launcher.HubVelocity(g, delay_s=self.delay_s)
            self.hub.gains = g
        if self.hub is not None:
            target = float(values.get(nt.TARGET_TPS, 0.0))
            u = self.hub.power(self.d.time, target, float(self.d.qpos[self.fly_q]))
            if self.hub.floating:
                u = launcher.float_ctrl(self.m, self.fly_act, float(self.d.qvel[self.fly_v]))
            self.d.ctrl[self.fly_act] = u

    def after_step(self, hal: Any) -> None:
        self.shooter.after_step(self.d)

    def sensor_updates(self, hal: Any) -> list[tuple[str, str, dict[str, Any]]]:
        tps = self.hub.measured_tps if self.hub is not None else 0.0
        return [(*DEVICE, {">launcher_tps": tps})]
