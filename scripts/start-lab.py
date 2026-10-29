"""Start and configure the Windows/Docker lab; safe to rerun without deleting data."""
import argparse
import json
from pathlib import Path
import subprocess
import sys
import time
import urllib.error
import urllib.request

ROOT = Path(__file__).resolve().parents[1]


def run(*args):
    subprocess.run(args, cwd=ROOT, check=True)


def wait_ready(name, url, condition=lambda value: True):
    deadline = time.monotonic() + 900
    last_notice = 0
    while time.monotonic() < deadline:
        try:
            with urllib.request.urlopen(url, timeout=5) as response:
                body = response.read().decode()
                value = json.loads(body) if body.strip().startswith('{') else body
                if response.status == 200 and condition(value):
                    print(f'{name} is ready.', flush=True)
                    return
        except (OSError, urllib.error.URLError, ValueError):
            pass
        if time.monotonic() - last_notice > 30:
            print(f'Waiting for {name}...', flush=True)
            last_notice = time.monotonic()
        time.sleep(3)
    raise SystemExit(f'{name} did not become ready. Inspect docker compose logs; data was preserved.')


def powershell(script, *args):
    run('powershell', '-NoProfile', '-File', str(ROOT / 'scripts' / script), *args)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--kubernetes', action='store_true')
    parser.add_argument('--terraform', action='store_true', help='Also create the Terraform deployment')
    parser.add_argument('--accept-nexus-eula', action='store_true',
                        help='Use only after reviewing and agreeing to the Nexus CE license')
    args = parser.parse_args()
    run('docker', 'info', '--format', '{{.OSType}}')
    powershell('Initialize-Lab.ps1')
    network = subprocess.run(['docker', 'network', 'inspect', 'events-devops'],
                             stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    if network.returncode:
        run('docker', 'network', 'create', 'events-devops')
    run('docker', 'compose', '--progress', 'plain', '-f', 'compose.devops.yml', 'up', '-d', '--build')
    wait_ready('SonarQube', 'http://localhost:9000/api/system/status', lambda value: value.get('status') == 'UP')
    wait_ready('Nexus', 'http://localhost:8081/service/rest/v1/status')
    bootstrap = [sys.executable, 'scripts/bootstrap-services.py']
    if args.accept_nexus_eula:
        bootstrap.append('--accept-nexus-eula')
    run(*bootstrap)
    run('docker', 'compose', '-f', 'compose.devops.yml', 'up', '-d', '--force-recreate', 'jenkins')
    wait_ready('Jenkins', 'http://localhost:8082/login')
    run('docker', 'compose', '--progress', 'plain', 'up', '-d', '--build', '--wait', '--wait-timeout', '300')
    run('docker', 'compose', '-f', 'compose.monitoring.yml', 'up', '-d')
    run(sys.executable, 'scripts/smoke-test.py')
    if args.kubernetes or args.terraform:
        powershell('Install-LabTools.ps1')
        powershell('New-LabCluster.ps1')
        powershell('Deploy-LabKubernetes.ps1')
    if args.terraform:
        powershell('Invoke-LabTerraform.ps1', '-Action', 'Apply')
    print('Lab ready. Open README.md for URLs and private login instructions.')


if __name__ == '__main__':
    try:
        main()
    except subprocess.CalledProcessError as error:
        raise SystemExit(f'Lab stopped after a failed command (exit {error.returncode}). Existing data was preserved.') from None
