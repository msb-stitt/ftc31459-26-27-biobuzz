"""Each robot's fitted numbers and where this folder's recipes and models are."""

from pathlib import Path

from mujocobiobuzz.launcher import Flywheel

HOME = Path(__file__).parent.parent.parent
MODELS = HOME / "models"
STARTERBOT = HOME / "robots" / "starterbot.json"

# Each robot's launcher, fitted 2026-11-22 to the spin-ups from rest and coast-downs in waggle's
# logs of Meets 2 and 5, through mujocobiobuzz's HubVelocity at the catalog's 0.144 N m stall
# torque, as mujocobiobuzz's plan's mjb.launcher records. 31459's logs record no battery voltage,
# so its fit took 12.5 V.
FLYWHEELS = {
    "31459": Flywheel(4.45e-5, 2868.0, 0.02, 257.0, 0.300, 12.5),
    "37523": Flywheel(1.09e-4, 2670.0, 0.04, 0.0, 0.253, 13.0),
}
