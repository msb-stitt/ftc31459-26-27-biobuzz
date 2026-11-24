# sim: the BIOBUZZ field and our robots for the sim servers

**Status: controlling.**

This folder holds what our team's repo gives the sim servers: the BIOBUZZ field's recipe, each
robot's recipe and fitted numbers, and the plugins that run its mechanisms. The servers' code
names no robot: `mujocobiobuzz` is the library that converts CAD and models the launcher and the
shot, and msb-stitt's Scrimmage fork is the server that steps the field for each robot program.

The models are built on each computer from goBILDA's and FIRST's own downloads, and are not in
git. `ninja model` downloads and converts them, and `ninja check` runs this folder's tests.

`uv run python -m biobuzz_sim.fork` serves the field and StarterBot to `simRun` started with
`SIMRUN_SCRIMMAGE=localhost:3300/37523`; `--help` gives its options. With `--two` it serves two
of 37523, the second at `localhost:3300/37523b`, for a second `simRun` on its own NetworkTables
port, as the README's cheat sheet says.
