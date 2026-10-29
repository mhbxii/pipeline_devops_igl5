# Events DevOps — Logistics

Spring Boot group project. The `logistics` branch contains Yassine's logistics contribution and a local delivery lab. The event and participant branches remain separate team contributions.

The lab runs Jenkins, SonarQube with PostgreSQL, Nexus, MySQL, the application, Prometheus and Grafana in Docker. A kind cluster runs Kubernetes inside Docker. Terraform provisions a separate application/database environment in that cluster. No cloud account is required.

## Prerequisites

- Windows PowerShell, Git, Python 3, Docker Desktop using Linux containers.
- Installed Java 17 and Maven on PATH, to demonstrate command-line Maven as required by the assignment. Jenkins includes its own Java 17/Maven tools; its controller runs Java 21.
- Approximately 10 GB Docker memory and 20 GB free disk for the complete lab. Stop unused environments when necessary.
- Internet access for initial images, Jenkins plugins, Maven dependencies, kind and Terraform providers. First startup can take a while.

## Run locally

From this repository:

For the complete automated startup, use `python scripts/start-lab.py --kubernetes --terraform`. On a fresh Nexus installation, review its [Community Edition license](https://links.sonatype.com/products/nxrm/ce-eula) and accept it in the Nexus UI, or add `--accept-nexus-eula` only if you agree to the terms. This machine's acceptance persists in its Nexus volume. The manual steps are:

```powershell
./scripts/Initialize-Lab.ps1
docker network create events-devops # once; skip if it already exists
docker compose -f compose.devops.yml up -d --build
```

Wait for SonarQube (`http://localhost:9000/api/system/status` reports `UP`) and Nexus (`http://localhost:8081/service/rest/v1/status` returns 200), then:

```powershell
python scripts/bootstrap-services.py
docker compose -f compose.devops.yml up -d --force-recreate jenkins
docker compose up -d --build --wait
docker compose -f compose.monitoring.yml up -d
python scripts/smoke-test.py
```

Bootstrap changes default service passwords, creates the private Sonar project and analysis token, creates Maven snapshot/release repositories in Nexus, and disables anonymous Nexus access. It can be rerun. Passwords and tokens stay in ignored `.env`; no credentials belong in source control. Jenkins reads them into its credential store through Configuration as Code.

| Service | URL | Login |
|---|---|---|
| Application health | http://localhost:8089/events/actuator/health | None, local demo |
| Jenkins | http://localhost:8082 | `admin` / `JENKINS_ADMIN_PASSWORD` in `.env` |
| SonarQube | http://localhost:9000 | `admin` / `SONAR_ADMIN_PASSWORD` |
| Nexus | http://localhost:8081 | `admin` / `NEXUS_PASSWORD` |
| Prometheus | http://localhost:9090 | Local demo |
| Grafana | http://localhost:3000/d/events-logistics | `admin` / `GRAFANA_PASSWORD` |
| Kubernetes app | http://localhost:8090/events/actuator/health | After Kubernetes deployment |
| Terraform app | http://localhost:8091/events/actuator/health | After Terraform apply |

All published lab ports bind to loopback. The Jenkins container has the host Docker socket and runs as root to build images on Docker Desktop. It therefore has control of this local Docker engine. Use this setup for a trusted local lab; don't expose Jenkins or run untrusted jobs. Persistent named volumes retain service history when containers stop. `down -v` deletes that history and database contents; avoid it before evaluation.

## Your module

Existing endpoints are preserved under `/events`:

- `PUT /event/addAffectLog/{eventDescription}` creates new logistics and assigns it to an existing event in one transaction. Supply `description`, `reserve`, `prixUnit`, and `quantite`. A positive quantity and a finite, non-negative price are required. Existing IDs are rejected on this create operation. Missing events produce 404; invalid values produce 400.
- `GET /event/getLogs/{start}/{end}` uses ISO dates and an inclusive interval on **event start dates**. It returns reserved logistics. Events without logistics are skipped; an empty result is `[]`. A reversed interval produces 400.

`LogisticsService` owns this behavior. `EventServicesImpl` delegates its existing logistics methods to it. INFO logs record assignments/lookups, DEBUG logs show logistics values, WARN logs record rejected input, and ERROR logs record database failures. SLF4J uses the Log4j2 backend.

```powershell
mvn -B -ntp clean test
mvn -B -ntp package
mvn -B -ntp clean install
```

Mock tests cover decisions and repository interactions. H2 tests exercise actual JPA persistence and date boundaries without mocks. MVC tests check HTTP status codes and date parsing. H2 is a test database; the smoke tests also verify behavior against MySQL in Docker/Kubernetes. Coverage is available at `target/site/jacoco/index.html` after `mvn verify`. The build enforces 90% line and 80% branch coverage on `LogisticsService`; the report includes the entire application, including teammates' areas.

## Jenkins delivery

Open `logistics-local` in Jenkins and use **Build with Parameters**. The job is created automatically from the mounted Jenkinsfile at Jenkins startup. Restart/recreate Jenkins after changing that file.

The default `LOCAL` source mode clones **committed** code from this PC's `logistics` branch. This supports verification before pushing to the shared repository. `REMOTE` checks out the selected branch from GitHub; the branch must already be pushed. Other members' branches are never merged automatically.

Stages: checkout → Maven verify/JUnit/coverage/JAR archive → Sonar analysis with blocking quality gate → authenticated Nexus deploy → Docker image build → optional Docker Hub push → Terraform validation → optional Kubernetes rollout.

Default behavior builds the image and publishes the JAR to local Nexus. Docker Hub publishing and Kubernetes deployment have explicit parameters. For Docker Hub, set `DOCKERHUB_USERNAME=arfaoui131` and a Read & Write token in `DOCKERHUB_TOKEN` in `.env`, recreate Jenkins, and enable `PUSH_IMAGE`. Images use `arfaoui131/events-logistics:<commit>` so a deployment can be traced to its source. Jenkins uses temporary Docker login configuration and removes it after pushing. The same pipeline can deploy its image into kind without needing a public registry.

For a scan from the host, set `SONAR_TOKEN` in the current process from the private `.env`, then run `mvn verify sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.qualitygate.wait=true`. Do not put tokens in command arguments or screenshots. Nexus credentials use `devops/maven/settings.xml`, whose password references an environment variable. Within Docker, service names such as `nexus` and `sonarqube` resolve on the lab network; from the host use `localhost`.

## Kubernetes and Terraform

```powershell
./scripts/Install-LabTools.ps1
./scripts/New-LabCluster.ps1
./scripts/Deploy-LabKubernetes.ps1
./scripts/Invoke-LabTerraform.ps1 -Action Plan
./scripts/Invoke-LabTerraform.ps1 -Action Apply
```

Tool downloads are checksum verified and stay in `.local/bin`. The cluster uses the existing lab Docker network, with separate host/container kubeconfigs under `.local/kube`; it does not replace your default kubeconfig. The manifest deployment uses namespace `events`, two app replicas, MySQL StatefulSet/PVC, ConfigMap/Secret, startup/readiness/liveness probes, resource limits, and a non-root app process. Terraform uses namespace `events-terraform` and owns its own ConfigMap, Secret, PVC, MySQL/app deployments and Services. Don't apply Kubernetes YAML into Terraform's namespace.

The Terraform provider version and dependency lock are committed. Passwords are supplied through `TF_VAR_*` environment variables, marked sensitive, and removed from the script's process environment afterward. Local Terraform state and saved plans contain secrets even when CLI output is redacted; they are ignored by Git and must stay private. Keep the state to demonstrate a second plan with no changes.

To demonstrate Kubernetes recovery, delete one **application pod** in `events` and show the Deployment replacing it. To demonstrate database persistence, restart the application and rerun the date query. Node-local storage in kind survives pod recreation, but deleting the kind cluster deletes its local storage. This is a single-machine learning environment, not a highly available production cluster.

## Monitoring and evaluation

Prometheus scrapes the Compose, Kubernetes and Terraform applications every 15 seconds. Grafana is provisioned with availability, traffic, server errors, response time, JVM heap, DB connections and CPU panels. Generate traffic with the smoke tests; allow a couple of minutes for rate panels. Targets for environments you haven't started will be down, and their availability alerts will fire. Alert rules are included; external notification routing is not configured.

See [the evaluation checklist](docs/EVALUATION.md) and [the defense guide](docs/DEFENSE.md). The supplied rubric's printed weights sum to 20.5 despite a `/20` total; confirm the marking convention with the instructor.

Useful references: [Jenkins in Docker](https://www.jenkins.io/doc/book/installing/docker/), [SonarQube container setup](https://docs.sonarsource.com/sonarqube-community-build/server-installation/from-docker-image/set-up-and-start-container), [kind quick start](https://kind.sigs.k8s.io/docs/user/quick-start/), and [Terraform Kubernetes deployment resource](https://registry.terraform.io/providers/hashicorp/kubernetes/2.38.0/docs/resources/deployment_v1).
