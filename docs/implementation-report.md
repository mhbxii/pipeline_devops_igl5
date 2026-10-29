# Rapport d'implémentation

## Socle MySQL — terminé
Cause : le serveur existant refuse root sans mot de passe. Correction : base Docker
indépendante sur 3307, compte events, secrets locaux et profil Spring local.
Preuve : mysql-api-success.txt et capture ; l'ancien endpoint getLogs répond 200.

## Tache, logs et tests — terminé localement
Nouvelle API de rôles, DTO, validation et erreurs HTTP isolées. Log4j2 remplace Logback.
20 tests passent, dont une intégration JPA/H2 sans mocks ; le serveur MySQL réel est
vérifié séparément par HTTP. Service Tache : 24 lignes et 4 branches couvertes.
Les six commandes Maven demandées et verify ont été exécutées avec succès.
Preuves : 02-maven, 03-logging, 04-unit-tests. Slides 7–10 ; JUnit et questions sources.

## Docker — terminé localement
Image Java 17, utilisateur 10001, secrets via environnement, base persistante.
Cycle métier réel réussi dans les conteneurs ; readiness UP.
Preuves : 09-docker ; slides 11–12. Publication Docker Hub non effectuée.

## Kubernetes — terminé localement
Cluster kind 1.32.2 créé ; app et MySQL prêts, Service et PVC fonctionnels.
Cycle HTTP réussi via port-forward 18089. Import multi-plateforme Docker Desktop corrigé
par archives linux/amd64. Preuves : 10-kubernetes ; slides 13–14.

## Terraform — appliqué et vérifié
Outils projet installés après accord. Plan de six créations examiné et approuvé par
l'utilisateur avant apply. Namespace, quota, configuration, PVC, Deployment et Service
Prometheus créés ; plan suivant sans changement. Prometheus scrape events UP.
Preuves : 11-terraform et 13-monitoring ; slides 15–17. Grafana reste à réaliser.

## CI et publication — en cours
Jenkinsfile et intégrations préparés ; images Jenkins/Sonar/Nexus en préparation.
Aucun build Jenkins, gate Sonar ou artefact Nexus n'est encore revendiqué.
Prochaine action : vérifier Nexus, publier et télécharger le JAR puis comparer SHA-256.
Ensuite Sonar et Jenkins avec gestion progressive de la mémoire.

## Présentation et Git
17 slides françaises, descriptions visibles et aucune note orateur. Captures authentiques
et source slides.json. Fichier courant : eventsProject-Tache.pptx.
Commits réels par jalon ; aucune date inventée, aucun commit vide planifié et aucun push.

## Résultat Nexus et limite de ressources
Nexus est maintenant vérifié : snapshot publié, téléchargé et SHA-256 identique.
EULA acceptée uniquement après accord utilisateur ; image verrouillée par digest.
Les étapes Jenkins/Sonar restent bloquées par le disque hôte, avec environ 1 Gio libre.
Le cache inutilisé approuvé a été nettoyé, mais un compactage VHDX administrateur
est nécessaire avant les gros builds. Voir scripts/Compact-DockerDisk.ps1.
La présentation courante contient les nouveaux résultats, sans notes orateur.
