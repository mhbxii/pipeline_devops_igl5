"""Check that Jenkins archive, Nexus JAR, and Kubernetes image describe the same delivery."""
import argparse
import hashlib
import json
import subprocess
import xml.etree.ElementTree as ET
from local_ci import ROOT, Jenkins, environment
import requests

parser = argparse.ArgumentParser()
parser.add_argument("--build", required=True, type=int)
args = parser.parse_args()
api = Jenkins()
build_url = f"{api.url}/job/tache-ci/{args.build}"
response = api.session.get(build_url + "/api/json", timeout=20)
response.raise_for_status()
build = response.json()
if build["result"] != "SUCCESS":
    raise SystemExit("Only a successful build can be verified as delivered")
revision = next((a["lastBuiltRevision"]["SHA1"] for a in build["actions"] if a.get("lastBuiltRevision")), None)
if not revision:
    raise SystemExit("Cannot establish SCM revision from actual Jenkins build metadata")
expected_image = f"events-tache:tache-{revision[:12]}-{args.build}"
parameters = {p["name"]: p.get("value") for a in build["actions"] for p in a.get("parameters", [])}
if parameters.get("DEPLOY_K8S"):
    expected_image = f"{parameters['DOCKER_REPOSITORY']}:tache-{revision[:12]}-{args.build}"
values = environment()
nexus = requests.Session()
nexus.auth = ("tache-publisher", values["NEXUS_PASSWORD"])
base = "http://localhost:8081/repository/maven-snapshots/tn/esprit/eventsProject/1.0.0-SNAPSHOT/"
response = nexus.get(base + "maven-metadata.xml", timeout=20)
response.raise_for_status()
metadata = ET.fromstring(response.content)
version = next(v.findtext("value") for v in metadata.findall("./versioning/snapshotVersions/snapshotVersion")
               if v.findtext("extension") == "jar" and not v.findtext("classifier"))

def checksum(session, url):
    with session.get(url, stream=True, timeout=40) as response:
        response.raise_for_status()
        digest = hashlib.sha256()
        for chunk in response.iter_content(131072):
            digest.update(chunk)
        return digest.hexdigest()

archive_hash = checksum(api.session, build_url + "/artifact/target/eventsProject-1.0.0-SNAPSHOT.jar")
nexus_hash = checksum(nexus, base + f"eventsProject-{version}.jar")
if archive_hash != nexus_hash:
    raise SystemExit("Nexus snapshot and Jenkins archived JAR differ")
result = subprocess.run(["kubectl", "--kubeconfig", str(ROOT / ".local/kubeconfig"), "--context", "kind-events-tache",
    "-n", "events-tache", "get", "deployment", "events", "-o", "json"], capture_output=True, text=True, check=True)
deployment = json.loads(result.stdout)
image = deployment["spec"]["template"]["spec"]["containers"][0]["image"]
if image != expected_image or deployment["status"].get("availableReplicas", 0) < 1:
    raise SystemExit("Available Kubernetes deployment does not match the expected unique build image")
result = subprocess.run(["kubectl", "--kubeconfig", str(ROOT / ".local/kubeconfig"), "--context", "kind-events-tache",
    "-n", "events-tache", "exec", "deployment/events", "--", "sha256sum", "/app/app.jar"],
    capture_output=True, text=True, check=True)
pod_hash = result.stdout.split()[0]
if pod_hash != archive_hash:
    raise SystemExit("The JAR inside the actual running pod differs from the published build")
summary = {"build": args.build, "commit": revision, "image": image, "nexusSnapshot": version,
    "jenkinsJarSha256": archive_hash, "nexusJarSha256": nexus_hash, "runningPodJarSha256": pod_hash,
    "availableReplicas": deployment["status"]["availableReplicas"]}
(ROOT / "docs/evidence/12-final-validation/local-delivery.json").write_text(json.dumps(summary, indent=2), encoding="utf-8")
print(json.dumps(summary, indent=2))
