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
