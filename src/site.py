"""Status: controlling, for this branch's front page, versions.json and top-level books.

Lays out the site from the version folders at this branch's root: releases such as
v0.1.0, pre-releases such as v0.1.1-rc.1, each holding guide/, mentor/, review/ and
both PDFs, and dev/ if there is one.

    python src/site.py

The newest release's books are copied to the top level, where the links in the
code point; a pre-release's never are. Each version folder gets an index.html,
and the front page and versions.json name every version. Nothing is listed by
hand: the folders are the list.
"""

import json
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
BOOKS = ("guide", "mentor", "review")
PDFS = ("ftc31459-book.pdf", "ftc31459-mentor-book.pdf")
STYLE = "body{font:16px system-ui;margin:3rem auto;max-width:42rem;padding:0 1rem}"


def folders(pattern: str) -> list[str]:
    """The version folders whose names match, newest first."""
    found = [d.name for d in ROOT.iterdir() if d.is_dir() and re.fullmatch(pattern, d.name)]

    def key(name):
        numbers, _, label = name[1:].partition("-")
        return tuple(int(n) for n in numbers.split(".")), label

    return sorted(found, key=key, reverse=True)


def releases() -> list[str]:
    return folders(r"v\d+\.\d+\.\d+")


def prereleases() -> list[str]:
    return folders(r"v\d+\.\d+\.\d+-[0-9A-Za-z.]+")


def version_page(version: str, heading: str) -> str:
    return f"""<!doctype html>
<meta charset="utf-8">
<title>ftc31459 26-27 BIOBUZZ — {version}</title>
<style>{STYLE}</style>
<h1>The BIOBUZZ lessons, {heading}</h1>
<ul>
  <li><a href="guide/">The student guide</a> and <a href="ftc31459-book.pdf">as a PDF</a>.</li>
  <li><a href="mentor/">The mentor guide</a> and <a href="ftc31459-mentor-book.pdf">as a PDF</a>.</li>
  <li><a href="review/">What the student writes</a>.</li>
  <li><a href="../">Every release</a>.</li>
</ul>
"""


def entry(version: str) -> str:
    return f"""  <li><a href="{version}/">{version}</a>:
      <a href="{version}/guide/">student guide</a>,
      <a href="{version}/ftc31459-book.pdf">PDF</a>;
      <a href="{version}/mentor/">mentor guide</a>,
      <a href="{version}/ftc31459-mentor-book.pdf">PDF</a>;
      <a href="{version}/review/">what the student writes</a>.</li>
"""


def front_page(versions: list[str], pre: list[str], dev: bool) -> str:
    page = f"""<!doctype html>
<meta charset="utf-8">
<title>ftc31459 26-27 BIOBUZZ — the book</title>
<style>{STYLE}code{{font-size:.9em}}</style>
<h1>The BIOBUZZ lessons</h1>
<p>The newest release, {versions[0]}:</p>
<ul>
  <li><a href="guide/">The student guide</a> and <a href="ftc31459-book.pdf">as a PDF</a>.</li>
  <li><a href="mentor/">The mentor guide</a> and <a href="ftc31459-mentor-book.pdf">as a PDF</a>.</li>
  <li><a href="review/">What the student writes</a>, each lesson's copies and every change its
      patches make.</li>
</ul>
<h2>Every release</h2>
<ul>
{"".join(entry(v) for v in versions)}</ul>
"""
    if pre:
        page += f"""<h2>Pre-releases</h2>
<ul>
{"".join(entry(v) for v in pre)}</ul>
"""
    if dev:
        page += """<h2>In development</h2>
<ul>
  <li><a href="dev/">dev</a>, built from <code>main</code> on every push.</li>
</ul>
"""
    return page


def main() -> None:
    versions = releases()
    pre = prereleases()
    dev = (ROOT / "dev").is_dir()
    newest = ROOT / versions[0]
    for book in BOOKS:
        shutil.rmtree(ROOT / book, ignore_errors=True)
        shutil.copytree(newest / book, ROOT / book)
    for pdf in PDFS:
        shutil.copy(newest / pdf, ROOT / pdf)
    for version in versions + pre:
        (ROOT / version / "index.html").write_text(version_page(version, version),
                                                   encoding="utf-8")
    if dev:
        (ROOT / "dev" / "index.html").write_text(
            version_page("dev", "in development, from main"), encoding="utf-8")
    listed = versions + pre + (["dev"] if dev else [])
    (ROOT / "versions.json").write_text(json.dumps({"versions": listed}, indent=2) + "\n",
                                        encoding="utf-8")
    (ROOT / "index.html").write_text(front_page(versions, pre, dev), encoding="utf-8")
    print(f"newest {versions[0]}; listed {', '.join(listed)}")


if __name__ == "__main__":
    main()
