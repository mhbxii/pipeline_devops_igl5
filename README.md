# eventsProject — contribution Tache

Application Spring Boot existante. `Tache` est un enum des rôles de participants.
La contribution ajoute leur consultation, affectation, remplacement et retrait.

## Démarrage local

Prérequis constatés : Docker Desktop, PowerShell, JDK 8 et Maven local existants.
Le script de sélection Java est adapté aux chemins de cette machine.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Initialize-LocalDatabase.ps1
. ./scripts/Use-ProjectJava.ps1
mvn clean verify
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Start-LocalApplication.ps1
```

MySQL écoute sur `127.0.0.1:3307`. Les secrets aléatoires sont dans `.env`, ignoré par Git.
Ne pas supprimer le volume pour corriger un mot de passe : les variables MYSQL initialisent
uniquement un volume vide. Le serveur MySQL existant sur 3306 n'est pas modifié.

## Docker

Après `mvn package` :

```powershell
docker compose -f docker-compose.devops.yml --profile app up -d --build app
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Test-TacheApi.ps1
```

Arrêter l'application Java locale avant de lancer le conteneur sur le même port 8089.
Le conteneur utilise Java 17 et un utilisateur non privilégié.

## Contrat HTTP

Base : `http://localhost:8089/events`.

| Méthode / chemin | Comportement |
|---|---|
| GET /taches | Liste des quatre valeurs existantes |
| GET /taches/participants/{id} | `{participantId, tache}`, rôle nullable |
| PUT /taches/participants/{id} | Affecte/remplace via `{"tache":"SERVEUR"}` |
| DELETE /taches/participants/{id} | Retire le rôle, conserve le participant, HTTP 204 |
| POST /event/addPart | Endpoint historique de création du participant |
| GET /actuator/health/readiness | Inclut la connexion DB |
| GET /actuator/prometheus | Métriques pour le laboratoire local |

Identifiant invalide/rôle invalide : 400. Participant absent : 404. Erreur inattendue :
500 avec message public neutre. DELETE est idempotent pour un participant existant.
Il n'existe pas de création/suppression dynamique des valeurs de l'enum.

## Tests et preuves

`mvn test` : Mockito service, contrat MVC et intégration JPA/H2 sans mocks.
L'intégration H2 ne remplace pas le test réel MySQL : `scripts/Test-TacheApi.ps1`.
Ce script crée un participant de démonstration et le conserve avec un rôle vide.
Rapports : `target/surefire-reports/` et `target/site/jacoco/index.html`.

Documentation de reprise : [docs/CURRENT_STATE.md](docs/CURRENT_STATE.md).
Suivi du barème : [docs/EVALUATION_TRACKER.md](docs/EVALUATION_TRACKER.md).
CI/CD vérifiée localement : [docs/CI_SETUP.md](docs/CI_SETUP.md).
Présentation : `docs/presentation/eventsProject-Tache.pptx` ; descriptions visibles,
aucune note orateur. Captures réelles et scripts reproductibles dans `scripts/`.

## Infrastructure

`k8s/` contient le cluster kind, les Deployments, Services, ConfigMap et PVC.
Le Secret est créé en mémoire par `scripts/Deploy-LocalKubernetes.ps1` depuis `.env`.
`terraform/` gère les six ressources Prometheus appliquées après accord dans un namespace indépendant ; aucun apply sans
plan examiné et accord utilisateur. Les profils Jenkins/Sonar/Nexus restent séparés
pour maîtriser la mémoire. Consulter le suivi pour distinguer préparé et exécuté.

## Livraison publique verifiee

Jenkins #7 SUCCESS : 38 tests, Sonar/gate, Nexus, Docker Hub, Kubernetes et HTTP.
Image publique : [ayyoub1/events-tache](https://hub.docker.com/r/ayyoub1/events-tache),
tag `tache-b822c015d8ab-7`. Les JAR Jenkins, Nexus et pod sont identiques par SHA-256.
Details et limites : [docs/VERIFIED_DELIVERY.md](docs/VERIFIED_DELIVERY.md).
