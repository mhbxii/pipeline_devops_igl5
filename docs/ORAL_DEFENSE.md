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
