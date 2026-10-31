"""Capture genuine EKS service pages via authenticated local port-forwards."""
import argparse
from playwright.sync_api import sync_playwright
from local_ci import ROOT, environment

parser = argparse.ArgumentParser()
parser.add_argument('--build', type=int, required=True)
args = parser.parse_args()
out = ROOT / 'docs/evidence/17-terraform-pipeline'
out.mkdir(parents=True, exist_ok=True)
with sync_playwright() as p:
    browser = p.chromium.launch(channel='msedge', headless=True)
    page = browser.new_page(viewport={'width': 1800, 'height': 1000}, device_scale_factor=1)
    page.goto('http://localhost:19190/targets', wait_until='domcontentloaded')
    page.wait_for_timeout(4000)
    page.screenshot(path=str(out / 'prometheus-targets.png'), full_page=True)
    page.goto('http://localhost:8080/login', wait_until='domcontentloaded')
    page.locator('input[name="j_username"]').fill('tache-admin')
    page.locator('input[name="j_password"]').fill(environment()['JENKINS_ADMIN_PASSWORD'])
    page.locator('button[type="submit"]').click()
    page.wait_for_timeout(1000)
    page.goto('http://localhost:8080/job/tache-ci/', wait_until='domcontentloaded')
    page.wait_for_timeout(3500)
    page.screenshot(path=str(out / 'jenkins-eks-pipeline.png'), full_page=True)
    page.set_viewport_size({'width': 3000, 'height': 1100})
    page.locator('.cbwf-stage-view').screenshot(path=str(out / 'jenkins-eks-stages.png'))
    page.goto(f'http://localhost:8080/job/tache-ci/{args.build}/', wait_until='domcontentloaded')
    page.wait_for_timeout(1000)
    page.screenshot(path=str(out / 'jenkins-eks-build.png'), full_page=True)
    browser.close()
print('Captured actual EKS Prometheus and Jenkins pages, including a readable stage view.')
