"""Demonstrate app replacement and MySQL PVC persistence in the local events namespace."""
import json
from pathlib import Path
import subprocess
import time
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
KUBECTL = ['kubectl', '--kubeconfig', str(ROOT / '.local/kube/host-config'), '-n', 'events']


def query(*args):
    return json.loads(subprocess.check_output(KUBECTL + list(args) + ['-o', 'json'],
                                              stderr=subprocess.DEVNULL))


def ready(selector, count):
    deadline = time.monotonic() + 240
    while time.monotonic() < deadline:
        pods = query('get', 'pods', '-l', selector)['items']
        if len(pods) == count and all(any(
                condition['type'] == 'Ready' and condition['status'] == 'True'
                for condition in pod.get('status', {}).get('conditions', [])) for pod in pods):
            return [pod['metadata']['uid'] for pod in pods]
        time.sleep(3)
    raise AssertionError(f'Pods did not recover: {selector}')


def stored_ids():
    with urllib.request.urlopen('http://localhost:8090/events/event/getLogs/2026-10-29/2026-10-29', timeout=30) as response:
        return {item['idLog'] for item in json.load(response)}


def main():
    before = stored_ids()
    assert before, 'Run the Kubernetes smoke test first to create demonstration data'
    app_before = ready('app=events-app', 2)
    selected = query('get', 'pods', '-l', 'app=events-app')['items'][0]['metadata']['name']
    subprocess.run(KUBECTL + ['delete', 'pod', selected], check=True)
    app_after = ready('app=events-app', 2)
    assert set(app_before) != set(app_after)
    assert before.issubset(stored_ids())
    print('PASS: application pod replaced; two replicas ready; saved logistics preserved.', flush=True)
    pvc_before = query('get', 'pvc', 'data-mysql-0')['metadata']['uid']
    mysql_before = ready('app=mysql', 1)
    subprocess.run(KUBECTL + ['delete', 'pod', 'mysql-0'], check=True)
    mysql_after = ready('app=mysql', 1)
    ready('app=events-app', 2)
    assert mysql_before != mysql_after
    assert pvc_before == query('get', 'pvc', 'data-mysql-0')['metadata']['uid']
    assert before.issubset(stored_ids())
    report = {'applicationPodReplaced': True, 'mysqlPodReplaced': True,
              'persistentVolumeClaimPreserved': True, 'logisticsIdsPreserved': sorted(before)}
    (ROOT / '.local/recovery-evidence.json').write_text(json.dumps(report, indent=2) + '\n')
    print('PASS: MySQL pod replaced; same PVC; saved logistics preserved.')


if __name__ == '__main__':
    main()
