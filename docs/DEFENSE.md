# Defense rehearsal

## A short demonstration

1. Show the `logistics` branch and several meaningful commits. Open `LogisticsService`, then one Mockito test and one H2 persistence test.
2. Explain the original defects: the relationship was not reliably saved, and date retrieval returned null at the first empty event. Show the regression tests and the transaction boundary.
3. Show the Maven verification result and JaCoCo report. Explain that module coverage is enforced, while the full report also contains group code.
4. Open a successful Jenkins build. Follow the same commit through tests, the Sonar quality gate, the Nexus snapshot and the Docker image tag.
5. Run `python scripts/smoke-test.py`. Explain that it creates a demonstration event and a reserved projector, then verifies retrieval and invalid cases.
6. Show `kubectl --kubeconfig .local/kube/host-config -n events get pods,svc,pvc`. Delete one app pod, watch its replacement, then call the app on port 8090.
7. Show Terraform's separate namespace and a plan reporting no changes. Explain how state connects configuration to actual resources.
8. Open Grafana, select an environment, run the smoke test to generate traffic, and show Prometheus targets and the alert rules.

## Questions to prepare

**Why use a transaction?** Creating the logistics record and updating the event's owning relationship are one unit. They must commit together; otherwise a failed assignment can leave inconsistent data.

**Why Mockito and H2?** Mockito isolates business decisions and checks interactions. H2 exercises actual JPA mappings and SQL queries without mocks. MySQL smoke tests cover a real deployment database because H2 isn't identical to MySQL.

**Why does an empty event not stop the date query?** It contributes no logistics. Other events in the interval may still contain reserved items.

**What does the interval mean?** Both bounds are inclusive and apply to an event's start date. It is not an overlap search on the full event duration.

**Why separate the logistics service?** It makes this student's module clear, gives it an explicit transaction boundary, and preserves existing endpoints through delegation.

**What does Sonar do that JUnit doesn't?** JUnit executes examples and assertions. Sonar performs static analysis and imports coverage. The quality gate blocks delivery when its conditions fail; it doesn't prove the application has no bugs.

**Nexus versus Docker Hub?** Nexus stores the Maven JAR/POM artifacts. Docker Hub stores the application container image. Both need credentials for this project's publishing flow.

**Why aren't container URLs localhost?** Inside a container, localhost refers to that container. Jenkins reaches Sonar and Nexus by Docker network service names. The browser reaches their published host ports.

**Readiness versus liveness versus startup?** Startup allows initialization time. Readiness controls whether the Service sends traffic to the pod. Liveness detects a process that needs restarting; transient database unavailability should not by itself force repeated process restarts.

**Why a PVC?** Container filesystems are disposable. A PVC lets database data survive database-pod recreation. The local kind cluster still shares one machine and isn't a production disaster-recovery solution.

**Why Terraform as well as YAML?** YAML demonstrates Kubernetes resources directly. Terraform demonstrates desired infrastructure, a reviewed plan, apply, state and drift detection in a separately owned namespace.

**Where are secrets?** Local `.env`, Jenkins' credential store, Kubernetes Secrets, and private Terraform state. Kubernetes Secret encoding is not encryption. Nothing sensitive should be committed or included in screenshots.

**What is Git Flow here?** The team has main/develop/module branches. Personal work is committed on logistics, reviewed, then integrated through the agreed team process. A branch name alone isn't proof of review or release discipline.
