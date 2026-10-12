"""Status: controlling, for what gh-pages holds after a publish.

Lays out the whole site in OUT: what the current gh-pages, OLD, holds that is
still served, with this run's build, BUILT, put in its place. A publish makes
gh-pages one commit of OUT, with no parent.

    python tools/run.py assemble_pages --old OLD --built BUILT --as KIND --version V
        --out OUT [--media DIR]

KIND is where BUILT goes: `release`, to the top level, as V, the newest release;
`pre`, into the folder V; `dev`, into dev/. From OLD it keeps the newest
release's books at the top level, dev/, videos/, and the newest pre-release's
folder while it is newer than the newest release; no older version is served.

With --media, a corbels-lfs-lessons checkout at keep.toml's [media] pin, videos/
becomes that commit's: a video whose bytes OLD already serves is copied from
there, and only a changed one is pulled from LFS. A release passes it; dev and a
pre-release keep OLD's videos, which the top-level books link. It fails if OUT
is left holding an LFS pointer in place of a file. Then pages_site.py writes the
front page and versions.json, listing every v* tag in this repository.
"""

import argparse
import hashlib
import json
import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
TOP = ("guide", "mentor", "review", "ftc31459-book.pdf", "ftc31459-mentor-book.pdf")
RELEASE = re.compile(r"v\d+\.\d+\.\d+")
PRE = re.compile(r"v\d+\.\d+\.\d+-[0-9A-Za-z.]+")
POINTER = b"version https://git-lfs.github.com/spec/"
sys.path.insert(0, str(Path(__file__).parent))
from pages_site import key  # noqa: E402


def copy(src: Path, dest: Path) -> None:
    if src.is_dir():
        shutil.copytree(src, dest)
    else:
        shutil.copy2(src, dest)


def newest_release(old: Path) -> str:
    """The release OLD serves at the top level, from its versions.json."""
    listed = json.loads((old / "versions.json").read_text())
    if "newest" in listed:
        return listed["newest"]
    # gh-pages before 2026-10-12 listed versions, newest release first.
    return next(v for v in listed["versions"] if RELEASE.fullmatch(v))


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with open(path, "rb") as fh:
        for block in iter(lambda: fh.read(1 << 20), b""):
            digest.update(block)
    return digest.hexdigest()


def videos(media: Path, old: Path, out: Path) -> None:
    """OUT's videos/ as MEDIA's, copying from OLD each one OLD already serves."""
    (out / "videos").mkdir()
    for pointer in sorted((media / "videos").glob("*")):
        text = pointer.read_bytes()
        served = old / "videos" / pointer.name
        if text.startswith(POINTER):
            oid = re.search(rb"oid sha256:([0-9a-f]{64})", text)[1].decode()
            if served.is_file() and sha256(served) == oid:
                shutil.copy2(served, out / "videos" / pointer.name)
                print(f"videos/{pointer.name}: unchanged, copied from gh-pages")
                continue
            subprocess.run(["git", "lfs", "pull", "--include", f"videos/{pointer.name}"],
                           cwd=media, check=True)
            print(f"videos/{pointer.name}: changed, pulled from LFS")
        shutil.copy2(pointer, out / "videos" / pointer.name)


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[1])
    ap.add_argument("--old", type=Path, required=True)
    ap.add_argument("--built", type=Path, required=True)
    ap.add_argument("--as", dest="kind", choices=("release", "pre", "dev"), required=True)
    ap.add_argument("--version", required=True)
    ap.add_argument("--out", type=Path, required=True)
    ap.add_argument("--media", type=Path)
    args = ap.parse_args()
    old, built, out = args.old.resolve(), args.built.resolve(), args.out.resolve()
    shutil.rmtree(out, ignore_errors=True)
    out.mkdir(parents=True)

    newest = newest_release(old)
    top = built if args.kind == "release" else old
    for name in TOP:
        copy(top / name, out / name)
    if args.kind == "release":
        newest = args.version
    pre = [d.name for d in old.iterdir() if d.is_dir() and PRE.fullmatch(d.name)]
    if args.kind == "pre":
        pre.append(args.version)
    pre = [v for v in pre if key(v) > key(newest)]
    if pre:
        latest = max(pre, key=key)
        copy(built if latest == args.version else old / latest, out / latest)
    dev = built if args.kind == "dev" else old / "dev"
    if dev.is_dir():
        copy(dev, out / "dev")
    if args.media:
        videos(args.media.resolve(), old, out)
    elif (old / "videos").is_dir():
        copy(old / "videos", out / "videos")

    pointers = [str(p.relative_to(out)) for p in out.rglob("*")
                if p.is_file() and p.stat().st_size < 200 and p.read_bytes().startswith(POINTER)]
    if pointers:
        print(f"FAILED: {len(pointers)} LFS pointer(s) in place of files, the first "
              f"{pointers[0]}")
        return 1
    tags = subprocess.run(["git", "tag", "-l", "v*"], cwd=ROOT, check=True, capture_output=True,
                          text=True).stdout.split()
    versions = [t for t in tags if (RELEASE.fullmatch(t) or PRE.fullmatch(t))
                and not t.endswith("-solutions")]
    subprocess.run([sys.executable, str(Path(__file__).with_name("pages_site.py")), str(out),
                    "--newest", newest, *versions], check=True)
    return 0


if __name__ == "__main__":
    sys.exit(main())
