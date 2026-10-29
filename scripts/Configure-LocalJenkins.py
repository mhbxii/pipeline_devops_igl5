"""Configure credentials without saving their values in source or evidence."""
import json
import subprocess
from local_ci import ROOT, Jenkins, environment

values = environment()
result = subprocess.run(["kubectl", "--kubeconfig", str(ROOT / ".local/kubeconfig"),
    "--context", "kind-events-tache", "config", "view", "--raw", "--minify", "--flatten", "-o", "json"],
    capture_output=True, text=True, check=True)
config = json.loads(result.stdout)
cluster = config["clusters"][0]["cluster"]
cluster["server"] = "https://events-tache-control-plane:6443"
cluster["tls-server-name"] = "127.0.0.1"
payload = {"kubeconfig": json.dumps(config), "nexus_password": values["NEXUS_PASSWORD"]}
if values.get("SONAR_TOKEN"):
    payload.update(sonar_token=values["SONAR_TOKEN"], sonar_webhook_secret=values["SONAR_WEBHOOK_SECRET"])
script = r"""
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl
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
upsert(new FileCredentialsImpl(CredentialsScope.GLOBAL, 'kubeconfig', 'Project kind cluster',
    'kubeconfig.json', SecretBytes.fromRawBytes(input.kubeconfig.getBytes('UTF-8'))))
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
