# Evaluation evidence

This checklist maps the supplied project instructions and evaluation grid to this branch. A file alone is not proof that its service ran: keep the successful histories and demonstrate the live system.

| Criterion | Printed points | Evidence to show |
|---|---:|---|
| Jenkins pipeline | 2 | Successful `logistics-local` stages, test report, archived JAR/coverage, stopped stages on failure |
| Git Flow | 1 | Personal incremental logistics commits; team main/develop/module branches; reviewed integration PR when agreed |
| JUnit | 1 | Mockito tests, real H2 persistence tests, MVC tests, passing Maven output |
| Sonar | 1 | Private `events-logistics` project, quality gate, findings and imported JaCoCo coverage |
| Nexus | 1 | Authenticated `events-snapshots` and `events-releases` repositories; published timestamped snapshot JAR/POM |
| Docker | 1 | Multi-stage non-root application image; running tools/app/database; commit-tagged Docker Hub image |
| Kubernetes | 3 | Ready node, app replicas, MySQL StatefulSet and PVC, config/secret references, probes, Service, rollout and pod replacement |
| Terraform | 2 | Pinned provider/lock; fmt/validate; saved plan; successful apply; resources/state; second plan with no changes |
| Grafana/Prometheus | 0.5 | Healthy scrape targets, application metrics, provisioned dashboard, evaluated alert rules |
| Excellence | 3 | Candidates: reproducible bootstrapping, blocking quality/coverage gates, immutable commit tags, safe error responses, recovery demonstration. Instructor decides credit. |
| Questions/source code | 5 | Explain logistics ownership, transactions, test choices, pipeline, container networking, secrets, Kubernetes and Terraform |

## Individual contribution

- The logistics entity and repository come from the starter project.
- The new logistics service owns creation/assignment and reserved-date retrieval.
- Existing event service methods delegate to it, preserving the group's API contracts.
- Tests and logs are attributable through separate personal commits.
- Infrastructure is reusable by the group. This branch doesn't claim the teammate's `event` tests or changes as personal work.
- `calculCout` and participant/event behavior outside logistics delegation remain inherited code. Coordinate future changes to these shared areas before team integration.

## Before validation

1. Check `git status`, the personal log, and the intended branch. Push only the reviewed logistics branch when ready; don't force-push or merge others' branches during the demonstration.
2. Keep a successful Jenkins run, Sonar analysis, Nexus artifact and Docker Hub image. Match the image tag to the commit shown in Git.
3. Start the required containers and check health before the evaluation. Preserve named volumes and Terraform state.
4. Open Jenkins, Sonar, Nexus, Docker Hub, Grafana and the coverage report in advance.
5. Rehearse the module smoke test and the Kubernetes recovery demonstration. Know which commands create demo data.
6. Avoid source edits, folder removal, volume deletion and infrastructure teardown on the validation day, as requested in the project instructions.

Optional project suggestions such as Angular and microservices are not required to implement this plan. Webhooks/email notifications need separate external routing/account configuration; no notification messages are sent by this lab.
