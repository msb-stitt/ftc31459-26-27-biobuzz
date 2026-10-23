"""Status: controlling, for what coding.stitt.cc serves.

Puts one version's books into SITE, a checkout of the branch site, writes what lies around
them, and uploads to R2 each media file the version links that R2 does not hold.

    python cloudflare/assemble.py --site SITE --built BUILT --version V --commit SHA [--offline]

BUILT is book/build/publish/ as tools/publish_check.py leaves it: guide/, mentor/, review/ and
media/. V is a tag, vX.Y.Z or vX.Y.Z-rc.N, or dev; SHA is the commit it was built from. In SITE
it writes:
- ftc-lessons-java/ver/V/, BUILT's three books without media/, in place of what was there;
- ftc-lessons-java/versions.html, each version in ver/, newest first and dev last;
- _redirects: /ftc-lessons-java/, and guide/, mentor/ and review/ with each page under them,
  302 to the newest release, or the newest pre-release while there is none, or dev while there
  is neither; and ftc-lessons-java/ver/<v>/media/<name> 301 to ftc-lessons-java/media/<name>;
- _routes.json, sending ftc-lessons-java/media/ to the Function, but for the index;
- index.html, the welcome page, from index.html here;
- ftc-lessons-java/media/media-index.jsonc, by media_index.py.

With --offline it uploads nothing and reads no R2 listing: R2 is taken to hold what the index
in SITE lists, and BUILT's media/.
"""

import argparse
import html
import json
import re
import shutil
import sys
from pathlib import Path

import media_index
import r2

HERE = Path(__file__).resolve().parent
LESSONS = media_index.LESSONS
BOOKS = ("guide", "mentor", "review")
REPO = "https://github.com/msb-stitt/ftc31459-26-27-biobuzz"
TITLE = "Corbels FTC Programming Lessons in Java"
TAG = re.compile(r"v(\d+)\.(\d+)\.(\d+)(?:-(.+))?")


def key(version: str) -> tuple:
    """Sorts tags oldest first: v0.1.3-rc.1 before v0.1.3, both after v0.1.2."""
    tag = TAG.fullmatch(version)
    return (*(int(n) for n in tag.groups()[:3]), tag[4] is None, tag[4] or "")


def newest(versions: list[str]) -> str:
    """The newest release; or the newest pre-release while there is none; or dev."""
    tags = sorted((v for v in versions if TAG.fullmatch(v)), key=key)
    releases = [v for v in tags if "-" not in v]
    return (releases or tags or ["dev"])[-1]


def redirects(landing: str) -> str:
    to = f"/{LESSONS}/ver/{landing}"
    lines = [f"/{LESSONS}/ver/:version/media/:name /{LESSONS}/media/:name 301"]
    for book in BOOKS:
        lines += [f"/{LESSONS}/{book} {to}/{book}/ 302",
                  f"/{LESSONS}/{book}/* {to}/{book}/:splat 302"]
    lines += [f"/{LESSONS} {to}/guide/ 302", f"/{LESSONS}/ {to}/guide/ 302"]
    return "\n".join(lines) + "\n"


def routes() -> str:
    return json.dumps({"version": 1, "include": [f"/{LESSONS}/media/*"],
                       "exclude": [f"/{media_index.INDEX.as_posix()}"]}, indent=2) + "\n"


def versions_page(versions: list[str], built_from: dict[str, str], landing: str) -> str:
    items = []
    tags = sorted((v for v in versions if TAG.fullmatch(v)), key=key, reverse=True)
    for v in tags + sorted(v for v in versions if not TAG.fullmatch(v)):
        books = (f'<a href="ver/{v}/guide/">the guide</a>, '
                 f'<a href="ver/{v}/mentor/">the mentor guide</a> and '
                 f'<a href="ver/{v}/review/">what the student writes</a>')
        if TAG.fullmatch(v):
            source = (f'its <a href="{REPO}/releases/tag/{v}">GitHub Release</a> and '
                      f'<a href="{REPO}/tree/{v}">code</a>')
            name = f"<b>{v}</b>" + (", the most recent release" if v == landing else "")
        else:
            commit = built_from.get(v, "")
            source = f'its <a href="{REPO}/tree/{commit}">code</a>, at {commit[:7]}'
            name = f"<b>{html.escape(v)}</b>, in development"
        items.append(f"  <li>{name}: {books}; {source}.</li>")
    return f"""<!doctype html>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>{TITLE}: versions</title>
<style>body{{font:16px system-ui;margin:3rem auto;max-width:42rem;padding:0 1rem}}
li{{margin:.5rem 0}}</style>
<h1>{TITLE}</h1>
<p>The versions here, newest first.</p>
<ul>
{chr(10).join(items)}
</ul>
"""


def place(site: Path, built: Path, version: str) -> None:
    dest = site / LESSONS / "ver" / version
    shutil.rmtree(dest, ignore_errors=True)
    for book in BOOKS:
        shutil.copytree(built / book, dest / book)


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    ap.add_argument("--site", type=Path, required=True)
    ap.add_argument("--built", type=Path, required=True)
    ap.add_argument("--version", required=True)
    ap.add_argument("--commit", required=True)
    ap.add_argument("--offline", action="store_true")
    args = ap.parse_args()
    site, built = args.site.resolve(), args.built.resolve()
    if not (TAG.fullmatch(args.version) or args.version == "dev"):
        print(f"FAILED: {args.version} is neither a tag vX.Y.Z or vX.Y.Z-rc.N nor dev")
        return 1

    place(site, built, args.version)
    media = {p.name: p for p in sorted((built / "media").iterdir())}
    if args.offline:
        in_r2 = {n: e["bytes"] for n, e in media_index.read(site).get("media", {}).items()}
    else:
        in_r2 = {k[len(media_index.PREFIX):]: n
                 for k, n in r2.objects(media_index.PREFIX).items()}
    new = [name for name in media if name not in in_r2]
    for name in new:
        if not args.offline:
            r2.put(media[name], media_index.PREFIX + name)
        in_r2[name] = media[name].stat().st_size
    print(f"{len(media)} media file(s) linked by {args.version}; {len(new)} new to R2"
          + (", not uploaded: offline" if args.offline else ", uploaded"))

    hashes = {name: media_index.sha256(path) for name, path in media.items()}
    index = media_index.make(site, in_r2, hashes, {args.version: args.commit})
    media_index.write(site, index)
    versions = list(index["versions_supported"])
    built_from = {v: e["built_from_commit"] for v, e in index["versions_supported"].items()}
    landing = newest(versions)
    (site / LESSONS / "versions.html").write_text(versions_page(versions, built_from, landing),
                                                  encoding="utf-8")
    (site / "_redirects").write_text(redirects(landing), encoding="utf-8")
    (site / "_routes.json").write_text(routes(), encoding="utf-8")
    shutil.copy(HERE / "index.html", site / "index.html")
    print(f"versions {', '.join(versions)}; landing {landing}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
