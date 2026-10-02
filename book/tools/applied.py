"""What applying solutions/ does to mytry, lesson by lesson.

The patches, and the order a student meets them in, live on the solutions line
in solutions/. This checks that line out into a throwaway worktree, runs that
line's own solutions/apply.py there one step at a time, and keeps each patched
file as it was just before and just after each patch. The answer pages and the
cheat sheet are built from that, so they say what applying the patches does, in
order, and cannot disagree with the tool that tests the patches.
"""

import importlib.util
import re
import subprocess
import tempfile
from dataclasses import dataclass
from pathlib import Path

from bookpaths import book_root


@dataclass
class Patched:
    """One patch, as the file it changed looked on either side of it."""
    lesson: str
    path: str
    before: list[str]
    after: list[str]


def _git(*args: str) -> None:
    subprocess.run(["git", *args], cwd=book_root().parent, check=True,
                   capture_output=True, text=True)


def _patched_path(patch: Path) -> str:
    """The file a patch changes, out of its own `+++ b/` line."""
    found = re.search(r"^\+\+\+ b/(.+)$", patch.read_text(encoding="utf-8"), flags=re.M)
    if not found:
        raise SystemExit(f"{patch}: no +++ b/ line, so it names no file")
    return found.group(1)


def _lines(path: Path) -> list[str]:
    return path.read_text(encoding="utf-8").splitlines() if path.exists() else []


def apply_all(ref: str) -> tuple[list[Patched], dict[str, list[str]]]:
    """Every patch in order, and every file in mytry once the last lesson is applied."""
    tree = Path(tempfile.mkdtemp(prefix="applied-"))
    _git("worktree", "add", "--detach", str(tree), ref)
    try:
        spec = importlib.util.spec_from_file_location("solutions_apply",
                                                      tree / "solutions" / "apply.py")
        apply = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(apply)
        patched = []
        for lesson in (tree / "solutions" / "order").read_text().split():
            try:
                for kind, *rest in apply.steps(tree, lesson):
                    if kind == "copy":
                        apply.copy(tree, *rest)
                    elif kind == "patch":
                        path = _patched_path(tree / "solutions" / lesson / rest[0])
                        before = _lines(tree / path)
                        apply.patch(tree, lesson, rest[0])
                        patched.append(Patched(lesson, path, before, _lines(tree / path)))
            except apply.Stop as e:
                raise SystemExit(f"applying {lesson} on {ref}: {e}")
        mytry = tree / apply.TEAMCODE / "mytry"
        final = {p.name: _lines(p) for p in sorted(mytry.glob("*.java"))}
        return patched, final
    finally:
        _git("worktree", "remove", "--force", str(tree))
