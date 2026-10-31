"""Capture the authenticated live dashboard without altering page contents."""
import argparse
from playwright.sync_api import sync_playwright
from local_ci import ROOT, environment

parser = argparse.ArgumentParser()
parser.add_argument('--eks', action='store_true')
parser.add_argument('--output-dir', help='Repository-relative evidence directory; preserves historical captures')
args = parser.parse_args()
base = 'http://localhost:13100' if args.eks else 'http://localhost:13000'
secret_key = 'EKS_GRAFANA_ADMIN_PASSWORD' if args.eks else 'GRAFANA_ADMIN_PASSWORD'
folder = '14-aws-eks' if args.eks else '13-monitoring'
output = ROOT / args.output_dir if args.output_dir else ROOT / 'docs/evidence' / folder
output.mkdir(parents=True, exist_ok=True)

with sync_playwright() as p:
    browser = p.chromium.launch(channel="msedge", headless=True)
    page = browser.new_page(viewport={"width": 1600, "height": 1000}, device_scale_factor=1)
    page.goto(base + '/login', wait_until="domcontentloaded")
    page.get_by_role("textbox", name="Email or username").fill("tache-admin")
    page.get_by_role("textbox", name="Password", exact=True).fill(environment()[secret_key])
    page.get_by_role("button", name="Log in", exact=True).click()
    page.wait_for_url(lambda url: "/login" not in url)
    page.goto(base + '/d/events-tache/?orgId=1&from=now-30m&to=now&kiosk', wait_until="domcontentloaded")
    page.get_by_text("Collecte Prometheus (up)", exact=True).wait_for(timeout=120000)
    page.get_by_text("UP", exact=True).wait_for(timeout=60000)
    page.wait_for_timeout(3000)
    page.screenshot(path=str(output / 'grafana-dashboard.png'), full_page=True)
    browser.close()
print("Captured the live provisioned Grafana dashboard.")
