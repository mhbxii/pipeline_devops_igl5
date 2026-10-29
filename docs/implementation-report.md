# Rapport d'implémentation — résultats vérifiés

## Socle et application
L'application réelle est eventsProject, Spring Boot 2.7.6. Tache est l'enum de rôles
Participant, conformément au périmètre confirmé. La base root existante refusait
l'authentification : MySQL dédié sur 3307, utilisateur events, volume et secrets
locaux ont rétabli le fonctionnement. L'ancien serveur 3306 est conservé.

Le service transactionnel Tache permet lecture, affectation, remplacement et retrait.
Les erreurs sont traduites en 400/404 ; les exceptions inattendues sont journalisées
sans divulguer leur détail au client. Log4j2 remplace Logback sans double backend.

38 tests passent : Mockito, MVC, JPA/H2 sans mocks et mapping DTO. Les quatre
anciennes entrées exposées aux problèmes de liaison d'entités ont été corrigées,
après accord, par une liste de champs autorisés. La compatibilité des routes a été
vérifiée séparément par HTTP avec MySQL. Aucun test n'a été masqué pour livrer.

## Livraison locale
Jenkins #4 SUCCESS : compile, 38 tests, couverture, Sonar, quality gate, package,
publication Nexus, image Docker, import kind, rollout et vérifications HTTP.
Les erreurs réelles #1 et #3 sont conservées avec leur correction ; aucun historique
fictif n'a été créé. #5 a confirmé les étapes jusqu'à l'image puis échoué au push
Docker Hub à cause d'une connexion fermée par le proxy Docker Desktop.

Sonar : quatre vulnérabilités initiales corrigées ; zéro vulnérabilité finale.
Couverture nouveau code 81,9 %, globale 49,6 %, service Tache entièrement couvert.
Le gate final comporte des conditions évaluées ; callback réel HTTP 200 avec HMAC.

Le snapshot Nexus du build #4 et le JAR archivé Jenkins ont le même SHA-256 que
le JAR présent dans le pod. L'image utilise Java 17 et l'utilisateur 10001.
Kubernetes kind 1.32.2 exécute app et MySQL, avec probes, Services et stockage.
L'API métier et la readiness incluant DB ont été vérifiées via le Service.

## Infrastructure et monitoring
Le plan Terraform de six créations Prometheus a été examiné puis appliqué après
accord utilisateur. Le plan suivant ne détectait aucune dérive. Terraform ne gère
ni le cluster kind ni l'application. Grafana est un manifeste séparé, avec PVC,
image épinglée, datasource et dashboard provisionnés. Version 13.3.0 vérifiée :
base OK, datasource sain et quatre requêtes Prometheus non vides.

## Présentation, Git et limites restantes
Présentation éditable : 25 slides françaises, descriptions visibles et zéro note
orateur. Captures Windows et dashboards authentiques, vérification XML et export
complet. Index : presentation/screenshots-index.md ; défense : ORAL_DEFENSE.md.

Les commits décrivent de vrais jalons et portent leurs dates réelles. Aucun push
Git distant ni fusion de branche partagée effectué. La publication publique Docker
Hub vers ayyoub1/events-tache a réussi avec Jenkins #7 ; manifeste accessible anonymement et identique au local.
Le build #4 est une chaîne locale complète ; ne pas le présenter comme une
publication externe. H2 Sonar et socket Docker Jenkins sont des choix de laboratoire.

## Livraison publique finale
Build #7 SUCCESS : publication publique, rollout et HTTP vérifiés.
Les trois JAR Jenkins/Nexus/pod ont le même SHA-256.
Voir VERIFIED_DELIVERY.md et evidence/09-docker/dockerhub-verification.json.
