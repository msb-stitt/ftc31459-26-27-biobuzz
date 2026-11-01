"""Status: controlling.

Try one release's lesson answers on another release's code, a lesson at a time, both ways.

    python3 tools/crosscheck.py v0.4.0 v0.5.0-rc.3
    python3 tools/crosscheck.py v0.4.0 v0.5.0-rc.3 --only l120
    python3 tools/crosscheck.py v0.5.0-rc.3 v0.5.0-rc.3 --one-way

Each argument is any ref or commit hash. One that holds solutions/order is used as it is; one that
does not, such as a release tag or a commit on dev, is followed to the solutions ref its
book/keep.toml names. --one-way runs only the first's answers on the second's code. A release run
against itself with --one-way lists the earlier lessons each lesson needs.

The lists are printed, and written under --out, by default crosscheck-<first>-<second>/: report.md,
and for each direction run <source>-on-<base>.jsonc, whose opening comments say what each field
holds.

For each direction, the answers' release is the source and the other is the base. A worktree of the
base's solutions ref is the base code, with none of its own answers applied. Each source lesson is
tried by itself: its copies and patches, then the base's tests for that lesson, as the base's
`steps` names them. A lesson the base's order does not have fails.

When the build, a test or a needed lesson's patch names a file or class that an earlier source
lesson made or patched, that lesson is applied first, the nearest such lesson first, and the lesson
is tried again, until it passes or no earlier lesson explains the error. A lesson pulled in brings
the lessons it was found to need when it passed, so a chain is found once. The result names the
lessons each one needed.

Each try is committed on the worktree's detached HEAD, and the worktree switched back to the base,
so nothing is discarded. The worktree is removed at the end unless --keep is given.
"""

import argparse
import datetime
import json
import re
import shutil
import subprocess
import sys
import tempfile
import xml.etree.ElementTree as ET
from pathlib import Path

from apply import PACKAGE, RESULTS, TEAMCODE, TESTCODE, Stop, copy, run, steps


def solutions_ref(home: Path, name: str) -> str:
    """The name itself when it holds solutions/order, or else the solutions ref its keep.toml
    names."""
    if run(home, "git", "cat-file", "-e", f"{name}:solutions/order").returncode == 0:
        return name
    keep = run(home, "git", "show", f"{name}:book/keep.toml")
    found = re.search(r'^solutions = "([^"]+)"', keep.stdout, re.M) if keep.returncode == 0 else None
    return found.group(1) if found else name


def export_solutions(home: Path, ref: str, into: Path) -> list[str]:
    """Write ref's solutions/ under into, and give back its order."""
    archive = subprocess.run(["git", "archive", ref, "solutions"], cwd=home, capture_output=True)
    if archive.returncode != 0:
        raise Stop(f"git archive {ref} solutions: {archive.stderr.decode().strip()}")
    subprocess.run(["tar", "-x", "-C", str(into)], input=archive.stdout, check=True)
    return (into / "solutions" / "order").read_text().split()


def touched(source: Path, lesson: str) -> set[str]:
    """The paths a lesson's copies make and its patches change, from the repository root."""
    out = set()
    for step in steps(source, lesson):
        if step[0] == "copy" and len(step) == 3:
            out.add(str(TEAMCODE / step[2]))
        elif step[0] == "copy" and len(step) == 4:
            out.add(str(TESTCODE / step[3]))
        elif step[0] == "patch":
            text = (source / "solutions" / lesson / step[1]).read_text()
            out.update(re.findall(r"^\+\+\+ b/(\S+)", text, re.M))
    return out


def patch_from(source: Path, tree: Path, lesson: str, name: str) -> None:
    text = (source / "solutions" / lesson / name).read_text().encode()
    done = subprocess.run(["git", "apply", "--index", "-"], cwd=tree, input=text,
                          capture_output=True)
    if done.returncode != 0:
        raise Stop(f"patch {lesson}/{name} does not apply:\n{done.stderr.decode().strip()}")


def apply_lesson(source: Path, tree: Path, lesson: str) -> None:
    """A source lesson's copies and patches, from the base code's starters."""
    for step in steps(source, lesson):
        kind, rest = step[0], step[1:]
        if kind == "copy" and len(rest) == 2:
            copy(tree, *rest)
        elif kind == "copy" and len(rest) == 3 and rest[0] == "test":
            copy(tree, rest[1], rest[2], TESTCODE)
        elif kind == "patch" and len(rest) == 1:
            patch_from(source, tree, lesson, rest[0])
        elif kind != "test":
            raise Stop(f"{lesson}/steps: cannot read {' '.join(step)}")


def base_tests(tree: Path, lesson: str) -> list[str]:
    """The base's tests for a lesson: its test files copied where the source left none, and its
    test patterns."""
    patterns = []
    for step in steps(tree, lesson):
        if step[0] == "copy" and len(step) == 4 and not (tree / TESTCODE / step[3]).exists():
            copy(tree, step[2], step[3], TESTCODE)
        elif step[0] == "test":
            patterns.append(step[1])
    return patterns


def run_tests(tree: Path, patterns: list[str]) -> tuple[list[tuple[str, str, str | None]], str]:
    """Each test case as (class, name, failure or None), and what Gradle printed."""
    shutil.rmtree(tree / RESULTS, ignore_errors=True)
    args = ["./gradlew", "-q", ":TeamCode:testDebugUnitTest"]
    for p in patterns:
        args += ["--tests", p]
    done = run(tree, *args)
    cases = []
    for xml in sorted((tree / RESULTS).glob("TEST-*.xml")):
        for case in xml_cases(xml):
            cases.append(case)
    return cases, done.stdout + done.stderr


def xml_cases(xml: Path) -> list[tuple[str, str, str | None]]:
    out = []
    for case in ET.parse(xml).getroot().iter("testcase"):
        bad = case.find("failure")
        if bad is None:
            bad = case.find("error")
        message = None
        if bad is not None:
            message = (bad.get("message") or bad.text or "").strip().splitlines()[0:1]
            message = message[0] if message else "(no message)"
        out.append((case.get("classname").rsplit(".", 1)[-1], case.get("name"), message))
    return out


def explaining(text: str, earlier: list[str], files: dict[str, set[str]]) -> str | None:
    """The nearest earlier lesson whose files the error names, by path, class or package."""
    paths = set(re.findall(r"(TeamCode/src/\S+?\.java)", text))
    names = set(re.findall(r"symbol:\s+class\s+(\w+)", text))
    names |= set(re.findall(r"location:\s+(?:class|interface)\s+(?:[\w.]+\.)?(\w+)", text))
    names |= set(re.findall(r"of type\s+(?:[\w.]+\.)?(\w+)", text))
    names |= set(re.findall(r"ClassNotFoundException:\s+[\w.]*?\.?(\w+)\b", text))
    packages = {p.removeprefix(PACKAGE + ".")
                for p in re.findall(r"package ([\w.]+) does not exist", text)}
    for lesson in reversed(earlier):
        for f in files[lesson]:
            stem = Path(f).stem
            root = TESTCODE if f.startswith(str(TESTCODE)) else TEAMCODE
            folder = ".".join(Path(f).parent.relative_to(root).parts) \
                if f.startswith(str(root)) else None
            if any(p.endswith(f) or f.endswith(p) for p in paths) or stem in names \
                    or folder in packages:
                return lesson
    return None


def fresh(tree: Path, base: str) -> None:
    """Commit whatever the last try left, then go back to the base."""
    run(tree, "git", "add", "-A", "TeamCode/src")
    run(tree, "git", "commit", "-q", "--no-verify", "--allow-empty", "-m", "crosscheck try")
    switched = run(tree, "git", "switch", "-q", "--detach", base)
    if switched.returncode != 0:
        raise Stop(f"git switch back to the base: {switched.stderr.strip()}")


def try_lesson(source: Path, tree: Path, base: str, lesson: str, needed: list[str]):
    """Apply needed then lesson, run the base's tests; give back (passed, detail, error text)."""
    fresh(tree, base)
    for earlier in needed:
        try:
            apply_lesson(source, tree, earlier)
        except Stop as e:
            return False, [f"needed {earlier}, which does not apply: {e}"], str(e)
    try:
        apply_lesson(source, tree, lesson)
        patterns = base_tests(tree, lesson)
    except Stop as e:
        return False, [str(e)], str(e)
    if not patterns:
        return False, ["the base names no tests for it"], ""
    cases, output = run_tests(tree, patterns)
    if not cases:
        errors = [line for line in output.splitlines() if "error:" in line][:5]
        return False, ["the build failed"] + errors, output
    failed = [f"{c}.{n}: {m}" for c, n, m in cases if m is not None]
    if failed:
        return False, failed, "\n".join(failed) + "\n" + output
    return True, [f"{len(cases)} test(s)"], ""


def direction(home: Path, src_ref: str, base_ref: str, only: list[str], keep: bool) -> list[dict]:
    """Each source lesson tried on the base code: lesson, result, needs, tests, reason, details."""
    work = Path(tempfile.mkdtemp(prefix="crosscheck-"))
    source = work / "source"
    source.mkdir()
    order = export_solutions(home, src_ref, source)
    files = {lesson: touched(source, lesson) for lesson in order}
    tree = work / "base"
    added = run(home, "git", "worktree", "add", "--detach", str(tree), base_ref)
    if added.returncode != 0:
        raise Stop(f"git worktree add {base_ref}: {added.stderr.strip()}")
    if (home / "local.properties").exists():
        shutil.copy(home / "local.properties", tree / "local.properties")
    base = run(tree, "git", "rev-parse", "HEAD").stdout.strip()
    base_order = (tree / "solutions" / "order").read_text().split()
    results: list[dict] = []
    known: dict[str, list[str]] = {}
    try:
        for i, lesson in enumerate(order):
            if only and lesson not in only:
                continue
            if lesson not in base_order:
                result = {"lesson": lesson, "result": "fail", "needs": [], "tests": 0,
                          "reason": "not in the other order", "details": []}
            else:
                needed: list[str] = []
                while True:
                    ok, detail, text = try_lesson(source, tree, base, lesson, needed)
                    if ok:
                        break
                    more = explaining(text, [e for e in order[:i] if e not in needed], files)
                    if more is None:
                        break
                    adding = {more, *known.get(more, [])}
                    needed = [e for e in order[:i] if e in needed or e in adding]
                if ok:
                    known[lesson] = needed
                    result = {"lesson": lesson, "result": "pass", "needs": needed,
                              "tests": int(detail[0].split()[0]), "reason": None, "details": []}
                else:
                    result = {"lesson": lesson, "result": "fail", "needs": needed, "tests": 0,
                              "reason": detail[0].splitlines()[0],
                              "details": detail[0].splitlines()[1:] + detail[1:]}
            results.append(result)
            print("\n".join(printed(result)), flush=True)
    finally:
        if keep:
            print(f"worktree left at {tree}")
        else:
            run(home, "git", "worktree", "remove", "--force", str(tree))
            shutil.rmtree(work, ignore_errors=True)
    print(count(results), flush=True)
    return results


def printed(r: dict) -> list[str]:
    need = f", needing {', '.join(r['needs'])}" if r["needs"] else ""
    if r["result"] == "pass":
        return [f"  pass  {r['lesson']}{need}: {r['tests']} test(s)"]
    return [f"  FAIL  {r['lesson']}{need}: {r['reason']}"] + [f"          {d}" for d in r["details"]]


def count(results: list[dict]) -> str:
    return f"{sum(r['result'] == 'pass' for r in results)} of {len(results)} pass"


def cell(text: str) -> str:
    return text.replace("|", "\\|").replace("\n", " ")


def report(runs: list[dict], command: str) -> str:
    out = ["# Crosscheck", "", f"`{command}`, run {runs[0]['date']}.", ""]
    for d in runs:
        s, b = d["source"], d["base"]
        out += [f"## {s['name']}'s answers on {b['name']}'s code", "",
                f"`{s['solutions']}` at `{s['commit'][:8]}` on `{b['solutions']}` at "
                f"`{b['commit'][:8]}`: {count(d['results'])}.", "",
                "| Lesson | Result | Needs | Reason |", "| --- | --- | --- | --- |"]
        for r in d["results"]:
            reason = r["reason"] or ""
            if reason.endswith(":") and r["details"]:
                reason += " " + r["details"][0]
            out.append(f"| {r['lesson']} | {r['result']} | {', '.join(r['needs'])} | "
                       f"{cell(reason)} |")
        out.append("")
    return "\n".join(out)


def chains(d: dict, command: str) -> str:
    head = f"""// Made by tools/crosscheck.py on {d['date']}: {command}
// {d['source']['name']}'s lesson answers, each tried by itself on {d['base']['name']}'s code with
// that code's own tests for the lesson.
//
// source, base: each release's name as given, the solutions ref used, and its commit.
// lessons: one entry a lesson, in the source's order.
//   result: "pass" or "fail".
//   needs: the earlier source lessons this one needed, directly or through another, in lesson
//     order. It holds only what a build error, a test failure or a patch that did not apply
//     named, by file, class or package; a dependency that is not code is not here.
//   tests: how many of the base's test cases ran and passed; 0 for a failure.
//   reason: for a failure, its first line; null for a pass.
//   details: for a failure, the test cases that failed, or the build or patch errors.
"""
    body = {"source": d["source"], "base": d["base"],
            "lessons": {r["lesson"]: {k: r[k] for k in ("result", "needs", "tests", "reason",
                                                        "details")}
                        for r in d["results"]}}
    return head + json.dumps(body, indent=2) + "\n"


def safe(name: str) -> str:
    return re.sub(r"[^\w.-]", "_", name)


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[1])
    ap.add_argument("first", help="a ref or commit hash")
    ap.add_argument("second", help="a ref or commit hash")
    ap.add_argument("--one-way", action="store_true",
                    help="run only the first's answers on the second's code")
    ap.add_argument("--only", action="append", default=[],
                    help="try only this lesson, as order names it; may be given again")
    ap.add_argument("--out", type=Path,
                    help="where report.md and the .jsonc files go; crosscheck-<first>-<second>/")
    ap.add_argument("--keep", action="store_true", help="leave each worktree in place")
    args = ap.parse_args()
    home = Path(run(Path.cwd(), "git", "rev-parse", "--show-toplevel").stdout.strip())
    out = args.out or Path(f"crosscheck-{safe(args.first)}-{safe(args.second)}")
    # --out is left out of the command written to the files, since its path can name the computer's
    # user.
    shown = [a for a in (args.first, args.second) if a]
    shown += ["--one-way"] if args.one_way else []
    shown += [f"--only {lesson}" for lesson in args.only]
    command = "python3 tools/crosscheck.py " + " ".join(shown)
    date = datetime.date.today().isoformat()
    pairs = [(args.first, args.second)]
    if not args.one_way:
        pairs.append((args.second, args.first))
    runs = []
    try:
        side = {}
        for name in (args.first, args.second):
            ref = solutions_ref(home, name)
            commit = run(home, "git", "rev-parse", f"{ref}^{{commit}}").stdout.strip()
            if not commit:
                raise Stop(f"no commit for {ref}")
            side[name] = {"name": name, "solutions": ref, "commit": commit}
        for src, base in pairs:
            print(f"{src}'s answers on {base}'s code ({side[src]['solutions']} on "
                  f"{side[base]['solutions']}):", flush=True)
            results = direction(home, side[src]["commit"], side[base]["commit"], args.only,
                                args.keep)
            runs.append({"source": side[src], "base": side[base], "date": date,
                         "results": results})
    except Stop as e:
        print(f"stopped: {e}")
        return 1
    out.mkdir(parents=True, exist_ok=True)
    (out / "report.md").write_text(report(runs, command))
    for d in runs:
        name = f"{safe(d['source']['name'])}-on-{safe(d['base']['name'])}.jsonc"
        (out / name).write_text(chains(d, command))
    print(f"wrote {out}/")
    return 0


if __name__ == "__main__":
    sys.exit(main())
