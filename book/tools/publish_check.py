"""Build what a release publishes, without publishing it.

Runs gh-pages' src/build.py twice, for the student book and the mentor book,
from this branch's HEAD and the solutions ref pinned in keep.toml, into
build/publish/: guide/, review/, mentor/ and both PDFs, with every figure in
place. build.py fails if a figure is still pencilled or a page shows an image
that is not there, and so does this.

It builds HEAD, the last commit, not the files as they stand: commit first. The
figures and build.py come from gh-pages, by default as last fetched from origin.

It runs in a Python with requirements.txt and requirements-publish.txt.

    python tools/run.py publish_check [--pages origin/gh-pages] [--solutions REF]
"""

import argparse
import subprocess
import sys
import tempfile
from pathlib import Path

import answers
from bookpaths import book_root

OUT = Path("build") / "publish"


def git(*args: str, cwd: Path) -> str:
    return subprocess.run(["git", *args], cwd=cwd, check=True, capture_output=True,
                          text=True).stdout.strip()


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    parser.add_argument("--pages", default="origin/gh-pages",
                        help="the ref holding src/build.py and src/figures/")
    parser.add_argument("--solutions", default=answers.pins()[1],
                        help="the solutions ref; a release names its vX.Y.Z-solutions tag")
    args = parser.parse_args()

    book = book_root()
    lessons = git("rev-parse", "HEAD", cwd=book)
    out = (book / OUT).resolve()

    pages = Path(tempfile.mkdtemp(prefix="pages-"))
    git("worktree", "add", "--detach", str(pages), args.pages, cwd=book)
    try:
        for book_tag in ([], ["--mentor"]):
            finished = subprocess.run(
                [sys.executable, "src/build.py", "--lessons", lessons, "--solutions", args.solutions,
                 "--out", str(out), *book_tag],
                cwd=pages,
            )
            if finished.returncode:
                return finished.returncode
    finally:
        git("worktree", "remove", "--force", str(pages), cwd=book)
    print(f"\nBuilt both books, the review and both PDFs into {out}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
