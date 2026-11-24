from pathlib import Path
from types import SimpleNamespace
from typing import Any

import mujoco
import pytest
from mujocobiobuzz import nt

from biobuzz_sim.plugins import DEVICE, Mechanisms

SCENE = Path(__file__).parent.parent / "models" / "starterbot" / "scene.xml"
needs_model = pytest.mark.skipif(not SCENE.exists(), reason="the model is built by `ninja model`")
# 37523's launcher gains, as test_link's launcher test gives them.
F = (0.05 + 0.00037963 * 1325) * 32767 / 1325
GAINS = {"<launcher_p": 40.0, "<launcher_i": 0.0, "<launcher_d": 0.0, "<launcher_f": F}


def world() -> Any:
    m = mujoco.MjModel.from_xml_path(str(SCENE))
    return SimpleNamespace(model=m, data=mujoco.MjData(m))


def hal(values: dict[str, Any]) -> Any:
    """A halsim state holding what SimRun sends on the plugin's device."""
    return SimpleNamespace(devices={DEVICE: dict(values)})


def run(w: Any, p: Mechanisms, h: Any, seconds: float) -> None:
    """The plugin's hooks around each physics step, as the fork calls them."""
    for _ in range(round(seconds / w.model.opt.timestep)):
        p.before_step(h)
        mujoco.mj_step(w.model, w.data)
        p.after_step(h)


def tps(p: Mechanisms, h: Any) -> float:
    ((typ, dev, data),) = p.sensor_updates(h)
    assert (typ, dev) == DEVICE
    return float(data[">launcher_tps"])


@needs_model
def test_given_the_launchers_gains_the_hub_holds_its_target_on_the_robots_flywheel() -> None:
    w = world()
    p = Mechanisms(w, prefix="", robot="37523")
    h = hal({"</launcher/target_tps": 1325.0, **GAINS})
    run(w, p, h, 1.0)
    assert tps(p, h) == pytest.approx(1325, rel=0.03)


@needs_model
def test_with_no_gains_yet_the_launcher_is_given_its_targets_share_as_the_link_gives_it() -> None:
    w = world()
    p = Mechanisms(w, prefix="", robot="37523")
    h = hal({"</launcher/target_tps": 1325.0})
    run(w, p, h, 0.2)
    assert tps(p, h) == 0.0
    free_tps = nt.LAUNCHER_FREE_RPM / 60 * nt.LAUNCHER_TICKS_PER_REV
    assert w.data.ctrl[p.fly_act] == pytest.approx(1325.0 / free_tps)


@needs_model
def test_the_intake_and_the_windmill_take_the_codes_values() -> None:
    w = world()
    p = Mechanisms(w, prefix="", robot="37523")
    p.before_step(hal({"</intake/speed": 0.75, "</launcher/feeding": True}))
    act = {
        n: w.data.ctrl[mujoco.mj_name2id(w.model, mujoco.mjtObj.mjOBJ_ACTUATOR, n)]
        for n in ("intake", "left_intake_servo", "right_intake_servo", "windmillServo")
    }
    assert act == {
        "intake": 0.75,
        "left_intake_servo": 0.75,
        "right_intake_servo": 0.75,
        "windmillServo": 1.0,
    }


@needs_model
def test_a_robot_with_no_fitted_launcher_is_named() -> None:
    with pytest.raises(ValueError, match="88888"):
        Mechanisms(world(), prefix="", robot="88888")
