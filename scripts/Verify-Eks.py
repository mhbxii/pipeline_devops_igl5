"""Verify the real EKS release and collect public, credential-free evidence."""
import argparse
import hashlib
import json
import subprocess
from datetime import datetime, timezone
import requests
from local_ci import ROOT, Jenkins, environment

parser = argparse.ArgumentParser()
parser.add_argument('--build', type=int, required=True, help='Successful unified tache-ci build deployed to EKS')
args = parser.parse_args()
api = Jenkins()
response = api.session.get(f'{api.url}/job/tache-ci/{args.build}/api/json', timeout=30)
response.raise_for_status()
build = response.json()
assert build['result'] == 'SUCCESS' and not build['building'], 'Upstream build must be complete and successful'
revisions = [a['lastBuiltRevision']['SHA1'] for a in build.get('actions', []) if a.get('lastBuiltRevision')]
assert revisions, 'Missing SCM revision in upstream build'
expected_image = f'ayyoub1/events-tache:tache-{revisions[-1][:12]}-{args.build}'
jars = [a for a in build['artifacts'] if a['relativePath'].endswith('.jar')]
assert len(jars) == 1, 'Expected one archived application JAR'
response = api.session.get(f"{api.url}/job/tache-ci/{args.build}/artifact/{jars[0]['relativePath']}", timeout=90)
response.raise_for_status()
expected_jar_hash = hashlib.sha256(response.content).hexdigest()
OUT = ROOT / 'docs/evidence/15-eks-reactivation'
OUT.mkdir(parents=True, exist_ok=True)
K = [str(ROOT / '.local/tools/kubectl-eks.exe'), '--kubeconfig',
     str(ROOT / '.local/kubeconfig-eks'), '--context', 'events-tache-eks']


def kubectl(*args):
    return subprocess.check_output(K + list(args), text=True)


def get(path):
    response = requests.get('http://localhost:18189/events' + path, timeout=30)
    response.raise_for_status()
    return response.json()


health = get('/actuator/health/readiness')
assert health['status'] == 'UP'
nodes = json.loads(kubectl('get', 'nodes', '-o', 'json'))['items']
assert len(nodes) == 1
assert any(c['type'] == 'Ready' and c['status'] == 'True' for c in nodes[0]['status']['conditions'])
node = nodes[0]
assert node['metadata']['labels']['node.kubernetes.io/instance-type'] == 't3.medium'
assert node['status']['nodeInfo']['kubeletVersion'].startswith('v1.36.')
resources = []
volumes = []
volume_ids = []
for ns in ['events-tache', 'events-monitoring', 'events-grafana']:
    for d in json.loads(kubectl('-n', ns, 'get', 'deployments', '-o', 'json'))['items']:
        assert d['status'].get('readyReplicas', 0) == d['spec']['replicas'] == 1
        resources.append({'namespace': ns, 'deployment': d['metadata']['name'], 'ready': 1,
                          'images': [c['image'] for c in d['spec']['template']['spec']['containers']]})
    for pvc in json.loads(kubectl('-n', ns, 'get', 'pvc', '-o', 'json'))['items']:
        assert pvc['status']['phase'] == 'Bound'
        assert pvc['spec']['storageClassName'] == 'events-gp3'
        pv = json.loads(kubectl('get', 'pv', pvc['spec']['volumeName'], '-o', 'json'))
        assert pv['spec']['persistentVolumeReclaimPolicy'] == 'Delete'
        volume_ids.append(pv['spec']['csi']['volumeHandle'])
        volumes.append({'namespace': ns, 'claim': pvc['metadata']['name'],
                        'size': pvc['status']['capacity']['storage'], 'status': 'Bound',
                        'storageClass': 'events-gp3'})
assert len(resources) == 4 and len(volumes) == 3
ebs = json.loads(subprocess.check_output(['aws', '--profile', 'events-tache-lab',
    '--region', 'us-east-1', 'ec2', 'describe-volumes', '--volume-ids'] + volume_ids, text=True))['Volumes']
assert len(ebs) == 3 and all(v['Encrypted'] and v['VolumeType'] == 'gp3' for v in ebs)
(ROOT / '.local/aws-preflight/workload-volume-ids.json').write_text(json.dumps(volume_ids), encoding='utf-8')
image = next(r for r in resources if r['deployment'] == 'events')['images'][0]
assert image == expected_image, 'EKS image differs from selected unified build'
jar_hash = kubectl('-n', 'events-tache', 'exec', 'deployment/events', '--', 'sha256sum', '/app/app.jar').split()[0]
assert jar_hash == expected_jar_hash, 'EKS JAR differs from Jenkins artifact'
prom = requests.get('http://localhost:19190/api/v1/targets', timeout=20).json()
targets = prom['data']['activeTargets']
assert len(targets) == 1 and targets[0]['health'] == 'up'
s = requests.Session()
s.auth = ('tache-admin', environment()['EKS_GRAFANA_ADMIN_PASSWORD'])


def grafana(path, **kwargs):
    r = s.get('http://localhost:13100' + path, timeout=30, **kwargs)
    r.raise_for_status()
    return r.json()


g_health = grafana('/api/health')
assert g_health['database'] == 'ok'
assert grafana('/api/datasources/uid/prometheus/health')['status'] == 'OK'
dashboard = grafana('/api/dashboards/uid/events-tache')['dashboard']
queries = {}
for panel in dashboard['panels']:
    for target in panel.get('targets', []):
        expr = target['expr']
        result = grafana('/api/datasources/proxy/uid/prometheus/api/v1/query', params={'query': expr})
        assert result['status'] == 'success' and result['data']['result'], expr
        queries[expr] = result['data']['result']
assert len(queries) == 4
proof = {'verifiedAt': datetime.now(timezone.utc).isoformat(), 'cluster': 'events-tache-ayyoub',
         'region': 'us-east-1', 'node': {'instanceType': 't3.medium', 'ready': True,
         'kubeletVersion': node['status']['nodeInfo']['kubeletVersion']},
         'deployments': resources, 'volumes': volumes, 'readiness': health,
         'jarSha256': jar_hash, 'upstreamJenkinsBuild': args.build,
         'prometheusTarget': targets[0]['health'], 'grafanaVersion': g_health['version'],
         'grafanaQueries': queries}
(OUT / 'verification.json').write_text(json.dumps(proof, indent=2), encoding='utf-8')
lines = ['EKS events-tache-ayyoub / us-east-1 / verified ' + proof['verifiedAt'],
         'Node: 1 t3.medium Ready / ' + proof['node']['kubeletVersion']]
lines += [r['namespace'] + ' / ' + r['deployment'] + ': 1/1 Ready' for r in resources]
lines += [v['claim'] + ': Bound / encrypted gp3 / ' + v['size'] for v in volumes]
lines += ['HTTP readiness: UP; MySQL-backed role lifecycle: see api-success.txt',
          f'JAR SHA256 matches Jenkins #{args.build} artifact:', jar_hash,
          'Prometheus: 1/1 UP; Grafana: healthy, four nonempty queries']
(OUT / 'resources.txt').write_text('\n'.join(lines) + '\n', encoding='utf-8')
print('\n'.join(lines))
