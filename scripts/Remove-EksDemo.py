"""Execute the separately approved personal EKS cleanup, never a fresh destroy plan.

Require the approved saved-plan digest. Verify account, cluster and PVC identities
before deletion, and retain private resource IDs for final orphan checks.
"""
import argparse
import hashlib
import json
import subprocess
import time
from datetime import datetime, timezone
from local_ci import ROOT

parser = argparse.ArgumentParser()
parser.add_argument('--approved-plan-sha256', required=True)
args = parser.parse_args()
plan = ROOT / '.local/aws-eks-destroy.tfplan'
assert hashlib.sha256(plan.read_bytes()).hexdigest() == args.approved_plan_sha256
AWS = ['aws', '--profile', 'events-tache-lab', '--region', 'us-east-1']
K = [str(ROOT / '.local/tools/kubectl-eks.exe'), '--kubeconfig',
     str(ROOT / '.local/kubeconfig-eks'), '--context', 'events-tache-eks']
TF = [str(ROOT / '.local/tools/terraform.exe'), '-chdir=' + str(ROOT / 'terraform/aws-eks')]


def query(command):
    return json.loads(subprocess.check_output(command, text=True))


expected = json.loads((ROOT / '.local/aws-eks.tfvars.json').read_text())['expected_account_id']
assert query(AWS + ['sts', 'get-caller-identity'])['Account'] == expected
changes = query(TF + ['show', '-json', str(plan)])['resource_changes']
managed = [r for r in changes if r['mode'] == 'managed']
assert {r['address'] for r in managed} == {
    'aws_eks_addon.ebs', 'aws_eks_cluster.demo', 'aws_eks_node_group.demo', 'aws_launch_template.worker'}
assert all(r['change']['actions'] == ['delete'] for r in managed)
cluster = next(r for r in managed if r['address'] == 'aws_eks_cluster.demo')
assert cluster['change']['before']['name'] == 'events-tache-ayyoub'
assert query(K + ['config', 'view', '--minify', '-o', 'json'])['contexts'][0]['name'] == 'events-tache-eks'
expected_volumes = set(json.loads((ROOT / '.local/aws-preflight/workload-volume-ids.json').read_text()))
assert len(expected_volumes) == 3
actual_volumes = set()
for ns in ['events-tache', 'events-monitoring', 'events-grafana']:
    claims = query(K + ['-n', ns, 'get', 'pvc', '-o', 'json'])['items']
    assert len(claims) == 1
    for claim in claims:
        pv = query(K + ['get', 'pv', claim['spec']['volumeName'], '-o', 'json'])
        assert pv['spec']['persistentVolumeReclaimPolicy'] == 'Delete'
        actual_volumes.add(pv['spec']['csi']['volumeHandle'])
assert actual_volumes == expected_volumes
nodes = query(K + ['get', 'nodes', '-o', 'json'])['items']
assert len(nodes) == 1
instance_id = nodes[0]['spec']['providerID'].rsplit('/', 1)[1]
instance = query(AWS + ['ec2', 'describe-instances', '--instance-ids', instance_id])['Reservations'][0]['Instances'][0]
root_volumes = [b['Ebs']['VolumeId'] for b in instance['BlockDeviceMappings']
                if b['DeviceName'] == instance['RootDeviceName']]
assert len(root_volumes) == 1
private_inventory = {'instanceId': instance_id, 'rootVolumeIds': root_volumes,
                     'dataVolumeIds': sorted(expected_volumes),
                     'clusterSecurityGroupId': cluster['change']['before']['vpc_config'][0]['cluster_security_group_id'],
                     'autoScalingGroupNames': [a['name'] for group in next(r for r in managed
                         if r['address'] == 'aws_eks_node_group.demo')['change']['before']['resources']
                         for a in group['autoscaling_groups']],
                     'launchTemplateId': next(r for r in managed if r['address'] == 'aws_launch_template.worker')['change']['before']['id']}
(ROOT / '.local/aws-preflight/cleanup-inventory.json').write_text(json.dumps(private_inventory), encoding='utf-8')
print('Account, approved plan and all three PVC volume identities verified.', flush=True)
subprocess.run(K + ['delete', 'namespace', 'events-tache', 'events-monitoring', 'events-grafana',
                    '--wait=true', '--timeout=300s'], check=True)
deadline = time.monotonic() + 300
pending = set(expected_volumes)
while pending:
    for volume in list(pending):
        r = subprocess.run(AWS + ['ec2', 'describe-volumes', '--volume-ids', volume], capture_output=True, text=True)
        if r.returncode and 'InvalidVolume.NotFound' in r.stderr:
            pending.remove(volume)
        elif r.returncode:
            raise RuntimeError('Cannot verify EBS deletion; Terraform destroy not started')
    if pending:
        if time.monotonic() > deadline:
            raise RuntimeError('Data volumes still exist; Terraform destroy not started')
        time.sleep(5)
print('Three demo namespaces and three EBS data volumes deleted. Applying reviewed destruction plan.', flush=True)
with (ROOT / '.local/aws-preflight/terraform-destroy.log').open('w', encoding='utf-8') as log:
    r = subprocess.run(TF + ['apply', '-input=false', '-no-color', str(plan)], stdout=log, stderr=subprocess.STDOUT, text=True)
if r.returncode:
    raise RuntimeError('Terraform destroy failed; inspect private terraform-destroy.log')
proof = {'completedAt': datetime.now(timezone.utc).isoformat(), 'approvedPlanSha256': args.approved_plan_sha256,
         'deletedNamespaces': ['events-tache', 'events-monitoring', 'events-grafana'],
         'dataVolumesConfirmedDeleted': 3, 'terraformApplyExit': 0,
         'note': 'Final AWS cluster, EC2 and root-volume orphan checks are still required.'}
(ROOT / 'docs/evidence/14-aws-eks/cleanup.json').write_text(json.dumps(proof, indent=2), encoding='utf-8')
print('Approved Terraform destruction completed. Final AWS orphan checks still required.', flush=True)
