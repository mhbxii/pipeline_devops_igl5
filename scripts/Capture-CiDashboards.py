"""Capture real authenticated CI dashboards without exposing credentials."""
import argparse
from local_ci import ROOT, environment
from playwright.sync_api import sync_playwright

parser = argparse.ArgumentParser()
parser.add_argument("--build", type=int, default=4)
args = parser.parse_args()
values = environment()
with sync_playwright() as p:
    browser = p.chromium.launch(channel="msedge", headless=True)
    page = browser.new_page(viewport={"width": 1800, "height": 1000}, device_scale_factor=1)
    page.goto("http://localhost:9000/dashboard?id=events-tache", wait_until="domcontentloaded")
    page.wait_for_timeout(2000)
    if page.locator('input[name="login"]').is_visible():
        page.locator('input[name="login"]').fill("admin")
        page.locator('input[type="password"]').fill(values["SONAR_ADMIN_PASSWORD"])
        page.locator('button[type="submit"]').click()
    page.wait_for_timeout(4000)
    page.goto("http://localhost:9000/dashboard?id=events-tache", wait_until="domcontentloaded")
    page.wait_for_timeout(2500)
    page.screenshot(path=str(ROOT / "docs/evidence/06-sonarqube/sonar-final-new-code.png"), full_page=True)
    overall = page.get_by_text("Overall Code", exact=True)
    if overall.count():
        overall.first.click()
        page.wait_for_timeout(1500)
    page.screenshot(path=str(ROOT / "docs/evidence/06-sonarqube/sonar-final-dashboard.png"), full_page=True)
    print("Captured actual SonarQube dashboard.")
    page.goto("http://localhost:8080/login", wait_until="domcontentloaded")
    if page.locator('input[name="j_username"]').is_visible():
        page.locator('input[name="j_username"]').fill("tache-admin")
        page.locator('input[name="j_password"]').fill(values["JENKINS_ADMIN_PASSWORD"])
        page.locator('button[type="submit"]').click()
    page.wait_for_timeout(1500)
    page.set_viewport_size({"width": 2400, "height": 1000})
    page.goto("http://localhost:8080/job/tache-ci/", wait_until="domcontentloaded")
    page.wait_for_timeout(3000)
    page.locator('.cbwf-stage-view').screenshot(path=str(ROOT / "docs/evidence/08-jenkins/jenkins-pipeline-overview.png"))
    page.set_viewport_size({"width": 1800, "height": 1000})
    page.goto(f"http://localhost:8080/job/tache-ci/{args.build}/testReport/", wait_until="domcontentloaded")
    page.wait_for_timeout(1500)
    page.screenshot(path=str(ROOT / "docs/evidence/08-jenkins/jenkins-tests.png"), full_page=True)
    print("Captured actual Jenkins pipeline and JUnit report.")
    browser.close()
