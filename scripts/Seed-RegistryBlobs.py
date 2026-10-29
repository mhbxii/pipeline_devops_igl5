"""Seed exact OCI blobs in the approved repository using bounded chunked uploads.

Workaround for Docker Desktop's proxy closing large monolithic uploads. This does
not publish a manifest/tag: Jenkins still performs the actual docker push.
Spec: https://distribution.github.io/distribution/spec/api/#chunked-upload
"""
import argparse
import hashlib
import json
import subprocess
import tarfile
import time
from urllib.parse import urljoin, urlparse
import requests

parser = argparse.ArgumentParser()
parser.add_argument("archive", help="docker image save --platform=linux/amd64 output")
args = parser.parse_args()
repository = "ayyoub1/events-tache"  # Explicitly approved public destination only.
registry = "https://registry-1.docker.io"
credential = subprocess.run(["docker-credential-desktop.exe", "get"],
    input="https://index.docker.io/v1/\n", capture_output=True, text=True, check=True)
credential = json.loads(credential.stdout)
if credential.get("Username") != "ayyoub1":
    raise SystemExit("The stored Docker Hub account differs from the approved destination")
session = requests.Session()

def refresh():
    response = requests.get("https://auth.docker.io/token", auth=(credential["Username"], credential["Secret"]),
        params=[("service", "registry.docker.io"), ("scope", f"repository:{repository}:pull,push"),
                ("scope", "repository:library/eclipse-temurin:pull")], timeout=30)
    if response.status_code != 200:
        raise RuntimeError(f"Registry authentication HTTP {response.status_code}")
    session.headers["Authorization"] = "Bearer " + response.json()["token"]

def request(method, url, expected, **kwargs):
    for attempt in range(3):
        try:
            response = session.request(method, url, timeout=(20, 120), **kwargs)
        except requests.RequestException as exc:
            print(f"Registry {method} transport {type(exc).__name__}; attempt {attempt + 1}/3", flush=True)
            detail = str(exc).replace(url, "[upload URL redacted]")
            parsed = urlparse(url)
            if parsed.query:
                detail = detail.replace(parsed.path + "?" + parsed.query, "[upload path redacted]")
            print(detail[:300], flush=True)
            if method == "PATCH":
                # Do not replay an ambiguous chunk: preserve server offset integrity.
                raise
            time.sleep(2)
            continue
        if response.status_code == 401:
            refresh()
            continue
        if response.status_code in expected:
            return response
        if response.status_code >= 500:
            time.sleep(2)
            continue
        raise RuntimeError(f"Registry {method} HTTP {response.status_code}")
    raise RuntimeError(f"Registry {method} exhausted retries")

def main():
    refresh()
    with tarfile.open(args.archive) as archive:
        index = json.load(archive.extractfile("index.json"))
        descriptor = index["manifests"][0]
        manifest = json.load(archive.extractfile("blobs/sha256/" + descriptor["digest"].split(":")[1]))
        for blob in [manifest["config"]] + manifest["layers"]:
            digest = blob["digest"]
            short = digest[7:19]
            head = request("HEAD", f"{registry}/v2/{repository}/blobs/{digest}", {200, 404})
            if head.status_code == 200:
                print(short, "already present", flush=True)
                continue
            # Reuse public Temurin base blobs server-side whenever the registry permits.
            mounted = request("POST", f"{registry}/v2/{repository}/blobs/uploads/", {201, 202},
                params={"mount": digest, "from": "library/eclipse-temurin"})
            if mounted.status_code == 201:
                print(short, "mounted from public Temurin base; no transfer", flush=True)
                continue
            stream = archive.extractfile("blobs/sha256/" + digest.split(":")[1])
            # Verify local content before sending a byte; never alter compressed layers.
            checksum = hashlib.sha256()
            for chunk in iter(lambda: stream.read(1024 * 1024), b""):
                checksum.update(chunk)
            actual = checksum.hexdigest()
            if actual != digest.split(":")[1]:
                raise RuntimeError("Archive blob digest mismatch")
            stream.seek(0)
            response = mounted
            location = urljoin(registry, response.headers["Location"])
            offset = 0
            while offset < blob["size"]:
                data = stream.read(256 * 1024)
                response = request("PATCH", location, {202}, data=data,
                    headers={"Content-Type": "application/octet-stream", "Content-Range": f"{offset}-{offset + len(data) - 1}"})
                location = urljoin(registry, response.headers["Location"])
                offset += len(data)
                print(short, f"accepted {offset}/{blob['size']} bytes", flush=True)
            request("PUT", location, {201}, params={"digest": digest}, data=b"",
                    headers={"Content-Type": "application/octet-stream"})
            print(short, "digest verified by registry", flush=True)
    print("Exact blobs seeded. No manifest/tag was published by this helper.", flush=True)

try:
    main()
except requests.RequestException as exc:
    # Signed upload URLs and bearer tokens must never appear in evidence.
    raise SystemExit("Registry transport error: " + type(exc).__name__) from None
