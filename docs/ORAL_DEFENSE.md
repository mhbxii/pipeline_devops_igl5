# Préparation orale

## Débutant
**Tache est-elle une table ?** Non. C'est un enum Java, stocké comme chaîne dans Participant.
**Que fait pom.xml ?** Il définit les dépendances, la cible Java et les plugins Maven.
**Que fait application.properties ?** Il configure HTTP, JDBC, JPA et les logs.

## Intermédiaire
**Pourquoi Mockito ?** Pour tester les décisions du service indépendamment de la base.
**Pourquoi un test d'intégration ?** Pour vérifier aussi le mapping JPA et les échanges réels.
**Que fait EventRestController ?** Il transforme les requêtes HTTP en appels métier.
**Que fait ParticipantRepository ?** Spring Data fournit les opérations de persistance.

## Avancé
**Pourquoi ne pas transformer Tache en entité ?** Cela changerait le modèle partagé
et le stockage existant sans nécessité fonctionnelle.
**Un port MySQL ouvert prouve-t-il le fonctionnement ?** Non : authentification,
schéma et requêtes doivent encore être vérifiés.
**Un pod créé prouve-t-il le déploiement ?** Non : readiness et requête applicative requises.

Démonstration actuelle : ouvrir Tache.java, Participant.java et suivre le champ
jusqu'au filtre de EventRepository. Si l'enum change sans migration, les anciennes
valeurs persistées peuvent devenir illisibles.

## Contribution Tache implementee
TacheService.java : valide les identifiants et roles, lit ParticipantRepository et modifie
uniquement Participant.tache dans une transaction. Un role nul est rejete en PUT ; DELETE
retire le role et conserve le participant. @Transactional assure commit/rollback.
TacheRestController.java : DTO participantId/tache, routes /events/taches ; pas de graphe JPA
serialise, donc pas de recursion Participant/Event sur cette API.
TacheExceptionHandler.java : 400 invalide, 404 absent, 500 inattendu avec message public neutre.
Portee limitee au nouveau controleur pour preserver les autres endpoints.
TacheServiceTest : dependance repository simulee, validation avant acces, aucune suppression.
TachePersistenceTest : vraie base H2, flush/clear force une relecture depuis la base.
TacheRestControllerTest : contrat HTTP, enum inconnu et panne simulee sans fuite du message.
Pourquoi garder un test MySQL ? H2 ne garantit pas la compatibilite SQL du serveur reel.
Pourquoi deux ports ? 3306 etait deja utilise ; 3307 isole le projet.
Pourquoi les mots de passe ne sont-ils pas dans Git ? .env est genere localement et ignore.
Log4j2 : backend ; SLF4J : API conservee dans les classes. Une seule implementation active.
ERROR est demontre par un test de panne simulee, pas par une panne de production inventee.

## Fichiers d'infrastructure : comprendre et démontrer

| Fichier | Rôle et interaction | Effet d'une erreur / démonstration |
|---|---|---|
| Dockerfile | Copie le JAR dans un JRE 17, UID 10001 | Mauvais chemin : build impossible ; vérifier docker image inspect et HTTP. |
| docker-compose.devops.yml | Réseaux, profils, volumes, ports et variables | Mauvais port DB : JDBC échoue ; vérifier healthcheck puis test API. |
| application-local.properties | Connexion dédiée sur 3307 et secret d'environnement | DB_PASSWORD absent : démarrage impossible ; ne pas afficher sa valeur. |
| application.properties | Contexte HTTP, JPA et endpoints Actuator | Mauvaise probe : pod non prêt ; interroger readiness. |
| Jenkinsfile | Stages, rapports, credentials et conditions de publication | Une commande en échec bloque la suite ; aucun build réel encore validé. |
| ci/jenkins.Dockerfile | Fournit outils CLI, JDK 21 contrôleur et JDK 17 build | Socket seul insuffisant ; sans CLI, docker introuvable. |
| ci/bootstrap.groovy | Compte local authentifié et job SCM | Sans mot de passe généré : arrêt du bootstrap ; aucun accès anonyme autorisé. |
| ci/settings.xml et profil nexus du POM | Associent identifiants serveurs et credentials d'environnement | Mauvais id serveur : authentification Maven absente ; un déploiement réel a réussi. |
| k8s/application.yaml | Deployment, ConfigMap et Service, probes et limites | Service sans endpoints si labels incohérents ; vérifier pods et HTTP via Service. |
| k8s/mysql.yaml | Base, volume persistant, Service et stratégie Recreate | Deux écrivains DB dangereux ; Recreate évite leur chevauchement. |
| terraform/main.tf | Déclare six ressources Prometheus et leurs dépendances | Un changement produit un plan à examiner ; après apply, plan sans changement. |
| .terraform.lock.hcl | Verrouille la version et les empreintes du provider | Sans verrou, résolution différente possible à l'init. |
| scripts/Publish-LocalNexus.ps1 | Publie, télécharge puis compare le JAR | Un fichier visible ne suffit pas : comparer SHA-256 prouve son contenu. |
| scripts/Compact-DockerDisk.ps1 | Intervention hôte administrateur préparée | Arrête toutes les WSL ; aucun compactage effectué par l'agent. |

### Questions intermédiaires
**Pourquoi deux JDK dans Jenkins ?** Le contrôleur et le projet ont des contraintes de
compatibilité différentes. Le build utilise explicitement /opt/java17.
**Pourquoi un dépôt Git local en lecture seule ?** Jenkins peut tester les commits sans
publier la branche ni exposer .env ; le vrai webhook distant reste à démontrer.
**Le bootstrap est-il validé par un build ?** Non, l'image a été interrompue par manque de disque.
**Pourquoi le Secret n'est-il pas dans YAML ?** Il est construit en mémoire et envoyé à kubectl ;
le fichier source .env et le kubeconfig sont ignorés par Git.
**Terraform gère-t-il toute l'infrastructure ?** Non : Prometheus et son namespace uniquement.
Le cluster et l'application sont gérés par kind et les manifests documentés.

### Questions avancées
**Liveness doit-elle inclure MySQL ?** Non, une panne DB ne doit pas provoquer des redémarrages
inutiles de l'application ; readiness inclut DB pour retirer le trafic lorsque nécessaire.
**Pourquoi /tmp est-il un volume avec un filesystem racine en lecture seule ?** Le serveur Java
peut avoir besoin de fichiers temporaires, tout en conservant le reste de l'image immuable.
**Le socket Docker est-il sûr parce que Jenkins n'est pas root ?** Non : contrôler le daemon
permet des opérations privilégiées ; laboratoire local, jobs de confiance uniquement.
**Pourquoi ne pas partager le state Terraform ?** Il décrit l'infrastructure et peut contenir
des données sensibles ; il reste local ici, un backend sécurisé serait nécessaire en équipe.
**Pourquoi Nexus a-t-il retourné 403 malgré les bons droits ?** L'édition installée imposait
l'acceptation de l'EULA, explicitement approuvée par l'utilisateur avant publication.
Le scanner Sonar préparé utilise Java 21 ; compiler/tester le projet reste sous Java 17.
Le contrôleur et l'étape Sonar peuvent donc partager le JDK 21 sans modifier le JDK du build.
