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
| Jenkinsfile | Stages, rapports, credentials et conditions de publication | Une commande en échec bloque la suite ; build #4 SUCCESS avec gate, Nexus et livraison kind. |
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
**Comment GitHub déclenche-t-il Jenkins ?** Le job lit directement la branche Tache
publique par HTTPS et vérifie les changements toutes les cinq minutes. Aucun token
de lecture ni tunnel entrant n'est nécessaire. Les commits limités à docs/ sont exclus.
Une cause SCMTriggerCause et le SHA checkouté prouvent le déclenchement automatique.
**Le bootstrap est-il validé par un build ?** Oui, build #4 réussi après reprise du stockage.
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

## Sécurité des anciennes entrées et preuve de livraison

**Pourquoi remplacer les entités dans les quatre entrées REST ?** La désérialisation
directe exposait des champs persistants non prévus. EventRequests.java autorise les
champs utiles, ignore les champs inconnus et reconstruit des entités ; les références
imbriquées ne fournissent que des IDs positifs. Les IDs générés et le coût calculé
ne peuvent pas être imposés par le client. Les routes et réponses sont conservées.

**Pourquoi 38 tests et aussi des appels MySQL ?** Les tests de mapping, MVC et service
vérifient des contrats et cas limites. H2 ne remplace pas MySQL : les quatre routes
historiques ont aussi été appelées par HTTP contre l'application déployée et MySQL.

**Pourquoi 81,9 % et 49,6 % ?** Le premier chiffre mesure le nouveau code, le second
le projet entier. Le service Tache est entièrement couvert ; les modules historiques
expliquent une couverture globale inférieure. Ne pas présenter un chiffre hors périmètre.

**Qu'est-ce qui prouve le gate ?** Des conditions réellement évaluées et un callback
Sonar signé reçu HTTP 200 par Jenkins. Le tout premier gate aux conditions vides
n'était qu'une baseline ; il n'est pas utilisé comme preuve finale.

**Le tag commit/build est-il immuable ?** Il est unique dans notre processus et
identifie une exécution. Un registre peut néanmoins l'écraser ; un digest est une
référence de contenu plus forte. Le vérificateur compare aussi le JAR dans le pod
avec le JAR archivé Jenkins et celui téléchargé de Nexus : trois SHA-256 identiques.

**Qui provisionne Grafana ?** Le manifeste k8s/grafana.yaml, séparé des six ressources
Terraform Prometheus. Dashboard et datasource sont provisionnés par ConfigMap.
Verify-Grafana.py exécute les quatre vraies requêtes via le proxy datasource.
Un panneau UP prouve la collecte ; readiness et tests HTTP vérifient le comportement.

**Pourquoi un outil Java pour Docker Hub ?** Le proxy Docker Desktop coupait les
gros uploads, même avec des credentials valides. ci/PushRegistry.java utilise le
JDK 17 déjà installé : blobs OCI exacts, réutilisation des couches publiques Temurin,
PATCH de 256 Kio, validation SHA-256 puis publication et relecture du manifeste.
L'image reste construite par Docker. Le programme refuse un tag existant différent
et n'affiche jamais le token ni les URLs signées. Une erreur de transport arrête
la publication ; une requête PATCH ambiguë est reprise après consultation de l'offset du registre.
