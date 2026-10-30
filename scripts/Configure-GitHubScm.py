"""Configure direct GitHub SCM and timer polling without exposing Jenkins publicly."""
import argparse
import json
from local_ci import ROOT, Jenkins

parser = argparse.ArgumentParser()
parser.add_argument("--enable", action="store_true", help="Enable the project job after the reviewed source commit is pushed")
args = parser.parse_args()
script = r"""
import jenkins.model.Jenkins
import hudson.plugins.git.*
import hudson.plugins.git.extensions.impl.PathRestriction
import hudson.triggers.SCMTrigger
import hudson.model.*
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition
def job = Jenkins.get().getItem('tache-ci')
if (job.isBuilding()) throw new IllegalStateException('Wait for the active project build to finish')
def scm = new GitSCM([new UserRemoteConfig('https://github.com/mhbxii/pipeline_devops_igl5.git', null, null, null)],
    [new BranchSpec('*/Tache')], false, [], null, null, [new PathRestriction('', 'docs/.*')])
def definition = new CpsScmFlowDefinition(scm, 'Jenkinsfile')
definition.setLightweight(false)
job.setDefinition(definition)
def parameters = (job.getProperty(ParametersDefinitionProperty)?.parameterDefinitions ?: []).findAll { !(it.name in ['DEPLOY_LOCAL_KIND', 'DEPLOY_K8S']) }.collect { parameter ->
    if (parameter.name in ['RUN_SONAR', 'PUBLISH_NEXUS', 'PUBLISH_IMAGE', 'DEPLOY_EKS']) {
        return new BooleanParameterDefinition(parameter.name, true, parameter.description)
    }
    if (parameter.name == 'DOCKER_REPOSITORY') return new StringParameterDefinition(parameter.name, 'ayyoub1/events-tache', parameter.description)
    return parameter
}
job.addProperty(new ParametersDefinitionProperty(parameters))
job.addTrigger(new SCMTrigger('H/5 * * * *'))
if (input.enable) job.setDisabled(false)
job.save()
def legacy = Jenkins.get().getItem('tache-eks-delivery')
if (legacy != null) {
    if (legacy.isBuilding()) throw new IllegalStateException('Wait for the legacy EKS delivery')
    legacy.setDisabled(true)
    legacy.setDescription('Archived history; merged into tache-ci / Jenkinsfile.')
    legacy.save()
}
println(groovy.json.JsonOutput.toJson([
    job: job.name, sourceUrl: scm.userRemoteConfigs[0].url, branch: scm.branches[0].name,
    scriptPath: definition.scriptPath, excludedRegions: 'docs/.*', polling: 'H/5 * * * *',
    disabled: job.disabled, anonymousSourceRead: true,
    parameters: parameters.collectEntries { [(it.name): it.defaultParameterValue.value] }
]))
"""
proof = json.loads(Jenkins().groovy(script, {"enable": args.enable}).strip())
(ROOT / "docs/evidence/08-jenkins/github-scm-config.json").write_text(json.dumps(proof, indent=2), encoding="utf-8")
print(json.dumps(proof, indent=2))
