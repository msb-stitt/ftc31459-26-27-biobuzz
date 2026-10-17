"""Generate the answer pages from what applying the patches does.

A student works in mytry: each lesson copies a file in and changes it, and the
solutions line keeps those changes as patches in solutions/, in the order a
student meets them. tools/applied.py applies them on the solutions line keep.toml
names, and this writes one page per mytry file: for each lesson that patches it,
in that order, what the file held just before the patch and what the patch put
there. The solutions line is read out of git rather than the working tree, so a
regeneration gives the same pages.

Aligning the two sides with difflib rather than parsing Java is deliberate: a
change is wherever the two sides differ, which is exactly what the student has
to write. A change made only of comments is counted and not shown.

Run it with --check to compare against what is committed instead of writing.
"""

import argparse
import difflib
import subprocess
import sys
from pathlib import Path

from applied import Patched, apply_all
from bookpaths import book_root
from register import load

LESSONS_DIR = "TeamCode/src/main/java/org/firstinspires/ftc/teamcode/lessons"
OUT = "source/r950"


def pins() -> tuple[str, str]:
    """The two refs to read, each a pinned commit or, blank, the branch beside it.

    A blank commit is the normal state: the tool then diffs the tips, which is
    what somebody running it by hand wants. A commit in there means a
    regeneration was deliberately frozen.
    """
    answers = load()["answers"]
    return (answers["lessons_commit"] or answers["lessons"],
            answers["solutions_commit"] or answers["solutions"])


def resolved(ref: str) -> str:
    """A ref with the commit it resolves to, for the console.

    Not for a generated page: a page naming HEAD's commit stops matching the
    generator the moment that page is committed, because the commit moves HEAD.
    """
    return f"{ref} ({git('rev-parse', '--short', ref).strip()})"


def git(*args: str) -> str:
    return subprocess.run(
        ["git", *args], capture_output=True, text=True, check=True,
        cwd=book_root().parent,
    ).stdout


def lesson_files(lessons: str) -> list[str]:
    listed = git("ls-tree", "-r", "--name-only", lessons, LESSONS_DIR + "/").splitlines()
    found = [path for path in listed if "TODO" in git("show", f"{lessons}:{path}")]
    if not found:
        raise SystemExit(
            f"answers: no file under {LESSONS_DIR} at {resolved(lessons)} holds a TODO"
            f" marker, out of {len(listed)} listed. A tree with no blanks is not the"
            " lessons line, and generating from it writes empty pages over real ones.")
    return found


def only_comment(block: list[str]) -> bool:
    """Is this block nothing but comment lines?

    The two lines differ in javadoc as well as in code: a lesson file says
    `Passes when:` and the solutions file does not. That is not a blank a student
    fills in, so a difference made only of comments and carrying no TODO is
    counted and not printed.
    """
    for line in block:
        stripped = line.strip()
        if not stripped:
            continue
        if not stripped.startswith(("*", "/*", "*/", "//")):
            return False
    return True


def differences(blank: list[str],
                filled: list[str]) -> list[tuple[int, int, int, int, bool]]:
    """Every place the two lines differ, and whether each one is a blank.

    A run of comment lines carrying no TODO is not a blank: the files differ in
    javadoc too, and a `Passes when:` line is not something a student writes. It
    is still a difference, and anything rebuilding one file from the other needs
    it, which is why this reports all of them and labels them.
    """
    matcher = difflib.SequenceMatcher(None, blank, filled, autojunk=False)
    out = []
    for tag, i1, i2, j1, j2 in matcher.get_opcodes():
        if tag == "equal":
            continue
        comment_only = ("TODO" not in "".join(blank[i1:i2])
                        and only_comment(blank[i1:i2]) and only_comment(filled[j1:j2]))
        out.append((i1, i2, j1, j2, not comment_only))
    return out


CONTEXT = 3  # unchanged lines shown on each side of a change


def hunks(before: list[str], after: list[str]) -> tuple[list[list[tuple]], int]:
    """The groups of changes a diff shows, and how many runs of comment lines it leaves out.

    A group whose every change is comment lines carrying no TODO is left out, as
    `differences` labels them: a `Passes when:` line is not something a student
    writes. A comment change inside a group with code in it is shown with the code.
    """
    matcher = difflib.SequenceMatcher(None, before, after, autojunk=False)
    comment_only = {(i1, i2, j1, j2) for i1, i2, j1, j2, is_blank in differences(before, after)
                    if not is_blank}
    shown, hidden = [], 0
    for group in matcher.get_grouped_opcodes(CONTEXT):
        changes = [tuple(op[1:]) for op in group if op[0] != "equal"]
        if all(change in comment_only for change in changes):
            hidden += len(changes)
        else:
            shown.append(group)
    return shown, hidden


def diff(path: str, lesson: str, before: list[str], groups: list[list[tuple]],
         after: list[str]) -> str:
    """One lesson's changes to one file, as a coloured diff numbered by the file after it.

    The class `answer-diff` sets the diff in a smaller type in the PDF, through
    source/answers.rts, so its lines fit the page.

    An unchanged line and an added one carry their line number in the file after the
    lesson; a removed line has none, since it is not in that file.
    """
    short = path.split("/teamcode/", 1)[-1]  # from mytry/, the folder the student works in
    out = ["```{code-block} diff", ":class: answer-diff", "", f"--- {short}  (before {lesson})",
           f"+++ {short}  (after {lesson})"]
    for group in groups:
        out.append(f"@@ lines {group[0][3] + 1} to {group[-1][4]} @@")
        for tag, i1, i2, j1, j2 in group:
            if tag == "equal":
                out += [f"  {k + 1:3} {after[k]}".rstrip() for k in range(j1, j2)]
                continue
            out += [f"-     {before[k]}".rstrip() for k in range(i1, i2)]
            out += [f"+ {k + 1:3} {after[k]}".rstrip() for k in range(j1, j2)]
    out.append("```")
    return "\n".join(out)


def label(lesson: str) -> str:
    """`l17a` as a reader writes it: `L17a`."""
    return "L" + lesson[1:]


def page(path: str, patches: list[Patched], solutions: str) -> str:
    name = Path(path).stem
    out = [f"# {name}", ""]
    out.append(f"What each lesson's patch does to this file, in the order the lessons come, from")
    out.append(f"applying `solutions/` on `{solutions}`. A line marked + is new, and a line marked -")
    out.append("is gone; each number is the line's place in the file after the lesson.")
    out.append("")
    for patch in patches:
        out.append(f"## {label(patch.lesson)}")
        out.append("")
        before = [line.rstrip("\n") for line in patch.before]
        after = [line.rstrip("\n") for line in patch.after]
        groups, comments = hunks(before, after)
        if groups:
            out.append(diff(path, label(patch.lesson), before, groups, after))
        else:
            out.append("Nothing but comments changes in this lesson.")
        out.append("")
        if comments:
            out.append(f"It also changes {comments} run(s) of comment lines, which are not shown.")
            out.append("")
    return "\n".join(out)


def index(names: list[str]) -> str:
    entries = "\n".join(names)
    return (
        "# 950 reference: answers\n"
        "\n"
        "What each lesson changes, file by file, out of the solutions line. Use it when you are\n"
        "stuck, not instead of being stuck.\n"
        "\n"
        "```{toctree}\n"
        ":maxdepth: 1\n"
        "\n"
        f"{entries}\n"
        "```\n"
    )


def generate(solutions: str | None = None) -> dict[str, str]:
    solutions = solutions or pins()[1]
    patched, _, _ = apply_all(solutions)
    by_file: dict[str, list[Patched]] = {}
    for patch in patched:
        by_file.setdefault(patch.path, []).append(patch)
    pages = {}
    for path, patches in by_file.items():
        pages[Path(path).stem + ".md"] = page(path, patches, solutions)
    # In the order a student first meets each file, which is the order they patch it in.
    pages["index.md"] = index([Path(path).stem for path in by_file])
    return pages


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true",
                        help="compare with what is committed instead of writing")
    _, pinned_solutions = pins()
    parser.add_argument("--solutions", default=pinned_solutions,
                        help=f"the solutions ref to read (default {pinned_solutions},"
                             " from keep.toml)")
    args = parser.parse_args()

    solutions = args.solutions
    print(f"applying solutions/ on {resolved(solutions)}")

    out = book_root() / OUT
    pages = generate(args.solutions)

    if args.check:
        problems = 0
        on_disk = {path.name for path in out.glob("*.md")} if out.exists() else set()
        for name in sorted(set(pages) | on_disk):
            if name not in pages:
                print(f"{OUT}/{name}: not generated any more, and still committed")
                problems += 1
            elif name not in on_disk:
                print(f"{OUT}/{name}: generated and missing. Run tools/answers.py.")
                problems += 1
            elif (out / name).read_text(encoding="utf-8") != pages[name]:
                print(f"{OUT}/{name}: differs from what the generator produces")
                problems += 1
        if problems:
            print(f"\n{problems} answer page(s) out of step with the patches."
                  f" Run tools/answers.py and commit what it writes.")
            return 1
        print(f"{len(pages) - 1} answer page(s) match the patches.")
        return 0

    out.mkdir(parents=True, exist_ok=True)
    # A lesson that was renamed leaves a page behind, and Sphinx fails on a page
    # in no toctree, so anything not generated any more goes.
    stale = [path for path in out.glob("*.md") if path.name not in pages]
    for path in stale:
        path.unlink()
    for name, text in pages.items():
        (out / name).write_text(text, encoding="utf-8")
    print(f"wrote {len(pages)} page(s) into {OUT}, removed {len(stale)} that moved")
    return 0


if __name__ == "__main__":
    sys.exit(main())
