# Décisions

- Présentation : descriptions visibles uniquement, sans notes orateur (demande utilisateur).
- MySQL dédié via Compose, port local 3307 ; ne pas modifier le serveur WSL sur 3306.
  Secrets générés dans .env ignoré par Git, utilisateur applicatif distinct de root.
  Volume persistant ; modifier .env ne modifie pas le mot de passe d'un volume déjà initialisé.
- Le profil local surcharge uniquement la connexion du développeur ; configuration
  par défaut conservée pour les autres membres. TLS désactivé uniquement sur ce lien local.
- Tache : PUT affecte ou remplace un rôle ; DELETE retire uniquement le rôle, pas le
  participant. Enum et schéma métier existants conservés. DTO évite les relations récursives.
- Log4j2 remplace Logback via exclusions Maven ; API SLF4J existante conservée.
- Tests sans mocks = intégration JPA/H2, distincte de la vérification HTTP/MySQL réelle.

Références consultées : https://hub.docker.com/_/mysql et
https://docs.spring.io/spring-boot/docs/2.7.6/reference/html/howto.html#howto.logging.log4j.

- Conserver eventsProject et Tache enum : périmètre explicitement confirmé par l'utilisateur.
- Utiliser les outils déjà présents avant d'en installer ; Java 8 pour le premier essai
  respecte la cible du POM. Java 8u202 est ancien : outil de diagnostic local, pas choix
  de runtime de production.
- Ne pas lancer toute l'infrastructure : mémoire disponible insuffisante au diagnostic.
- Noms Markdown majuscules canoniques ; Windows ne permet pas deux fichiers distincts
  TROUBLESHOOTING.md/troubleshooting.md ou EVALUATION_TRACKER.md/evaluation-tracker.md.
