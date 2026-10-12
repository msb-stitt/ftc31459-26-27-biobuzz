"""Status: controlling, for what a release archives in corbels-lfs-lessons.

Commits a built release, the guide/, mentor/, review/ and both PDFs that
publish_check writes, to corbels-lfs-lessons as published/, on top of the media
commit it was built from, so the commit holds the figures and videos beside what
they made. Then pushes it: under a tag named for the release, or, for a trial, to
a branch.

    python tools/run.py archive_release --media DIR --built DIR --name NAME (--tag | --branch B) [--remote R]

MEDIA is a corbels-lfs-lessons checkout at keep.toml's [media] ref. The commit is
made in a throwaway worktree, so MEDIA's own checkout is not touched. Its LFS
files are not downloaded for this: the worktree is made with
GIT_LFS_SKIP_SMUDGE=1, and only published/ is added.
"""

import argparse
import os
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

SKIP = {**os.environ, "GIT_LFS_SKIP_SMUDGE": "1"}


def git(*args: str, cwd: Path) -> str:
    return subprocess.run(["git", *args], cwd=cwd, check=True, capture_output=True, text=True,
                          env=SKIP).stdout.strip()


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[1])
    ap.add_argument("--media", type=Path, required=True)
    ap.add_argument("--built", type=Path, required=True)
    ap.add_argument("--name", required=True, help="the release, such as v0.1.3, in the message")
    where = ap.add_mutually_exclusive_group(required=True)
    where.add_argument("--tag", action="store_true", help="push under a tag named --name")
    where.add_argument("--branch", help="push to this branch instead, replacing it")
    ap.add_argument("--remote", default="origin")
    args = ap.parse_args()
    media, built = args.media.resolve(), args.built.resolve()

    tree = Path(tempfile.mkdtemp(prefix="archive-"))
    git("worktree", "add", "--detach", str(tree), "HEAD", cwd=media)
    try:
        shutil.rmtree(tree / "published", ignore_errors=True)
        shutil.copytree(built, tree / "published")
        git("add", "-A", "published", cwd=tree)
        lessons = os.environ.get("GITHUB_SHA", "")[:7] or "a local build"
        git("-c", "user.name=Mike S.B. Stitt", "-c", "user.email=mike@stitt.cc", "commit", "-q",
            "-m", f"{args.name} as published, built from {lessons}.", cwd=tree)
        commit = git("rev-parse", "HEAD", cwd=tree)
        if args.tag:
            git("tag", args.name, commit, cwd=tree)
            subprocess.run(["git", "push", args.remote, f"refs/tags/{args.name}"], cwd=tree,
                           check=True)
        else:
            subprocess.run(["git", "push", "-f", args.remote, f"{commit}:refs/heads/{args.branch}"],
                           cwd=tree, check=True)
        print(f"archived {args.name} as {commit[:7]} in corbels-lfs-lessons")
    finally:
        git("worktree", "remove", "--force", str(tree), cwd=media)
    return 0


if __name__ == "__main__":
    sys.exit(main())
