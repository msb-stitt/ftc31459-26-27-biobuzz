"""Status: controlling, for the report a deploy prints of what coding.stitt.cc holds.

    python cloudflare/usage.py --site SITE

Counts the files in SITE, the folder `wrangler pages deploy` uploads, its .git left out, under
ftc-lessons-java/ and in the whole folder, which is what Pages' limit of files a site counts;
and sums the objects in the bucket coding-stitt-cc-media, read from Cloudflare's list of them,
under the key prefix ftc-lessons-java/ and in the whole bucket. Each line says what it measured.
"""

import argparse
from pathlib import Path

import r2

LESSONS = "ftc-lessons-java"


def files(folder: Path) -> tuple[int, int]:
    found = [p for p in folder.rglob("*") if p.is_file() and ".git" not in p.parts]
    return len(found), sum(p.stat().st_size for p in found)


def report(site: Path, in_r2: dict[str, int]) -> list[str]:
    under, whole = files(site / LESSONS), files(site)
    ours = [n for k, n in in_r2.items() if k.startswith(f"{LESSONS}/")]
    return [
        f"Pages, {under[0]} files, {under[1]:,} bytes: the files under {LESSONS}/ in the folder "
        f"deployed, the media index included.",
        f"Pages, {whole[0]} files, {whole[1]:,} bytes: every file in the folder deployed, "
        f"coding.stitt.cc's own index.html, _redirects and _routes.json included, and the "
        f"Function not.",
        f"R2, {len(ours)} objects, {sum(ours):,} bytes: the objects in {r2.BUCKET} whose keys "
        f"start {LESSONS}/, from Cloudflare's list of them.",
        f"R2, {len(in_r2)} objects, {sum(in_r2.values()):,} bytes: every object in {r2.BUCKET}, "
        f"from the same list.",
    ]


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    ap.add_argument("--site", type=Path, required=True)
    args = ap.parse_args()
    print("\n".join(report(args.site, r2.objects())))


if __name__ == "__main__":
    main()
