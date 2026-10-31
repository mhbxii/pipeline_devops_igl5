"""Opt-in public API access; preserve existing services, workloads and forwarding.

Uses the existing EKS service controller, without installing a controller or
changing Terraform. Creates one billable Classic Load Balancer in the worker AZ.
"""
import argparse
import json
import subprocess
import time
from datetime import datetime, timezone

import requests
from local_ci import ROOT

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--enable', action='store_true', required=True,
                    help='Explicitly enable billable, public HTTP access to the API')
parser.parse_args()
AWS = ['aws', '--profile', 'events-tache-lab', '--region', 'us-east-1']
K = [str(ROOT / '.local/tools/kubectl-eks.exe'), '--kubeconfig',
     str(ROOT / '.local/kubeconfig-eks'), '--context', 'events-tache-eks',
     '--request-timeout=20s']


def query(command):
    return json.loads(subprocess.check_output(command, text=True))


def pods():
    result = {}
    for namespace in ['events-tache', 'events-monitoring', 'events-grafana']:
        for pod in query(K + ['-n', namespace, 'get', 'pods', '-o', 'json'])['items']:
            statuses = pod['status'].get('containerStatuses', [])
            assert statuses and all(c['ready'] for c in statuses), 'A workload is not Ready'
            result[namespace + '/' + pod['metadata']['name']] = {
                'uid': pod['metadata']['uid'],
                'restarts': sum(c['restartCount'] for c in statuses)}
    assert len(result) == 4
    return result


expected = json.loads((ROOT / '.local/aws-eks.tfvars.json').read_text())['expected_account_id']
assert query(AWS + ['sts', 'get-caller-identity'])['Account'] == expected
cluster = query(AWS + ['eks', 'describe-cluster', '--name', 'events-tache-ayyoub'])['cluster']
assert cluster['status'] == 'ACTIVE'
context = query(K + ['config', 'view', '--minify', '-o', 'json'])
assert context['clusters'][0]['cluster']['server'] == cluster['endpoint']
before = pods()
nodes = query(K + ['get', 'nodes', '-o', 'json'])['items']
assert len(nodes) == 1
az = nodes[0]['metadata']['labels']['topology.kubernetes.io/zone']
subnets = query(AWS + ['ec2', 'describe-subnets', '--subnet-ids',
                      *cluster['resourcesVpcConfig']['subnetIds']])['Subnets']
selected = [s for s in subnets if s['AvailabilityZone'] == az and s['MapPublicIpOnLaunch']]
assert len(selected) == 1, 'Expected one existing public cluster subnet in worker AZ'
manifest = query(K + ['create', '--dry-run=client', '-f',
                     str(ROOT / 'k8s/eks/public-application.yaml'), '-o', 'json'])
manifest['metadata']['annotations']['service.beta.kubernetes.io/aws-load-balancer-subnets'] = selected[0]['SubnetId']
print('Reviewed: one additional public Service, port 80 -> existing API 8089; no pod or Terraform changes.', flush=True)
encoded = json.dumps(manifest)
subprocess.run(K + ['apply', '--dry-run=server', '-f', '-'], input=encoded, text=True, check=True)
subprocess.run(K + ['apply', '-f', '-'], input=encoded, text=True, check=True)
deadline = time.monotonic() + 600
http = requests.Session()
http.trust_env = False
last_status = 'waiting for AWS load balancer'
while time.monotonic() < deadline:
    service = query(K + ['-n', 'events-tache', 'get', 'service/events-public', '-o', 'json'])
    ingress = service.get('status', {}).get('loadBalancer', {}).get('ingress', [])
    if ingress:
        host = ingress[0].get('hostname')
        assert host and host.endswith('.elb.amazonaws.com')
        base = 'http://' + host
        try:
            health = http.get(base + '/events/actuator/health/readiness', timeout=10)
            api = http.get(base + '/events/taches', timeout=10)
            last_status = f'public readiness HTTP {health.status_code}; API HTTP {api.status_code}'
            if health.status_code == api.status_code == 200 and health.json()['status'] == 'UP':
                after = pods()
                assert after == before, 'Workload identities or restart counts changed; inspect before claiming no restarts'
                proof = {'verifiedAt': datetime.now(timezone.utc).isoformat(),
                         'apiUrl': base + '/events/taches', 'healthHttp': health.status_code,
                         'healthStatus': health.json()['status'], 'apiHttp': api.status_code,
                         'existingWorkloadsUnchanged': True, 'workloads': after,
                         'service': 'events-tache/events-public', 'loadBalancerType': 'classic',
                         'publicPort': 80, 'workerAz': az, 'terraformApply': False}
                folder = ROOT / 'docs/evidence/18-eks-public-access'
                folder.mkdir(parents=True, exist_ok=True)
                (folder / 'verification.json').write_text(json.dumps(proof, indent=2), encoding='utf-8')
                print('VERIFIED: ' + proof['apiUrl'] + '; four workloads unchanged and Ready.', flush=True)
                break
        except requests.RequestException as exc:
            last_status = type(exc).__name__
    print(last_status, flush=True)
    time.sleep(10)
else:
    raise RuntimeError('Public access not yet verified: ' + last_status + '. Service retained; inspect EKS events.')
