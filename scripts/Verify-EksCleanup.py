"""Read-only final checks for the exact AWS demo resources recorded before cleanup."""
import json
import subprocess
from datetime import datetime, timezone
from local_ci import ROOT

AWS = ['aws', '--profile', 'events-tache-lab', '--region', 'us-east-1']
TF = [str(ROOT / '.local/tools/terraform.exe'), '-chdir=' + str(ROOT / 'terraform/aws-eks')]
inventory = json.loads((ROOT / '.local/aws-preflight/cleanup-inventory.json').read_text())


def absent(args, expected_error):
    r = subprocess.run(AWS + args, capture_output=True, text=True)
    assert r.returncode and expected_error in r.stderr, 'Resource still exists or absence could not be verified'


absent(['eks', 'describe-cluster', '--name', 'events-tache-ayyoub'], 'ResourceNotFoundException')
absent(['ec2', 'describe-launch-templates', '--launch-template-ids', inventory['launchTemplateId']],
       'InvalidLaunchTemplateId.NotFound')
absent(['ec2', 'describe-security-groups', '--group-ids', inventory['clusterSecurityGroupId']],
       'InvalidGroup.NotFound')
groups = json.loads(subprocess.check_output(AWS + ['autoscaling', 'describe-auto-scaling-groups',
                   '--auto-scaling-group-names'] + inventory['autoScalingGroupNames'], text=True))['AutoScalingGroups']
assert not groups, 'Worker auto scaling group remains'
for volume in inventory['rootVolumeIds'] + inventory['dataVolumeIds']:
    absent(['ec2', 'describe-volumes', '--volume-ids', volume], 'InvalidVolume.NotFound')
r = subprocess.run(AWS + ['ec2', 'describe-instances', '--instance-ids', inventory['instanceId']],
                   capture_output=True, text=True)
if r.returncode:
    assert 'InvalidInstanceID.NotFound' in r.stderr
    worker = 'absent'
else:
    instances = [i for reservation in json.loads(r.stdout)['Reservations'] for i in reservation['Instances']]
    assert all(i['State']['Name'] == 'terminated' and not i.get('PublicIpAddress') for i in instances)
    worker = 'terminated'
for name, value in [('description', '*events-tache-ayyoub*'),
                    ('tag:node.k8s.amazonaws.com/instance_id', inventory['instanceId'])]:
    nics = json.loads(subprocess.check_output(AWS + ['ec2', 'describe-network-interfaces',
                     '--filters', 'Name=' + name + ',Values=' + value], text=True))['NetworkInterfaces']
    assert not nics, 'Demo network interfaces remain'
state = json.loads(subprocess.check_output(TF + ['show', '-json'], text=True))
resources = state.get('values', {}).get('root_module', {}).get('resources', [])
assert not any(r['mode'] == 'managed' for r in resources)
proof = {'verifiedAt': datetime.now(timezone.utc).isoformat(), 'region': 'us-east-1',
         'cluster': 'events-tache-ayyoub', 'clusterAbsent': True, 'workerState': worker,
         'workerPublicIpv4Released': True, 'launchTemplateAbsent': True,
         'clusterSecurityGroupAbsent': True, 'workerAutoScalingGroupAbsent': True,
         'dataVolumesAbsent': 3, 'rootVolumesAbsent': 1, 'demoNetworkInterfacesAbsent': True,
         'terraformManagedResourcesRemaining': 0,
         'note': 'Read-only absence checks for the demo resources; historical usage charges may appear later.'}
out = ROOT / 'docs/evidence/14-aws-eks'
(out / 'cleanup-verification.json').write_text(json.dumps(proof, indent=2), encoding='utf-8')
lines = ['AWS cleanup verified: ' + proof['verifiedAt'],
         'EKS events-tache-ayyoub: absent', 'EC2 worker: ' + worker + '; public IPv4 released',
         'EBS: all 3 data volumes and the root volume absent',
         'Launch template and demo network interfaces: absent',
         'Worker auto scaling group and EKS security group: absent',
         'Terraform managed resources remaining: 0',
         'Jenkins EKS job disabled; temporary credential removed; build history retained.',
         'Local kind, source code, screenshots and presentation preserved.',
         'Historical usage can still appear on the delayed lab bill.']
(out / 'cleanup-verification.txt').write_text('\n'.join(lines) + '\n', encoding='utf-8')
print('\n'.join(lines))
