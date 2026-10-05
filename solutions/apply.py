"""Status: controlling.

Apply the lessons to mytry in order, the way a student does them, and run the
tests of the lesson it stops at.

    python3 solutions/apply.py l2a
    python3 solutions/apply.py l2b --copies-only --keep
    python3 solutions/apply.py l2b --unpatched
    python3 solutions/apply.py l17b --every

It makes a fresh worktree of solutions-try, so nothing in the checkout it runs
from is touched, and reads `order` and each lesson's `steps` out of that
worktree, so only what is committed is applied. Each step is staged as it is
applied, which makes `git diff` in a kept worktree show what is not yet a patch.

A `steps` file holds one step per line; `#` starts a comment.

    copy lessons/L2aSticksOpMode.java mytry/L2aSticksOpMode.java
    patch L2aSticksOpMode.patch
    test *LessonsTest.l2a*

`copy` paths are under teamcode/, and the copy does what a student does to it:
the folder is made if it is not there, the package line becomes mytry's, an
`@Disabled` line goes, and a copy under a new name has its class renamed. `patch` names a file beside `steps`, applied
from the root with `git apply`. `test` is a `--tests` pattern.
"""

import argparse
import re
import shutil
import subprocess
import sys
import tempfile
import xml.etree.ElementTree as ET
from pathlib import Path

TEAMCODE = Path("TeamCode/src/main/java/org/firstinspires/ftc/teamcode")
PACKAGE = "org.firstinspires.ftc.teamcode"
RESULTS = Path("TeamCode/build/test-results/testDebugUnitTest")


class Stop(Exception):
    """A step that cannot go on, with what to tell the reader."""


def run(cwd: Path, *args: str) -> subprocess.CompletedProcess:
    return subprocess.run(args, cwd=cwd, text=True, capture_output=True)


def steps(tree: Path, lesson: str) -> list[list[str]]:
    path = tree / "solutions" / lesson / "steps"
    if not path.exists():
        raise Stop(f"no {path.relative_to(tree)}")
    out = []
    for line in path.read_text().splitlines():
        line = line.split("#", 1)[0].strip()
        if line:
            out.append(line.split())
    return out


def copy(tree: Path, src: str, dst: str) -> None:
    source, target = tree / TEAMCODE / src, tree / TEAMCODE / dst
    if not source.exists():
        raise Stop(f"copy: no {TEAMCODE / src}")
    if target.exists():
        raise Stop(f"copy: {TEAMCODE / dst} is already there")
    text = source.read_text()
    package = dst.split("/")[0]
    text = re.sub(r"^package [\w.]+;", f"package {PACKAGE}.{package};", text, count=1, flags=re.M)
    text = re.sub(r"^@Disabled\n", "", text, flags=re.M)
    old, new = Path(src).stem, Path(dst).stem
    if old != new:
        text = re.sub(rf"\b{old}\b", new, text)
    target.parent.mkdir(exist_ok=True)
    target.write_text(text)
    run(tree, "git", "add", str(TEAMCODE / dst))


def patch(tree: Path, lesson: str, name: str) -> None:
    path = Path("solutions") / lesson / name
    done = run(tree, "git", "apply", "--index", str(path))
    if done.returncode != 0:
        raise Stop(f"patch {path} does not apply:\n{done.stderr.strip()}")


def test(tree: Path, patterns: list[str]) -> bool:
    if not patterns:
        raise Stop("no test lines, so nothing says the lesson worked")
    shutil.rmtree(tree / RESULTS, ignore_errors=True)
    args = ["./gradlew", "-q", ":TeamCode:testDebugUnitTest"]
    for p in patterns:
        args += ["--tests", p]
    done = run(tree, *args)
    cases = []
    for xml in sorted((tree / RESULTS).glob("TEST-*.xml")):
        for case in ET.parse(xml).getroot().iter("testcase"):
            bad = case.find("failure")
            if bad is None:
                bad = case.find("error")
            cases.append((case.get("classname").rsplit(".", 1)[-1], case.get("name"),
                          None if bad is None else bad.get("message", "").splitlines()[0]))
    if not cases:
        print(done.stdout + done.stderr)
        raise Stop("no test results were written")
    for cls, name, failure in cases:
        print(f"  {'pass' if failure is None else 'FAIL'}  {cls}.{name}")
        if failure is not None:
            print(f"        {failure}")
    return all(f is None for _, _, f in cases)


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[1])
    ap.add_argument("lesson", help="the lesson to stop at, as order names it")
    ap.add_argument("--ref", default="solutions-try", help="what the worktree checks out")
    ap.add_argument("--copies-only", action="store_true",
                    help="stop the last lesson after its copies, and run no tests")
    ap.add_argument("--unpatched", action="store_true",
                    help="leave the last lesson's patches out and run its tests, to see them fail")
    ap.add_argument("--every", action="store_true",
                    help="after each lesson, run the tests of every lesson so far")
    ap.add_argument("--keep", action="store_true", help="leave the worktree in place")
    args = ap.parse_args()

    home = Path(run(Path.cwd(), "git", "rev-parse", "--show-toplevel").stdout.strip())
    tree = Path(tempfile.mkdtemp(prefix=f"apply-{args.lesson}-"))
    added = run(home, "git", "worktree", "add", "--detach", str(tree), args.ref)
    if added.returncode != 0:
        print(added.stderr, file=sys.stderr)
        return 1
    if (home / "local.properties").exists():
        shutil.copy(home / "local.properties", tree / "local.properties")
    keep = args.keep or args.copies_only
    ok = False
    try:
        order = (tree / "solutions" / "order").read_text().split()
        if args.lesson not in order:
            raise Stop(f"{args.lesson} is not in solutions/order")
        upto = order[: order.index(args.lesson) + 1]
        tests: list[str] = []
        so_far: list[str] = []
        every_ok = True
        for lesson in upto:
            print(f"{lesson}:")
            last = lesson == args.lesson
            for step in steps(tree, lesson):
                kind, rest = step[0], step[1:]
                if kind == "copy" and len(rest) == 2:
                    copy(tree, *rest)
                    print(f"  copy {rest[0]} -> {rest[1]}")
                elif kind == "patch" and len(rest) == 1:
                    if last and (args.copies_only or args.unpatched):
                        continue
                    patch(tree, lesson, rest[0])
                    print(f"  patch {rest[0]}")
                elif kind == "test" and len(rest) == 1:
                    so_far.append(rest[0])
                    if last:
                        tests.append(rest[0])
                else:
                    raise Stop(f"solutions/{lesson}/steps: cannot read {' '.join(step)}")
            if args.every and not last:
                print(f"every test through {lesson}:")
                every_ok = test(tree, so_far) and every_ok
        if args.every:
            print(f"every test through {args.lesson}:")
            ok = test(tree, so_far) and every_ok
        elif args.copies_only:
            ok = True
        else:
            print(f"{args.lesson} tests:")
            ok = test(tree, tests)
    except Stop as e:
        print(f"stopped: {e}")
    finally:
        if (ok or args.unpatched) and not keep:
            run(home, "git", "worktree", "remove", "--force", str(tree))
        else:
            print(f"worktree left at {tree}")
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
