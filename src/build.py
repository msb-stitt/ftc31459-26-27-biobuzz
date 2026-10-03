"""Status: controlling, for this branch's guide, review and PDF.

Builds the guide with every figure in place, the review of what each lesson asks,
and a PDF of the guide, from a lessons branch and its solutions branch, into this
branch's guide/, review/ and ftc31459-book.pdf.

    python src/build.py [--lessons lessons-try] [--solutions solutions-try]

Run it from this branch's root, in a Python that has the book's requirements and
rinohtype. The lessons branch is checked out into a throwaway worktree, so
nothing there is touched. In that copy, every pencilled box
(`:::{admonition} fig-...` with `:class: pencil`) whose id has a picture in
src/figures/ becomes a figure with the box's own words as its caption. The
lessons branch keeps its boxes; the pictures live only here.

The PDF needs the route diagrams as PNG, because rinohtype places no SVG; they
are rendered with macOS's qlmanage, so the PDF step runs on a Mac only.
"""

import argparse
import re
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent
FIGURES = HERE / "figures"
PENCIL = re.compile(r"^:::\{admonition\} (fig-[\w-]+)\n:class: pencil\n(.*?)\n:::$",
                    flags=re.M | re.S)


def run(*args, cwd=None):
    subprocess.run(args, cwd=cwd, check=True)


def fill(source: Path) -> tuple[int, list[str]]:
    """Every pencilled box with a picture becomes a figure. Returns filled, and ids left."""
    static = source / "_static" / "figures"
    static.mkdir(parents=True, exist_ok=True)
    filled, left = 0, []
    for page in sorted(source.rglob("*.md")):
        text = page.read_text(encoding="utf-8")
        depth = len(page.relative_to(source).parts) - 1

        def figure(m):
            nonlocal filled
            fig, caption = m.group(1), " ".join(m.group(2).split())
            png = FIGURES / f"{fig}.png"
            if not png.exists():
                left.append(fig)
                return m.group(0)
            shutil.copy(png, static / png.name)
            filled += 1
            path = "../" * depth + f"_static/figures/{png.name}"
            return f":::{{figure}} {path}\n:name: {fig}\n\n{caption}\n:::"

        new = PENCIL.sub(figure, text)
        if new != text:
            page.write_text(new, encoding="utf-8")
    return filled, left


def svg_to_png(source: Path) -> None:
    """The route diagrams as PNG beside the SVG, and the reference page pointed at them."""
    routes = source / "_static" / "routes"
    for svg in routes.glob("*.svg"):
        run("qlmanage", "-t", "-s", "1600", "-o", str(routes), str(svg))
    page = source / "reference" / "pedro-routes.rst"
    page.write_text(re.sub(r"(_static/routes/route-[a-z]+)\.svg", r"\1.svg.png",
                           page.read_text(encoding="utf-8")), encoding="utf-8")


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[1])
    ap.add_argument("--lessons", default="lessons-try")
    ap.add_argument("--solutions", default="solutions-try")
    args = ap.parse_args()

    tree = Path(tempfile.mkdtemp(prefix="book-"))
    run("git", "worktree", "add", "--detach", str(tree), args.lessons, cwd=ROOT)
    try:
        book = tree / "book"
        filled, left = fill(book / "source")
        print(f"{filled} figure(s) filled; still pencilled: {', '.join(left) or 'none'}")

        out = tree / "out"
        run(sys.executable, "-m", "sphinx", "-b", "html", "source", str(out / "guide"), cwd=book)
        run(sys.executable, "tools/run.py", "diffs", "--solutions", args.solutions, cwd=book)
        run(sys.executable, "-m", "sphinx", "-b", "html", "review/source", str(out / "review"),
            cwd=book)
        pdf_source = tree / "pdf-source"
        shutil.copytree(book / "source", pdf_source)
        svg_to_png(pdf_source)
        run(sys.executable, "-m", "sphinx", "-b", "rinoh", "-D",
            "extensions=myst_parser,rinoh.frontend.sphinx", str(pdf_source), str(out / "pdf"))

        for name in ("guide", "review"):
            shutil.rmtree(ROOT / name, ignore_errors=True)
            shutil.copytree(out / name, ROOT / name)
        shutil.copy(next((out / "pdf").glob("*.pdf")), ROOT / "ftc31459-book.pdf")
        for name in ("guide", "review"):
            shutil.rmtree(ROOT / name / ".doctrees", ignore_errors=True)
        lessons = subprocess.run(["git", "rev-parse", "--short", args.lessons], cwd=ROOT,
                                 capture_output=True, text=True).stdout.strip()
        solutions = subprocess.run(["git", "rev-parse", "--short", args.solutions], cwd=ROOT,
                                   capture_output=True, text=True).stdout.strip()
        print(f"built from {args.lessons} at {lessons} and {args.solutions} at {solutions}")
    finally:
        run("git", "worktree", "remove", "--force", str(tree), cwd=ROOT)
    return 0


if __name__ == "__main__":
    sys.exit(main())
