# Contenu visible des diapositives

Source : slides.json. Aucune note orateur dans le PowerPoint.

## 1. Contribution Tache

eventsProject : gestion des rôles des participants. Diagnostic, tests et déploiement progressifs.

Preuve : page de garde.

## 2. Un périmètre respecté

Tache est un enum persisté en chaîne dans Participant ; le modèle partagé est conservé.

Preuve : 01-project-setup/setup-02-tache-model.png.

## 3. Une branche dédiée

Branche locale Tache et commits vérifiés ; synchronisation distante encore à réaliser.

Preuve : 05-git/git-01-branch-history.png.

## 4. Maven et Java

Maven 3.9.14 sélectionné pour la session ; cible Java 8 et runtime Docker Java 17.

Preuve : 02-maven/maven-01-toolchain.png.

## 5. La compilation initiale

Journal sauvegardé : BUILD SUCCESS pour les 12 sources du dépôt initial.

Preuve : 02-maven/maven-02-baseline-build.png.

## 6. Le blocage initial : MySQL

Journal du diagnostic initial : authentification root refusée et HTTP 500.

Preuve : 01-project-setup/setup-03-database-blocker.png.

## 7. MySQL : correction vérifiée

Base dédiée sur 3307 : création, affectation, lecture et retrait du rôle réussis.

Preuve : 01-project-setup/mysql-api-success.png.

## 8. Des tests à trois niveaux

20 tests réussis : Mockito, contrat HTTP et intégration JPA/H2 sans mocks.

Preuve : 04-unit-tests/tests-success.png.

## 9. Une couverture métier mesurée

TacheService : 24 lignes et 4 branches couvertes ; ce résultat concerne ce service.

Preuve : 04-unit-tests/coverage-service.png.

## 10. Des logs utiles

DEBUG, INFO, WARN sur API ; ERROR démontré par une panne simulée dans un test.

Preuve : 03-logging/tache-levels.png.

## 11. Application et MySQL en conteneurs

Cycle HTTP vérifié dans Docker ; readiness UP et utilisateur applicatif non privilégié.

Preuve : 09-docker/docker-api-success.png.

## 12. Vérifier les conteneurs

Application et base isolées ; volumes persistants et ports liés à localhost.

Preuve : 09-docker/docker-containers.png.

## 13. Un déploiement Kubernetes réel

Pods applicatif et MySQL prêts, Services actifs et volume persistant lié.

Preuve : 10-kubernetes/kubernetes-resources.png.

## 14. Vérifier le service métier

Le cycle HTTP complet réussit via Kubernetes ; readiness inclut la base MySQL.

Preuve : 10-kubernetes/kubernetes-api-success.png.

## 15. Provisionner avec Terraform

Plan examiné et approuvé : 6 ressources Prometheus créées, aucune destruction.

Preuve : 11-terraform/terraform-apply.png.

## 16. Contrôler la convergence

Un nouveau plan ne détecte aucune différence entre configuration et infrastructure.

Preuve : 11-terraform/terraform-no-drift.png.

## 17. Observer une application réellement collectée

Prometheus collecte la cible Kubernetes events : 1/1 UP, métriques exposées par Actuator.

Preuve : 13-monitoring/prometheus-targets.png.

## 18. Publier le livrable dans Nexus

Le dépôt Maven snapshots contient le JAR et le POM de tn.esprit:eventsProject:1.0.0-SNAPSHOT.

Preuve : 07-nexus/nexus-repository.png.

## 19. Vérifier le livrable publié

Le JAR téléchargé depuis Nexus possède le même SHA-256 que le livrable local.

Preuve : 07-nexus/nexus-verification.png.
