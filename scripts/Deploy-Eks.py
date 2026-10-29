"""Deploy the verified release to the approved personal EKS demo cluster only.

Requires the separately approved Terraform infrastructure and kubeconfig-eks.
No Terraform apply, IAM changes or public load balancers are performed here.
"""
import json
import subprocess
from local_ci import ROOT, ensure_secret

K = [str(ROOT / '.local/tools/kubectl-eks.exe'), '--kubeconfig',
     str(ROOT / '.local/kubeconfig-eks'), '--context', 'events-tache-eks']


def run(args, **kwargs):
    return subprocess.run(K + args, check=True, text=True, **kwargs)


def apply(obj):
    run(['apply', '-f', '-'], input=json.dumps(obj))


config = json.loads(run(['config', 'view', '--minify', '-o', 'json'], capture_output=True).stdout)
assert config['contexts'][0]['name'] == 'events-tache-eks'
assert 'events-tache-ayyoub' in config['contexts'][0]['context']['cluster']
rendered = run(['kustomize', str(ROOT / 'k8s/eks'), '--load-restrictor',
                'LoadRestrictionsNone'], capture_output=True).stdout
# Ensure namespaces exist before creating secrets. Passwords travel over stdin,
# not command-line arguments, evidence files or generated manifest files.
for ns in ['events-tache', 'events-monitoring', 'events-grafana']:
    apply({'apiVersion': 'v1', 'kind': 'Namespace', 'metadata': {'name': ns}})
apply({'apiVersion': 'v1', 'kind': 'Secret', 'type': 'Opaque',
       'metadata': {'name': 'database', 'namespace': 'events-tache'},
       'stringData': {'password': ensure_secret('EKS_DB_PASSWORD'),
                      'root-password': ensure_secret('EKS_MYSQL_ROOT_PASSWORD')}})
apply({'apiVersion': 'v1', 'kind': 'Secret', 'type': 'Opaque',
       'metadata': {'name': 'grafana-admin', 'namespace': 'events-grafana'},
       'stringData': {'password': ensure_secret('EKS_GRAFANA_ADMIN_PASSWORD')}})
apply({'apiVersion': 'v1', 'kind': 'ConfigMap',
       'metadata': {'name': 'grafana-dashboard', 'namespace': 'events-grafana'},
       'data': {'dashboard.json': (ROOT / 'monitoring/grafana-dashboard.json').read_text(encoding='utf-8')}})
run(['apply', '-f', '-'], input=rendered)
for ns, deployment in [('events-tache', 'mysql'), ('events-tache', 'events'),
                       ('events-monitoring', 'prometheus'), ('events-grafana', 'grafana')]:
    run(['-n', ns, 'rollout', 'status', 'deployment/' + deployment, '--timeout=900s'])
print('Four EKS deployments ready. HTTP, storage and monitoring verification still required.')
