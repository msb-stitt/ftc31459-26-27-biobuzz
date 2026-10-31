"""Check that a Java block's caption names the file its code goes in.

WRITING.md's *A Java block names its file* sets the rule. Each Java block on a
task page is a `{code-block} java` with a caption, the file's path under
teamcode/, `(test) ` in front for a test, such as
`(test) mytry/l093/MenuItemTest.java`. A code example that is in no file has
`:class: example` in place of a caption, and is not read.

Each line of a captioned block has to be in that file as the page leaves it:
for a file a lesson patches, as the last lesson up to the page's number leaves
it, read by applying solutions/ on the ref keep.toml pins, as the answer pages
are; for any other file, as it is in this checkout. A caption may hold a `*`,
as `pedro/Bot*.java`, for the robot's own file; then each line has to be in one
file it matches. A line a step types and a later step on the page changes is
read at that step: a highlighted line that is not in the file passes when the
page later quotes it, as `Delete the two lines`, or shows the same place again
in a later block with the same caption.
"""

import fnmatch
import re
import sys

from answers import pins
from applied import apply_all
from bookpaths import book_root, relative, tracked

BLOCK = re.compile(r"^```(\{code-block\} java|java)\n((?::[\w-]+:[^\n]*\n)*)(.*?)^```",
                   re.M | re.S)
CAPTION = re.compile(r"^:caption: (.*)$", re.M)
EXAMPLE = re.compile(r"^:class: example$", re.M)
EMPHASIZE = re.compile(r"^:emphasize-lines: (.*)$", re.M)
ROOTS = {"TeamCode/src/main/java/org/firstinspires/ftc/teamcode/": "",
         "TeamCode/src/test/java/org/firstinspires/ftc/teamcode/": "(test) "}


def label(path: str) -> str:
    """A patched file's caption: its path under teamcode/, a test's with `(test) ` in front."""
    for root, prefix in ROOTS.items():
        if path.startswith(root):
            return prefix + path[len(root):]
    return path


def number(name: str) -> int:
    """A page's or a lesson's number: 67 for e067, 65 for l065."""
    return int(re.sub(r"\D", "", name) or 0)


def checkout() -> dict[str, set[str]]:
    """Each Java file in this checkout, by caption, as the set of its stripped lines."""
    files = {}
    for root, prefix in ROOTS.items():
        base = book_root().parent / root
        for path in base.rglob("*.java"):
            text = path.read_text(encoding="utf-8").splitlines()
            files[prefix + path.relative_to(base).as_posix()] = {line.strip() for line in text}
    return files


def highlighted(options: str, count: int) -> set[int]:
    """The block's highlighted line numbers, counting from 1, out of `:emphasize-lines:`."""
    found = EMPHASIZE.search(options)
    if not found:
        return set()
    out = set()
    for part in found.group(1).split(","):
        low, _, high = part.strip().partition("-")
        out.update(range(int(low), int(high or low) + 1))
    return {n for n in out if n <= count}


def main() -> int:
    patched, _, _ = apply_all(pins()[1])
    # Each patched file as each lesson leaves it, in the order the lessons come.
    states: dict[str, list[tuple[int, set[str]]]] = {}
    for p in patched:
        states.setdefault(label(p.path), []).append(
            (number(p.lesson), {line.strip() for line in p.after}))
    others = checkout()

    def file_at(caption: str, page: int) -> list[set[str]] | None:
        """The file a caption names, as the page leaves it; a `*` gives each file it matches."""
        if caption in states:
            upto = [lines for n, lines in states[caption] if n <= page]
            return [upto[-1]] if upto else None
        matched = [lines for name, lines in others.items() if fnmatch.fnmatch(name, caption)]
        return matched or None

    problems, captioned = [], 0
    for page in tracked("source/tasks/*.md"):
        text = page.read_text(encoding="utf-8")
        blocks = list(BLOCK.finditer(text))
        for i, block in enumerate(blocks):
            body = block.group(3).splitlines()
            if body and not body[0].strip():
                body = body[1:]  # the blank line after the options, which Sphinx skips
            first = next((line.strip() for line in body if line.strip()), "")
            where = f"{relative(page)}: the Java block starting `{first}`"
            options = block.group(2)
            if EXAMPLE.search(options):
                continue
            caption = CAPTION.search(options)
            if block.group(1) == "java" or not caption:
                problems.append(f"{where} has no :caption: naming its file, "
                                "nor :class: example")
                continue
            captioned += 1
            files = file_at(caption.group(1), number(page.stem))
            if files is None:
                problems.append(f"{where} names {caption.group(1)}, which is no file here")
                continue
            later_text = text[block.end():]
            later_blocks = [{line.strip() for line in b.group(3).splitlines() if line.strip()}
                            for b in blocks[i + 1:]
                            if (c := CAPTION.search(b.group(2))) and c.group(1) == caption.group(1)]
            typed = highlighted(options, len(body))
            code_typed = {line.strip() for n, line in enumerate(body, start=1)
                          if n in typed and line.strip() and not line.strip().startswith("//")}
            shown_again = any(later & code_typed for later in later_blocks)
            for n, line in enumerate(body, start=1):
                line = line.strip()
                if not line or any(line in lines for lines in files):
                    continue
                changed_later = f"`{line}`" in later_text or shown_again
                if n in typed and changed_later:
                    continue
                problems.append(f"{where}: `{line}` is not in {caption.group(1)}")
    for problem in problems:
        print(problem)
    print(f"{captioned} Java block(s) captioned, {len(problems)} problem(s)")
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
