"""Verify provisioned Grafana queries against its real Prometheus data source."""
import json
from datetime import datetime, timezone
import requests
from local_ci import ROOT, environment

base = "http://localhost:13000"
session = requests.Session()
session.auth = ("tache-admin", environment()["GRAFANA_ADMIN_PASSWORD"])

def get(path, **kwargs):
    response = session.get(base + path, timeout=30, **kwargs)
    response.raise_for_status()
    return response.json()

health = get("/api/health")
assert health["database"] == "ok", health
dashboard = get("/api/dashboards/uid/events-tache")
datasource = get("/api/datasources/uid/prometheus/health")
assert datasource["status"] == "OK", datasource
queries = {}
for panel in dashboard["dashboard"]["panels"]:
    for target in panel.get("targets", []):
        expression = target["expr"]
        result = get("/api/datasources/proxy/uid/prometheus/api/v1/query", params={"query": expression})
        assert result["status"] == "success" and result["data"]["result"], expression
        queries[expression] = result["data"]["result"]
assert any(float(item["value"][1]) == 1 for expr, rows in queries.items() if expr.startswith("up{") for item in rows)
proof = {"verifiedAt": datetime.now(timezone.utc).isoformat(), "health": health,
         "dashboardUid": dashboard["dashboard"]["uid"], "datasourceHealth": datasource, "queries": queries}
(ROOT / "docs/evidence/13-monitoring/grafana-verification.json").write_text(json.dumps(proof, indent=2), encoding="utf-8")
print(f"Grafana {health['version']}: database OK, Prometheus healthy, {len(queries)} nonempty real queries.")
