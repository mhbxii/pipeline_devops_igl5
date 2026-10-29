# Captures et diapositives verifiees

Les PNG proviennent soit de fenetres Windows visibles affichant les sources/commandes
et journaux reels, soit de pages locales ouvertes dans Edge/Playwright.
Les journaux sauvegardes sont identifies comme tels ; aucune reussite inventee.
Manifeste des captures Windows : docs/evidence/01-project-setup/capture-manifest.json.
Le contenu affiche est conserve dans les fichiers *-displayed.txt.

| Slide | Titre | Capture sous docs/evidence | Description visible |
|---|---|---|---|
| 1 | Contribution Tache | Page de garde sans capture | eventsProject : gestion des rôles des participants. Diagnostic, tests et déploiement progressifs. |
| 2 | Un périmètre respecté | 01-project-setup/setup-02-tache-model.png | Tache est un enum persisté en chaîne dans Participant ; le modèle partagé est conservé. |
| 3 | Une branche dédiée | 05-git/git-01-branch-history.png | Branche locale Tache et commits vérifiés ; synchronisation distante encore à réaliser. |
| 4 | Maven et Java | 02-maven/maven-01-toolchain.png | Maven 3.9.14 sélectionné pour la session ; cible Java 8 et runtime Docker Java 17. |
| 5 | La compilation initiale | 02-maven/maven-02-baseline-build.png | Journal sauvegardé : BUILD SUCCESS pour les 12 sources du dépôt initial. |
| 6 | Le blocage initial : MySQL | 01-project-setup/setup-03-database-blocker.png | Journal du diagnostic initial : authentification root refusée et HTTP 500. |
| 7 | MySQL : correction vérifiée | 01-project-setup/mysql-api-success.png | Base dédiée sur 3307 : création, affectation, lecture et retrait du rôle réussis. |
| 8 | Des tests à trois niveaux | 04-unit-tests/tests-success.png | 20 tests réussis : Mockito, contrat HTTP et intégration JPA/H2 sans mocks. |
| 9 | Une couverture métier mesurée | 04-unit-tests/coverage-service.png | TacheService : 24 lignes et 4 branches couvertes ; ce résultat concerne ce service. |
| 10 | Des logs utiles | 03-logging/tache-levels.png | DEBUG, INFO, WARN sur API ; ERROR démontré par une panne simulée dans un test. |
| 11 | Application et MySQL en conteneurs | 09-docker/docker-api-success.png | Cycle HTTP vérifié dans Docker ; readiness UP et utilisateur applicatif non privilégié. |
| 12 | Vérifier les conteneurs | 09-docker/docker-containers.png | Application et base isolées ; volumes persistants et ports liés à localhost. |
| 13 | Un déploiement Kubernetes réel | 10-kubernetes/kubernetes-resources.png | Pods applicatif et MySQL prêts, Services actifs et volume persistant lié. |
| 14 | Vérifier le service métier | 10-kubernetes/kubernetes-api-success.png | Le cycle HTTP complet réussit via Kubernetes ; readiness inclut la base MySQL. |
| 15 | Provisionner avec Terraform | 11-terraform/terraform-apply.png | Plan examiné et approuvé : 6 ressources Prometheus créées, aucune destruction. |
| 16 | Contrôler la convergence | 11-terraform/terraform-no-drift.png | Un nouveau plan ne détecte aucune différence entre configuration et infrastructure. |
| 17 | Observer une application réellement collectée | 13-monitoring/prometheus-targets.png | Prometheus collecte la cible Kubernetes events : 1/1 UP, métriques exposées par Actuator. |
| 18 | Publier le livrable dans Nexus | 07-nexus/nexus-repository.png | Le dépôt Maven snapshots contient le JAR et le POM de tn.esprit:eventsProject:1.0.0-SNAPSHOT. |
| 19 | Vérifier le livrable publié | 07-nexus/nexus-verification.png | Le JAR téléchargé depuis Nexus possède le même SHA-256 que le livrable local. |

Reproduction : scripts/Capture-BaselineEvidence.ps1 (bureau Windows deverrouille),
scripts/Capture-ServiceDashboards.py (port-forward Prometheus et Nexus actifs),
scripts/Build-BaselinePresentation.ps1 puis scripts/Validate-Presentation.py.
Le fichier courant est eventsProject-Tache.pptx, avec descriptions visibles uniquement.
L'ancien fichier phase-01 conserve un instantane anterieur ; ne pas le confondre avec le courant.
