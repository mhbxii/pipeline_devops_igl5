"""Configure credentials without saving their values in source or evidence."""
import json
import subprocess
from local_ci import Jenkins, environment

values = environment()
payload = {"nexus_password": values["NEXUS_PASSWORD"]}
if values.get("SONAR_TOKEN"):
    payload.update(sonar_token=values["SONAR_TOKEN"], sonar_webhook_secret=values["SONAR_WEBHOOK_SECRET"])
credential = subprocess.run(["docker-credential-desktop.exe", "get"],
    input="https://index.docker.io/v1/\n", capture_output=True, text=True, check=True)
docker = json.loads(credential.stdout)
if docker.get("Username") == "ayyoub1" and docker.get("Secret", "").startswith("dckr_pat_"):
    payload.update(docker_user=docker["Username"], docker_token=docker["Secret"])
script = r"""
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl
import hudson.util.Secret
def store = SystemCredentialsProvider.getInstance().getStore()
def domain = Domain.global()
def upsert = { credential ->
    def old = store.getCredentials(domain).find { it.id == credential.id }
    if (old) { store.updateCredentials(domain, old, credential) }
    else { store.addCredentials(domain, credential) }
}
upsert(new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, 'nexus',
    'Local Maven publisher', 'tache-publisher', input.nexus_password))
if (input.docker_token) {
    upsert(new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, 'dockerhub',
        'User-approved Docker Hub publication', input.docker_user, input.docker_token))
}
if (input.sonar_token) {
    upsert(new StringCredentialsImpl(CredentialsScope.GLOBAL, 'sonar-token', 'Local analysis token', Secret.fromString(input.sonar_token)))
    upsert(new StringCredentialsImpl(CredentialsScope.GLOBAL, 'sonar-webhook', 'Webhook signature', Secret.fromString(input.sonar_webhook_secret)))
    def installation = new hudson.plugins.sonar.SonarInstallation('local-sonar', 'http://sonar:9000',
        'sonar-token', null, 'sonar-webhook', null, '', '', null)
    def sonar = hudson.plugins.sonar.SonarGlobalConfiguration.get()
    sonar.setInstallations([installation] as hudson.plugins.sonar.SonarInstallation[])
    sonar.save()
}
println('Configured project credentials; secret values are not displayed.')
"""
print(Jenkins().groovy(script, payload).strip())
