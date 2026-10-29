"""Capture actual local service pages using the already-installed Edge/Playwright.

Does not modify page content. Services/port-forwards must already be running.
"""
from pathlib import Path
from playwright.sync_api import sync_playwright

root = Path(__file__).resolve().parents[1]
with sync_playwright() as p:
    browser = p.chromium.launch(channel="msedge", headless=True)
    page = browser.new_page(viewport={"width": 1440, "height": 900}, device_scale_factor=1)
    page.goto("http://localhost:19090/targets", wait_until="domcontentloaded")
    page.wait_for_timeout(5000)
    page.screenshot(path=str(root / "docs/evidence/13-monitoring/prometheus-targets.png"), clip={"x": 0, "y": 0, "width": 1440, "height": 400})
    print("Captured actual Prometheus target dashboard.")
    browser.close()
