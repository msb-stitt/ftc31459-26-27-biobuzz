"""Status: controlling, for the videos the release workflow builds with.

    python cloudflare/videos.py --media MEDIA

MEDIA is a corbels-lfs-lessons checkout whose videos/ are LFS pointers. Each pointer names its
video's SHA-256, so its hashed name, NAME_HASH.mp4. A video coding.stitt.cc already serves by
that name is downloaded from there, which costs no LFS bandwidth; any other is pulled from LFS.
Either way it is checked against the pointer's SHA-256 and replaces the pointer.
"""

import argparse
import hashlib
import re
import subprocess
import sys
import urllib.error
import urllib.request
from pathlib import Path

import media_index

POINTER = re.compile(rb"^oid sha256:([0-9a-f]{64})$", re.M)


def fetch(name: str) -> bytes | None:
    try:
        return media_index.served(name)
    except urllib.error.HTTPError as error:
        if error.code == 404:
            return None
        raise


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    ap.add_argument("--media", type=Path, required=True)
    args = ap.parse_args()
    for video in sorted((args.media / "videos").glob("*.mp4")):
        head = video.read_bytes()[:200]
        oid = POINTER.search(head) if head.startswith(b"version https://git-lfs") else None
        if not oid:
            print(f"{video.name}: already pulled")
            continue
        sha = oid[1].decode()
        name = f"{video.stem}_{sha[:12]}{video.suffix}"
        data = fetch(name)
        if data is None:
            subprocess.run(["git", "lfs", "pull", "--include", f"videos/{video.name}"],
                           cwd=args.media, check=True)
            print(f"{video.name}: {name} is not served yet, so pulled from LFS")
        else:
            video.write_bytes(data)
            print(f"{video.name}: downloaded as {name} from {media_index.SERVED}")
        if hashlib.sha256(video.read_bytes()).hexdigest() != sha:
            print(f"FAILED: {video.name} is not the video its pointer names, {sha[:12]}")
            return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
