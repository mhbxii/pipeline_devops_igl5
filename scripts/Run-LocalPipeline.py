"""Trigger or observe tache-ci; cloud planning/delivery requires explicit flags."""
import argparse
import json
import re
import time
from urllib.parse import urlparse
from local_ci import ROOT, Jenkins, environment

parser = argparse.ArgumentParser()
parser.add_argument("--full", action="store_true", help="Enable configured local Sonar analysis and Nexus publication")
parser.add_argument("--publish", action="store_true", help="Use the approved public Docker Hub repository and deploy its published image to the prepared EKS application")
parser.add_argument("--terraform", action="store_true", help="Plan AWS infrastructure; never applies automatically")
parser.add_argument("--build", type=int, help="Observe an already-triggered build instead")
args = parser.parse_args()
if args.publish and not args.full:
    parser.error("--publish requires --full")
api = Jenkins()
job = api.url + "/job/tache-ci"
if args.build:
    number = args.build
else:
    endpoint = "/buildWithParameters"
    params = {"RUN_SONAR": str(args.full).lower(), "PUBLISH_NEXUS": str(args.full).lower(),
              "PUBLISH_IMAGE": str(args.publish).lower(), "DEPLOY_EKS": str(args.publish).lower(),
              "RUN_TERRAFORM": str(args.terraform).lower(), "APPLY_TERRAFORM": "false",
              "NEXUS_URL": "http://nexus:8081", "DOCKER_REPOSITORY": "ayyoub1/events-tache"}
    response = api.session.post(job + endpoint, data=params, timeout=20)
    response.raise_for_status()
    queue_path = urlparse(response.headers["Location"]).path.rstrip("/")
    deadline = time.monotonic() + 240
    while True:
        response = api.session.get(api.url + queue_path + "/api/json", timeout=20)
        response.raise_for_status()
        queued = response.json()
        if queued.get("cancelled"):
            raise SystemExit("Jenkins queue item was cancelled")
        if queued.get("executable"):
            number = queued["executable"]["number"]
            break
        if time.monotonic() > deadline:
            raise SystemExit("Jenkins queue wait timed out")
        time.sleep(2)
print(f"Observing actual Jenkins build #{number}", flush=True)
build_url = f"{job}/{number}"
deadline = time.monotonic() + 3600
last_stage = None
while True:
    response = api.session.get(build_url + "/api/json", timeout=30)
    response.raise_for_status()
    build = response.json()
    stages_response = api.session.get(build_url + "/wfapi/describe", timeout=20)
    stages = stages_response.json() if stages_response.status_code == 200 else {}
    state = [(s["name"], s["status"]) for s in stages.get("stages", [])]
    if state != last_stage:
        print("Stages:", state, flush=True)
        last_stage = state
    if not build["building"]:
        break
    if time.monotonic() > deadline:
        raise SystemExit("Observation timed out; build has not been cancelled")
    time.sleep(10)
response = api.session.get(build_url + "/consoleText", timeout=30)
response.raise_for_status()
console = response.text
console = re.sub(r'https?://[^\s"\']+/blobs/uploads/[^\s"\']+', '[registry upload URL redacted]', console)
for key, value in environment().items():
    if value and ("PASSWORD" in key or "TOKEN" in key or "SECRET" in key):
        console = console.replace(value, "[redacted]")
evidence = ROOT / "docs/evidence/08-jenkins"
(evidence / f"build-{number}-console.log").write_text(console, encoding="utf-8")
summary = {"number": number, "url": build_url, "result": build["result"],
           "durationMillis": build["duration"], "timestampMillis": build["timestamp"], "stages": stages}
(evidence / f"build-{number}-summary.json").write_text(json.dumps(summary, indent=2), encoding="utf-8")
print(f"Jenkins build #{number}: {build['result']}", flush=True)
if build["result"] != "SUCCESS":
    print("\n".join(console.splitlines()[-20:]))
    raise SystemExit(1)
