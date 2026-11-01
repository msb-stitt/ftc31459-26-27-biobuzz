"""Status: controlling.

What a run of .github/workflows/release.yml publishes, or why it publishes
nothing, so each rule can be tried before a tag is pushed.

    python3 tools/release.py tag v0.4.1-rc.1
    python3 tools/release.py tag v0.4.1 --branches refs/remotes/origin
    python3 tools/release.py branch dev

It prints `version=`, `kind=`, `solutions=`, `media=` and `latest=` lines for
$GITHUB_OUTPUT, or the reason a tag is refused and exits 1. release.yml fetches
every branch as a local branch, so it reads refs/heads; run here, before the
branch is pushed or after, --branches refs/remotes/origin reads origin's.

- A release line is `dev`, `main` or a branch `release-X.Y`.
- A pre-release, `vX.Y.Z-rc.N`, publishes when its commit is on a release line.
- A release, `vX.Y.Z`, publishes when its commit is the head of `main` or of
  `release-X.Y`.
- A tag publishes only when book/keep.toml names a solutions tag.
- A release is GitHub's Latest only when no higher `vX.Y.Z` tag exists:
  `latest=false` otherwise, and for a pre-release.
- A push to `dev` publishes `ver/dev/`. Anything else publishes nothing.

tools/test_release.py tries each rule.
"""

import argparse
import re
import subprocess
import sys
import tomllib
from pathlib import Path

RELEASE = re.compile(r"v(\d+)\.(\d+)\.(\d+)")
LINE = re.compile(r"dev|main|release-\d+\.\d+")


class Refused(Exception):
    pass


def numbers(tag):
    """(X, Y, Z) of vX.Y.Z, or of vX.Y.Z-anything; None for any other name."""
    m = RELEASE.match(tag)
    if not m or (tag[m.end():] and not tag[m.end():].startswith("-")):
        return None
    return tuple(int(n) for n in m.groups())


def decide_tag(version, commit, heads, is_on, tags, solutions_is_tag, solutions):
    """kind and latest for a tag, or Refused.

    heads maps each release line's name to its head commit; is_on(commit, line)
    says whether commit is on line; tags are the repository's tags.
    """
    xyz = numbers(version)
    if xyz is None:
        raise Refused(f"{version} is not vX.Y.Z or vX.Y.Z-rc.N.")
    if not solutions_is_tag:
        raise Refused(
            f"keep.toml names {solutions}, which is not a tag. Run tools/pin.py, commit what it "
            "writes, and tag again.")
    short = commit[:8]
    if "-" in version:
        if not any(is_on(commit, line) for line in heads):
            raise Refused(
                f"{version} is on {short}, which is on no release line: "
                f"{', '.join(sorted(heads))}. Tag a commit of one.")
        return "pre", False
    own = f"release-{xyz[0]}.{xyz[1]}"
    at = [line for line in ("main", own) if heads.get(line) == commit]
    if not at:
        where = ", ".join(f"{line} is at {heads[line][:8]}"
                          for line in ("main", own) if line in heads) or f"there is no {own}"
        raise Refused(
            f"{version} is on {short}, which is the head of neither main nor {own}; {where}. "
            f"Move one of them to the commit, push it, then tag its head.")
    higher = [t for t in tags
              if "-" not in t and numbers(t) is not None and numbers(t) > xyz]
    return "release", not higher


def git(*args):
    return subprocess.run(["git", *args], check=True, capture_output=True,
                          text=True).stdout.strip()


def main():
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[2])
    parser.add_argument("ref_type", choices=["tag", "branch"])
    parser.add_argument("ref_name")
    parser.add_argument("--branches", default="refs/heads",
                        help="where the release lines are read, refs/heads by default")
    args = parser.parse_args()

    keep = tomllib.loads(Path("book/keep.toml").read_text())
    solutions = keep["answers"]["solutions_commit"] or keep["answers"]["solutions"]
    media = keep["media"]["commit"] or keep["media"]["ref"]
    version, kind, latest = "", "", False
    if args.ref_type == "tag":
        version = args.ref_name
        prefix = args.branches.rstrip("/") + "/"
        heads = {}
        for row in git("for-each-ref", "--format=%(refname) %(objectname)", prefix).splitlines():
            ref, sha = row.split()
            name = ref[len(prefix):]
            if LINE.fullmatch(name):
                heads[name] = sha
        commit = git("rev-parse", f"{version}^{{commit}}")
        solutions_is_tag = subprocess.run(
            ["git", "rev-parse", "--verify", "--quiet", f"refs/tags/{solutions}"],
            capture_output=True).returncode == 0

        def is_on(c, line):
            return subprocess.run(["git", "merge-base", "--is-ancestor", c, prefix + line],
                                  capture_output=True).returncode == 0

        try:
            kind, latest = decide_tag(version, commit, heads, is_on,
                                      git("tag", "-l", "v*").split(), solutions_is_tag, solutions)
        except Refused as e:
            print(e, file=sys.stderr)
            sys.exit(1)
    elif args.ref_name == "dev":
        version, kind = "dev", "dev"
    print(f"version={version}")
    print(f"kind={kind}")
    print(f"solutions={solutions}")
    print(f"media={media}")
    print(f"latest={'true' if latest else 'false'}")


if __name__ == "__main__":
    main()
