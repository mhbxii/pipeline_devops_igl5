"""Observe a scheduler-triggered build from GitHub; never request a build/poll."""
import argparse
import json
import time
from local_ci import ROOT, Jenkins

parser = argparse.ArgumentParser()
parser.add_argument("--after-build", required=True, type=int)
parser.add_argument("--expected-commit", required=True)
args = parser.parse_args()
api = Jenkins()
job = api.url + "/job/tache-ci"
deadline = time.monotonic() + 900
print("Waiting for Jenkins' scheduled SCM poll; no build or poll is requested by this observer.", flush=True)
while time.monotonic() < deadline:
    response = api.session.get(job + "/api/json", params={"tree": "builds[number]"}, timeout=20)
    response.raise_for_status()
    numbers = [b["number"] for b in response.json()["builds"] if b["number"] > args.after_build]
    for number in sorted(numbers):
        response = api.session.get(f"{job}/{number}/api/json", timeout=20)
        response.raise_for_status()
        build = response.json()
        causes = [c for action in build["actions"] for c in action.get("causes", [])]
        if not any("SCMTriggerCause" in c.get("_class", "") for c in causes):
            continue
        actions = [a for a in build["actions"] if a.get("lastBuiltRevision")]
        matching = [a for a in actions if a["lastBuiltRevision"]["SHA1"] == args.expected_commit
                    and "https://github.com/mhbxii/pipeline_devops_igl5.git" in a.get("remoteUrls", [])]
        if not matching:
            continue  # Checkout may still be in progress.
        proof = {"build": number, "commit": args.expected_commit, "sourceUrls": matching[0]["remoteUrls"],
                 "causes": causes, "timestampMillis": build["timestamp"], "observedWithoutManualTrigger": True}
        (ROOT / "docs/evidence/08-jenkins/github-trigger.json").write_text(json.dumps(proof, indent=2), encoding="utf-8")
        response = api.session.get(f"{job}/{number}/pollingLog/pollingLog", timeout=20)
        response.raise_for_status()
        (ROOT / "docs/evidence/08-jenkins/github-polling.log").write_text(response.text, encoding="utf-8")
        print(json.dumps(proof, indent=2), flush=True)
        raise SystemExit(0)
    time.sleep(10)
raise SystemExit("No matching automatic GitHub checkout was observed; no manual trigger was issued")
