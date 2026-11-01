"""Check each lesson's list of what it needs first is what tools/needs.py apply writes.

It reads the committed needs-code.jsonc and needs.toml and runs no crosscheck, so a list edited by
hand, or a needs.toml changed without `apply`, fails here, and a publish never reruns crosscheck.
"""

import sys

from bookpaths import book_root
from needs import TASKS, Wrong, wanted


def main() -> int:
    try:
        stale = [page for page, (_, body, new) in wanted(book_root()).items() if body != new]
    except Wrong as e:
        print(e)
        return 1
    for page in stale:
        print(f"{TASKS}/{page}.md: its list is not what needs.py apply writes.")
    if stale:
        print("\nRun `python tools/run.py needs apply` and commit what it writes.")
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
