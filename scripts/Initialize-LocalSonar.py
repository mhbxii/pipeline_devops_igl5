"""Initialize the project's own local SonarQube; never print a token."""
from local_ci import ROOT, environment, ensure_secret, save_secret
import requests

url = "http://localhost:9000"
status = requests.get(url + "/api/system/status", timeout=15)
status.raise_for_status()
if status.json()["status"] != "UP":
    raise SystemExit("SonarQube is not ready yet")
values = environment()
session = requests.Session()
if not values.get("SONAR_ADMIN_PASSWORD"):
    session.auth = ("admin", "admin")
    # Persist only after the server confirms the change.
    import secrets
    password = secrets.token_urlsafe(32)
    response = session.post(url + "/api/users/change_password", data={
        "login": "admin", "previousPassword": "admin", "password": password}, timeout=20)
    response.raise_for_status()
    save_secret("SONAR_ADMIN_PASSWORD", password)
else:
    password = values["SONAR_ADMIN_PASSWORD"]
session.auth = ("admin", password)
response = session.get(url + "/api/projects/search", params={"projects": "events-tache"}, timeout=20)
response.raise_for_status()
if not response.json()["components"]:
    response = session.post(url + "/api/projects/create", data={
        "project": "events-tache", "name": "eventsProject - Tache", "visibility": "private"}, timeout=20)
    response.raise_for_status()
if not environment().get("SONAR_TOKEN"):
    response = session.post(url + "/api/user_tokens/generate", data={
        "name": "events-tache-local", "type": "PROJECT_ANALYSIS_TOKEN", "projectKey": "events-tache"}, timeout=20)
    response.raise_for_status()
    save_secret("SONAR_TOKEN", response.json()["token"])
webhook_secret = ensure_secret("SONAR_WEBHOOK_SECRET")
response = session.get(url + "/api/webhooks/list", params={"project": "events-tache"}, timeout=20)
response.raise_for_status()
if not any(h["name"] == "local-jenkins" for h in response.json()["webhooks"]):
    response = session.post(url + "/api/webhooks/create", data={
        "project": "events-tache", "name": "local-jenkins",
        "url": "http://jenkins:8080/sonarqube-webhook/", "secret": webhook_secret}, timeout=20)
    response.raise_for_status()
response = session.post(url + "/api/settings/set", data={
    "key": "sonar.core.serverBaseURL", "value": url}, timeout=20)
response.raise_for_status()
(ROOT / "docs/evidence/06-sonarqube/sonar-server-status.txt").write_text(
    "SonarQube status=UP\nProject=events-tache\nPrivate project; token stored in ignored .env\n"
    "Jenkins webhook configured with a shared secret\n", encoding="utf-8")
print("Initialized local SonarQube project and authenticated Jenkins webhook. No analysis claimed yet.")
