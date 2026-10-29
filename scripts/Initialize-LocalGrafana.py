"""Deploy a small local dashboard; Terraform's existing resources are unchanged."""
import json
import subprocess
from local_ci import ROOT, ensure_secret

password = ensure_secret("GRAFANA_ADMIN_PASSWORD")
kubectl = ["kubectl", "--kubeconfig", str(ROOT / ".local/kubeconfig"), "--context", "kind-events-tache"]

def apply(value):
    subprocess.run(kubectl + ["apply", "-f", "-"], input=json.dumps(value), text=True, check=True)

apply({"apiVersion": "v1", "kind": "Namespace", "metadata": {"name": "events-grafana"}})
apply({"apiVersion": "v1", "kind": "Secret", "type": "Opaque", "metadata": {"name": "grafana-admin", "namespace": "events-grafana"},
       "stringData": {"password": password}})
apply({"apiVersion": "v1", "kind": "ConfigMap", "metadata": {"name": "grafana-dashboard", "namespace": "events-grafana"},
       "data": {"dashboard.json": (ROOT / "monitoring/grafana-dashboard.json").read_text(encoding="utf-8")}})
subprocess.run(kubectl + ["apply", "-f", str(ROOT / "k8s/grafana.yaml")], check=True)
subprocess.run(kubectl + ["-n", "events-grafana", "rollout", "status", "deployment/grafana", "--timeout=900s"], check=True)
print("Grafana deployment ready. Verify the provisioned data source and dashboard before claiming monitoring success.")
