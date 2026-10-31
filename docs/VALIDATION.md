# Verified delivery - 2026-10-29

Observed on the local Docker Desktop lab. Histories and private evidence remain in named volumes and `.local/`.

| Check | Result |
|---|---|
| Maven/JUnit | 26 tests, 0 failures/errors/skips; Mockito, H2 persistence and MVC suites |
| Logistics coverage | 100% lines (37/37), 100% branches (36/36) |
| Jenkins | Build #3 SUCCESS, all delivery stages enabled |
| Sonar | Quality gate OK; JaCoCo coverage imported |
| Nexus | Authenticated snapshot JAR/POM published |
| Docker Hub | `arfaoui131/events-logistics:0eb1979e6ace` published |
| Kubernetes | Two app replicas, persistent MySQL, rollout and live API smoke test passed |
| Terraform | 8 resources created; second plan reports no changes; live API passed |
| Recovery | App/MySQL pods replaced; same PVC and original logistics data preserved |
| Monitoring | All 3 application targets and Prometheus self-scrape UP; Grafana dashboard provisioned |
| Credentials | Tracked source scan passed; local credentials, Terraform state and plans ignored |

The successful pipeline checked out source commit `0eb1979e6ace`. Later evidence/documentation commits do not change the tested application.

- Jenkins run: http://localhost:8082/job/logistics-local/3/
- Sonar: http://localhost:9000/dashboard?id=events-logistics
- Nexus: http://localhost:8081/
- Docker Hub tags: https://hub.docker.com/r/arfaoui131/events-logistics/tags
- Grafana: http://localhost:3000/d/events-logistics.
- App health: ports 8089 (Compose), 8090 (Kubernetes), 8091 (Terraform), path `/events/actuator/health`

Published registry digests:

- `arfaoui131/events-logistics@sha256:c4d5404d2468f008fae2ed6f4fdd01c96dcb49976dea5511094b7876964c49b6`

Reproduce the live snapshot with `python scripts/collect-evidence.py`. It records unavailable checks instead of assuming success. Jenkins builds #1 and #2 retain setup failures and their fixes in the histories.

Team review/integration into develop/main and the individual oral defense remain human steps. No other team branch is merged automatically.
