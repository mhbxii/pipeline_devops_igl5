# Contenu visible des diapositives

## 1. Contribution Tache

eventsProject : gestion des rôles des participants. Diagnostic, tests et déploiement progressifs.

Preuve : Page de garde

## 2. Un périmètre respecté

Tache est un enum persisté en chaîne dans Participant ; le modèle partagé est conservé.

Preuve : 01-project-setup/setup-02-tache-model.png

## 3. Une branche dédiée et synchronisée

Branche Tache publiée sur origin après accord ; historique préservé et révisions locale/distante identiques.

Preuve : 05-git/git-01-branch-history.png

## 4. Maven et Java

Maven 3.9.14 sélectionné pour la session ; cible Java 8 et runtime Docker Java 17.

Preuve : 02-maven/maven-01-toolchain.png

## 5. La compilation initiale

Journal sauvegardé : BUILD SUCCESS pour les 12 sources du dépôt initial.

Preuve : 02-maven/maven-02-baseline-build.png

## 6. Le blocage initial : MySQL

Journal du diagnostic initial : authentification root refusée et HTTP 500.

Preuve : 01-project-setup/setup-03-database-blocker.png

## 7. MySQL : correction vérifiée

Base dédiée sur 3307 : création, affectation, lecture et retrait du rôle réussis.

Preuve : 01-project-setup/mysql-api-success.png

## 8. 38 tests automatisés

Service Mockito, contrats HTTP, JPA/H2 sans mocks et mapping sécurisé des anciennes entrées.

Preuve : 08-jenkins/jenkins-tests.png

## 9. Sécuriser les entrées existantes

Quatre entrées utilisent des DTO : identifiants générés et coût calculé protégés ; compatibilité HTTP/MySQL vérifiée.

Preuve : 12-final-validation/legacy-dto-api.png

## 10. Une analyse Sonar réellement évaluée

Gate OK : nouveau code couvert à 81,9 %, duplication nulle et aucune violation nouvelle.

Preuve : 06-sonarqube/sonar-final-new-code.png

## 11. Une qualité mesurée sans masquer le périmètre

Zéro vulnérabilité après correction des quatre entrées ; couverture globale 49,6 %, distincte du nouveau code.

Preuve : 06-sonarqube/sonar-final-dashboard.png

## 12. Une chaîne Jenkins exécutée

Build #9 automatique réussi : 38 tests, gate Sonar, Nexus, image Docker Hub, rollout Kubernetes et vérification HTTP.

Preuve : 08-jenkins/jenkins-pipeline-overview.png

## 13. GitHub déclenche Jenkins automatiquement

Le polling détecte le commit GitHub f510b9b et lance le build #9 : cause SCM enregistrée, sans lancement manuel.

Preuve : 08-jenkins/github-trigger.png

## 14. Une couverture métier mesurée

TacheService : 24 lignes et 4 branches couvertes ; ce résultat concerne ce service.

Preuve : 04-unit-tests/coverage-service.png

## 15. Des logs utiles

DEBUG, INFO, WARN sur API ; ERROR démontré par une panne simulée dans un test.

Preuve : 03-logging/tache-levels.png

## 16. Application et MySQL en conteneurs

Cycle HTTP vérifié dans Docker ; readiness UP et utilisateur applicatif non privilégié.

Preuve : 09-docker/docker-api-success.png

## 17. Vérifier les conteneurs

Application et base isolées ; volumes persistants et ports liés à localhost.

Preuve : 09-docker/docker-containers.png

## 18. Une image publique vérifiée

ayyoub1/events-tache : manifeste linux/amd64 accessible sans compte et identique à l'image locale du build #9.

Preuve : 09-docker/dockerhub-repository.png

## 19. Un déploiement Kubernetes réel

Pods applicatif et MySQL prêts, Services actifs et volume persistant lié.

Preuve : 10-kubernetes/kubernetes-resources.png

## 20. Vérifier le service métier

Le cycle HTTP complet réussit via Kubernetes ; readiness inclut la base MySQL.

Preuve : 10-kubernetes/kubernetes-api-success.png

## 21. Provisionner avec Terraform

Plan examiné et approuvé : 6 ressources Prometheus créées, aucune destruction.

Preuve : 11-terraform/terraform-apply.png

## 22. Contrôler la convergence

Un nouveau plan ne détecte aucune différence entre configuration et infrastructure.

Preuve : 11-terraform/terraform-no-drift.png

## 23. Observer une application réellement collectée

Prometheus collecte la cible Kubernetes events : 1/1 UP, métriques exposées par Actuator.

Preuve : 13-monitoring/prometheus-targets.png

## 24. Visualiser les métriques dans Grafana

Dashboard provisionné : cible UP, CPU Java, mémoire JVM et débit HTTP ; quatre requêtes réelles vérifiées.

Preuve : 13-monitoring/grafana-dashboard.png

## 25. Publier le livrable dans Nexus

Le dépôt Maven snapshots contient le JAR et le POM de tn.esprit:eventsProject:1.0.0-SNAPSHOT.

Preuve : 07-nexus/nexus-repository.png

## 26. Relier le build au programme exécuté

Même SHA-256 pour le JAR archivé par Jenkins, téléchargé de Nexus et présent dans le pod Kubernetes.

Preuve : 12-final-validation/local-delivery.png
