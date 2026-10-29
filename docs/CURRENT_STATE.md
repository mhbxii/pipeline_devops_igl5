# État courant

Phase 1 partielle : compilation réussie, fonctionnement bloqué par MySQL.
Branche Tache vérifiée ; dépôt initial propre ; périmètre confirmé par l'utilisateur.
Maven 3.9.14 trouvé hors PATH ; compilation avec Java 8 réussie (12 sources).
Application lancée avec ddl-auto=none et sans createDatabaseIfNotExist pour le diagnostic.
MySQL sur 127.0.0.1:3306 refuse root sans mot de passe : Access denied.
GET /events/event/getLogs/2026-01-01/2026-12-31 retourne HTTP 500.
Aucun test dans le dépôt initial. Aucun service CI/CD du projet identifié.
Pause demandée par les instructions de preuve : captures manuelles du socle à collecter
selon docs/presentation/screenshots-index.md avant la phase suivante.
Prochaine action technique : préparer une base MySQL Docker dédiée sur un autre port
et des paramètres via environnement, puis vérifier JDBC et API sans toucher au MySQL existant.
Surveiller RAM/disque avant démarrage. Aucun changement métier encore implémenté.
La branche Tache n'existe pas encore sur origin ; aucun push autorisé/exécuté.
Pas de publication distante, de déploiement Kubernetes ni de Terraform apply exécuté.
`nProcessus Java du diagnostic arr�t�s apr�s collecte des erreurs pour lib�rer la m�moire.
