"""Build what a release publishes, without publishing it.

Runs tools/build.py twice, for the student book and the mentor book, from this
branch's HEAD and the solutions ref pinned in keep.toml, into build/publish/:
guide/, review/, mentor/ and both PDFs, with every figure in place. build.py
fails if a figure is still pencilled or a page shows an image that is not there,
and so does this.

It builds HEAD, the last commit, not the files as they stand: commit first. The
figures come from MEDIA's figures/, a corbels-lfs-lessons checkout at the commit
keep.toml's [media] pins, or its branch while none is, with its LFS files pulled. It fails if MEDIA is at
another commit, or if a figure there is still an LFS pointer. It fails if a page
links a video, by a relative link, other than an MP4 in MEDIA's videos/ at
build/publish/videos/, where assemble_pages.py puts the version's videos.

It runs in a Python with requirements.txt and requirements-publish.txt.

    python tools/run.py publish_check [--media DIR] [--solutions REF] [--as KIND --version V]

MEDIA defaults to corbels-lfs-lessons beside this repository, as ftc-claude has it.
KIND and V name the version being published, and go to build.py.
"""

import argparse
import re
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


def unplayable(out: Path, media: Path) -> list[str]:
    """Every relative .mp4 link in OUT's pages that is not OUT/videos/NAME with NAME in MEDIA."""
    have = {p.name for p in (media / "videos").glob("*.mp4")}
    bad = []
    for page in sorted(out.rglob("*.html")):
        for href in re.findall(r'href="([^"]+\.mp4)"', page.read_text(encoding="utf-8")):
            if "://" in href:
                continue
            target = (page.parent / href).resolve()
            if target.parent != out / "videos" or target.name not in have:
                bad.append(f"{page.relative_to(out)}: {href}")
    return bad


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    parser.add_argument("--media", type=Path, default=None,
                        help="a corbels-lfs-lessons checkout at keep.toml's [media] pin")
    parser.add_argument("--solutions", default=answers.pins()[1],
                        help="the solutions ref; a release names its vX.Y.Z-solutions tag")
    parser.add_argument("--as", dest="kind", choices=("release", "pre", "dev"))
    parser.add_argument("--version", default="")
    args = parser.parse_args()
    version = ["--as", args.kind, "--version", args.version] if args.kind else []

    book = book_root()
    lessons = git("rev-parse", "HEAD", cwd=book)
    out = (book / OUT).resolve()
    media = (args.media or book.parent.parent / "corbels-lfs-lessons").resolve()
    ref = register.media()[1]

    at = git("rev-parse", "HEAD", cwd=media)
    want = (git_or_none("rev-parse", f"{ref}^{{commit}}", cwd=media)
            or git_or_none("rev-parse", f"origin/{ref}^{{commit}}", cwd=media))
    if at != want:
        print(f"FAILED: {media} is at {at[:7]}, not at keep.toml's [media] pin {ref} ({want})")
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
             *book_tag, *version],
            cwd=book,
        )
        if finished.returncode:
            return finished.returncode
    bad = unplayable(out, media)
    if bad:
        print("FAILED: pages link videos the media pin does not have:\n  " + "\n  ".join(bad))
        return 1
    print(f"\nBuilt both books, the review and both PDFs into {out}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
