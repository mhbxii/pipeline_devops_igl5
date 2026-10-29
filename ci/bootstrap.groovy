import jenkins.model.Jenkins
import hudson.security.HudsonPrivateSecurityRealm
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy
import hudson.plugins.git.GitSCM
import hudson.plugins.git.BranchSpec
import hudson.plugins.git.UserRemoteConfig
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
def scm = new GitSCM([new UserRemoteConfig('/git-src', null, null, null)],
    [new BranchSpec('*/Tache')], false, [], null, null, [])
def definition = new CpsScmFlowDefinition(scm, 'Jenkinsfile')
definition.setLightweight(false)
job.setDefinition(definition)
job.save()
instance.save()
