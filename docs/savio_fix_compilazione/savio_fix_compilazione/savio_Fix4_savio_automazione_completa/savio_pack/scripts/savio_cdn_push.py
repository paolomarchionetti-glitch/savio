#!/usr/bin/env python3
"""
savio_cdn_push.py
=================
Carica offers_current.json su GitHub Releases come "latest-data".
L'app Android scarica da: https://github.com/USERNAME/savio/releases/latest/download/offers_current.json

Uso:
  python3 savio_cdn_push.py
  python3 savio_cdn_push.py --tag v2026.04.21

Richiede:
  export GITHUB_TOKEN="ghp_..."
  export GITHUB_REPO="username/savio"   # es: paolo/savio
"""

import os, sys, json, requests
from datetime import date
from pathlib import Path

GITHUB_TOKEN = os.environ.get("GITHUB_TOKEN", "")
GITHUB_REPO  = os.environ.get("GITHUB_REPO", "")   # es: paolomarchionetti/savio
BASE_DIR     = Path(__file__).parent.parent
ASSETS_DIR   = BASE_DIR / "app" / "src" / "main" / "assets"

FILES_TO_UPLOAD = [
    "offers_current.json",
    "stores_pilot_v1.json",
    "areas_v1.json",
    "catalog_v1.json",
    "equivalences_v1.json",
]

def github_api(method, endpoint, **kwargs):
    headers = {
        "Authorization": f"token {GITHUB_TOKEN}",
        "Accept": "application/vnd.github.v3+json",
    }
    r = requests.request(
        method,
        f"https://api.github.com/repos/{GITHUB_REPO}/{endpoint}",
        headers=headers,
        **kwargs
    )
    r.raise_for_status()
    return r.json()

def get_or_create_release(tag: str) -> dict:
    """Ottieni o crea la release 'latest-data'."""
    try:
        return github_api("GET", f"releases/tags/{tag}")
    except requests.HTTPError:
        pass   # 404 — non esiste ancora

    today = date.today().isoformat()
    return github_api("POST", "releases", json={
        "tag_name": tag,
        "name": f"Data {today}",
        "body": f"Offerte aggiornate al {today}. Scaricate automaticamente dall'app Savio.",
        "prerelease": False,
        "draft": False,
    })

def delete_existing_asset(release_id: int, filename: str):
    """Elimina un asset esistente con lo stesso nome."""
    assets = github_api("GET", f"releases/{release_id}/assets")
    for asset in assets:
        if asset["name"] == filename:
            github_api("DELETE", f"releases/assets/{asset['id']}")
            print(f"  🗑  Eliminato asset esistente: {filename}")

def upload_asset(release: dict, file_path: Path):
    """Carica un file come asset della release."""
    upload_url = release["upload_url"].split("{")[0]
    filename    = file_path.name

    delete_existing_asset(release["id"], filename)

    with open(file_path, "rb") as f:
        data = f.read()

    r = requests.post(
        upload_url,
        headers={
            "Authorization": f"token {GITHUB_TOKEN}",
            "Content-Type": "application/json",
        },
        params={"name": filename},
        data=data,
    )
    r.raise_for_status()
    asset = r.json()
    print(f"  ✅ Caricato: {filename} → {asset['browser_download_url']}")
    return asset["browser_download_url"]

def main():
    if not GITHUB_TOKEN or not GITHUB_REPO:
        print("❌ Imposta GITHUB_TOKEN e GITHUB_REPO")
        print("   export GITHUB_TOKEN='ghp_...'")
        print("   export GITHUB_REPO='tuonome/savio'")
        sys.exit(1)

    tag = f"data-{date.today().isoformat()}"
    if len(sys.argv) > 2 and sys.argv[1] == "--tag":
        tag = sys.argv[2]

    print(f"📦 CDN Push — tag: {tag} — repo: {GITHUB_REPO}")

    release = get_or_create_release(tag)
    print(f"  Release ID: {release['id']}")

    uploaded_urls = {}
    for fname in FILES_TO_UPLOAD:
        fpath = ASSETS_DIR / fname
        if not fpath.exists():
            print(f"  ⚠️  File non trovato: {fpath}")
            continue
        url = upload_asset(release, fpath)
        uploaded_urls[fname] = url

    # Aggiorna anche la release "latest-data" (tag fisso per l'app)
    try:
        latest = get_or_create_release("latest-data")
        for fname in FILES_TO_UPLOAD:
            fpath = ASSETS_DIR / fname
            if fpath.exists():
                upload_asset(latest, fpath)
        print(f"\n✅ Aggiornato anche 'latest-data' release")
        print(f"   L'app scaricherà da:")
        print(f"   https://github.com/{GITHUB_REPO}/releases/latest/download/offers_current.json")
    except Exception as e:
        print(f"  ⚠️  Errore aggiornamento latest-data: {e}")

    print(f"\nDone. {len(uploaded_urls)}/{len(FILES_TO_UPLOAD)} file caricati.")

if __name__ == "__main__":
    main()
