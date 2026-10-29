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
