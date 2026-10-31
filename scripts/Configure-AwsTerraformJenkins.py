"""Validate the dedicated lab session and configure Terraform Secret files.

No infrastructure changes or build triggers. Never copies the default profile.
"""
import configparser
import ipaddress
import json
import os
from pathlib import Path
import subprocess
import requests

from local_ci import ROOT, Jenkins


def main():
    variables = json.loads((ROOT / '.local/aws-eks.tfvars.json').read_text(encoding='utf-8-sig'))
    allowed = {'aws_profile', 'aws_region', 'expected_account_id', 'cluster_name', 'operator_ipv4_cidrs'}
    if set(variables) - allowed:
        raise SystemExit('Unexpected tfvars keys; review the private configuration.')
    profile = 'events-tache-lab'
    result = subprocess.run(['aws', 'sts', 'get-caller-identity', '--profile', profile,
        '--region', 'us-east-1', '--output', 'json', '--cli-connect-timeout', '10',
        '--cli-read-timeout', '20'], capture_output=True, text=True,
        env={**os.environ, 'AWS_MAX_ATTEMPTS': '1'})
    if result.returncode:
        raise SystemExit('Lab identity verification failed. Renew credentials or check AWS connectivity.')
    if json.loads(result.stdout)['Account'] != variables['expected_account_id']:
        raise SystemExit('Lab account does not match the reviewed Terraform account guard.')
    access = subprocess.run(['aws', 'eks', 'list-clusters', '--profile', profile,
        '--region', 'us-east-1', '--output', 'json', '--cli-connect-timeout', '10',
        '--cli-read-timeout', '20'], capture_output=True, text=True,
        env={**os.environ, 'AWS_MAX_ATTEMPTS': '1'})
    if access.returncode:
        raise SystemExit('EKS access is denied. Start the lab and renew the dedicated profile first.')
    response = requests.get('https://checkip.amazonaws.com', timeout=20)
    response.raise_for_status()
    host_ip = str(ipaddress.IPv4Address(response.text.strip()))
    egress = subprocess.run(['docker', 'exec', 'events-tache-jenkins-1', 'curl',
        '--fail', '--silent', '--max-time', '20', 'https://checkip.amazonaws.com'],
        capture_output=True, text=True, check=True)
    jenkins_ip = str(ipaddress.IPv4Address(egress.stdout.strip()))
    variables['operator_ipv4_cidrs'] = sorted({host_ip + '/32', jenkins_ip + '/32'})
    config = configparser.ConfigParser(interpolation=None)
    config.read(Path.home() / '.aws/credentials', encoding='utf-8-sig')
    keys = ('aws_access_key_id', 'aws_secret_access_key', 'aws_session_token')
    if profile not in config or any(not config[profile].get(key, '').strip() for key in keys):
        raise SystemExit('The dedicated profile needs all three temporary credentials.')
    credentials = '[' + profile + ']\n' + ''.join(
        key + '=' + config[profile][key].strip() + '\n' for key in keys)
    variables['aws_profile'] = profile
    (ROOT / '.local/aws-eks.tfvars.json').write_text(json.dumps(variables, indent=2) + '\n', encoding='utf-8')
    script = r'''
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.Domain
import org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl
def store = SystemCredentialsProvider.getInstance().getStore()
def domain = Domain.global()
def save = { id, filename, value ->
    def credential = new FileCredentialsImpl(CredentialsScope.GLOBAL, id,
        'Temporary dedicated lab session for tache-ci Terraform', filename,
        SecretBytes.fromRawBytes(value.getBytes('UTF-8')))
    def old = store.getCredentials(domain).find { it.id == id }
    if (old) store.updateCredentials(domain, old, credential)
    else store.addCredentials(domain, credential)
}
save('aws-lab-credentials', 'credentials', input.AWS_SECRET)
save('aws-eks-tfvars', 'eks.tfvars.json', input.TFVARS_SECRET)
println('Lab identity verified; Terraform credentials configured. No build triggered.')
'''
    print(Jenkins().groovy(script, {'AWS_SECRET': credentials,
        'TFVARS_SECRET': json.dumps(variables)}).strip())


if __name__ == '__main__':
    main()
