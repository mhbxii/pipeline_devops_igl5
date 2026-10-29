# Captures et diapositives verifiees

Captures authentiques Windows ou Edge/Playwright, aucune note orateur.
Les journaux sauvegardes sont identifies comme tels.

| Slide | Titre | Capture sous docs/evidence | Description visible |
|---|---|---|---|
| 1 | Contribution Tache | Page de garde | eventsProject : gestion des rôles des participants. Diagnostic, tests et déploiement progressifs. |
| 2 | Un périmètre respecté | 01-project-setup/setup-02-tache-model.png | Tache est un enum persisté en chaîne dans Participant ; le modèle partagé est conservé. |
| 3 | Une branche dédiée | 05-git/git-01-branch-history.png | Branche locale Tache et commits vérifiés ; synchronisation distante encore à réaliser. |
| 4 | Maven et Java | 02-maven/maven-01-toolchain.png | Maven 3.9.14 sélectionné pour la session ; cible Java 8 et runtime Docker Java 17. |
| 5 | La compilation initiale | 02-maven/maven-02-baseline-build.png | Journal sauvegardé : BUILD SUCCESS pour les 12 sources du dépôt initial. |
| 6 | Le blocage initial : MySQL | 01-project-setup/setup-03-database-blocker.png | Journal du diagnostic initial : authentification root refusée et HTTP 500. |
| 7 | MySQL : correction vérifiée | 01-project-setup/mysql-api-success.png | Base dédiée sur 3307 : création, affectation, lecture et retrait du rôle réussis. |
| 8 | 38 tests automatisés | 08-jenkins/jenkins-tests.png | Service Mockito, contrats HTTP, JPA/H2 sans mocks et mapping sécurisé des anciennes entrées. |
| 9 | Sécuriser les entrées existantes | 12-final-validation/legacy-dto-api.png | Quatre entrées utilisent des DTO : identifiants générés et coût calculé protégés ; compatibilité HTTP/MySQL vérifiée. |
| 10 | Une analyse Sonar réellement évaluée | 06-sonarqube/sonar-final-new-code.png | Gate OK : nouveau code couvert à 81,9 %, duplication nulle et aucune violation nouvelle. |
| 11 | Une qualité mesurée sans masquer le périmètre | 06-sonarqube/sonar-final-dashboard.png | Zéro vulnérabilité après correction des quatre entrées ; couverture globale 49,6 %, distincte du nouveau code. |
| 12 | Une chaîne Jenkins exécutée | 08-jenkins/jenkins-pipeline-overview.png | Build #7 réussi : 38 tests, gate Sonar, Nexus, image publique Docker Hub, rollout Kubernetes et vérification HTTP. |
| 13 | Une couverture métier mesurée | 04-unit-tests/coverage-service.png | TacheService : 24 lignes et 4 branches couvertes ; ce résultat concerne ce service. |
| 14 | Des logs utiles | 03-logging/tache-levels.png | DEBUG, INFO, WARN sur API ; ERROR démontré par une panne simulée dans un test. |
| 15 | Application et MySQL en conteneurs | 09-docker/docker-api-success.png | Cycle HTTP vérifié dans Docker ; readiness UP et utilisateur applicatif non privilégié. |
| 16 | Vérifier les conteneurs | 09-docker/docker-containers.png | Application et base isolées ; volumes persistants et ports liés à localhost. |
| 17 | Une image publique vérifiée | 09-docker/dockerhub-repository.png | ayyoub1/events-tache : manifeste linux/amd64 accessible sans compte et identique à l'image locale du build #7. |
| 18 | Un déploiement Kubernetes réel | 10-kubernetes/kubernetes-resources.png | Pods applicatif et MySQL prêts, Services actifs et volume persistant lié. |
| 19 | Vérifier le service métier | 10-kubernetes/kubernetes-api-success.png | Le cycle HTTP complet réussit via Kubernetes ; readiness inclut la base MySQL. |
| 20 | Provisionner avec Terraform | 11-terraform/terraform-apply.png | Plan examiné et approuvé : 6 ressources Prometheus créées, aucune destruction. |
| 21 | Contrôler la convergence | 11-terraform/terraform-no-drift.png | Un nouveau plan ne détecte aucune différence entre configuration et infrastructure. |
| 22 | Observer une application réellement collectée | 13-monitoring/prometheus-targets.png | Prometheus collecte la cible Kubernetes events : 1/1 UP, métriques exposées par Actuator. |
| 23 | Visualiser les métriques dans Grafana | 13-monitoring/grafana-dashboard.png | Dashboard provisionné : cible UP, CPU Java, mémoire JVM et débit HTTP ; quatre requêtes réelles vérifiées. |
| 24 | Publier le livrable dans Nexus | 07-nexus/nexus-repository.png | Le dépôt Maven snapshots contient le JAR et le POM de tn.esprit:eventsProject:1.0.0-SNAPSHOT. |
| 25 | Relier le build au programme exécuté | 12-final-validation/local-delivery.png | Même SHA-256 pour le JAR archivé par Jenkins, téléchargé de Nexus et présent dans le pod Kubernetes. |
