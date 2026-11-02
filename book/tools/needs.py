"""Status: controlling.

Writes each lesson's list of the pages it needs finished first, under its *Before you start*, and
the mentor-only lists of the skills each lesson needs and the skills and concepts each page teaches.

    python tools/run.py needs check
    python tools/run.py needs apply

`check` runs ../tools/crosscheck.py on the solutions keep.toml names, against itself and one way,
and writes what it found to needs-code.jsonc: the earlier lessons each lesson's code needs. Then it
prints each lesson whose list would change, as it is and as it would be. It writes no page, and
takes a minute or two. Run it after a round that changes the answers or the order.

`apply` reads needs-code.jsonc, needs.toml and learned.toml, and writes each lesson's list between
SECTION_START and SECTION_END, and each lesson's and what's-happening page's skills and concepts
between SKILLS_START and SKILLS_END. It runs no crosscheck, and takes seconds.

needs.toml holds what crosscheck cannot find, kept by hand: a table per lesson, a key per page, and
why, or a list of whys, each starting `setup:`, `robot:` or `skill:`. A `setup:` or `robot:` why
is a need: the lesson will not work without that page. A list holds the whole chain: each page the
lesson needs, directly or through another. Its bullets run B pages, then H, then E, then lessons, each in number order,
each with its page's title, read from the page's heading.

A `skill:` entry names a skill a step uses and the page that teaches it. It is no need: it goes in
the lesson's *Skills needed*, and in the teaching page's *Skills learned*, with the lessons that
use it. Entries naming a skill in the same words are one skill.

learned.toml, where a release line has one, holds the rest of what a lesson or what's-happening
page teaches: `skills` no later lesson's `skill:` entry names, and `concepts`, each `area: idea`.
Where it is, each lesson has a skill learned, and each lesson and what's-happening page a concept.

tools/check_needs.py, in `ninja book`, fails a list that is not what `apply` would write, and a
page missing what learned.toml asks of it.
"""

import datetime
import json
import re
import subprocess
import sys
import tempfile
import textwrap
import tomllib
from pathlib import Path

from bookpaths import book_root
from register import load

CODE = "needs-code.jsonc"
HAND = "needs.toml"
LEARNED = "learned.toml"
TASKS = "source/tasks"
SECTION_START = ("<!-- needs: written by book/tools/needs.py; edit book/needs.toml, "
                 "then run needs.py apply -->")
SECTION_END = "<!-- /needs -->"
SKILLS_START = ("<!-- skills: written by book/tools/needs.py from book/needs.toml and "
                "book/learned.toml -->")
SKILLS_END = "<!-- /skills -->"
KINDS = ("setup:", "skill:", "robot:")
SKILL = "skill: "
TYPE_ORDER = "bherl"
PAGE = re.compile(r"[bhelr]\d{3}")
# The areas a concept is filed under, as .docs/concepts-by-lessons.md in ftc-claude gave them.
AREAS = ("programming", "Java", "Android Studio", "AdvantageScope", "`CorbelsOpMode`",
         "corbelsflightlog", "FTC programming", "robot control", "tank drive", "mecanum drive",
         "game controllers", "NetworkTables", "log files", "this project", "Pedro Pathing")


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


def by_order(page: str) -> tuple[int, str]:
    return TYPE_ORDER.index(page[0]), page


def direct(root: Path, titles: dict[str, str]
           ) -> tuple[dict[str, set[str]], dict[str, list[tuple[str, str]]]]:
    """Each lesson's direct needs, crosscheck's and needs.toml's, and the skills it needs, each a
    (teaching page, skill). Wrong on a bad entry."""
    code = read_jsonc(root / CODE)["lessons"]
    try:
        hand = tomllib.loads((root / HAND).read_text(encoding="utf-8"))
    except tomllib.TOMLDecodeError as e:
        raise Wrong(f"{HAND}: {e}")
    needs: dict[str, set[str]] = {}
    skills: dict[str, list[tuple[str, str]]] = {}
    for lesson, wanted in [*((k, v) for k, v in code.items()), *hand.items()]:
        if lesson not in titles or not lesson.startswith("l"):
            raise Wrong(f"{lesson}: no lesson page {TASKS}/{lesson}.md")
        for page in wanted:
            if page not in titles:
                raise Wrong(f"{lesson} needs {page}: no page {TASKS}/{page}.md")
            if int(page[1:]) >= int(lesson[1:]):
                raise Wrong(f"{lesson} needs {page}, which is not earlier in the book")
            whys = wanted[page] if isinstance(wanted, dict) else "code: crosscheck"
            for why in [whys] if isinstance(whys, str) else whys:
                if isinstance(wanted, dict) and not str(why).startswith(KINDS):
                    raise Wrong(f"{HAND}: {lesson}.{page}'s why does not start "
                                f"{', '.join(KINDS)}")
                if why.startswith(SKILL):
                    skills.setdefault(lesson, []).append((page, why[len(SKILL):].rstrip(".")))
                else:
                    needs.setdefault(lesson, set()).add(page)
    for lesson in skills:
        skills[lesson].sort(key=lambda s: by_order(s[0]))
    return needs, skills


def learned(root: Path, titles: dict[str, str]) -> dict[str, dict[str, list[str]]] | None:
    """learned.toml's skills and concepts by page, or None where the line has no learned.toml.
    Wrong on a bad entry."""
    path = root / LEARNED
    if not path.exists():
        return None
    try:
        table = tomllib.loads(path.read_text(encoding="utf-8"))
    except tomllib.TOMLDecodeError as e:
        raise Wrong(f"{LEARNED}: {e}")
    for page, entry in table.items():
        if page not in titles or page[0] not in "le":
            raise Wrong(f"{LEARNED}: {page}: no lesson or what's-happening page {TASKS}/{page}.md")
        if not isinstance(entry, dict) or set(entry) - {"skills", "concepts"}:
            raise Wrong(f"{LEARNED}: {page} holds only `skills` and `concepts`")
        for concept in entry.get("concepts", []):
            if concept.split(": ", 1)[0] not in AREAS or ": " not in concept:
                raise Wrong(f"{LEARNED}: {page}: {concept!r} does not start with an area and "
                            f"': ', one of {', '.join(AREAS)}")
    return table


def chain(lesson: str, needs: dict[str, set[str]]) -> list[str]:
    """Every page lesson needs, directly or through another, in the order a list shows them."""
    seen: set[str] = set()
    todo = list(needs.get(lesson, ()))
    while todo:
        page = todo.pop()
        if page not in seen:
            seen.add(page)
            todo.extend(needs.get(page, ()))
    return sorted(seen, key=by_order)


def section(pages_needed: list[str], titles: dict[str, str]) -> str:
    """The lines between the markers: none for a lesson that needs nothing."""
    if not pages_needed:
        return ""
    bullets = "".join(f"- [{p.upper()}]({p}.md), {titles[p]}\n" for p in pages_needed)
    return "Finish these first:\n\n" + bullets


def link(page: str) -> str:
    return f"[{page.upper()}]({page}.md)"


def bullet(text: str) -> str:
    """A list item wrapped at 100 columns."""
    return textwrap.fill(text, 100, initial_indent="- ", subsequent_indent="  ",
                         break_long_words=False, break_on_hyphens=False) + "\n"


def skills_section(needed: list[tuple[str, str]], taught: list[tuple[str, list[str]]],
                   concepts: list[str]) -> str:
    """The lines between the skills markers: none for a page with nothing in any list."""
    parts = []
    if needed:
        parts.append("**Skills needed.**\n\n"
                     + "".join(bullet(f"{skill}, from {link(page)}") for page, skill in needed))
    if taught:
        parts.append("**Skills learned.**\n\n" + "".join(
            bullet(f"{skill}, used in {', '.join(link(u) for u in users)}" if users else skill)
            for skill, users in taught))
    if concepts:
        parts.append("**Concepts learned.**\n\n" + "".join(bullet(c) for c in concepts))
    if not parts:
        return ""
    return "::::{only} mentor\n" + "\n".join(parts) + "::::\n"


def split(text: str, name: str, start: str = SECTION_START, end: str = SECTION_END
          ) -> tuple[str, str, str]:
    """A page's text before a section, the section, and after. Wrong without the markers."""
    if text.count(start) != 1 or text.count(end) != 1:
        raise Wrong(f"{name}: has not one each of the markers {start} and {end}")
    head, rest = text.split(start + "\n", 1)
    body, tail = rest.split(end, 1)
    return head + start + "\n", body, end + tail


def wanted(root: Path) -> dict[tuple[str, str], tuple[Path, str, str]]:
    """Each (page, which section): its path, the section as it is, and as apply writes it."""
    titles = pages(root)
    needs, skills = direct(root, titles)
    table = learned(root, titles)
    taught: dict[str, dict[str, list[str]]] = {}
    for lesson in sorted(skills, key=by_order):
        for page, skill in skills[lesson]:
            users = taught.setdefault(page, {}).setdefault(skill, [])
            if lesson not in users:
                users.append(lesson)
    out = {}
    for page in titles:
        if page[0] not in "le":
            continue
        path = root / TASKS / f"{page}.md"
        text = path.read_text(encoding="utf-8")
        if page.startswith("l"):
            _, body, _ = split(text, f"{TASKS}/{page}.md")
            out[(page, "needs")] = (path, body, section(chain(page, needs), titles))
        mine = (table or {}).get(page, {})
        for skill in mine.get("skills", []):
            if skill in taught.get(page, {}):
                raise Wrong(f"{LEARNED}: {page}: {skill!r} is in {HAND} already, as a skill "
                            f"{', '.join(taught[page][skill])} needs")
        lines = [*taught.get(page, {}).items(), *((s, []) for s in mine.get("skills", []))]
        _, body, _ = split(text, f"{TASKS}/{page}.md", SKILLS_START, SKILLS_END)
        out[(page, "skills")] = (path, body, skills_section(skills.get(page, []), lines,
                                                            mine.get("concepts", [])))
    return out


def missing(root: Path) -> list[str]:
    """Where the line has a learned.toml: each lesson with no skill learned, and each lesson or
    what's-happening page with no concept."""
    titles = pages(root)
    table = learned(root, titles)
    if table is None:
        return []
    _, skills = direct(root, titles)
    taught = {page for needed in skills.values() for page, _ in needed}
    out = []
    for page in titles:
        mine = table.get(page, {})
        if page.startswith("l") and page not in taught and not mine.get("skills"):
            out.append(f"{TASKS}/{page}.md teaches no skill: give it `skills` in {LEARNED}.")
        if page[0] in "le" and not mine.get("concepts"):
            out.append(f"{TASKS}/{page}.md teaches no concept: give it `concepts` in {LEARNED}.")
    return out


def apply(root: Path) -> int:
    for (page, which), (path, body, new) in wanted(root).items():
        if body != new:
            start, end = (SECTION_START, SECTION_END) if which == "needs" else (SKILLS_START,
                                                                                  SKILLS_END)
            head, _, tail = split(path.read_text(encoding="utf-8"), page, start, end)
            path.write_text(head + new + tail, encoding="utf-8")
            print(f"wrote {TASKS}/{page}.md, its {which}")
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
        changes = [(p, b, n) for (p, which), (_, b, n) in wanted(root).items()
                   if which == "needs" and b != n]
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
