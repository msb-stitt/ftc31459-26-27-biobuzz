"""Status: controlling, for gh-pages' front page, its index pages and versions.json.

Writes the pages around the books at a gh-pages root: the newest release's books
at the top level, the newest pre-release's in its own folder while it is newer, and
dev/ if there is one. book/tools/assemble_pages.py puts the books there first.

    python book/tools/pages_site.py SITE --newest VERSION [VERSION ...]

NEWEST is the release at the top level. The versions after it are every release
and pre-release, each a tag; the front page links each to its GitHub Release,
which carries its PDFs and its web pages. Moved here from gh-pages' src/site.py
on 2026-10-11, and renamed, since tools/ is put first on Python's path and a
site.py there would stand in for Python's own.
"""

import argparse
import json
import re
from pathlib import Path

RELEASES = "https://github.com/msb-stitt/ftc31459-26-27-biobuzz/releases/tag"
STYLE = "body{font:16px system-ui;margin:3rem auto;max-width:42rem;padding:0 1rem}"
TITLE = "Corbels FTC Programming Lessons in Java"
PRE = re.compile(r"v\d+\.\d+\.\d+-[0-9A-Za-z.]+")


def key(version: str) -> tuple:
    """Sorts versions oldest first: v0.1.3-rc.1 before v0.1.3, both after v0.1.2."""
    numbers, _, label = version[1:].partition("-")
    return tuple(int(n) for n in numbers.split(".")), label == "", label


def books(prefix: str) -> str:
    return f"""<ul>
  <li><a href="{prefix}guide/">The student guide</a> and
      <a href="{prefix}ftc31459-book.pdf">as a PDF</a>.</li>
  <li><a href="{prefix}mentor/">The mentor guide</a> and
      <a href="{prefix}ftc31459-mentor-book.pdf">as a PDF</a>.</li>
  <li><a href="{prefix}review/">What the student writes</a>, each lesson's copies and every
      change its patches make.</li>
</ul>
"""


def folder_page(heading: str) -> str:
    return f"""<!doctype html>
<meta charset="utf-8">
<title>{TITLE} — {heading}</title>
<style>{STYLE}</style>
<h1>{TITLE}, {heading}</h1>
{books("")}<p><a href="../">Every version</a>.</p>
"""


def front_page(newest: str, pre: str | None, dev: bool, versions: list[str]) -> str:
    page = f"""<!doctype html>
<meta charset="utf-8">
<title>{TITLE}</title>
<style>{STYLE}code{{font-size:.9em}}</style>
<h1>{TITLE}</h1>
<p>The newest release, {newest}:</p>
{books("")}"""
    if pre:
        page += f"<p>The next release, being tried as {pre}:</p>\n{books(pre + '/')}"
    if dev:
        page += ("<p>In development, built from <code>dev</code> on every push:</p>\n"
                 + books("dev/"))
    entries = "".join(f'  <li><a href="{RELEASES}/{v}">{v}</a></li>\n' for v in versions)
    page += f"""<h2>Every version</h2>
<p>Each is a release on GitHub, with both PDFs, and the newer ones with their web pages as a
   zip.</p>
<ul>
{entries}</ul>
"""
    return page


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[1])
    ap.add_argument("site", type=Path)
    ap.add_argument("--newest", required=True)
    ap.add_argument("versions", nargs="*")
    args = ap.parse_args()
    site = args.site
    versions = sorted(set(args.versions) | {args.newest}, key=key, reverse=True)
    pre = next((d.name for d in site.iterdir() if d.is_dir() and PRE.fullmatch(d.name)), None)
    dev = (site / "dev").is_dir()
    if pre:
        (site / pre / "index.html").write_text(folder_page(f"{pre}, a pre-release"),
                                               encoding="utf-8")
    if dev:
        (site / "dev" / "index.html").write_text(folder_page("in development, from dev"),
                                                 encoding="utf-8")
    (site / "versions.json").write_text(json.dumps(
        {"newest": args.newest, "pre": pre, "dev": dev, "versions": versions}, indent=2) + "\n",
        encoding="utf-8")
    (site / "index.html").write_text(front_page(args.newest, pre, dev, versions),
                                     encoding="utf-8")
    # Without it, GitHub Pages runs Jekyll, which leaves out every folder starting with _,
    # _static among them.
    (site / ".nojekyll").touch()
    print(f"newest {args.newest}; pre-release {pre or 'none'}; dev {'yes' if dev else 'no'};"
          f" {len(versions)} versions listed")


if __name__ == "__main__":
    main()
