"""Exercise the four original HTTP routes against the deployed MySQL application."""
import json
import time
from local_ci import ROOT
import requests

base = "http://localhost:18089/events"
session = requests.Session()
prefix = f"dto-evidence-{int(time.time())}"
results = []

def call(method, path, body=None):
    response = session.request(method, base + path, json=body, timeout=25)
    results.append({"method": method, "path": path, "status": response.status_code})
    response.raise_for_status()
    return response.json()

participant = call("POST", "/event/addPart", {"idPart": 999999, "nom": "DTO", "prenom": "Evidence", "tache": "INVITE"})
assert participant["idPart"] > 0 and participant["idPart"] != 999999
event = call("POST", f"/event/addEvent/{participant['idPart']}", {
    "idEvent": 999999, "cout": 99999, "description": prefix + "-by-id",
    "dateDebut": "2026-11-05", "dateFin": "2026-11-05"})
assert event["idEvent"] > 0 and event["idEvent"] != 999999 and event["cout"] == 0
second = call("POST", "/event/addEvent", {"description": prefix + "-references",
    "dateDebut": "2026-11-06", "dateFin": "2026-11-06",
    "participants": [{"idPart": participant["idPart"], "nom": "must-not-bind"}]})
assert second["idEvent"] > 0
item = call("PUT", "/event/addAffectLog/" + prefix + "-by-id", {
    "idLog": 999999, "description": "Chairs", "reserve": True, "prixUnit": 12.5, "quantite": 4})
assert item["idLog"] > 0 and item["idLog"] != 999999
assert item["description"] == "Chairs" and item["prixUnit"] == 12.5 and item["quantite"] == 4
role = call("GET", f"/taches/participants/{participant['idPart']}")
assert role["tache"] == "INVITE"
report = {"result": "PASS", "participantId": participant["idPart"], "eventIds": [event["idEvent"], second["idEvent"]],
          "logisticsId": item["idLog"], "operations": results,
          "note": "Real HTTP/MySQL calls; generated IDs and computed cost cannot be assigned by clients."}
(ROOT / "docs/evidence/12-final-validation/legacy-dto-api.json").write_text(json.dumps(report, indent=2), encoding="utf-8")
print(json.dumps(report, indent=2))
