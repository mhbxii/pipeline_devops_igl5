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
