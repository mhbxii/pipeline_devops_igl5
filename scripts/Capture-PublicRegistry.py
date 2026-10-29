"""Capture the real public Docker Hub tag page after anonymous verification."""
import json
from playwright.sync_api import sync_playwright
from local_ci import ROOT

proof = json.loads((ROOT / "docs/evidence/09-docker/dockerhub-verification.json").read_text())
tag = proof["image"].rsplit(":", 1)[1]
with sync_playwright() as p:
    browser = p.chromium.launch(channel="msedge", headless=True)
    page = browser.new_page(viewport={"width": 1600, "height": 1000}, device_scale_factor=1)
    page.goto(proof["url"] + "/tags?name=" + tag, wait_until="domcontentloaded", timeout=90000)
    page.get_by_text(tag, exact=True).first.wait_for(timeout=90000)
    page.wait_for_timeout(2000)
    page.screenshot(path=str(ROOT / "docs/evidence/09-docker/dockerhub-repository.png"), full_page=True)
    browser.close()
print("Captured actual public Docker Hub tag page.")
