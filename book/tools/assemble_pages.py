"""Status: controlling, for what gh-pages holds after a publish.

Lays out the whole site in OUT: what the current gh-pages, OLD, holds that is
still served, with this run's build, BUILT, put in its place. A publish makes
gh-pages one commit of OUT, with no parent.

    python tools/run.py assemble_pages --old OLD --built BUILT --as KIND --version V
        --out OUT --media DIR

KIND is where BUILT goes: `release`, to the top level, as V, the newest release;
`pre`, into the folder V; `dev`, into dev/. From OLD it keeps the newest
release's books and videos/ at the top level, dev/, and the newest pre-release's
folder while it is newer than the newest release; no older version is served.

Each version's folder holds its own videos/, which its pages link relatively.
BUILT's is the MP4s of DIR's videos/, a corbels-lfs-lessons checkout at
keep.toml's [media] pin: a video whose bytes OLD already serves, in any
version's videos/, is copied from there, and only a new one is pulled from LFS.
It fails if OUT is left holding an LFS pointer in place of a file. Then
pages_site.py writes the front page and versions.json, listing every v* tag in
this repository.
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


def served_copy(old: Path, oid: str, size: int) -> Path | None:
    """A video OLD serves, in any version's videos/, whose bytes have this SHA-256."""
    for served in sorted(old.glob("videos/*.mp4")) + sorted(old.glob("*/videos/*.mp4")):
        if served.stat().st_size == size and sha256(served) == oid:
            return served
    return None


def videos(media: Path, old: Path, dest: Path) -> None:
    """DEST/videos/ as MEDIA's MP4s, copying from OLD each one OLD already serves."""
    (dest / "videos").mkdir()
    for pointer in sorted((media / "videos").glob("*.mp4")):
        text = pointer.read_bytes()[:200]
        if text.startswith(POINTER):
            oid = re.search(rb"oid sha256:([0-9a-f]{64})", text)[1].decode()
            size = int(re.search(rb"size (\d+)", text)[1])
            served = served_copy(old, oid, size)
            if served:
                shutil.copy2(served, dest / "videos" / pointer.name)
                print(f"{served.relative_to(old)} copied from gh-pages for {pointer.name}")
                continue
            subprocess.run(["git", "lfs", "pull", "--include", f"videos/{pointer.name}"],
                           cwd=media, check=True)
            print(f"videos/{pointer.name}: new, pulled from LFS")
        shutil.copy2(pointer, dest / "videos" / pointer.name)


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[1])
    ap.add_argument("--old", type=Path, required=True)
    ap.add_argument("--built", type=Path, required=True)
    ap.add_argument("--as", dest="kind", choices=("release", "pre", "dev"), required=True)
    ap.add_argument("--version", required=True)
    ap.add_argument("--out", type=Path, required=True)
    ap.add_argument("--media", type=Path, required=True)
    args = ap.parse_args()
    old, built, out = args.old.resolve(), args.built.resolve(), args.out.resolve()
    shutil.rmtree(out, ignore_errors=True)
    out.mkdir(parents=True)

    newest = newest_release(old)
    if args.kind == "release":
        for name in TOP:
            copy(built / name, out / name)
        videos(args.media.resolve(), old, out)
        newest = args.version
    else:
        for name in (*TOP, "videos"):
            if (old / name).exists():
                copy(old / name, out / name)
    pre = [d.name for d in old.iterdir() if d.is_dir() and PRE.fullmatch(d.name)]
    if args.kind == "pre":
        pre.append(args.version)
    pre = [v for v in pre if key(v) > key(newest)]
    if pre:
        latest = max(pre, key=key)
        copy(built if latest == args.version else old / latest, out / latest)
        if latest == args.version:
            videos(args.media.resolve(), old, out / latest)
    if args.kind == "dev":
        copy(built, out / "dev")
        videos(args.media.resolve(), old, out / "dev")
    elif (old / "dev").is_dir():
        copy(old / "dev", out / "dev")

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
