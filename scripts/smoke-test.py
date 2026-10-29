"""Exercise real HTTP and persistence paths; creates one named demonstration event."""
import argparse
import json
import urllib.error
import urllib.request
import uuid


def call(base, path, method='GET', payload=None, expected=200):
    body = json.dumps(payload).encode() if payload is not None else None
    req = urllib.request.Request(base + path, data=body, method=method,
                                 headers={'Content-Type': 'application/json'})
    try:
        with urllib.request.urlopen(req, timeout=30) as response:
            status, result = response.status, response.read()
    except urllib.error.HTTPError as error:
        status, result = error.code, error.read()
    if status != expected:
        raise AssertionError(f'{method} {path}: expected {expected}, received {status}')
    return json.loads(result) if result else None


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--base-url', default='http://localhost:8089/events')
    args = parser.parse_args()
    base = args.base_url.rstrip('/')
    assert call(base, '/actuator/health')['status'] == 'UP'
    name = 'logistics-smoke-' + uuid.uuid4().hex[:10]
    call(base, '/event/addEvent', 'POST', {
        'description': name, 'dateDebut': '2026-10-29', 'dateFin': '2026-10-30', 'participants': []})
    item = {'description': 'Demo projector', 'reserve': True, 'prixUnit': 25, 'quantite': 2}
    saved = call(base, '/event/addAffectLog/' + name, 'PUT', item)
    assert saved['idLog'] > 0
    found = call(base, '/event/getLogs/2026-10-29/2026-10-29')
    assert saved['idLog'] in [entry['idLog'] for entry in found]
    call(base, '/event/addAffectLog/missing-' + uuid.uuid4().hex, 'PUT', item, expected=404)
    call(base, '/event/addAffectLog/' + name, 'PUT', dict(item, quantite=0), expected=400)
    call(base, '/event/getLogs/2026-10-30/2026-10-29', expected=400)
    call(base, '/event/getLogs/invalid/2026-10-29', expected=400)
    print(f'PASS {base}: health, persisted assignment, date lookup, 404 and 400 responses. Event: {name}')


if __name__ == '__main__':
    main()
