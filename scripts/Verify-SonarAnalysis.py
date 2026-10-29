"""Record actual compute-engine, quality-gate and coverage results."""
import json
import time
import requests
import argparse
import re
from local_ci import ROOT, environment

values = environment()
session = requests.Session()
session.auth = ("admin", values["SONAR_ADMIN_PASSWORD"])
url = "http://localhost:9000"
parser = argparse.ArgumentParser()
parser.add_argument("--build", type=int)
args = parser.parse_args()
prefix = f"build-{args.build}-" if args.build else "local-"
if args.build:
    console = (ROOT / f"docs/evidence/08-jenkins/build-{args.build}-console.log").read_text(encoding="utf-8")
    match = re.search(r"SonarQube task '([^']+)'", console)
    if not match:
        raise SystemExit("No Sonar task found in this actual build log")
    task_id = match.group(1)
else:
    report = ROOT / "target/sonar/report-task.txt"
    properties = dict(line.split("=", 1) for line in report.read_text().splitlines() if "=" in line)
    task_id = properties["ceTaskId"]
deadline = time.monotonic() + 300
while True:
    response = session.get(url + "/api/ce/task", params={"id": task_id}, timeout=20)
    response.raise_for_status()
    task = response.json()["task"]
    if task["status"] in ("SUCCESS", "FAILED", "CANCELED"):
        break
    if time.monotonic() > deadline:
        raise SystemExit("Compute-engine task is still pending")
    time.sleep(3)
evidence = ROOT / "docs/evidence/06-sonarqube"
(evidence / (prefix + "analysis-task.json")).write_text(json.dumps(task, indent=2), encoding="utf-8")
if task["status"] != "SUCCESS":
    raise SystemExit("Analysis compute task did not succeed")
response = session.get(url + "/api/qualitygates/project_status", params={"analysisId": task["analysisId"]}, timeout=20)
response.raise_for_status()
gate = response.json()
(evidence / (prefix + "quality-gate.json")).write_text(json.dumps(gate, indent=2), encoding="utf-8")
response = session.get(url + "/api/measures/component", params={"component": "events-tache",
    "metricKeys": "coverage,line_coverage,branch_coverage,bugs,vulnerabilities,code_smells,security_hotspots,ncloc"}, timeout=20)
response.raise_for_status()
(evidence / (prefix + "project-metrics.json")).write_text(json.dumps(response.json(), indent=2), encoding="utf-8")
print("Actual Sonar quality-gate status:", gate["projectStatus"]["status"])
print("Whole-project metrics:", response.json()["component"]["measures"])
response = session.get(url + "/api/webhooks/deliveries", params={"ceTaskId": task_id}, timeout=20)
response.raise_for_status()
(evidence / (prefix + "webhook-deliveries.json")).write_text(json.dumps(response.json(), indent=2), encoding="utf-8")
print("Actual webhook deliveries:", response.json()["deliveries"])
