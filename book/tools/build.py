"""Status: controlling, for the published guide and review.

Builds the guide with every figure in place, and the review of what each lesson
asks, from a lessons ref and its solutions ref, into OUT's guide/ and review/.
Moved here from gh-pages' src/ on 2026-10-11; tools/publish_check.py runs it.

    python tools/run.py build --figures DIR --solutions REF --out OUT [--lessons REF] [--mentor]

`--mentor` builds the mentor book, with Sphinx's `mentor` tag, into mentor/, and
leaves the review alone. Without it, the student book goes into guide/.
`--lessons` is HEAD unless named.

Every figure, each file Sphinx puts in a book's _images/, goes into OUT's media/,
beside the books, named NAME_HASH.SUFFIX, where HASH is the first 12 hex digits
of the file's SHA-256, and the pages link it there. conf.py's `{video}` role
links the videos by the same names; tools/publish_check.py puts them there.

It fails, and writes nothing, if a pencilled box has no picture in --figures.
It fails after writing if a built page shows an image file that is not there.

Run it in a Python that has the book's requirements. The lessons
ref is checked out into a throwaway worktree, so nothing here is touched. In
that copy, every pencilled box (`:::{admonition} fig-...` with `:class: pencil`)
whose id has a picture in --figures becomes that picture, with no caption: the
box's words only stand in for it. The lessons ref keeps its boxes; the pictures
live in corbels-lfs-lessons.
"""

import argparse
import hashlib
import os
import re
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]  # the repository: book/tools/build.py
FIGURES = Path()  # set from --figures
PENCIL = re.compile(r"^:::\{admonition\} (fig-[\w-]+)\n:class: pencil\n(.*?)\n:::$",
                    flags=re.M | re.S)


def run(*args, cwd=None):
    subprocess.run(args, cwd=cwd, check=True)


def fill(source: Path) -> tuple[int, list[str]]:
    """Every pencilled box with a picture becomes a figure. Returns filled, and ids left.
    The pictures go in _figures/, not _static/, which Sphinx copies whole beside _images/."""
    static = source / "_figures"
    static.mkdir(parents=True, exist_ok=True)
    filled, left = 0, []
    for page in sorted(source.rglob("*.md")):
        text = page.read_text(encoding="utf-8")
        depth = len(page.relative_to(source).parts) - 1

        def figure(m):
            nonlocal filled
            fig = m.group(1)
            png = FIGURES / f"{fig}.png"
            if not png.exists():
                left.append(fig)
                return m.group(0)
            shutil.copy(png, static / png.name)
            filled += 1
            path = "../" * depth + f"_figures/{png.name}"
            return f":::{{figure}} {path}\n:name: {fig}\n:::"

        new = PENCIL.sub(figure, text)
        if new != text:
            page.write_text(new, encoding="utf-8")
    return filled, left


def hashed(path: Path, sha256: str | None = None) -> str:
    """NAME_HASH.SUFFIX for a media file: HASH is the first 12 hex digits of its SHA-256."""
    if sha256 is None:
        digest = hashlib.sha256()
        with open(path, "rb") as fh:
            for block in iter(lambda: fh.read(1 << 20), b""):
                digest.update(block)
        sha256 = digest.hexdigest()
    return f"{path.stem}_{sha256[:12]}{path.suffix}"


def to_media(site: Path, media: Path) -> int:
    """Moves SITE's _images/ into MEDIA by hashed name, and points SITE's pages there.
    A page links `../` once per folder it is in, then `_images/`; that becomes `../` once
    more, then `media/`, so media/ sits beside the book. Returns the files moved."""
    images = site / "_images"
    if not images.is_dir():
        return 0
    media.mkdir(exist_ok=True)
    names = {}
    for each in sorted(images.iterdir()):
        names[each.name] = hashed(each)
        shutil.copy2(each, media / names[each.name])
    shutil.rmtree(images)
    link = re.compile(r'((?:\.\./)*)_images/([^"\'#?\s)]+)')
    for page in site.rglob("*.html"):
        text = page.read_text(encoding="utf-8")
        new = link.sub(lambda m: f"{m[1]}../media/{names[m[2]]}", text)
        if new != text:
            page.write_text(new, encoding="utf-8")
    return len(names)


def missing_images(site: Path) -> list[str]:
    """Every <img> in a built HTML site whose file does not exist, as page: src."""
    missing = []
    for page in sorted(site.rglob("*.html")):
        for src in re.findall(r'<img[^>]*\ssrc="([^"]+)"', page.read_text(encoding="utf-8")):
            if "://" in src or src.startswith("data:"):
                continue
            if not (page.parent / src.split("#")[0].split("?")[0]).exists():
                missing.append(f"{page.relative_to(site)}: {src}")
    return missing


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[1])
    ap.add_argument("--lessons", default="HEAD")
    ap.add_argument("--solutions", required=True)
    ap.add_argument("--figures", type=Path, required=True)
    ap.add_argument("--mentor", action="store_true")
    ap.add_argument("--out", type=Path, required=True)
    args = ap.parse_args()
    global FIGURES
    FIGURES = args.figures.resolve()
    dest = args.out.resolve()
    tag = ["-t", "mentor"] if args.mentor else []
    name = "mentor" if args.mentor else "guide"
    built = (name,) if args.mentor else (name, "review")

    tree = Path(tempfile.mkdtemp(prefix="book-"))
    run("git", "worktree", "add", "--detach", str(tree), args.lessons, cwd=ROOT)
    try:
        book = tree / "book"
        filled, left = fill(book / "source")
        print(f"{filled} figure(s) filled; still pencilled: {', '.join(left) or 'none'}")
        if left:
            print(f"FAILED: every pencilled box needs its picture in {FIGURES}")
            return 1

        out = tree / "out"
        # conf.py names these on B003, describing them as committed, and names the videos from
        # BOOK_MEDIA's videos/.
        os.environ.update(BOOK_LESSONS=args.lessons, BOOK_SOLUTIONS=args.solutions,
                          BOOK_MEDIA=str(FIGURES.parent))
        run(sys.executable, "-m", "sphinx", "-b", "html", *tag, "source", str(out / name),
            cwd=book)
        if not args.mentor:
            run(sys.executable, "tools/run.py", "diffs", "--solutions", args.solutions, cwd=book)
            run(sys.executable, "-m", "sphinx", "-b", "html", "review/source", str(out / "review"),
                cwd=book)

        dest.mkdir(parents=True, exist_ok=True)
        for each in built:
            print(f"{each}: {to_media(out / each, dest / 'media')} figure(s) into media/")
            shutil.rmtree(dest / each, ignore_errors=True)
            shutil.copytree(out / each, dest / each)
            shutil.rmtree(dest / each / ".doctrees", ignore_errors=True)
        lessons = subprocess.run(["git", "rev-parse", "--short", args.lessons], cwd=ROOT,
                                 capture_output=True, text=True).stdout.strip()
        solutions = subprocess.run(["git", "rev-parse", "--short", args.solutions], cwd=ROOT,
                                   capture_output=True, text=True).stdout.strip()
        print(f"built from {args.lessons} at {lessons} and {args.solutions} at {solutions}")
        missing = [f"{each}/{m}" for each in built for m in missing_images(dest / each)]
        if missing:
            print("FAILED: pages show images that are not there:\n  " + "\n  ".join(missing))
            return 1
    finally:
        run("git", "worktree", "remove", "--force", str(tree), cwd=ROOT)
    return 0


if __name__ == "__main__":
    sys.exit(main())
