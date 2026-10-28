"""Cross-check the pencilled figures against the register, both ways.

A figure that has not been taken yet is a box in the page carrying its id and
what makes it. FIGURES.md is the register: one row per figure, with what it
must show, how it will be obtained, and `pencilled`, `unused`, `mermaid` or the
path of the image. An unused figure keeps its row, and draw.py keeps what draws
it, so it can come back; no page shows it. A mermaid figure is a ```mermaid block
in a page, named by `:name: fig-...` on its first line, and the build draws it.

Both directions are checked, because either one alone lets the guide lie: a box
with no row is a figure nobody is going to take, and a row with no box is a
promise the pages never make. The count of pencilled figures is never written
down anywhere; ask this check for it.
"""

import re
import sys
from pathlib import Path

from bookpaths import book_root, relative, tracked

REGISTER = "FIGURES.md"

# ::: {admonition} fig-l2-sticks   /  :class: pencil
BOX = re.compile(r"^:{3,}\s*\{admonition\}\s*(?P<id>fig-[a-z0-9-]+)\s*$")
CLASS = re.compile(r"^\s*:class:\s*(?P<classes>.+?)\s*$")
# ```mermaid   /  :name: fig-l135-classes
MERMAID = re.compile(r"^`{3,}\s*mermaid\s*$")
NAME = re.compile(r"^\s*:name:\s*(?P<id>fig-[a-z0-9-]+)\s*$")
ROW = re.compile(r"^\|\s*`(?P<id>fig-[a-z0-9-]+)`\s*\|(?P<rest>.*)\|\s*$")


def boxes() -> dict[str, str]:
    """Pencilled figure id to the page it sits in."""
    found = {}
    for path in tracked("source/*.md", "source/**/*.md"):
        lines = path.read_text(encoding="utf-8").splitlines()
        for n, line in enumerate(lines):
            match = BOX.match(line)
            if not match:
                continue
            following = lines[n + 1] if n + 1 < len(lines) else ""
            classes = CLASS.match(following)
            if classes and "pencil" in classes.group("classes").split():
                found[match.group("id")] = relative(path)
    return found


def mermaids() -> tuple[dict[str, str], list[str]]:
    """Named Mermaid figure id to the page it sits in, and each unnamed block as page:line."""
    named, unnamed = {}, []
    for path in tracked("source/*.md", "source/**/*.md"):
        lines = path.read_text(encoding="utf-8").splitlines()
        for n, line in enumerate(lines):
            if not MERMAID.match(line):
                continue
            name = NAME.match(lines[n + 1]) if n + 1 < len(lines) else None
            if name:
                named[name.group("id")] = relative(path)
            else:
                unnamed.append(f"{relative(path)}:{n + 1}")
    return named, unnamed


def cells(root: Path = None) -> dict[str, list[str]]:
    """Registered figure id to its row's cells after the id: what it must show, how it is
    obtained and its status."""
    found = {}
    for line in ((root or book_root()) / REGISTER).read_text(encoding="utf-8").splitlines():
        match = ROW.match(line)
        if match:
            found[match.group("id")] = [cell.strip() for cell in match.group("rest").split("|")]
    return found


def rows() -> dict[str, str]:
    """Registered figure id to its status: `pencilled`, `unused`, `mermaid` or an image path."""
    return {figure: row[-1].strip("`") for figure, row in cells().items()}


def descriptions(root: Path = None) -> dict[str, str]:
    """Registered figure id to what it must show; conf.py puts it in the figure's box."""
    return {figure: row[0] for figure, row in cells(root).items()}


def main() -> int:
    drawn = boxes()
    named, unnamed = mermaids()
    registered = rows()
    problems = 0

    for block in unnamed:
        print(f"{block}: a Mermaid block with no `:name: fig-...` on its first line")
        problems += 1
    for figure, page in sorted(named.items()):
        if registered.get(figure) != "mermaid":
            print(f"{page}: {figure} is a Mermaid block, and {REGISTER} does not list it as mermaid")
            problems += 1

    for figure, page in sorted(drawn.items()):
        if figure not in registered:
            print(f"{page}: {figure} is pencilled in and not in {REGISTER}")
            problems += 1
        elif registered[figure] == "unused":
            print(f"{page}: {figure} is pencilled in, and {REGISTER} says it is unused")
            problems += 1
        elif registered[figure] != "pencilled":
            print(f"{page}: {figure} is pencilled in, and {REGISTER} says it exists as"
                  f" {registered[figure]}")
            problems += 1

    for figure, status in sorted(registered.items()):
        if status == "pencilled":
            if figure not in drawn:
                print(f"{REGISTER}: {figure} is pencilled and no page shows a box for it")
                problems += 1
        elif status == "mermaid":
            if figure not in named:
                print(f"{REGISTER}: {figure} is mermaid and no page has a Mermaid block named it")
                problems += 1
        elif status != "unused" and not (book_root() / status).exists():
            print(f"{REGISTER}: {figure} names {status}, which does not exist")
            problems += 1

    if problems:
        print(f"\n{problems} figure(s) out of step between the pages and {REGISTER}.")
        return 1
    statuses = list(registered.values())
    print(f"{len(drawn)} figure(s) pencilled in, "
          f"{sum(1 for s in statuses if s not in ('pencilled', 'unused', 'mermaid'))} taken, "
          f"{statuses.count('mermaid')} drawn by Mermaid, {statuses.count('unused')} unused.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
