"""Status: controlling, for what gh-pages holds after a release.

Lays out the whole published site in OUT from corbels-lfs-lessons: each release
tag's published/ as that version's folder, the media ref's videos/ as videos/,
and dev/ carried over from the current gh-pages; then pages_site.py writes the top
level, the front page and versions.json. A release makes gh-pages one commit of
OUT, with no parent.

    python tools/run.py assemble_pages --media DIR --out OUT [--keep DIR]

MEDIA is a corbels-lfs-lessons checkout at keep.toml's [media] ref, with its tags
fetched. Each tag is checked out into a throwaway worktree, and its published/
pulled from LFS there; LFS objects already in MEDIA's .git/lfs, as from the
Actions cache, are not downloaded again. KEEP is the current gh-pages, whose
dev/ is copied. It fails if OUT is left holding an LFS pointer in place of a
file.
"""

import argparse
import os
import re
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

SKIP = {**os.environ, "GIT_LFS_SKIP_SMUDGE": "1"}
VERSION = re.compile(r"v\d+\.\d+\.\d+(-rc\.\d+)?")
POINTER = b"version https://git-lfs.github.com/spec/"


def git(*args: str, cwd: Path) -> str:
    return subprocess.run(["git", *args], cwd=cwd, check=True, capture_output=True, text=True,
                          env=SKIP).stdout.strip()


def pulled(media: Path, ref: str, folder: str) -> Path:
    """REF's FOLDER, with its LFS files, in a throwaway worktree; the caller removes it."""
    tree = Path(tempfile.mkdtemp(prefix="pages-"))
    git("worktree", "add", "--detach", str(tree), ref, cwd=media)
    subprocess.run(["git", "lfs", "pull", "--include", f"{folder}/"], cwd=tree, check=True)
    return tree


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[1])
    ap.add_argument("--media", type=Path, required=True)
    ap.add_argument("--out", type=Path, required=True)
    ap.add_argument("--keep", type=Path, default=None)
    args = ap.parse_args()
    media, out = args.media.resolve(), args.out.resolve()
    shutil.rmtree(out, ignore_errors=True)
    out.mkdir(parents=True)

    store = Path(git("rev-parse", "--git-common-dir", cwd=media))
    store = (store if store.is_absolute() else media / store) / "lfs" / "objects"

    def stored() -> int:
        return sum(1 for p in store.rglob("*") if p.is_file()) if store.is_dir() else 0

    before = stored()
    tags = [t for t in git("tag", "-l", "v*", cwd=media).split() if VERSION.fullmatch(t)]
    for ref, folder, dest in [(t, "published", t) for t in tags] + [("HEAD", "videos", "videos")]:
        tree = pulled(media, ref, folder)
        try:
            if (tree / folder).is_dir():
                shutil.copytree(tree / folder, out / dest)
                print(f"{ref}: {folder}/ into {dest}/")
            else:
                print(f"{ref}: no {folder}/")
        finally:
            git("worktree", "remove", "--force", str(tree), cwd=media)
    print(f"{stored() - before} LFS object(s) downloaded; {before} were already here")
    if args.keep and (args.keep / "dev").is_dir():
        shutil.copytree(args.keep / "dev", out / "dev")
        print("dev/ carried over")

    pointers = [str(p.relative_to(out)) for p in out.rglob("*")
                if p.is_file() and p.stat().st_size < 200 and p.read_bytes().startswith(POINTER)]
    if pointers:
        print(f"FAILED: {len(pointers)} LFS pointer(s) in place of files, the first "
              f"{pointers[0]}")
        return 1
    subprocess.run([sys.executable, str(Path(__file__).with_name("pages_site.py")), str(out)],
                   check=True)
    return 0


if __name__ == "__main__":
    sys.exit(main())
