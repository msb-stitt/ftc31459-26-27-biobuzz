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


@needs_model
def test_the_robots_contacts_are_its_recipes(tmp_path: Path) -> None:
    import json

    import mujoco

    from biobuzz_sim.robots import STARTERBOT

    recipe = json.loads(STARTERBOT.read_text())
    recipe["contacts"]["starterbot_intake"]["friction"] = [0.25, 0.005, 0.0001]
    given = tmp_path / "starterbot.json"
    given.write_text(json.dumps(recipe))
    m = fork.world(spec=given).model
    intake = mujoco.mj_name2id(m, mujoco.mjtObj.mjOBJ_BODY, fork.PREFIX + "starterbot_intake")
    frictions = {float(m.geom_friction[g, 0]) for g in range(m.ngeom) if m.geom_bodyid[g] == intake}
    assert 0.25 in frictions


@needs_model
def test_two_of_37523_each_drive_on_their_own_program_only() -> None:
    import mujoco

    w = fork.world(robots=fork.TWO)
    a, b = fork.NAME, fork.NAME + "b"
    a0, b0 = w.robots[a].robot_pose(), w.robots[b].robot_pose()
    for _ in range(1000):
        w.step({a: wheels(6.0), b: HalState()})
    a1, b1 = w.robots[a].robot_pose(), w.robots[b].robot_pose()
    assert a1[0] - a0[0] > 0.25
    assert math.dist(b1[:2], b0[:2]) < 0.05
    for prefix in ("robot_", "robotb_"):
        for hardware, _ in fork.WHEELS:
            act = mujoco.mj_name2id(
                w.model, mujoco.mjtObj.mjOBJ_ACTUATOR, fork.actuator(hardware, prefix)
            )
            assert w.data.actuator_force[act] == 0.0
