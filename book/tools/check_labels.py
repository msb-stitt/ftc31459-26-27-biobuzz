"""Check that a Java block's caption names the file its code goes in.

WRITING.md's *A Java block names its file* sets the rule. A caption is
the file's path under teamcode/, `(test) ` in front for a test, such as
`(test) mytry/l135/MenuItemTest.java`. Each line of the block has to be in that
file as the page's lesson leaves it, read by applying solutions/ on the ref
keep.toml pins, as the answer pages are. A page in LABELLED has a caption on
each Java block.
"""

import re
import sys

from answers import pins
from applied import apply_all
from bookpaths import relative, tracked

LABELLED = {"l135"}
BLOCK = re.compile(r"^```\{code-block\} java\n((?::[\w-]+:[^\n]*\n)*)(.*?)^```", re.M | re.S)
CAPTION = re.compile(r"^:caption: (.*)$", re.M)
ROOTS = {"TeamCode/src/main/java/org/firstinspires/ftc/teamcode/": "",
         "TeamCode/src/test/java/org/firstinspires/ftc/teamcode/": "(test) "}


def label(path: str) -> str:
    """A patched file's caption: its path under teamcode/, a test's with `(test) ` in front."""
    for root, prefix in ROOTS.items():
        if path.startswith(root):
            return prefix + path[len(root):]
    return path


def main() -> int:
    patched, _, _ = apply_all(pins()[1])
    # Each file as each lesson leaves it, by caption.
    after = {(p.lesson, label(p.path)): {line.strip() for line in p.after} for p in patched}
    problems, captioned = [], 0
    for page in tracked("source/tasks/*.md"):
        lesson = page.stem
        for block in BLOCK.finditer(page.read_text(encoding="utf-8")):
            first = block.group(2).strip().splitlines()[0].strip()
            where = f"{relative(page)}: the Java block starting `{first}`"
            caption = CAPTION.search(block.group(1))
            if not caption:
                if lesson in LABELLED:
                    problems.append(f"{where} has no :caption: naming its file")
                continue
            captioned += 1
            lines = after.get((lesson, caption.group(1)))
            if lines is None:
                problems.append(f"{where} names {caption.group(1)}, which {lesson} does not change")
                continue
            for line in block.group(2).splitlines():
                if line.strip() and line.strip() not in lines:
                    problems.append(f"{where}: `{line.strip()}` is not in {caption.group(1)}")
    for problem in problems:
        print(problem)
    print(f"{captioned} Java block(s) captioned, {len(problems)} problem(s)")
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
