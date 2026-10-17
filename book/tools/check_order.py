"""Check that every page is named and titled for its type and number, and that
both front pages list the pages in number order.

WRITING.md's *Every page has a type and a number* sets the rule. A page's id is
a letter, L, H, R, E or B, and three digits. Its file under source/tasks/ is the
id in lower case; the answers are the folder source/r950/, whose index carries
the title. The order is read from the files, not kept in a list by hand.
"""

import re
import sys

from bookpaths import book_root, relative, tracked

ID = re.compile(r"^(?P<letter>[lhreb])(?P<number>\d{3})$")
WORDS = {"l": "lesson: ", "h": "how-to: ", "r": "reference: ", "e": "what's happening: "}
FRONT_PAGES = ("source/index.md", "source/student_index.md")


def title(path) -> str:
    """The page's title: a Markdown `# ` line, or the line above an RST rule."""
    lines = path.read_text(encoding="utf-8").splitlines()
    if path.suffix == ".md":
        return next((line[2:].strip() for line in lines if line.startswith("# ")), "")
    for above, line in zip(lines, lines[1:]):
        if above.strip() and re.fullmatch(r"=+", line.strip()):
            return above.strip()
    return ""


def title_problem(page_id: str, text: str) -> str:
    """What is wrong with a title for this id, or an empty string."""
    letter, number = page_id[0], page_id[1:]
    if letter == "b":
        expected = f"{number} "
        if not text.startswith(expected) or len(text) == len(expected):
            return f"a body page's title is its number and its own title, as `{number} welcome`"
        return ""
    expected = f"{number} {WORDS[letter]}"
    if not text.startswith(expected) or len(text) == len(expected):
        return f"the title starts `{expected}`"
    return ""


def file_problems() -> list[str]:
    """Every problem with a page's file name or title."""
    problems = []
    for path in tracked("source/tasks/*.md", "source/tasks/*.rst", "source/r950/index.md"):
        page_id = path.parent.name if path.parent.name == "r950" else path.stem
        if not ID.match(page_id):
            problems.append(f"{relative(path)}: not named for an id, such as l020 or h017")
            continue
        found = title(path)
        problem = title_problem(page_id, found)
        if problem:
            problems.append(f"{relative(path)}: `{found}`: {problem}")
    return problems


def contents(front_page) -> list[str]:
    """The entries of every toctree on a front page, in order."""
    entries, inside = [], False
    for line in (book_root() / front_page).read_text(encoding="utf-8").splitlines():
        if line.startswith("```{toctree}"):
            inside = True
        elif inside and line.startswith("```"):
            inside = False
        elif inside and line.strip() and not line.startswith(":"):
            entries.append(line.strip())
    return entries


def order_problems(front_page) -> list[str]:
    """Each entry that is not an id, or that comes out of number order."""
    problems, last = [], -1
    for entry in contents(front_page):
        parts = entry.split("/")
        page_id = parts[0] if parts[0] == "r950" else parts[-1]
        match = ID.match(page_id)
        if not match:
            problems.append(f"{front_page}: `{entry}` is not a page id")
            continue
        number = int(match["number"])
        if number <= last:
            problems.append(f"{front_page}: `{entry}` comes after a page numbered {last:03d}")
        last = number
    return problems


def main() -> int:
    problems = file_problems()
    for front_page in FRONT_PAGES:
        problems += order_problems(front_page)
    for problem in problems:
        print(problem)
    if problems:
        print(f"\n{len(problems)} page(s) out of type, name or order.")
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
