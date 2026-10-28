"""Status: controlling, for ftc-lessons-java/media/media-index.jsonc on the branch site.

The index lists the versions coding.stitt.cc supports, the media each one's pages link, and
every file in R2 under ftc-lessons-java/media/, with the versions that use it. A file no
version uses is listed as removable. assemble.py writes it on each run; it is JSONC so a person
may add a comment and it still parses, and the next run writes it again without one.

    python cloudflare/media_index.py --site SITE [--bytes]

checks the index in SITE, a checkout of the branch site, against SITE's pages and R2:
- each file it lists is in R2, with its bytes, and its name's hash is its SHA-256's first 12
  hex digits; with --bytes, each is downloaded and its SHA-256 read;
- each version's pages link exactly the media it lists for that version;
- each file in R2 is listed, and is used by a version or listed as removable;
- each version it lists has its pages in SITE, and each version in SITE is listed.
"""

import argparse
import hashlib
import json
import re
import sys
import urllib.request
from pathlib import Path

import r2

LESSONS = "ftc-lessons-java"
PREFIX = f"{LESSONS}/media/"
INDEX = Path(LESSONS) / "media" / "media-index.jsonc"
NAMED = re.compile(r"(.+)_([0-9a-f]{12})(\.\w+)")
# data= is how a page shows a Mermaid diagram's SVG file, in an <object>.
LINK = re.compile(r'(?:href|src|data)="([^"#?]*media/[^"#?]+)"')
SERVED = "https://coding-stitt-cc.pages.dev/"


def load(text: str) -> dict:
    """JSONC: JSON, with // and /* */ comments outside strings taken out first."""
    out, i, quoted = [], 0, False
    while i < len(text):
        c = text[i]
        if quoted:
            out.append(c)
            if c == "\\":
                out.append(text[i + 1])
                i += 1
            elif c == '"':
                quoted = False
        elif c == '"':
            quoted = True
            out.append(c)
        elif text.startswith("//", i):
            i = text.find("\n", i) - 1 if "\n" in text[i:] else len(text)
        elif text.startswith("/*", i):
            i = text.index("*/", i) + 1
        else:
            out.append(c)
        i += 1
    return json.loads("".join(out))


def read(site: Path) -> dict:
    path = site / INDEX
    return load(path.read_text(encoding="utf-8")) if path.exists() else {}


def versions(site: Path) -> list[str]:
    folder = site / LESSONS / "ver"
    return sorted(d.name for d in folder.iterdir() if d.is_dir()) if folder.is_dir() else []


def linked(site: Path, version: str) -> list[str]:
    """The media names a version's pages link, each by a relative link into its media/."""
    root = (site / LESSONS / "ver" / version).resolve()
    names = set()
    for page in root.rglob("*.html"):
        for href in LINK.findall(page.read_text(encoding="utf-8")):
            if "://" not in href and (page.parent / href).resolve().parent == root / "media":
                names.add(Path(href).name)
    return sorted(names)


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with open(path, "rb") as fh:
        for block in iter(lambda: fh.read(1 << 20), b""):
            digest.update(block)
    return digest.hexdigest()


def make(site: Path, in_r2: dict[str, int], hashes: dict[str, str],
         built_from: dict[str, str]) -> dict:
    """The index for SITE's versions and the files IN_R2, as name: bytes. HASHES and BUILT_FROM
    give the SHA-256 of a name and the commit of a version this run knows; the rest are taken
    from the index SITE holds now."""
    before = read(site)
    known = {name: entry["sha256"] for name, entry in before.get("media", {}).items()}
    known.update(hashes)
    made = {v: before.get("versions_supported", {}).get(v, {}).get("built_from_commit", "")
            for v in versions(site)}
    made.update({v: c for v, c in built_from.items() if v in made})
    uses = {v: linked(site, v) for v in made}
    media = {name: {"sha256": known.get(name, ""), "bytes": size,
                    "used_by_versions": [v for v in made if name in uses[v]]}
             for name, size in sorted(in_r2.items())}
    return {
        "site": f"https://coding.stitt.cc/{LESSONS}/",
        "r2_bucket": r2.BUCKET,
        "r2_key_prefix": PREFIX,
        "media_name_format": "NAME_HASH.SUFFIX, where HASH is the first 12 hex digits of the "
                             "file's SHA-256",
        "versions_supported": {v: {"pages": f"{LESSONS}/ver/{v}/", "built_from_commit": c,
                                   "media_used": uses[v]} for v, c in made.items()},
        "media": media,
        "media_used_by_no_supported_version": [n for n, e in media.items()
                                               if not e["used_by_versions"]],
    }


def write(site: Path, index: dict) -> None:
    path = site / INDEX
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(index, indent=2) + "\n", encoding="utf-8")


def check(site: Path, in_r2: dict[str, int], fetch=None) -> list[str]:
    """What is wrong with SITE's index, against SITE's pages and IN_R2, as name: bytes. FETCH,
    if given, reads a name's bytes from where it is served, so its SHA-256 can be read."""
    index = read(site)
    if not index:
        return [f"no {INDEX} in {site}"]
    bad = []
    listed = index.get("media", {})
    for name, entry in listed.items():
        named = NAMED.fullmatch(name)
        if name not in in_r2:
            bad.append(f"{name}: listed, and not in R2")
        elif in_r2[name] != entry["bytes"]:
            bad.append(f"{name}: listed as {entry['bytes']} bytes, and R2 holds {in_r2[name]}")
        if not named or not entry["sha256"].startswith(named[2]):
            bad.append(f"{name}: its name's hash is not its SHA-256's, {entry['sha256'][:12]}")
        elif fetch and name in in_r2:
            got = hashlib.sha256(fetch(name)).hexdigest()
            if got != entry["sha256"]:
                bad.append(f"{name}: served with the SHA-256 {got[:12]}")
    supported = index.get("versions_supported", {})
    for version, entry in supported.items():
        if not (site / entry["pages"] / "guide" / "index.html").is_file():
            bad.append(f"{version}: listed, and its pages are not in {site / entry['pages']}")
        elif linked(site, version) != entry["media_used"]:
            bad.append(f"{version}: its pages link {len(linked(site, version))} media, and it "
                       f"lists {len(entry['media_used'])}, not the same")
    for version in versions(site):
        if version not in supported:
            bad.append(f"{version}: its pages are in {site}, and it is not listed")
    removable = set(index.get("media_used_by_no_supported_version", []))
    for name in in_r2:
        users = [v for v, e in supported.items() if name in e["media_used"]]
        if name not in listed:
            bad.append(f"{name}: in R2, and not listed")
        elif not users and name not in removable:
            bad.append(f"{name}: in R2, used by no version, and not listed as removable")
        elif users and name in removable:
            bad.append(f"{name}: listed as removable, and used by {', '.join(users)}")
    return bad


def served(name: str) -> bytes:
    # Cloudflare answers 403 to Python's own User-Agent.
    request = urllib.request.Request(f"{SERVED}{PREFIX}{name}",
                                     headers={"User-Agent": "ftc31459-media-index"})
    with urllib.request.urlopen(request) as answer:
        return answer.read()


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    ap.add_argument("--site", type=Path, required=True)
    ap.add_argument("--bytes", action="store_true", help="download each file and hash it")
    args = ap.parse_args()
    in_r2 = {k[len(PREFIX):]: n for k, n in r2.objects(PREFIX).items()}
    bad = check(args.site, in_r2, served if args.bytes else None)
    if bad:
        print("FAILED: the media index does not match:\n  " + "\n  ".join(bad))
        return 1
    index = read(args.site)
    print(f"the media index matches: {len(index['versions_supported'])} version(s), "
          f"{len(index['media'])} file(s) in R2, "
          f"{len(index['media_used_by_no_supported_version'])} removable")
    return 0


if __name__ == "__main__":
    sys.exit(main())
