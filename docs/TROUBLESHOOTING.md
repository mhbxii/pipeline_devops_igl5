# Diagnostic

| Problème | Cause constatée | Action | Vérification |
|---|---|---|---|
| mvn introuvable | Maven absent du PATH | Réutiliser Maven 3.9.14 du cache local | Version enregistrée dans evidence/02-maven |
| Périmètre Timesheet ambigu | Le dépôt contient eventsProject, Tache enum | Périmètre rôles confirmé par utilisateur | Sources inspectées |
| Kubernetes inaccessible | Aucun current-context | Choisir/configurer un cluster local ultérieurement | kubectl config current-context échoue |

Ne pas confondre port 3306 ouvert et connexion JDBC vérifiée.

## Blocage JDBC confirmé
`mvn -B compile` réussit avec Java 8. Le démarrage HTTP réussit mais Hikari échoue :
`java.sql.SQLException: Access denied for user 'root'@'localhost'`.
L'API GET /events/event/getLogs/2026-01-01/2026-12-31 répond 500.
Cause : la configuration root/mot de passe vide du dépôt n'est pas acceptée par le
serveur existant. Aucun mot de passe n'a été recherché ni changé.
Correction proposée : base Docker dédiée et configuration par environnement.
Correction non encore appliquée ; preuves : baseline-startup.log et baseline-api.txt.

## Correction MySQL verifiee
Base dediee mysql:8.4, port 3307, volume events-tache_mysql-data et compte events.
Profil application-local.properties : variables DB_PASSWORD, dialecte MySQL8Dialect.
L ancien serveur 3306 reste intact. Authentification et schemas verifies par l API :
POST participant, PUT role, GET persiste, DELETE role, getLogs -> HTTP 200.
Preuve : evidence/01-project-setup/mysql-api-success.txt.
Les secrets sont generes dans .env non suivi. Ne pas supprimer le volume pour reinitialiser
sans accord : cela detruirait les donnees locales.

## Publication Nexus terminée
Le HTTP 403 venait de l'EULA Community Edition, et non d'un mauvais mot de passe.
EULA acceptée après demande et accord explicite de l'utilisateur.
Maven deploy : BUILD SUCCESS ; snapshot 1.0.0-20261029.085753-1 publié.
Le JAR téléchargé est identique au JAR local (SHA-256 comparé).
Preuves : 07-nexus/nexus-publish.log et nexus-verification.txt.

## Limite de disque hôte
C: est descendu à environ 700 Mio ; builds/téléchargements lourds interrompus.
Prune du cache inutilisé approuvé : 31,91 Go récupérés dans Docker, mais le VHDX
ne rétrécit pas automatiquement sur l'hôte. Un compactage administrateur est requis
avant de reprendre les images lourdes. Le helper est préparé, pas exécuté.
Référence : https://learn.microsoft.com/en-us/windows-server/administration/windows-commands/compact-vdisk.

## Reprise Docker Desktop
Après espace libéré (environ 22 Gio), l'API Windows Docker restait bloquée.
WSL répondait mais le redémarrage Desktop échouait sur com.docker.build ; un ancien
processus helper subsistait. Redémarrage des processus Desktop/backend/build puis
restauration explicite du cluster et Nexus. kubectl rollout confirme l'application prête.
Aucun reset usine, suppression de volume ou arrêt global WSL n'a été utilisé.

## Jenkins : échec et reprise vérifiés
Build #1 : option timestamps non reconnue car le plugin optionnel n'était pas installé.
Correction minimale : retirer cette option. Validation de Jenkinsfile via POST
/pipeline-model-converter/validate avec paramètre texte jenkinsfile.
Build #2 : SUCCESS, tests/JaCoCo, Docker et déploiement kind vérifiés.
Preuves : 08-jenkins/build-1-console.log et build-2-summary.json.

## Sonar : réauthentification après mot de passe initial
Après changement de mot de passe, le cookie initial restait dans requests.Session.
Effacer les cookies avant d'utiliser le nouveau mot de passe corrige le 401.
Projet privé et token d'analyse limité au projet configurés ; analyse initiale réussie.
Les quatre S4684 préexistantes restent ouvertes ; le gate initial sans conditions
n'est pas un certificat de qualité du code existant.

## Reprise et livraison vérifiée
Jenkins #4 exécute toute la chaîne locale. La première erreur venait de timestamps
sans plugin ; l'option inutile a été retirée. Le build #3 attendait le gate car Sonar
refusait un webhook vers le réseau Docker privé ; l'instance locale autorise désormais
ces callbacks (réglage global), avec vérification HMAC maintenue. #4 a reçu HTTP 200.

Après correction des quatre entrées REST par DTO, 38 tests passent et les quatre
routes historiques fonctionnent contre MySQL. Sonar ne détecte plus de vulnérabilité.

Les pushes Docker Hub échouent sur une connexion fermée par le proxy Docker Desktop
à http.docker.internal:3128. Le build #5 s'est correctement arrêté avant rollout.
Les blobs Temurin publics peuvent être montés côté registre, sans transfert. Le JAR
est envoyé par petits morceaux en conservant les octets OCI exacts et les digests.
Seed-RegistryBlobs.py ne publie pas de tag ; Jenkins doit encore terminer le push.

## Resolution finale
Build #7 SUCCESS : le client Java de chunks a publie et relu le manifeste exact.
Kubernetes a tire l'image publique, termine le rollout et passe les appels HTTP.
La verification anonyme confirme le depot public. Le moteur Docker expose ici
l'index OCI comme .Id : le verificateur compare donc les manifestes amd64 exacts
et leur configuration, au lieu de comparer un index a une configuration.
Les trois JAR Jenkins/Nexus/pod ont des SHA-256 identiques.
