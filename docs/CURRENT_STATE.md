# AWS/EKS : déploiement et monitoring vérifiés

Le 29 octobre 2026 : profil events-tache-lab vérifié, compte personnel dédié,
us-east-1, budget restant déclaré de 50 USD. Credentials temporaires hors dépôt.
Avant création : inventaire AWS et simulations IAM effectués en lecture seule.
Terraform AWS initialisé et validé dans terraform/aws-eks/.
Plan privé .local/aws-eks.tfplan inspecté : 4 créations, 0 modification, 0 suppression.
EKS 1.36 standard, un worker t3.medium, disque gp3 20 Gio, add-on EBS CSI ; API /32.
Estimation prudente : 0,16 USD/h, soit environ 0,65 USD pour quatre heures, hors
transfert/taxes. Revue : docs/AWS_EKS_PLAN.md. L'utilisateur a approuvé ce plan exact
et le téléchargement local de kubectl. Apply réussi : 4 added, 0 changed, 0 destroyed.
Control plane créé après 14 min 54 s, node group après 1 min 59 s ; worker Ready,
EBS CSI prêt. Application, MySQL, Prometheus et Grafana : chacun 1/1 Ready.
Cycle HTTP Tache et quatre routes historiques vérifiés contre MySQL EKS.
Trois volumes gp3 chiffrés liés : MySQL 4 Gio, Prometheus et Grafana 1 Gio chacun.
JAR du pod identique au build Jenkins #9 et à Nexus ; image publique par digest.
Prometheus : 1/1 UP ; Grafana : datasource saine et quatre requêtes non vides.
Job distinct tache-eks-delivery #2 SUCCESS : promotion de #9, rollout et HTTP réels.
#1 a été bloqué par ScriptApproval avant exécution ; seul le script exact a été approuvé.
Plan Terraform après création : exit 0, aucune différence.
Preuves : docs/evidence/14-aws-eks/. Infrastructure toujours active et facturée.
Persistance : rôle INVITE du participant 2 conservé après remplacement du pod MySQL.
Présentation AWS générée : 33 slides françaises, descriptions visibles, aucune note ;
docs/presentation/eventsProject-Tache-AWS-EKS.pptx, validée et previews exportées.
kubectl 1.36.6 téléchargé dans .local/tools/kubectl-eks.exe, SHA-256 vérifié ; PATH inchangé.
Session lab annoncée jusqu'à 23 h 16 heure de Tunis : prévoir preuves et nettoyage.

## Exigence initiale et préparation d'accès (historique)

Utilisateur : AWS Academy Learner Lab disponible ; EKS est exige par le professeur.
La livraison locale ci-dessous reste verifiee, mais une phase cloud est obligatoire.
AWS CLI 2.31.13 deja installe ; profil default existant eu-north-1 laisse intact.
À ce stade initial, aucun appel AWS avec ce profil, aucune ressource cloud créée.
Action utilisateur : demarrer son lab et configurer le profil events-tache-lab avec
les trois credentials temporaires localement, puis confirmer region/budget/session.
Guide : docs/AWS_LEARNER_LAB_SETUP.md. Prochaine action agent : inventaire en lecture
seule, conception EKS/Terraform separee et plan/cout a revoir avant tout apply.
Comparaison du PDF de reference terminee ; rapport prive .local/moheb-review/comparison.md.

# Livraison vérifiée — 29 octobre 2026

## Résultat final
Jenkins build #9 automatique SUCCESS, commit f510b9b9f3d6447f58176a6e4e366cbc4ba37538 :
compilation, 38 tests, couverture, Sonar, quality gate, package, Nexus, image Docker,
publication publique, rollout Kubernetes et vérifications HTTP.

Image : ayyoub1/events-tache:tache-f510b9b9f3d6-9, linux/amd64.
Dépôt public : https://hub.docker.com/r/ayyoub1/events-tache.
Accès anonyme au manifeste HTTP 200 ; manifeste identique à celui de l'image locale :
sha256:abd5ce51159c9588d7f863c5a9682c0c0f7da7e19b16f6f45bda9719458d182c.
Snapshot Nexus : 1.0.0-20261029.153342-6.
JAR Jenkins, Nexus et pod : SHA-256 identique
 46b9ea9cc5fb3cffc5b284d57e184ea5b8b9e4729edf26eca426ef077efaa284.

## Application et qualité
Tache reste un enum de rôles Participant. Les quatre entrées historiques ont été
sécurisées par DTO après accord utilisateur. Les tests Mockito, MVC, mapping et
JPA/H2 passent ; les quatre routes ont aussi été vérifiées par HTTP contre MySQL.
Le cycle Tache complet, ses erreurs 400/404 et son retrait 204 sont vérifiés.
MySQL dédié sur 3307 ; serveur existant conservé ; secrets dans .env ignoré.

Sonar : gate OK avec conditions évaluées, webhook signé HTTP 200 ; zéro vulnérabilité,
bug, code smell ou hotspot. Couverture nouveau code 81,9 %, globale 49,6 % ; service
Tache entièrement couvert. Les quatre vulnérabilités de la baseline sont corrigées.

## GitHub vers Jenkins
Le job lit directement la branche Tache publique sur GitHub, sans miroir local.
Polling H/5 : detection toutes les cinq minutes lorsque Jenkins tourne.
Le commit f510b9b a declenche #9 via SCMTriggerCause, sans lancement manuel.
Le polling log montre Changes found et le checkout du SHA attendu.
Les commits limites a docs/ sont exclus. Le checkout local est desactive.
#8 etait la baseline manuelle GitHub ; #9 prouve le vrai declenchement automatique.

## Infrastructure et preuves
kind Kubernetes 1.32.2 : application et MySQL prêts, stockage persistant et probes.
Terraform : six ressources Prometheus appliquées après revue et accord ; dernier
plan détaillé exit 0 sans différence. Aucun nouvel apply effectué.
Grafana 13.3.0 : base OK, datasource sain, quatre requêtes réelles non vides.
Grafana utilise un manifeste séparé dans events-grafana ; Terraform gère Prometheus.

Captures authentiques Jenkins, Sonar, Nexus, Docker Hub, Prometheus et Grafana.
Présentation française éditable, 26 slides, descriptions visibles et aucune note
orateur ; contenu XML et export image controles.
Version actuelle : presentation/eventsProject-Tache-GitHub.pptx ; ancien deck ouvert conserve.
Index : presentation/screenshots-index.md.

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
Branche Tache synchronisee sur origin ; aucune fusion ni force-push.
Les premieres preuves de synchronisation restent conservees dans 05-git/.

## Reprise

Compose : docker-compose.devops.yml, profils jenkins/sonar/nexus.
Kubeconfig : .local/kubeconfig ; secrets .env et state Terraform ignores.
Jenkins 8080, Sonar 9000, Nexus 8081 ; forwards app 18089, Prometheus 19090,
Grafana 13000 via Start-LocalForwards.ps1 -IncludeGrafana.
SCM GitHub direct ; aucune mise a jour de miroir a effectuer.
Presentation : docs/presentation/eventsProject-Tache-GitHub.pptx, 26 slides sans notes.
Generer avec docs/presentation/Build-GitHubPresentation.ps1 et valider avec
docs/presentation/Validate-GitHubPresentation.py. Ancien deck ouvert conserve.
Prochaine action : revue de la presentation et preparation orale ; automatisation GitHub validee.

Exclusion docs/ verifiee reellement : commit 1727b73 ignore par polling, No changes,
#9 toujours dernier build SUCCESS. Preuve : 08-jenkins/github-docs-ignored.json et .log.
