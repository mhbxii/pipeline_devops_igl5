"""Install the approved project-local kubectl and a temporary, scoped EKS credential.

Does not trigger a build. The dedicated promotion job reuses the verified release.
The Kubernetes service-account token expires in two hours; rerun to renew it while
the lab is active. No AWS keys are given to Jenkins.
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
container = 'events-tache-jenkins-1'
subprocess.run(['docker', 'exec', '-u', 'root', container, 'mkdir', '-p',
                '/var/jenkins_home/eks-tools'], check=True)
subprocess.run(['docker', 'cp', str(ROOT / '.local/tools/kubectl-eks-linux'),
                container + ':/var/jenkins_home/eks-tools/kubectl'], check=True)
subprocess.run(['docker', 'exec', '-u', 'root', container, 'chmod', '755',
                '/var/jenkins_home/eks-tools/kubectl'], check=True)
script = r'''
import jenkins.model.Jenkins
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.Domain
import org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
def store = SystemCredentialsProvider.getInstance().getStore()
def domain = Domain.global()
def credential = new FileCredentialsImpl(CredentialsScope.GLOBAL, 'eks-kubeconfig',
    'Personal EKS; events deployment only; token expires in 2h', 'eks-kubeconfig.json',
    SecretBytes.fromRawBytes(input.EKS_SECRET.getBytes('UTF-8')))
def old = store.getCredentials(domain).find { it.id == credential.id }
if (old) store.updateCredentials(domain, old, credential)
else store.addCredentials(domain, credential)
def instance = Jenkins.get()
def job = instance.getItem('tache-eks-delivery') ?: instance.createProject(WorkflowJob, 'tache-eks-delivery')
if (job.isBuilding()) throw new IllegalStateException('Wait for current EKS delivery')
job.setDefinition(new CpsFlowDefinition(input.pipeline, false))
job.setDescription('AWS EKS promotion of verified tache-ci #9; no Terraform apply; temporary namespace-scoped credential.')
job.save()
// Approve only this exact reviewed pipeline, which reads upstream Jenkins metadata.
// Do not disable script security or approve other pending scripts/signatures.
def approval = org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval.get()
approval.pendingScripts.findAll { it.script == input.pipeline }.each { approval.approveScript(it.hash) }
println('Configured tache-eks-delivery; namespace-scoped token expires in two hours. No build triggered.')
'''
print(Jenkins().groovy(script, {'EKS_SECRET': json.dumps(config),
      'pipeline': (ROOT / 'ci/Jenkinsfile.eks').read_text(encoding='utf-8')}).strip())
