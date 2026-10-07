"""Pin the solutions the answer pages are generated from, or check the pin.

keep.toml's [answers] solutions names the solutions line that tools/answers.py,
tools/cheatsheet.py and tools/diffs.py apply, and every answer page and the
cheat sheet name it in their text. While a lesson is being worked on it names
the solutions branch, so the tools read its tip. On main it names a tag, the
branch's name, a dot and a number, so the pages stay the pages of one commit.

    python tools/run.py pin [BRANCH]           tag BRANCH's tip, pin the tag, regenerate
    python tools/run.py pin --follow [BRANCH]  pin BRANCH itself, regenerate
    python tools/run.py pin --check --on REF   what CI checks about the pin

BRANCH defaults to the branch keep.toml names, or the branch a pinned tag was
made from. Pinning is the last commit of a round of lesson work, and --follow
the first. Neither commits nor pushes: look at the diff, commit it, then push
the tag before the commit, since CI fetches the tag keep.toml names.

--check fails on main when keep.toml names anything but a tag. On a lessons
branch it fails when the pinned tag's solutions/ is not the branch tip's, which
means the patches changed after pinning. The tip moving is not enough: merging
lessons into solutions moves it without touching solutions/.
"""

import argparse
import re
import subprocess
import sys
from pathlib import Path

from bookpaths import book_root
from register import PATH, load


def git(*args: str) -> subprocess.CompletedProcess:
    return subprocess.run(["git", *args], capture_output=True, text=True,
                          cwd=book_root().parent)


def is_tag(ref: str) -> bool:
    return git("rev-parse", "--verify", "--quiet", f"refs/tags/{ref}").returncode == 0


def pinned() -> str:
    answers = load()["answers"]
    return answers["solutions_commit"] or answers["solutions"]


def branch_of(ref: str) -> str:
    """The branch a pin follows: the ref itself, or the branch a tag was made from."""
    match = re.fullmatch(r"(.+)\.(\d+)", ref)
    return match[1] if match and is_tag(ref) else ref


def local_or_origin(branch: str) -> str | None:
    for ref in (f"refs/heads/{branch}", f"refs/remotes/origin/{branch}"):
        if git("rev-parse", "--verify", "--quiet", ref).returncode == 0:
            return ref
    return None


def solutions_tree(ref: str) -> str:
    """The id of ref's solutions/, or empty when it has none."""
    found = git("rev-parse", "--verify", "--quiet", f"{ref}^{{commit}}:solutions")
    return found.stdout.strip() if found.returncode == 0 else ""


def tags_of(branch: str) -> dict[int, str]:
    """The pin tags made from a branch, by number."""
    found = {}
    for tag in git("tag", "--list", f"{branch}.*").stdout.split():
        match = re.fullmatch(re.escape(branch) + r"\.(\d+)", tag)
        if match:
            found[int(match[1])] = tag
    return found


def write_pin(ref: str) -> None:
    """Name ref in keep.toml, leaving every other line and comment as it was."""
    path = book_root() / PATH
    text = path.read_text(encoding="utf-8")
    for key, value in (("solutions", ref), ("solutions_commit", "")):
        text, count = re.subn(rf'^{key} = ".*"$', f'{key} = "{value}"', text,
                              flags=re.MULTILINE)
        if count != 1:
            sys.exit(f"pin: {PATH} has {count} lines setting {key}; expected one")
    path.write_text(text, encoding="utf-8")


def regenerate() -> None:
    run = Path(__file__).resolve().parent / "run.py"
    for tool in ("answers", "cheatsheet"):
        subprocess.run([sys.executable, str(run), tool], check=True, cwd=book_root())


def pin(branch: str) -> int:
    ref = local_or_origin(branch)
    if ref is None:
        print(f"pin: no branch {branch}, here or on origin")
        return 1
    tree = solutions_tree(ref)
    if not tree:
        print(f"pin: {branch} has no solutions/ to pin")
        return 1
    tags = tags_of(branch)
    for number in sorted(tags):
        if solutions_tree(tags[number]) == tree:
            print(f"pin: {tags[number]} already holds {branch}'s solutions/;"
                  f" keep.toml can name it")
            return 1
    tag = f"{branch}.{max(tags, default=0) + 1}"
    commit = git("rev-parse", "--short", f"{ref}^{{commit}}").stdout.strip()
    made = git("tag", "--annotate", tag, f"{ref}^{{commit}}", "--message",
               f"{branch} at {commit}, pinned for the answer pages and the cheat sheet.")
    if made.returncode != 0:
        print(f"pin: could not tag {tag}: {made.stderr.strip()}")
        return 1
    write_pin(tag)
    regenerate()
    print(f"\nTagged {tag} on {commit} and pinned it in {PATH}. Look at the diff, commit"
          f" it, then push the tag before the commit:\n    git push origin {tag}")
    return 0


def follow(branch: str) -> int:
    ref = local_or_origin(branch)
    if ref is None:
        print(f"pin: no branch {branch}, here or on origin")
        return 1
    if not solutions_tree(ref):
        print(f"pin: {branch} has no solutions/ to follow")
        return 1
    write_pin(branch)
    regenerate()
    print(f"\n{PATH} follows {branch}. Look at the diff and commit it.")
    return 0


def check(on: str) -> int:
    ref = pinned()
    tag = is_tag(ref)
    if on == "main" and not tag:
        print(f"pin: on main, {PATH} has to name a solutions tag, and names {ref}."
              f" Run `python tools/run.py pin` and commit what it writes.")
        return 1
    if on.startswith("lessons") and tag:
        branch = branch_of(ref)
        tip = local_or_origin(branch)
        if tip is None:
            print(f"pin: {ref} is pinned; no branch {branch} to compare it with")
        elif solutions_tree(ref) != solutions_tree(tip):
            print(f"pin: {ref} is stale: {branch}'s solutions/ has changed since it was"
                  f" tagged. Run `python tools/run.py pin` and commit what it writes.")
            return 1
    print(f"pin: {PATH} names {ref}, {'a tag' if tag else 'a branch'}")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("branch", nargs="?", help="the solutions branch (default: from keep.toml)")
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument("--follow", action="store_true", help="pin the branch itself")
    mode.add_argument("--check", action="store_true", help="check the pin, for CI")
    parser.add_argument("--on", help="with --check: the branch or tag being checked")
    args = parser.parse_args()
    if args.check:
        if not args.on:
            parser.error("--check needs --on")
        return check(args.on)
    branch = args.branch or branch_of(pinned())
    return follow(branch) if args.follow else pin(branch)


if __name__ == "__main__":
    sys.exit(main())
