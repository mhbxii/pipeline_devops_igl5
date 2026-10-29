"""Shared helpers for local lab services. Secrets remain in ignored .env."""
import base64
import json
import secrets
from pathlib import Path
import requests

ROOT = Path(__file__).resolve().parents[1]


def environment():
    return dict(line.split("=", 1) for line in (ROOT / ".env").read_text(encoding="utf-8-sig").splitlines()
                if "=" in line and not line.startswith("#"))


def ensure_secret(key):
    values = environment()
    if not values.get(key):
        value = secrets.token_urlsafe(32)
        with (ROOT / ".env").open("a", encoding="ascii") as f:
            f.write(f"\n{key}={value}\n")
        return value
    return values[key]


def save_secret(key, value):
    if key in environment():
        raise RuntimeError(f"Refusing to overwrite {key}; review local credentials first")
    with (ROOT / ".env").open("a", encoding="ascii") as f:
        f.write(f"\n{key}={value}\n")


class Jenkins:
    def __init__(self):
        self.url = "http://localhost:8080"
        self.session = requests.Session()
        self.session.auth = ("tache-admin", environment()["JENKINS_ADMIN_PASSWORD"])
        response = self.session.get(self.url + "/crumbIssuer/api/json", timeout=20)
        response.raise_for_status()
        crumb = response.json()
        self.session.headers[crumb["crumbRequestField"]] = crumb["crumb"]

    def groovy(self, script, payload):
        encoded = base64.b64encode(json.dumps(payload).encode()).decode()
        prelude = "def input = new groovy.json.JsonSlurper().parseText(new String('" + encoded + "'.decodeBase64(), 'UTF-8'))\n"
        response = self.session.post(self.url + "/scriptText", data={"script": prelude + script}, timeout=40)
        text = response.text.replace(encoded, "[redacted payload]")
        for key, value in payload.items():
            if isinstance(value, str) and value and any(word in key.upper() for word in ("PASSWORD", "TOKEN", "SECRET")):
                text = text.replace(value, "[redacted]")
        for key, value in environment().items():
            if value and ("PASSWORD" in key or "TOKEN" in key or "SECRET" in key):
                text = text.replace(value, "[redacted]")
        if response.status_code != 200 or "Exception:" in text or "Error:" in text:
            raise RuntimeError("Jenkins configuration failed: " + text[-1200:])
        return text
