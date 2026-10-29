# CI/CD : configuration préparée, exécution encore à vérifier

La branche locale Tache contient les sources récentes ; elle n'est pas encore publiée
sur origin. Aucun push ni artefact externe publié par l'agent. Un Jenkinsfile préparé
ne constitue pas une exécution Jenkins réussie.

## Ressources et lancement progressif

Compose sépare les profils app, jenkins, sonar, nexus. Ne pas lancer tous les profils
sur cette machine sans surveiller `docker stats` et la RAM Windows.
Jenkins a besoin de 1 Gio maximum, Sonar 2 Gio et Nexus 1,5 Gio, en plus du cluster.
Les images Sonar/Nexus utilisent actuellement des tags flottants : configuration de
démarrage préparée, à verrouiller par digest après le premier lancement vérifié.
Sonar utilise ici sa base embarquée : démonstration locale seulement ; ses volumes sont
persistants. L'option de vérification Elasticsearch est désactivée pour ce laboratoire.

```powershell
docker compose -f docker-compose.devops.yml --profile nexus up -d nexus
docker compose -f docker-compose.devops.yml --profile sonar up -d sonar
docker compose -f docker-compose.devops.yml --profile jenkins up -d --build jenkins
```

Ces commandes restent à exécuter. Ports locaux : Jenkins 8080, Sonar 9000, Nexus 8081.
La création des utilisateurs et credentials doit être réalisée avant les publications.

## Jenkins et Docker

ci/jenkins.Dockerfile installe Maven, Docker CLI et kubectl ; JDK 21 exécute Jenkins,
JDK 17 exécute Maven dans la pipeline. L'utilisateur du conteneur reste jenkins.
Le groupe supplémentaire correspond au groupe du socket Docker (0 par défaut sur ce
Docker Desktop ; à vérifier avant lancement).

L'accès au socket permet de contrôler le daemon Docker et équivaut à un accès privilégié
à son hôte Linux. Ce choix est limité au laboratoire personnel, ports liés à localhost,
jobs de confiance et aucune exécution de PR non fiable. Un agent éphémère dédié serait
préférable sur une infrastructure partagée. Le montage seul ne fournit pas le client
Docker : il est installé explicitement dans l'image.

## Intégrations

Jenkinsfile contient checkout, compile, tests/JaCoCo, Sonar, quality gate, package,
publication Nexus, image Docker, publication et déploiement/vérification Kubernetes.
Les opérations externes restent désactivées par défaut. Un build local peut donc
passer sans démontrer Sonar, Nexus ou déploiement ; le suivi d'évaluation doit le préciser.

- Configurer SonarQube dans Jenkins sous `local-sonar`, token dans Jenkins Credentials.
  Webhook Sonar : `http://jenkins:8080/sonarqube-webhook/` (slash final nécessaire).
  Activer RUN_SONAR pour exécuter analyse puis gate ; aucun gate encore constaté.
- Nexus : dépôts maven-snapshots et maven-releases, utilisateur dédié avec droits de
  publication. Credential Jenkins `nexus` (username/password).
  `ci/settings.xml` lit les variables sans contenir de secret. Coordonnées attendues :
  tn.esprit:eventsProject:1.0.0-SNAPSHOT.
- Docker Hub : credential `dockerhub`, DOCKER_REPOSITORY=compte/depot, publication
  explicitement autorisée avant activation. Tags immuables basés sur le commit.
- Kubernetes : credential fichier `kubeconfig`, contexte `kind-events-tache` joignable
  depuis l'agent. Le kubeconfig local pointe sur une adresse loopback : il faut adapter
  l'adresse pour un agent conteneurisé et conserver la vérification du certificat.
  Ne pas remplacer cela par insecure-skip-tls-verify.
- Déploiement exige une image publiée et vérifie rollout puis readiness HTTP avec DB.
  La base et le Secret doivent déjà exister ; la pipeline ne supprime pas de PVC.

Après publication autorisée de la branche, créer un job Pipeline depuis SCM, branche
Tache, chemin Jenkinsfile. Un webhook GitHub peut pointer vers /github-webhook/ si
Jenkins est accessible ; sans tunnel, utiliser Build Now ou un polling SCM configuré.
Une URL ngrok n'est pas nécessaire pour le premier build manuel local.

## Mise à jour d'exécution
Nexus a réellement démarré et reçu le livrable. Le JAR téléchargé a le même SHA-256.
L'EULA obligatoire a été acceptée avec accord utilisateur. Le script refuse désormais
une instance non acceptée plutôt que de consentir automatiquement.
Jenkins/Sonar : téléchargements/builds interrompus par manque de disque hôte.
L'image Nexus est maintenant épinglée par digest ; Sonar reste une configuration préparée.

Analyse Sonar préparée : scanner Maven 5.8.0.7211 épinglé, Java 21 pour cette étape
(JAVA_HOME /opt/java/openjdk), build/test sous Java 17. Token transmis par environnement,
provisionnement d'un autre JRE désactivé. Exécution toujours en attente d'espace disque.
Référence : https://docs.sonarsource.com/sonarqube-community-build/analyzing-source-code/scanners/sonarscanner-for-maven.
