"""Validate or run the local Jenkins pipeline with private .env credentials."""
import argparse
import base64
import http.cookiejar
import json
from pathlib import Path
import time
import urllib.error
import urllib.parse
import urllib.request
import uuid

ROOT = Path(__file__).resolve().parents[1]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--lint', action='store_true')
    parser.add_argument('--push', action='store_true')
    parser.add_argument('--deploy', action='store_true')
    parser.add_argument('--wait', action='store_true')
    args = parser.parse_args()
    values = dict(line.split('=', 1) for line in (ROOT / '.env').read_text().splitlines()
                  if line.strip() and not line.startswith('#'))
    auth = base64.b64encode(('admin:' + values['JENKINS_ADMIN_PASSWORD']).encode()).decode()
    opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))
    headers = {'Authorization': 'Basic ' + auth}
    base = 'http://localhost:8080'

    def request(path, data=None, content_type=None):
        selected = dict(headers)
        if content_type:
            selected['Content-Type'] = content_type
        return opener.open(urllib.request.Request(base + path, data=data, headers=selected), timeout=30)

    with request('/crumbIssuer/api/json') as response:
        crumb = json.load(response)
    headers[crumb['crumbRequestField']] = crumb['crumb']
    if args.lint:
        boundary = uuid.uuid4().hex
        body = (f'--{boundary}\r\nContent-Disposition: form-data; name="jenkinsfile"; filename="Jenkinsfile"\r\n'
                'Content-Type: text/plain\r\n\r\n').encode() + (ROOT / 'Jenkinsfile').read_bytes()
        body += f'\r\n--{boundary}--\r\n'.encode()
        with request('/pipeline-model-converter/validate', body, 'multipart/form-data; boundary=' + boundary) as response:
            result = response.read().decode()
        print(result.strip())
        if 'successfully validated' not in result:
            raise SystemExit(1)
        return
    parameters = urllib.parse.urlencode({
        'SOURCE_MODE': 'LOCAL', 'BUILD_BRANCH': 'logistics', 'RUN_SONAR': 'true',
        'PUBLISH_NEXUS': 'true', 'BUILD_IMAGE': 'true',
        'PUSH_IMAGE': str(args.push).lower(), 'DEPLOY_KUBERNETES': str(args.deploy).lower(),
        'IMAGE_NAMESPACE': values.get('DOCKERHUB_USERNAME', 'arfaoui131')})
    try:
        response = request('/job/logistics-local/buildWithParameters', parameters.encode(),
                           'application/x-www-form-urlencoded')
    except urllib.error.HTTPError as error:
        if error.code != 400 or args.push or args.deploy:
            raise
        # Declarative parameter properties are installed by the first run.
        response = request('/job/logistics-local/build', b'')
    queue_path = urllib.parse.urlsplit(response.headers['Location']).path
    response.close()
    print('Jenkins build queued.')
    if not args.wait:
        return
    deadline = time.monotonic() + 3600
    number = None
    while time.monotonic() < deadline:
        with request(queue_path + 'api/json') as response:
            queue = json.load(response)
        if queue.get('cancelled'):
            raise SystemExit('Jenkins build was cancelled.')
        if queue.get('executable'):
            number = queue['executable']['number']
            break
        time.sleep(3)
    if number is None:
        raise SystemExit('Jenkins queue timed out.')
    print(f'Jenkins build #{number} started.', flush=True)
    while time.monotonic() < deadline:
        with request(f'/job/logistics-local/{number}/api/json?tree=number,result,building,url') as response:
            build = json.load(response)
        if not build['building']:
            (ROOT / '.local/jenkins-last-build.json').write_text(json.dumps(build, indent=2))
            print(f'Jenkins build #{number}: {build["result"]}')
            raise SystemExit(0 if build['result'] == 'SUCCESS' else 1)
        time.sleep(10)
    raise SystemExit('Jenkins build wait timed out; check the running job in the UI.')


if __name__ == '__main__':
    try:
        main()
    except urllib.error.HTTPError as error:
        raise SystemExit(f'Jenkins HTTP {error.code}; check service readiness and .env credentials.') from None
