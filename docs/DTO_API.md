# DTO et contrats HTTP

Revue du 31 octobre 2026. Tache reste l'enum des rôles Participant ; aucune entité
de tâche indépendante n'est créée.

| Route sous /events | Entrée | Sortie |
| --- | --- | --- |
| POST /event/addPart | EventRequests.ParticipantCreateRequest | EventResponses.ParticipantResponse |
| POST /event/addEvent/{id} | EventRequests.EventCreateRequest | EventResponses.EventResponse |
| POST /event/addEvent | EventRequests.EventCreateRequest | EventResponses.EventResponse |
| PUT /event/addAffectLog/{description} | EventRequests.LogisticsCreateRequest | EventResponses.LogisticsResponse |
| GET /event/getLogs/{d1}/{d2} | Dates du chemin | Liste de LogisticsResponse |
| PUT /taches/participants/{id} | RoleRequest | RoleResponse |
| GET /taches/participants/{id} | Identifiant du chemin | RoleResponse |
| DELETE /taches/participants/{id} | Identifiant du chemin | HTTP 204 |
| GET /taches | Aucun corps | Valeurs de l'enum Tache |

Les créations utilisent une liste explicite de champs modifiables. Les identifiants
générés et le coût calculé ne sont pas affectables depuis le JSON. Les relations
d'entrée acceptent seulement des identifiants positifs ; références nulles rejetées.

Les réponses conservent les noms de champs scalaires (idPart, idEvent, idLog, dates,
cout, nom, prenom, tache, description, reserve, prixUnit, quantite). Les collections
de relations exposent uniquement leurs identifiants : events:[{idEvent}],
participants:[{idPart}], logistics:[{idLog}]. Le détail imbriqué des entités n'est
plus sérialisé ; un client qui l'utilisait doit tenir compte de cette limitation
explicite. Les URLs et codes HTTP existants sont conservés.

Cette séparation évite les cycles Participant → Event → Participant et limite
l'exposition du modèle JPA. Les DTO n'ajoutent aucune opération métier et ne
modifient pas le schéma de base de données. Le service conserve les transactions
et vérifications d'existence ; le contrôleur réalise la conversion HTTP.

Tests : EventRequestsTest (allowlists et références), EventResponsesTest (graphe
cyclique, champs et collections), EventRestControllerCompatibilityTest (contrats
HTTP historiques), TacheRestControllerTest (enum, erreurs, rôle absent et champs
non affectables), TachePersistenceTest (persistance réelle JPA/H2).
Les appels HTTP/MySQL réels sont vérifiés séparément : H2 ne prouve pas MySQL.
