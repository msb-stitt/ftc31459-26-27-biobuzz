import math
from pathlib import Path

import pytest
from scrimmage.protocol import HalState, frame

from biobuzz_sim import fork

FIELD = Path(__file__).parent.parent / "models" / "biobuzz" / "biobuzz.xml"
needs_model = pytest.mark.skipif(not FIELD.exists(), reason="the models are built by `ninja model`")


def wheels(volts: float) -> HalState:
    """Each wheel's motor sent `volts`, as SimRun sends the power the code set times the battery."""
    hal = HalState()
    for name, _ in fork.WHEELS:
        hal.apply(frame("ScrimmageMotor", name, {"<motorVoltage": volts}))
    return hal


@needs_model
def test_the_same_voltage_on_each_wheel_drives_the_robot_forward_along_itself() -> None:
    w = fork.world()
    x0, y0, h0 = w.robots[fork.NAME].robot_pose()
    hal = wheels(6.0)
    for _ in range(1000):
        w.step({fork.NAME: hal})
    x, y, h = w.robots[fork.NAME].robot_pose()
    assert x - x0 > 0.25  # 0.35 m measured; its speed against the link's is scr.compare's
    assert abs(y - y0) < 0.05 and abs(math.remainder(h - h0, 2 * math.pi)) < 0.1


@needs_model
def test_only_the_forks_motors_turn_the_wheels() -> None:
    import mujoco

    w = fork.world()
    for _ in range(200):
        w.step({fork.NAME: wheels(6.0)})
    for hardware, _ in fork.WHEELS:
        a = mujoco.mj_name2id(w.model, mujoco.mjtObj.mjOBJ_ACTUATOR, fork.actuator(hardware))
        assert a >= 0
        assert w.data.actuator_force[a] == 0.0


@needs_model
def test_each_sync_carries_the_drivebases_motion_and_the_launchers_speed() -> None:
    w = fork.world([(72.0, 40.0)])
    w.step({fork.NAME: wheels(0.0)})
    sent = {(typ, dev) for typ, dev, _ in w.robots[fork.NAME].plugins.sensor_updates(HalState())}
    assert sent == {("ScrimmageDrivebase", "drive"), ("Biobuzz", "mechanisms")}
    assert any(n.startswith("pollen") for n in _bodies(w))


def _bodies(w: object) -> list[str]:
    import mujoco

    m = w.model  # type: ignore[attr-defined]
    return [mujoco.mj_id2name(m, mujoco.mjtObj.mjOBJ_BODY, b) or "" for b in range(m.nbody)]
