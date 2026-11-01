"""Status: controlling.

Writes each lesson's list of the pages it needs finished first, under its *Before you start*.

    python tools/run.py needs check
    python tools/run.py needs apply

`check` runs ../tools/crosscheck.py on the solutions keep.toml names, against itself and one way,
and writes what it found to needs-code.jsonc: the earlier lessons each lesson's code needs. Then it
prints each lesson whose list would change, as it is and as it would be. It writes no page, and
takes a minute or two. Run it after a round that changes the answers or the order.

`apply` reads needs-code.jsonc and needs.toml and writes each lesson's list, between the markers
SECTION_START and SECTION_END. It runs no crosscheck, and takes seconds.

needs.toml holds the needs crosscheck cannot find, kept by hand: a table per lesson, a key per page
it needs, and why, starting `setup:`, `skill:` or `robot:`. A list holds the whole chain: each
page the lesson needs, directly or through another. Its bullets run B pages, then H, then E, then
lessons, each in number order, each with its page's title, read from the page's heading.

tools/check_needs.py, in `ninja book`, fails a list that is not what `apply` would write.
"""

import datetime
import json
import re
import subprocess
import sys
import tempfile
import tomllib
from pathlib import Path

from bookpaths import book_root
from register import load

CODE = "needs-code.jsonc"
HAND = "needs.toml"
TASKS = "source/tasks"
SECTION_START = ("<!-- needs: written by book/tools/needs.py; edit book/needs.toml, "
                 "then run needs.py apply -->")
SECTION_END = "<!-- /needs -->"
KINDS = ("setup:", "skill:", "robot:")
TYPE_ORDER = "bherl"
PAGE = re.compile(r"[bhelr]\d{3}")


class Wrong(Exception):
    pass


def pages(root: Path) -> dict[str, str]:
    """Each page's id to its title, read from its `# NNN kind: title` heading."""
    found = {}
    for path in sorted((root / TASKS).glob("*.md")):
        if not PAGE.fullmatch(path.stem):
            continue
        heading = path.read_text(encoding="utf-8").splitlines()[0]
        title = re.sub(r"^# \d{3} ", "", heading)
        found[path.stem] = title.split(": ", 1)[1] if ": " in title else title
    return found


def read_jsonc(path: Path) -> dict:
    text = "".join(line for line in path.read_text(encoding="utf-8").splitlines(keepends=True)
                   if not line.lstrip().startswith("//"))
    return json.loads(text)


def direct(root: Path, titles: dict[str, str]) -> dict[str, set[str]]:
    """Each lesson's direct needs: crosscheck's, and needs.toml's. Wrong on a bad entry."""
    code = read_jsonc(root / CODE)["lessons"]
    try:
        hand = tomllib.loads((root / HAND).read_text(encoding="utf-8"))
    except tomllib.TOMLDecodeError as e:
        raise Wrong(f"{HAND}: {e}")
    needs: dict[str, set[str]] = {}
    for lesson, wanted in [*((k, v) for k, v in code.items()), *hand.items()]:
        if lesson not in titles or not lesson.startswith("l"):
            raise Wrong(f"{lesson}: no lesson page {TASKS}/{lesson}.md")
        for page in wanted:
            if page not in titles:
                raise Wrong(f"{lesson} needs {page}: no page {TASKS}/{page}.md")
            if int(page[1:]) >= int(lesson[1:]):
                raise Wrong(f"{lesson} needs {page}, which is not earlier in the book")
            if isinstance(wanted, dict) and not str(wanted[page]).startswith(KINDS):
                raise Wrong(f"{HAND}: {lesson}.{page}'s why does not start "
                            f"{', '.join(KINDS)}")
            needs.setdefault(lesson, set()).add(page)
    return needs


def chain(lesson: str, needs: dict[str, set[str]]) -> list[str]:
    """Every page lesson needs, directly or through another, in the order a list shows them."""
    seen: set[str] = set()
    todo = list(needs.get(lesson, ()))
    while todo:
        page = todo.pop()
        if page not in seen:
            seen.add(page)
            todo.extend(needs.get(page, ()))
    return sorted(seen, key=lambda p: (TYPE_ORDER.index(p[0]), p))


def section(pages_needed: list[str], titles: dict[str, str]) -> str:
    """The lines between the markers."""
    if not pages_needed:
        return "Nothing needs finishing first.\n"
    bullets = "".join(f"- [{p.upper()}]({p}.md), {titles[p]}\n" for p in pages_needed)
    return "Finish these first:\n\n" + bullets


def split(text: str, name: str) -> tuple[str, str, str]:
    """A page's text before its section, the section, and after. Wrong without the markers."""
    if text.count(SECTION_START) != 1 or text.count(SECTION_END) != 1:
        raise Wrong(f"{name}: has not one each of the markers {SECTION_START} and {SECTION_END}")
    head, rest = text.split(SECTION_START + "\n", 1)
    body, tail = rest.split(SECTION_END, 1)
    return head + SECTION_START + "\n", body, SECTION_END + tail


def wanted(root: Path) -> dict[str, tuple[Path, str, str]]:
    """Each lesson page's path, its section as it is, and as apply writes it."""
    titles = pages(root)
    needs = direct(root, titles)
    out = {}
    for page in titles:
        if not page.startswith("l"):
            continue
        path = root / TASKS / f"{page}.md"
        _, body, _ = split(path.read_text(encoding="utf-8"), f"{TASKS}/{page}.md")
        out[page] = (path, body, section(chain(page, needs), titles))
    return out


def apply(root: Path) -> int:
    for page, (path, body, new) in wanted(root).items():
        if body != new:
            head, _, tail = split(path.read_text(encoding="utf-8"), page)
            path.write_text(head + new + tail, encoding="utf-8")
            print(f"wrote {TASKS}/{page}.md")
    return 0


def check(root: Path) -> int:
    answers = load()["answers"]
    ref = answers["solutions_commit"] or answers["solutions"]
    repo = root.parent
    with tempfile.TemporaryDirectory() as out:
        ran = subprocess.run([sys.executable, "-u", "tools/crosscheck.py", ref, ref, "--one-way",
                              "--out", out], cwd=repo)
        if ran.returncode != 0:
            print(f"crosscheck failed on {ref}; {CODE} is unchanged.")
            return 1
        (found,) = Path(out).glob("*.jsonc")
        result = read_jsonc(found)
    failed = [k for k, v in result["lessons"].items() if v["result"] != "pass"]
    if failed:
        print(f"{', '.join(failed)} failed on {ref}'s own code; {CODE} is unchanged.")
        return 1
    source = result["source"]
    lines = [
        f"// Written by book/tools/needs.py check on {datetime.date.today()}, from "
        f"tools/crosscheck.py {ref} {ref} --one-way.",
        "// solutions, commit: the solutions ref crosscheck ran on, and its commit.",
        "// lessons: each lesson, in the solutions' order, and the earlier lessons its code needs,",
        "//   directly or through another, as crosscheck found them from build, test and patch",
        "//   errors. Needs that are not code are in needs.toml.",
    ]
    data = {"solutions": source["solutions"], "commit": source["commit"],
            "lessons": {k: v["needs"] for k, v in result["lessons"].items()}}
    old = (root / CODE).read_text(encoding="utf-8") if (root / CODE).exists() else None
    (root / CODE).write_text("\n".join(lines) + "\n" + json.dumps(data, indent=2) + "\n",
                             encoding="utf-8")
    print(f"wrote {CODE}, from {source['solutions']} at {source['commit'][:8]}")
    try:
        changes = [(p, b, n) for p, (_, b, n) in wanted(root).items() if b != n]
    except Wrong as e:
        print(e)
        return 1
    for page, body, new in changes:
        print(f"\n{page}, as it is:\n{body}{page}, as apply would write it:\n{new}")
    print(f"\n{len(changes)} lesson(s) would change." if changes else "\nNo lesson would change.")
    if old is None:
        print(f"{CODE} is new.")
    return 0


def main() -> int:
    if sys.argv[1:] not in (["check"], ["apply"]):
        print("usage: python tools/run.py needs check|apply")
        return 2
    root = book_root()
    try:
        return check(root) if sys.argv[1] == "check" else apply(root)
    except Wrong as e:
        print(e)
        return 1


if __name__ == "__main__":
    sys.exit(main())
