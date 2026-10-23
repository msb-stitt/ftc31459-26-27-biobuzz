"""Status: controlling. The bucket coding-stitt-cc-media, as the tools here read and fill it.

Two calls to Cloudflare and nothing else, so the tools that use them can be run on made-up
listings: `objects` reads the bucket's list by Cloudflare's API, and `put` uploads one file by
wrangler.sh. The API token is CLOUDFLARE_API_TOKEN, or on a Mac the login Keychain's, service
cloudflare-api, account coding-stitt-cc-mac.
"""

import json
import os
import subprocess
import urllib.parse
import urllib.request
from pathlib import Path

ACCOUNT = "8750f54dd1cc6ffc4a7d066b02d08d85"
BUCKET = "coding-stitt-cc-media"
HERE = Path(__file__).resolve().parent
TYPES = {".mp4": "video/mp4", ".png": "image/png", ".jpg": "image/jpeg", ".svg": "image/svg+xml",
         ".gif": "image/gif", ".webp": "image/webp"}


def token() -> str:
    if os.environ.get("CLOUDFLARE_API_TOKEN"):
        return os.environ["CLOUDFLARE_API_TOKEN"]
    return subprocess.run(["security", "find-generic-password", "-s", "cloudflare-api", "-a",
                           "coding-stitt-cc-mac", "-w"], capture_output=True, text=True,
                          check=True).stdout.strip()


def objects(prefix: str = "") -> dict[str, int]:
    """Each object in the bucket whose key starts with PREFIX, as key: bytes."""
    found, cursor = {}, ""
    while True:
        query = urllib.parse.urlencode({"per_page": 1000, "prefix": prefix,
                                        **({"cursor": cursor} if cursor else {})})
        request = urllib.request.Request(
            f"https://api.cloudflare.com/client/v4/accounts/{ACCOUNT}/r2/buckets/{BUCKET}/objects"
            f"?{query}", headers={"Authorization": f"Bearer {token()}"})
        with urllib.request.urlopen(request) as answer:
            page = json.load(answer)
        found.update({each["key"]: each["size"] for each in page["result"]})
        cursor = page.get("result_info", {}).get("cursor")
        if not page.get("result_info", {}).get("is_truncated") or not cursor:
            return found


def put(path: Path, key: str) -> None:
    """Uploads PATH to the bucket as KEY, with the Content-Type its suffix names."""
    subprocess.run([str(HERE / "wrangler.sh"), "r2", "object", "put", f"{BUCKET}/{key}",
                    "--file", str(path), "--content-type",
                    TYPES.get(path.suffix, "application/octet-stream"), "--remote"],
                   env={**os.environ, "CLOUDFLARE_API_TOKEN": token()}, check=True)
