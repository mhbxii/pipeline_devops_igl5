"""Renew the namespace-scoped EKS credential for the unified tache-ci pipeline.

Requires the approved cluster and workloads. Does not install tools, create a
second job, run Terraform, or trigger a build. The token expires in two hours.
"""
import json
import subprocess
from local_ci import ROOT, Jenkins

K = [str(ROOT / '.local/tools/kubectl-eks.exe'), '--kubeconfig',
     str(ROOT / '.local/kubeconfig-eks'), '--context', 'events-tache-eks']
config = json.loads(subprocess.check_output(K + ['config', 'view', '--raw', '--minify',
                                               '--flatten', '-o', 'json'], text=True))
token = subprocess.check_output(K + ['-n', 'events-tache', 'create', 'token',
    'jenkins-deployer', '--duration=2h'], text=True).strip()
config['users'] = [{'name': 'jenkins-deployer', 'user': {'token': token}}]
config['contexts'][0]['context']['user'] = 'jenkins-deployer'
config['contexts'][0]['context']['namespace'] = 'events-tache'
script = r'''
import jenkins.model.Jenkins
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.Domain
import org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl
def store = SystemCredentialsProvider.getInstance().getStore()
def domain = Domain.global()
def credential = new FileCredentialsImpl(CredentialsScope.GLOBAL, 'eks-kubeconfig',
    'Personal EKS; events deployment only; token expires in 2h', 'eks-kubeconfig.json',
    SecretBytes.fromRawBytes(input.EKS_SECRET.getBytes('UTF-8')))
def old = store.getCredentials(domain).find { it.id == credential.id }
if (old) store.updateCredentials(domain, old, credential)
else store.addCredentials(domain, credential)
def legacy = Jenkins.get().getItem('tache-eks-delivery')
if (legacy != null) {
    if (legacy.isBuilding()) throw new IllegalStateException('Wait for the legacy EKS build')
    legacy.setDisabled(true)
    legacy.setDescription('Archived history; EKS delivery now belongs to tache-ci / Jenkinsfile.')
    legacy.save()
}
println('Renewed eks-kubeconfig for tache-ci; token expires in two hours. Legacy job disabled; no build triggered.')
'''
print(Jenkins().groovy(script, {'EKS_SECRET': json.dumps(config)}).strip())
