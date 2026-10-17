"""Sends commands to simRun's virtual gamepads.

simRun takes them when it was started with SIMRUN_VIRTUAL_PADS set to a port
and --pad or --pad-check given; .docs/simRunRequirementsREADME.md, *Virtual
gamepads*, says what a command does. This sends one command, or is imported by
a script that sends many. It keeps no motion: a script moves a stick over time
by sending a run of values.

Usage: python tools/vpad.py [--port N] <gamepad> <control> <value>
       python tools/vpad.py 1 left_stick_y -0.73

The port is --port, else SIMRUN_VIRTUAL_PADS. Standard library only.
"""

import os
import socket
import sys


class Pads:
    """One connection to simRun's port, for any number of virtual gamepads."""

    def __init__(self, port: int, host: str = "127.0.0.1", timeout: float = 2.0):
        self._socket = socket.create_connection((host, port), timeout=timeout)
        self._replies = self._socket.makefile("r", encoding="utf-8", newline="\n")

    def set(self, pad: int, control: str, value: float) -> None:
        """Sets one control of gamepad `pad`, attaching it if it is new.

        Raises ValueError with simRun's reason when it refuses the command.
        """
        text = f"{value:g}" if isinstance(value, float) else str(value)
        self._socket.sendall(f"pad {pad} {control} {text}\n".encode("utf-8"))
        reply = self._replies.readline().strip()
        if reply != "ok":
            raise ValueError(reply.removeprefix("error ") or "simRun closed the connection")

    def close(self) -> None:
        self._replies.close()
        self._socket.close()

    def __enter__(self) -> "Pads":
        return self

    def __exit__(self, *exc) -> None:
        self.close()


def main(argv: list[str]) -> int:
    port = os.environ.get("SIMRUN_VIRTUAL_PADS")
    if len(argv) >= 2 and argv[0] == "--port":
        port, argv = argv[1], argv[2:]
    if len(argv) != 3 or port is None:
        print(__doc__.split("\n\n")[2], file=sys.stderr)
        return 2
    try:
        with Pads(int(port)) as pads:
            pads.set(int(argv[0]), argv[1], argv[2])
    except (OSError, ValueError) as e:
        print(f"vpad.py: {e}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
