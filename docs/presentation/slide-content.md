# Descriptions visibles dans les diapositives

## 1. Contribution Tache

eventsProject : gestion des rôles des participants. Diagnostic, tests et déploiement progressifs.

## 2. Un périmètre respecté

Tache est un enum persisté en chaîne dans Participant ; le modèle partagé est conservé.

## 3. Une branche dédiée et synchronisée

Branche Tache publiée sur origin après accord ; historique préservé et révisions locale/distante identiques.

## 4. Maven et Java

Maven 3.9.14 sélectionné pour la session ; cible Java 8 et runtime Docker Java 17.

## 5. La compilation initiale

Journal sauvegardé : BUILD SUCCESS pour les 12 sources du dépôt initial.

## 6. Le blocage initial : MySQL

Journal du diagnostic initial : authentification root refusée et HTTP 500.

## 7. MySQL : correction vérifiée

Base dédiée sur 3307 : création, affectation, lecture et retrait du rôle réussis.

## 8. 38 tests automatisés

Service Mockito, contrats HTTP, JPA/H2 sans mocks et mapping sécurisé des anciennes entrées.

## 9. Sécuriser les entrées existantes

Quatre entrées utilisent des DTO : identifiants générés et coût calculé protégés ; compatibilité HTTP/MySQL vérifiée.

## 10. Une analyse Sonar réellement évaluée

Gate OK : nouveau code couvert à 81,9 %, duplication nulle et aucune violation nouvelle.

## 11. Une qualité mesurée sans masquer le périmètre

Zéro vulnérabilité après correction des quatre entrées ; couverture globale 49,6 %, distincte du nouveau code.

## 12. Une chaîne Jenkins exécutée

Build #9 automatique réussi : 38 tests, gate Sonar, Nexus, image Docker Hub, rollout Kubernetes et vérification HTTP.

## 13. GitHub déclenche Jenkins automatiquement

Le polling détecte le commit GitHub f510b9b et lance le build #9 : cause SCM enregistrée, sans lancement manuel.

## 14. Une couverture métier mesurée

TacheService : 24 lignes et 4 branches couvertes ; ce résultat concerne ce service.

## 15. Des logs utiles

DEBUG, INFO, WARN sur API ; ERROR démontré par une panne simulée dans un test.

## 16. Application et MySQL en conteneurs

Cycle HTTP vérifié dans Docker ; readiness UP et utilisateur applicatif non privilégié.

## 17. Vérifier les conteneurs

Application et base isolées ; volumes persistants et ports liés à localhost.

## 18. Une image publique vérifiée

ayyoub1/events-tache : manifeste linux/amd64 accessible sans compte et identique à l'image locale du build #9.

## 19. Un déploiement Kubernetes réel

Pods applicatif et MySQL prêts, Services actifs et volume persistant lié.

## 20. Vérifier le service métier

Le cycle HTTP complet réussit via Kubernetes ; readiness inclut la base MySQL.

## 21. Provisionner avec Terraform

Plan examiné et approuvé : 6 ressources Prometheus créées, aucune destruction.

## 22. Contrôler la convergence

Un nouveau plan ne détecte aucune différence entre configuration et infrastructure.

## 23. Observer une application réellement collectée

Prometheus collecte la cible Kubernetes events : 1/1 UP, métriques exposées par Actuator.

## 24. Visualiser les métriques dans Grafana

Dashboard provisionné : cible UP, CPU Java, mémoire JVM et débit HTTP ; quatre requêtes réelles vérifiées.

## 25. Publier le livrable dans Nexus

Le dépôt Maven snapshots contient le JAR et le POM de tn.esprit:eventsProject:1.0.0-SNAPSHOT.

## 26. Relier le build au programme exécuté

Même SHA-256 pour le JAR archivé par Jenkins, téléchargé de Nexus et présent dans le pod Kubernetes.

## 27. Créer une infrastructure AWS réelle

Learner Lab personnel, us-east-1 : plan examiné et approuvé, quatre ressources Terraform créées pour EKS, son worker et EBS CSI.

## 28. Exécuter l'application sur EKS

Un worker t3.medium Ready, quatre deployments disponibles et trois volumes EBS gp3 chiffrés : MySQL 4 Gio, Prometheus et Grafana 1 Gio chacun.

## 29. Vérifier le métier et la persistance AWS

Lecture, affectation, remplacement et retrait du rôle vérifiés par HTTP ; le participant et son rôle survivent au remplacement du pod MySQL.

## 30. Promouvoir la release avec Jenkins vers EKS

tache-eks-delivery #2 SUCCESS : image vérifiée du build CI #9, déployée par digest ; nouveau rollout et contrôles HTTP depuis Jenkins.

## 31. Collecter les métriques dans AWS

Prometheus tourne sur EKS et collecte réellement les métriques Actuator du Service applicatif : une cible, état UP.

## 32. Observer le déploiement EKS dans Grafana

Dashboard provisionné sur AWS : cible UP, CPU Java, mémoire JVM et débit HTTP ; datasource saine et quatre requêtes non vides.

## 33. Contrôler la convergence et les coûts

Le plan suivant ne détecte aucun changement. Estimation : 0,16 USD/h ; démonstration courte et nettoyage approuvé des volumes puis du cluster.
