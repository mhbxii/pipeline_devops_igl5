"""Collect sanitized verification evidence; never export credentials or Terraform state."""
import base64
from datetime import datetime, timezone
import json
from pathlib import Path
import subprocess
import urllib.request
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
VALUES = dict(line.split('=', 1) for line in (ROOT / '.env').read_text().splitlines()
              if line.strip() and not line.startswith('#'))


def command(*args):
    return subprocess.check_output(args, cwd=ROOT, text=True, stderr=subprocess.DEVNULL).strip()


def api(url, password=None):
    headers = {}
    if password:
        headers['Authorization'] = 'Basic ' + base64.b64encode(('admin:' + password).encode()).decode()
    with urllib.request.urlopen(urllib.request.Request(url, headers=headers), timeout=15) as response:
        return json.load(response)


def collect():
    result = {'observedAtUtc': datetime.now(timezone.utc).isoformat()}
    checks = {
        'gitCommit': lambda: command('git', 'rev-parse', 'HEAD'),
        'gitBranch': lambda: command('git', 'branch', '--show-current'),
        'containers': lambda: command('docker', 'ps', '--format', '{{.Names}} | {{.Status}} | {{.Ports}}').splitlines(),
        'composeHealth': lambda: api('http://localhost:8089/events/actuator/health'),
        'kubernetesHealth': lambda: api('http://localhost:8090/events/actuator/health'),
        'terraformHealth': lambda: api('http://localhost:8091/events/actuator/health'),
        'jenkins': lambda: api('http://localhost:8082/job/logistics-local/lastBuild/api/json?tree=number,result,building,url', VALUES['JENKINS_ADMIN_PASSWORD']),
        'sonar': lambda: api('http://localhost:9000/api/measures/component?component=events-logistics&metricKeys=coverage,line_coverage,branch_coverage,alert_status,ncloc', VALUES['SONAR_ADMIN_PASSWORD']),
        'nexus': lambda: [{
            'name': item['name'], 'version': item['version'],
            'assets': [{'path': asset['path'], 'checksum': asset.get('checksum', {})} for asset in item['assets']]
        } for item in api('http://localhost:8081/service/rest/v1/search?repository=events-snapshots&group=tn.esprit&name=eventsProject', VALUES['NEXUS_PASSWORD'])['items']],
        'prometheus': lambda: [{
            'job': target['labels']['job'], 'health': target['health'], 'scrapeUrl': target['scrapeUrl']
        } for target in api('http://localhost:9090/api/v1/targets')['data']['activeTargets']],
        'grafana': lambda: {
            key: value for key, value in api('http://localhost:3000/api/dashboards/uid/events-logistics', VALUES['GRAFANA_PASSWORD'])['dashboard'].items()
            if key in ('uid', 'title', 'version')},
        'terraformResources': lambda: command(str(ROOT / '.local/bin/terraform.exe'), '-chdir=infra', 'state', 'list').splitlines(),
        'kubernetesResources': lambda: command('kubectl', '--kubeconfig', str(ROOT / '.local/kube/host-config'), '-n', 'events', 'get', 'deploy,sts,svc,pvc').splitlines(),
        'kubernetesImage': lambda: command('kubectl', '--kubeconfig', str(ROOT / '.local/kube/host-config'), '-n', 'events', 'get', 'deployment', 'events-app', '-o', 'jsonpath={.spec.template.spec.containers[0].image}'),
    }
    for name, action in checks.items():
        try:
            result[name] = action()
        except Exception as error:
            result[name] = {'unavailable': type(error).__name__}
    image = result.get('kubernetesImage', '')
    if isinstance(image, str) and image.startswith('events-logistics:'):
        published = VALUES.get('DOCKERHUB_USERNAME', 'arfaoui131') + '/' + image
        try:
            result['dockerHub'] = {'image': published, 'digests': json.loads(command(
                'docker', 'image', 'inspect', published, '--format', '{{json .RepoDigests}}'))}
        except Exception as error:
            result['dockerHub'] = {'unavailable': type(error).__name__}
    recovery = ROOT / '.local/recovery-evidence.json'
    if recovery.exists():
        result['recovery'] = json.loads(recovery.read_text())
    reports = list((ROOT / 'target/surefire-reports').glob('TEST-*.xml'))
    result['tests'] = {'suites': len(reports), 'tests': 0, 'failures': 0, 'errors': 0, 'skipped': 0}
    for path in reports:
        suite = ET.parse(path).getroot()
        for field in ('tests', 'failures', 'errors', 'skipped'):
            result['tests'][field] += int(suite.get(field, '0'))
    coverage = ROOT / 'target/site/jacoco/jacoco.xml'
    if coverage.exists():
        for cls in ET.parse(coverage).getroot().iter('class'):
            if cls.get('name', '').endswith('/LogisticsService'):
                result['logisticsCoverage'] = {
                    counter.get('type'): {'covered': int(counter.get('covered')), 'missed': int(counter.get('missed'))}
                    for counter in cls.findall('counter')}
    path = ROOT / '.local/evidence.json'
    path.write_text(json.dumps(result, indent=2) + '\n')
    print('Sanitized evidence saved to .local/evidence.json.')
    print('Unavailable checks:', ', '.join(name for name, value in result.items()
                                          if isinstance(value, dict) and 'unavailable' in value) or 'none')


if __name__ == '__main__':
    collect()
