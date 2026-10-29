import jenkins.model.Jenkins
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition

def instance = Jenkins.get()
def job = instance.getItem('logistics-local') ?: instance.createProject(WorkflowJob, 'logistics-local')
job.description = 'Docker lab pipeline. LOCAL checks out committed logistics work; REMOTE reads GitHub.'
def source = new File('/opt/project/Jenkinsfile')
if (source.exists()) {
    job.definition = new CpsFlowDefinition(source.text, true)
    job.save()
}
