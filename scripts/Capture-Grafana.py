"""Capture the authenticated live dashboard without altering page contents."""
from playwright.sync_api import sync_playwright
from local_ci import ROOT, environment

with sync_playwright() as p:
    browser = p.chromium.launch(channel="msedge", headless=True)
    page = browser.new_page(viewport={"width": 1600, "height": 1000}, device_scale_factor=1)
    page.goto("http://localhost:13000/login", wait_until="domcontentloaded")
    page.get_by_role("textbox", name="Email or username").fill("tache-admin")
    page.get_by_role("textbox", name="Password", exact=True).fill(environment()["GRAFANA_ADMIN_PASSWORD"])
    page.get_by_role("button", name="Log in", exact=True).click()
    page.wait_for_url(lambda url: "/login" not in url)
    page.goto("http://localhost:13000/d/events-tache/?orgId=1&from=now-30m&to=now&kiosk", wait_until="domcontentloaded")
    page.get_by_text("Collecte Prometheus (up)", exact=True).wait_for(timeout=120000)
    page.get_by_text("UP", exact=True).wait_for(timeout=60000)
    page.wait_for_timeout(3000)
    page.screenshot(path=str(ROOT / "docs/evidence/13-monitoring/grafana-dashboard.png"), full_page=True)
    browser.close()
print("Captured the live provisioned Grafana dashboard.")
