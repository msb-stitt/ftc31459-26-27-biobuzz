"""Build what a release publishes, without publishing it.

Runs tools/build.py twice, for the student book and the mentor book, from this
branch's HEAD and the solutions ref pinned in keep.toml, into build/publish/:
guide/, review/ and mentor/, with every figure in place. build.py
fails if a figure is still pencilled or a page shows an image that is not there,
and so does this.

It builds HEAD, the last commit, not the files as they stand: commit first. The
figures come from MEDIA's figures/, a corbels-lfs-lessons checkout at the commit
keep.toml's [media] pins, or its branch while none is, with its LFS files pulled. It fails if MEDIA is at
another commit, or if a figure there is still an LFS pointer.

build/publish/ is emptied first. build.py puts the figures in build/publish/media/,
named by their hash; this copies there each video a page links, from MEDIA's
videos/, and fails if one is still an LFS pointer. Then it fails if a page links
a file in media/ that is not there, or whose name's hash is not its own.

It runs in a Python with requirements.txt.

    python tools/run.py publish_check [--media DIR] [--solutions REF]

MEDIA defaults to corbels-lfs-lessons beside this repository, as ftc-claude has it.
"""

import argparse
import hashlib
import re
import shutil
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


LINK = re.compile(r'(?:href|src)="([^"#?]*media/[^"#?]+)"')
NAMED = re.compile(r"(.+)_([0-9a-f]{12})(\.\w+)")
POINTER = b"version https://git-lfs"


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with open(path, "rb") as fh:
        for block in iter(lambda: fh.read(1 << 20), b""):
            digest.update(block)
    return digest.hexdigest()


def linked(out: Path) -> dict[str, list[str]]:
    """Each relative link from OUT's pages into a media/ folder, as its target relative to OUT,
    with the pages that link it."""
    found: dict[str, list[str]] = {}
    for page in sorted(out.rglob("*.html")):
        for href in LINK.findall(page.read_text(encoding="utf-8")):
            if "://" in href:
                continue
            target = (page.parent / href).resolve()
            where = target.relative_to(out).as_posix() if target.is_relative_to(out) else href
            found.setdefault(where, []).append(page.relative_to(out).as_posix())
    return found


def videos(out: Path, media: Path, links: dict[str, list[str]]) -> list[str]:
    """Copies each linked video from MEDIA's videos/ into OUT/media/. Returns the failures."""
    bad = []
    for target in links:
        named = NAMED.fullmatch(Path(target).name)
        if not target.startswith("media/") or not target.endswith(".mp4") or not named:
            continue
        source = media / "videos" / f"{named[1]}{named[3]}"
        if not source.exists():
            bad.append(f"{target}: no {source.name} in {media / 'videos'}")
        elif source.read_bytes()[:40].startswith(POINTER):
            bad.append(f"{target}: {source.name} is an LFS pointer; run git lfs pull in {media}")
        elif not (out / target).exists():
            shutil.copy2(source, out / target)
    return bad


def unserved(out: Path, links: dict[str, list[str]]) -> list[str]:
    """Each media link whose target is not OUT/media/NAME_HASH.SUFFIX with HASH its own."""
    bad = []
    for target, pages in links.items():
        named = NAMED.fullmatch(Path(target).name)
        path = out / target
        if not target.startswith("media/") or "/" in target[6:] or not named:
            bad.append(f"{pages[0]}: {target} is not a hashed name in media/")
        elif not path.is_file():
            bad.append(f"{pages[0]}: {target} is not there")
        elif sha256(path)[:12] != named[2]:
            bad.append(f"{pages[0]}: {target} has the SHA-256 {sha256(path)[:12]}")
    return bad


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    parser.add_argument("--media", type=Path, default=None,
                        help="a corbels-lfs-lessons checkout at keep.toml's [media] pin")
    parser.add_argument("--solutions", default=answers.pins()[1],
                        help="the solutions ref; a release names its vX.Y.Z-solutions tag")
    args = parser.parse_args()

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

    shutil.rmtree(out, ignore_errors=True)
    for book_tag in ([], ["--mentor"]):
        finished = subprocess.run(
            [sys.executable, "tools/run.py", "build", "--lessons", lessons,
             "--solutions", args.solutions, "--figures", str(figures), "--out", str(out),
             *book_tag],
            cwd=book,
        )
        if finished.returncode:
            return finished.returncode
    links = linked(out)
    bad = videos(out, media, links) or unserved(out, links)
    if bad:
        print("FAILED: pages link media that is not there as named:\n  " + "\n  ".join(bad))
        return 1
    print(f"{len(links)} media file(s) linked, each in media/ with its own hash")
    print(f"\nBuilt both books and the review into {out}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
