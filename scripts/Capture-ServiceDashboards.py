"""Capture actual local service pages using the already-installed Edge/Playwright.

Does not modify page content. Services/port-forwards must already be running.
"""
from pathlib import Path
import re
from playwright.sync_api import sync_playwright

root = Path(__file__).resolve().parents[1]
with sync_playwright() as p:
    browser = p.chromium.launch(channel="msedge", headless=True)
    page = browser.new_page(viewport={"width": 1440, "height": 900}, device_scale_factor=1)
    page.goto("http://localhost:19090/targets", wait_until="domcontentloaded")
    page.wait_for_timeout(5000)
    page.screenshot(path=str(root / "docs/evidence/13-monitoring/prometheus-targets.png"), clip={"x": 0, "y": 0, "width": 1440, "height": 400})
    print("Captured actual Prometheus target dashboard.")
    page.goto("http://localhost:8081/#browse/browse:maven-snapshots", wait_until="domcontentloaded")
    page.wait_for_timeout(2000)
    if page.get_by_label("Username", exact=True).is_visible():
        values = dict(line.split("=", 1) for line in (root / ".env").read_text().splitlines() if "=" in line)
        page.get_by_label("Username", exact=True).fill("tache-publisher")
        page.get_by_label("Password", exact=True).fill(values["NEXUS_PASSWORD"])
        page.get_by_role("button", name="Log in", exact=True).click()
    page.wait_for_timeout(5000)
    page.get_by_text("1.0.0-SNAPSHOT", exact=True).dblclick()
    page.wait_for_timeout(1000)
    page.get_by_text(re.compile(r"^1\.0\.0-\d{8}\.\d{6}-\d+$")).first.dblclick()
    page.wait_for_timeout(800)
    page.get_by_text(re.compile(r"\.jar$")).first.click()
    page.wait_for_timeout(800)
    page.screenshot(path=str(root / "docs/evidence/07-nexus/nexus-repository.png"), clip={"x": 0, "y": 0, "width": 1440, "height": 650})
    print("Captured actual Nexus repository page.")
    browser.close()
