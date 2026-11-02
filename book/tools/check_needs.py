"""Check each page's generated lists are what tools/needs.py apply writes.

It reads the committed needs-code.jsonc, needs.toml and learned.toml and runs no crosscheck, so a
list edited by hand, or a needs.toml or learned.toml changed without `apply`, fails here, and a
publish never reruns crosscheck. It fails a page without its markers, which apply adds. Where the
line has a learned.toml, it fails a lesson that teaches no skill, and a lesson or what's-happening
page that teaches no concept.
"""

import sys

from bookpaths import book_root
from needs import TASKS, Wrong, missing, wanted


def main() -> int:
    root = book_root()
    try:
        lists = wanted(root)
        stale = [(page, which) for (page, which), (_, body, new) in lists.items() if body != new]
        gaps = missing(root)
    except Wrong as e:
        print(e)
        return 1
    for page, which in stale:
        if lists[(page, which)][1] is None:
            print(f"{TASKS}/{page}.md: has no {which} markers.")
        else:
            print(f"{TASKS}/{page}.md: its {which} list is not what needs.py apply writes.")
    if stale:
        print("\nRun `python tools/run.py needs apply` and commit what it writes.")
    for gap in gaps:
        print(gap)
    return 1 if stale or gaps else 0


if __name__ == "__main__":
    sys.exit(main())
