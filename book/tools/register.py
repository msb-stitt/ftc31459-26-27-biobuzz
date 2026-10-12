"""Read keep.toml, the guide's register of kept passages and word ceilings.

One reader, because two checks read the same file and a second parser is a
second thing to keep right.
"""

import tomllib

from bookpaths import book_root

PATH = "keep.toml"


def load() -> dict:
    with open(book_root() / PATH, "rb") as fh:
        return tomllib.load(fh)


def media() -> tuple[str, str]:
    """The corbels-lfs-lessons branch, and what to check out: its pinned commit, or the branch."""
    pin = load()["media"]
    return pin["ref"], pin["commit"] or pin["ref"]


def pages() -> dict[str, int]:
    """Page path to word ceiling."""
    return {entry["path"]: entry["words"] for entry in load().get("page", [])}


def kept() -> list[dict]:
    """Every kept passage: page, anchor, sha256, date."""
    return load().get("keep", [])
