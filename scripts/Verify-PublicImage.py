"""Verify public visibility and anonymous access to the exact delivered OCI image."""
import hashlib
import json
import subprocess
import tarfile
import tempfile
from pathlib import Path
import time
from datetime import datetime, timezone
import requests
from local_ci import ROOT

delivery = json.loads((ROOT / "docs/evidence/12-final-validation/local-delivery.json").read_text())
image = delivery["image"]
repository, tag = image.rsplit(":", 1)
if repository != "ayyoub1/events-tache":
    raise SystemExit("The verified deployment is not using the approved public repository")
session = requests.Session()  # Deliberately no account credentials or cookies.
for attempt in range(12):
    response = session.get(f"https://hub.docker.com/v2/repositories/{repository}/", timeout=30)
    if response.status_code == 200:
        break
    if response.status_code != 404:
        response.raise_for_status()
    time.sleep(5)
response.raise_for_status()
metadata = response.json()
assert metadata["is_private"] is False, "Repository visibility is not public"
response = session.get("https://auth.docker.io/token", params={
    "service": "registry.docker.io", "scope": f"repository:{repository}:pull"}, timeout=30)
response.raise_for_status()
token = response.json()["token"]
response = session.get(f"https://registry-1.docker.io/v2/{repository}/manifests/{tag}", headers={
    "Authorization": "Bearer " + token, "Accept": "application/vnd.oci.image.manifest.v1+json"}, timeout=30)
response.raise_for_status()
digest = "sha256:" + hashlib.sha256(response.content).hexdigest()
assert response.headers["Docker-Content-Digest"] == digest
manifest = response.json()
# This engine reports the OCI index as .Id. Compare the amd64 manifest/config,
# rather than comparing unlike objects (multi-platform index versus config).
with tempfile.NamedTemporaryFile(dir=ROOT / ".local", suffix=".tar", delete=False) as temporary:
    archive_path = Path(temporary.name)
try:
    subprocess.run(["docker", "image", "save", "--platform=linux/amd64", "-o", str(archive_path), image], check=True)
    with tarfile.open(archive_path) as archive:
        index = json.load(archive.extractfile("index.json"))
        local_digest = index["manifests"][0]["digest"]
        local_manifest = json.load(archive.extractfile("blobs/sha256/" + local_digest.split(":")[1]))
    assert local_digest == digest, "Remote manifest differs from the exact local amd64 image"
    assert manifest["config"]["digest"] == local_manifest["config"]["digest"]
finally:
    archive_path.unlink()
proof = {"verifiedAt": datetime.now(timezone.utc).isoformat(), "repository": repository,
    "public": True, "anonymousManifestHttp": response.status_code, "image": image,
    "manifestDigest": digest, "configurationDigest": manifest["config"]["digest"],
    "layerCount": len(manifest["layers"]), "build": delivery["build"],
    "url": f"https://hub.docker.com/r/{repository}"}
(ROOT / "docs/evidence/09-docker/dockerhub-verification.json").write_text(json.dumps(proof, indent=2), encoding="utf-8")
print(json.dumps(proof, indent=2))
