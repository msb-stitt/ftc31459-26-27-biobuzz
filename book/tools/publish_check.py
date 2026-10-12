"""Build what a release publishes, without publishing it.

Runs tools/build.py twice, for the student book and the mentor book, from this
branch's HEAD and the solutions ref pinned in keep.toml, into build/publish/:
guide/, review/, mentor/ and both PDFs, with every figure in place. build.py
fails if a figure is still pencilled or a page shows an image that is not there,
and so does this.

It builds HEAD, the last commit, not the files as they stand: commit first. The
figures come from MEDIA's figures/, a corbels-lfs-lessons checkout at the ref
keep.toml's [media] names, with its LFS files pulled. It fails if MEDIA is at
another commit, or if a figure there is still an LFS pointer.

It runs in a Python with requirements.txt and requirements-publish.txt.

    python tools/run.py publish_check [--media DIR] [--solutions REF]

MEDIA defaults to corbels-lfs-lessons beside this repository, as ftc-claude has it.
"""

import argparse
import subprocess
import sys
from pathlib import Path

import answers
import register
from bookpaths import book_root

OUT = Path("build") / "publish"


def git_or_none(*args: str, cwd: Path) -> str | None:
    found = subprocess.run(["git", *args], cwd=cwd, capture_output=True, text=True)
    return found.stdout.strip() if found.returncode == 0 else None


def git(*args: str, cwd: Path) -> str:
    return subprocess.run(["git", *args], cwd=cwd, check=True, capture_output=True,
                          text=True).stdout.strip()


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    parser.add_argument("--media", type=Path, default=None,
                        help="a corbels-lfs-lessons checkout at keep.toml's [media] ref")
    parser.add_argument("--solutions", default=answers.pins()[1],
                        help="the solutions ref; a release names its vX.Y.Z-solutions tag")
    args = parser.parse_args()

    book = book_root()
    lessons = git("rev-parse", "HEAD", cwd=book)
    out = (book / OUT).resolve()
    media = (args.media or book.parent.parent / "corbels-lfs-lessons").resolve()
    ref = register.load()["media"]["ref"]

    at = git("rev-parse", "HEAD", cwd=media)
    want = (git_or_none("rev-parse", f"{ref}^{{commit}}", cwd=media)
            or git_or_none("rev-parse", f"origin/{ref}^{{commit}}", cwd=media))
    if at != want:
        print(f"FAILED: {media} is at {at[:7]}, not at keep.toml's [media] ref {ref} ({want})")
        return 1
    figures = media / "figures"
    pointers = [p.name for p in sorted(figures.glob("*.png"))
                if p.read_bytes()[:40].startswith(b"version https://git-lfs")]
    if pointers:
        print(f"FAILED: {len(pointers)} figure(s) are LFS pointers; run git lfs pull in {media}")
        return 1

    for book_tag in ([], ["--mentor"]):
        finished = subprocess.run(
            [sys.executable, "tools/run.py", "build", "--lessons", lessons,
             "--solutions", args.solutions, "--figures", str(figures), "--out", str(out),
             *book_tag],
            cwd=book,
        )
        if finished.returncode:
            return finished.returncode
    print(f"\nBuilt both books, the review and both PDFs into {out}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
