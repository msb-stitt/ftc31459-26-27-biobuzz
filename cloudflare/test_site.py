"""Status: controlling. assemble.py and media_index.py, run offline on made-up versions.

    python cloudflare/test_site.py

Each made-up version has three books whose pages link figures and a video in its media/, by
hashed name, as tools/publish_check.py leaves them. Nothing reaches Cloudflare.
"""

import hashlib
import json
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
import assemble  # noqa: E402
import media_index  # noqa: E402

L = media_index.LESSONS


def built(root: Path, contents: dict[str, bytes]) -> Path:
    """A made-up build/publish/: each book's index and one task page link every file."""
    names = {}
    (root / "media").mkdir(parents=True)
    for stem, data in contents.items():
        name = f"{Path(stem).stem}_{hashlib.sha256(data).hexdigest()[:12]}{Path(stem).suffix}"
        (root / "media" / name).write_bytes(data)
        names[stem] = name
    for book in assemble.BOOKS:
        (root / book / "tasks").mkdir(parents=True)
        (root / book / "index.html").write_text("".join(
            f'<img src="../media/{n}">' for n in names.values()))
        (root / book / "tasks" / "l020.html").write_text("".join(
            f'<a href="../../media/{n}">v</a>' for n in names.values()))
    return root


def run(site: Path, build: Path, version: str, commit: str) -> None:
    subprocess.run([sys.executable, str(HERE / "assemble.py"), "--site", str(site), "--built",
                    str(build), "--version", version, "--commit", commit, "--offline"],
                   check=True, capture_output=True)


def in_r2(site: Path) -> dict[str, int]:
    return {n: e["bytes"] for n, e in media_index.read(site)["media"].items()}


def main() -> int:
    work = Path(tempfile.mkdtemp(prefix="site-test-"))
    site = work / "site"
    site.mkdir()
    old = built(work / "b1", {"fig-a.png": b"a1", "fig-b.png": b"b1", "l020.mp4": b"video1"})
    new = built(work / "b2", {"fig-a.png": b"a1", "fig-b.png": b"b2", "l020.mp4": b"video2"})
    failed = []

    def expect(what: str, ok: bool) -> None:
        print(("ok      " if ok else "FAILED  ") + what)
        if not ok:
            failed.append(what)

    run(site, new, "dev", "d" * 40)
    expect("dev alone: the landing is dev",
           f"{L}/ver/dev/guide/ 302" in (site / "_redirects").read_text())
    page = site / "404.html"
    expect("404.html at the site's root, its picture beside it, its links from the root",
           page.is_file() and (site / "not-found.png").is_file()
           and 'src="/not-found.png"' in page.read_text()
           and f'href="/{L}/"' in page.read_text())
    run(site, old, "v0.1.0-rc.1", "a" * 40)
    expect("a pre-release and dev: the landing is the pre-release",
           f"/{L}/guide/* /{L}/ver/v0.1.0-rc.1/guide/:splat 302"
           in (site / "_redirects").read_text())
    run(site, old, "v0.1.0", "b" * 40)
    run(site, new, "v0.2.0-rc.1", "c" * 40)
    redirects = (site / "_redirects").read_text()
    expect("a release beats a newer pre-release as the landing",
           f"/{L}/ /{L}/ver/v0.1.0/guide/ 302" in redirects)
    expect("media under any version answers 301 to the one media folder",
           f"/{L}/ver/:version/media/:name /{L}/media/:name 301" in redirects)
    page = (site / L / "versions.html").read_text()
    order = [page.index(f"<b>{v}</b>") for v in ("v0.2.0-rc.1", "v0.1.0", "v0.1.0-rc.1", "dev")]
    expect("versions.html lists newest first, dev last", order == sorted(order))
    expect("versions.html links dev's code at its commit", f"/tree/{'d' * 40}" in page)
    expect("versions.html links a tag's GitHub Release",
           "/releases/tag/v0.1.0\"" in page)
    expect("no media in the pages' folders", not list(site.glob(f"{L}/ver/*/media")))
    routes = json.loads((site / "_routes.json").read_text())
    expect("_routes.json sends media to the Function, not the index",
           routes["include"] == [f"/{L}/media/*"]
           and routes["exclude"] == [f"/{L}/media/media-index.jsonc"])

    index = media_index.read(site)
    expect("five media files: fig-a once, fig-b and the video twice", len(index["media"]) == 5)
    shared = next(n for n in index["media"] if n.startswith("fig-a_"))
    expect("fig-a, the same bytes, is used by all four versions",
           len(index["media"][shared]["used_by_versions"]) == 4)
    expect("nothing is removable while every version is listed",
           index["media_used_by_no_supported_version"] == [])
    expect("the checks pass", media_index.check(site, in_r2(site)) == [])

    shutil.rmtree(site / L / "ver" / "v0.1.0-rc.1")
    shutil.rmtree(site / L / "ver" / "v0.1.0")
    run(site, new, "dev", "e" * 40)
    index = media_index.read(site)
    gone = sorted(index["media_used_by_no_supported_version"])
    expect("purging the old versions makes their two files removable",
           [n.split("_")[0] for n in gone] == ["fig-b", "l020"])
    expect("the checks pass after the purge", media_index.check(site, in_r2(site)) == [])
    expect("the landing moves to the pre-release left",
           f"/{L}/ver/v0.2.0-rc.1/guide/ 302" in (site / "_redirects").read_text())

    planted = {
        "a listed file not in R2": lambda r: r.pop(shared),
        "a file in R2 not listed": lambda r: r.update({"fig-z_0123456789ab.png": 3}),
        "a file whose size R2 does not match": lambda r: r.update({shared: 99}),
    }
    for what, plant in planted.items():
        r = in_r2(site)
        plant(r)
        expect(f"the checks catch {what}", media_index.check(site, r) != [])
    page = site / L / "ver" / "dev" / "guide" / "index.html"
    page.write_text(page.read_text() + '<img src="../media/fig-q_0123456789ab.png">')
    expect("the checks catch a page linking media not listed for it",
           media_index.check(site, in_r2(site)) != [])
    shutil.copytree(site / L / "ver" / "dev", site / L / "ver" / "v9.9.9")
    expect("the checks catch a version's pages not listed",
           any("v9.9.9" in b for b in media_index.check(site, in_r2(site))))
    text = (site / media_index.INDEX).read_text()
    (site / media_index.INDEX).write_text("// a person's note\n" + text.replace(
        '"site":', '/* another */ "site":', 1))
    expect("a comment in the index still parses", media_index.read(site)["r2_bucket"])

    shutil.rmtree(work)
    print(f"\n{len(failed)} failed" if failed else "\nall passed")
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
