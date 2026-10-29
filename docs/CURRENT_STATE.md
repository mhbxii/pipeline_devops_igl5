# Livraison vérifiée — 29 octobre 2026

## Résultat final
Jenkins build #7 SUCCESS, commit b822c015d8ab476b984886ffbcb1215eaefcea4b :
compilation, 38 tests, couverture, Sonar, quality gate, package, Nexus, image Docker,
publication publique, rollout Kubernetes et vérifications HTTP.

Image : ayyoub1/events-tache:tache-b822c015d8ab-7, linux/amd64.
Dépôt public : https://hub.docker.com/r/ayyoub1/events-tache.
Accès anonyme au manifeste HTTP 200 ; manifeste identique à celui de l'image locale :
sha256:03c654798e96949084e99d3740d0e9c4a61997388c3111ea89b7829a30b383ec.
Snapshot Nexus : 1.0.0-20261029.142652-5.
JAR Jenkins, Nexus et pod : SHA-256 identique
 a6773b4aaa68868f59b88ff952986e3e94f8b336d6e35f111716a4a0e3339bee.

## Application et qualité
Tache reste un enum de rôles Participant. Les quatre entrées historiques ont été
sécurisées par DTO après accord utilisateur. Les tests Mockito, MVC, mapping et
JPA/H2 passent ; les quatre routes ont aussi été vérifiées par HTTP contre MySQL.
Le cycle Tache complet, ses erreurs 400/404 et son retrait 204 sont vérifiés.
MySQL dédié sur 3307 ; serveur existant conservé ; secrets dans .env ignoré.

Sonar : gate OK avec conditions évaluées, webhook signé HTTP 200 ; zéro vulnérabilité,
bug, code smell ou hotspot. Couverture nouveau code 81,9 %, globale 49,6 % ; service
Tache entièrement couvert. Les quatre vulnérabilités de la baseline sont corrigées.

## Infrastructure et preuves
kind Kubernetes 1.32.2 : application et MySQL prêts, stockage persistant et probes.
Terraform : six ressources Prometheus appliquées après revue et accord ; dernier
plan détaillé exit 0 sans différence. Aucun nouvel apply effectué.
Grafana 13.3.0 : base OK, datasource sain, quatre requêtes réelles non vides.
Grafana utilise un manifeste séparé dans events-grafana ; Terraform gère Prometheus.

Captures authentiques Jenkins, Sonar, Nexus, Docker Hub, Prometheus et Grafana.
Présentation française éditable, 25 slides, descriptions visibles et aucune note
orateur ; contenu XML et export image contrôlés. Index : presentation/screenshots-index.md.

## Incidents résolus et limites
#1 : option Jenkins sans plugin retirée ; #3 : callback Sonar privé débloqué ;
#5 : push interrompu par proxy ; #6 : résolution DNS du runtime flottant échouée.
#7 corrige la livraison : runtime épinglé, uploader OCI Java 17 en chunks de 256 Kio,
réutilisation des bases publiques, vérification des digests et reprise par offset.
La publication est réellement exécutée dans Jenkins, sans runtime supplémentaire.

Sonar utilise H2 dans ce laboratoire personnel. sonar.validateWebhooks=false est
un réglage global local pour le callback privé ; la signature HMAC reste vérifiée.
Le socket Docker de Jenkins reste réservé aux jobs de confiance.
Commits réels par jalon, sans dates falsifiées ; branche Tache publiee apres accord utilisateur.
Push de 20 commits confirme ; revisions locale et distante c3c58e3 identiques.
Preuve : evidence/05-git/remote-sync.txt. Aucune fusion ni force-push.

## Reprise

Compose: docker-compose.devops.yml; profils jenkins/sonar/nexus.
Kubeconfig: .local/kubeconfig; secrets et state hors Git.
Ports localhost: Jenkins 8080, Sonar 9000, Nexus 8081; forwards app 18089,
Prometheus 19090, Grafana 13000 (Start-LocalForwards.ps1 -IncludeGrafana).
Presentation: docs/presentation/eventsProject-Tache.pptx; slides.json; sans notes.
Ne pas modifier les branches des autres membres ni publier Git sans autorisation.


Prochaine action : demonstration utilisateur et preparation orale ; livraison terminee.
