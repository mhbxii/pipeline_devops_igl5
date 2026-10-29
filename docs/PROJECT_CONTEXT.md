# Contexte du projet

Date de référence : 2026-10-29 (Africa/Tunis).
Projet réel : eventsProject, et non Timesheet. Branche : Tache, base de7dfda.
Dépôt : https://github.com/mhbxii/pipeline_devops_igl5.git.
L'utilisateur confirme le périmètre : affectation des rôles Participant autour de
l'enum Tache (INVITE, ORGANISATEUR, SERVEUR, ANIMATEUR). Ne pas créer une entité
de tâches indépendante ni remplacer l'application.

Objectif : application vérifiée, tests, logs, CI/CD démontrable, preuves réelles,
présentation française et compréhension des sources.
Windows 11 Home / PowerShell ; 16 processeurs logiques ; environ 15,2 Gio RAM.
Au diagnostic : 0,7 Gio RAM libre et 16,8 Gio disque libre. Services à lancer progressivement.
Java par défaut 22.0.2 ; Java 8u202 déjà présent ; Maven 3.9.14 trouvé dans le cache wrapper.
Docker Desktop 4.40.0 actif, Engine 28.0.4, Compose 2.34.0.
kubectl présent sans contexte ; Terraform absent du PATH.

Barème officiel : Jenkins 2 ; Git Flow 1 ; JUnit 1 ; Sonar 1 ; Nexus 1 ; Docker 1 ;
Kubernetes 3 ; Terraform 2 ; Grafana/Prometheus 0,5 ; Excellence 3 ; questions sources 5.
Total annoncé 20 ; somme 20,5. Ne pas normaliser.

Contraintes : aucune installation sans intervention, aucune publication externe
sans autorisation ; plan Terraform à examiner avant approbation de l'apply.
Captures authentiques uniquement. Documenter les résultats immédiatement.

## Exigence cloud confirmee par l'utilisateur
Le 29 octobre 2026, l'utilisateur confirme AWS Academy Learner Lab et AWS/EKS
obligatoire pour l'evaluation. La livraison kind verifiee ne termine donc pas la
phase cloud. Acces au profil lab, region, budget et plan AWS restent a preparer.
Aucune ressource AWS creee par l'agent ; apply et destruction soumis a revue/accord.
