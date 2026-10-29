"""Configure the local SonarQube/Nexus lab without printing credentials."""
import argparse
import base64
import json
from pathlib import Path
import subprocess
import urllib.error
import urllib.parse
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
ENV = ROOT / '.env'


def read_env():
    return dict(line.split('=', 1) for line in ENV.read_text().splitlines()
                if line.strip() and not line.startswith('#'))


def save_env(values):
    ENV.write_text(''.join(f'{key}={value}\n' for key, value in values.items()))


def request(url, user, password, method='GET', data=None, form=None, raw=None):
    headers = {'Authorization': 'Basic ' + base64.b64encode(
        f'{user}:{password}'.encode()).decode()}
    body = None
    if data is not None:
        body = json.dumps(data).encode()
        headers['Content-Type'] = 'application/json'
    if form is not None:
        body = urllib.parse.urlencode(form).encode()
        headers['Content-Type'] = 'application/x-www-form-urlencoded'
    if raw is not None:
        body = raw.encode()
        headers['Content-Type'] = 'text/plain'
    req = urllib.request.Request(url, data=body, headers=headers, method=method)
    with urllib.request.urlopen(req, timeout=30) as response:
        text = response.read().decode()
        return json.loads(text) if text else None


def sonar(values):
    base = 'http://localhost:9000/api/'
    password = values['SONAR_ADMIN_PASSWORD']
    try:
        valid = request(base + 'authentication/validate', 'admin', password)['valid']
    except urllib.error.HTTPError as error:
        if error.code != 401:
            raise
        valid = False
    if not valid:
        request(base + 'users/change_password', 'admin', 'admin', 'POST', form={
            'login': 'admin', 'previousPassword': 'admin', 'password': password})
    projects = request(base + 'projects/search?projects=events-logistics', 'admin', password)
    if not projects['components']:
        request(base + 'projects/create', 'admin', password, 'POST', form={
            'project': 'events-logistics', 'name': 'Events - Logistics', 'visibility': 'private'})
    token = values.get('SONAR_TOKEN')
    if not token or not request(base + 'authentication/validate', token, '')['valid']:
        # Revoke only the token owned by this bootstrap script before regenerating.
        request(base + 'user_tokens/revoke', 'admin', password, 'POST', form={'name': 'logistics-jenkins'})
        result = request(base + 'user_tokens/generate', 'admin', password, 'POST',
                         form={'name': 'logistics-jenkins', 'type': 'PROJECT_ANALYSIS_TOKEN',
                               'projectKey': 'events-logistics'})
        values['SONAR_TOKEN'] = result['token']
        save_env(values)
    print('SonarQube: admin password updated, private project and analysis token ready.')


def nexus(values):
    base = 'http://localhost:8081/service/rest/v1/'
    password = values['NEXUS_PASSWORD']
    try:
        repositories = request(base + 'repositories', 'admin', password)
    except urllib.error.HTTPError as error:
        if error.code not in (401, 403):
            raise
        initial = subprocess.check_output(
            ['docker', 'compose', '-f', 'compose.devops.yml', 'exec', '-T', 'nexus',
             'cat', '/nexus-data/admin.password'], cwd=ROOT, text=True).strip()
        request(base + 'security/users/admin/change-password', 'admin', initial,
                'PUT', raw=password)
        repositories = request(base + 'repositories', 'admin', password)
    existing = {repo['name'] for repo in repositories}
    for name, policy in [('events-releases', 'RELEASE'), ('events-snapshots', 'SNAPSHOT')]:
        if name not in existing:
            request(base + 'repositories/maven/hosted', 'admin', password, 'POST', data={
                'name': name, 'online': True,
                'storage': {'blobStoreName': 'default', 'strictContentTypeValidation': True,
                            'writePolicy': 'ALLOW' if policy == 'SNAPSHOT' else 'ALLOW_ONCE'},
                'maven': {'versionPolicy': policy, 'layoutPolicy': 'STRICT',
                          'contentDisposition': 'ATTACHMENT'}})
    request(base + 'security/anonymous', 'admin', password, 'PUT', data={
        'enabled': False, 'userId': 'anonymous', 'realmName': 'NexusAuthorizingRealm'})
    print('Nexus: authenticated snapshot/release Maven repositories ready.')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('service', choices=['sonar', 'nexus', 'all'], default='all', nargs='?')
    args = parser.parse_args()
    values = read_env()
    for name, action in [('sonar', sonar), ('nexus', nexus)]:
        if args.service in (name, 'all'):
            action(values)


if __name__ == '__main__':
    try:
        main()
    except urllib.error.HTTPError as error:
        # Never dump request bodies: they may contain passwords or tokens.
        raise SystemExit(f'Service bootstrap HTTP {error.code}; check readiness and local credentials.') from None
