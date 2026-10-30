import jenkins.model.Jenkins
import hudson.security.HudsonPrivateSecurityRealm
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy
import hudson.plugins.git.GitSCM
import hudson.plugins.git.BranchSpec
import hudson.plugins.git.UserRemoteConfig
import hudson.plugins.git.extensions.impl.PathRestriction
import hudson.triggers.SCMTrigger
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition

def instance = Jenkins.get()
def password = System.getenv('JENKINS_ADMIN_PASSWORD')
if (!password) { throw new IllegalStateException('Generate JENKINS_ADMIN_PASSWORD before startup') }
def realm = new HudsonPrivateSecurityRealm(false)
realm.createAccount('tache-admin', password)
instance.setSecurityRealm(realm)
def authorization = new FullControlOnceLoggedInAuthorizationStrategy()
authorization.setAllowAnonymousRead(false)
instance.setAuthorizationStrategy(authorization)
instance.setNumExecutors(1)
instance.setSystemMessage('Local Tache laboratory: trusted jobs only; Docker daemon access is privileged.')
def job = instance.getItem('tache-ci') ?: instance.createProject(WorkflowJob, 'tache-ci')
def scm = new GitSCM([new UserRemoteConfig('https://github.com/mhbxii/pipeline_devops_igl5.git', null, null, null)],
    [new BranchSpec('*/Tache')], false, [], null, null, [new PathRestriction('', 'docs/.*')])
def definition = new CpsScmFlowDefinition(scm, 'Jenkinsfile')
definition.setLightweight(false)
job.setDefinition(definition)
job.addTrigger(new SCMTrigger('H/5 * * * *'))
job.save()
def legacy = instance.getItem('tache-eks-delivery')
if (legacy != null && !legacy.isBuilding()) {
    legacy.setDisabled(true)
    legacy.setDescription('Archived history; merged into tache-ci / Jenkinsfile.')
    legacy.save()
}
instance.save()
