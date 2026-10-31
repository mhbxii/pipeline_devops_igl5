# Accès public AWS vérifié — 31 octobre 2026

URL : http://a5c49085a99274081beb23415906953b-415643546.us-east-1.elb.amazonaws.com/events/taches
Autorisation : accès AWS sans arrêter les services. Le nouveau Service events-public
expose seulement l'API via un Classic Load Balancer internet-facing dans une AZ.
HTTP externe 200 ; readiness UP ; backend AWS InService. Les quatre pods sont
Ready et leurs UID/compteurs de redémarrage sont identiques avant et après.
API, Jenkins, Grafana et Prometheus restent accessibles localement : HTTP 200.
Aucun Deployment modifié ni apply Terraform. Coût supplémentaire estimé : environ
0,03 USD/h puis trafic ; total indicatif 0,19 USD/h hors trafic. Cluster conservé.
Preuves : docs/evidence/18-eks-public-access/verification.json et public-api.png.
Exploitation : docs/AWS_EKS_PUBLIC_ACCESS.md. L'API est publique en HTTP, sans
nouvelle authentification ni certificat. Aucun autre service exposé par cet ajout.
Les sections suivantes retracent les étapes antérieures.

# Pipeline complet EKS vérifié — 31 octobre 2026

Jenkins #19 SUCCESS : plan revu et approuvé, quatre ressources AWS créées.
Jenkins #20 SUCCESS sur 4dd4e98 : 52 tests, gate Sonar, Terraform validate et plan
sans différence, Nexus, Docker Hub, rollout EKS et HTTP réels. Aucun nouvel apply
nécessaire dans #20. L'extension JSON des variables et l'allowlist /32 sont corrigées.
Un worker t3.medium Ready ; application, MySQL, Prometheus et Grafana : 1/1 Ready.
Trois PVC gp3 chiffrés Bound (4 + 1 + 1 Gio). Prometheus : 1/1 UP. Grafana :
datasource OK et quatre requêtes non vides. JAR Jenkins/Nexus/pod identiques.
Utilisateur du pod : 10001. Rôle conservé après remplacement du pod applicatif.
Preuves : docs/evidence/17-terraform-pipeline/. Le cluster reste ACTIF et facturé
(environ 0,16 USD/h). Aucun nettoyage de cette nouvelle infrastructure effectué.
Les sections suivantes retracent les étapes antérieures, dont les anciens blocages.

# Correction Terraform Jenkins #17 — 31 octobre 2026

#19 : tests, gate, validate et plan réussis avec CIDR actuel vérifié. L'utilisateur
a explicitement approuvé création et déploiement application/MySQL/Prometheus/
Grafana. Plan privé sauvegardé .local/aws-eks-jenkins19.tfplan, SHA-256
22e873452448ee9021d8feae03424ae897abd8459b8e594fcb936ab2d9575e2c.
Apply lancé dans Jenkins après comparaison du hash ; #19 SUCCESS, 4 ressources
créées. Kubeconfig EKS privé actualisé et bootstrap approuvé appliqué : namespaces,
Secrets, RBAC, stockage, MySQL, application et monitoring. MySQL Ready ; attente
des autres rollouts au moment de cette mise à jour. Image initiale fixée au digest
vérifié du build #15, puis le pipeline livrera son propre nouveau build.

Exécution demandée après correction : #18 a réussi compilation, 52 tests, Sonar,
gate, Terraform validate et plan (4 créations). Arrêt volontaire avant approbation :
allowlist API /32 périmée. Credential de variables et fichier privé local actualisés
avec la sortie commune hôte/Jenkins ; autres variables et credentials AWS conservés.
Nouveau build #19 demandé avec apply soumis à approbation et livraison/publications
désactivées pour créer d'abord l'infrastructure. Aucun apply effectué à ce stade.

Plan arrêté avant tout appel de création : fichier de variables JSON traité comme
HCL, car le nom de fichier du credential ne porte pas le suffixe .json. Le pipeline
copie désormais ce Secret file vers eks.tfvars.json dans son répertoire temporaire
privé ; suppression inchangée en post/always. Aucun apply effectué.

# Présentation prête — 31 octobre 2026

Nouvelle soutenance : docs/presentation/eventsProject-Tache-Soutenance.pptx et PDF
du même nom. 26 diapositives françaises, descriptions visibles, aucune note
orateur. DTO entrée/sortie, 52 tests, JaCoCo, Sonar, pipeline unique et release #15
actualisés. Les preuves EKS/Prometheus/Grafana du 29 octobre sont explicitement
datées ; la réactivation actuelle reste indiquée en attente du lab.
Validation : contenu XML exact, 26 pages PDF, aucun débordement de texte, exports
complets et contrôle visuel des slides. Anciennes présentations et fichier ouvert
conservés. Génération : Build-ReadyPresentation.ps1 ; validation : Validate-ReadyPresentation.py.
Sources : slides-ready.json ; plan et index des preuves mis à jour.

# Revue DTO et préparation finale — 31 octobre 2026

Continuation demandée : vérifier les DTO et la préparation complète. DTO de
requête existants confirmés. Ajout local de DTO de réponse explicites pour les
routes historiques (champs scalaires conservés, relations limitées aux identifiants
pour éviter la récursion) ; DTO rôle déplacés dans controllers/dto.
Vérification Maven verify réussie : 46 tests, aucun échec/erreur/skipped. Rapports
JaCoCo HTML/XML produits. JAR lancé temporairement sur 127.0.0.1:18099 contre le
MySQL dédié : quatre routes historiques, cycle rôle complet, erreurs 400/404,
readiness UP et métriques Prometheus JVM/HTTP vérifiés. Processus de vérification
arrêté ensuite. Preuves : docs/evidence/16-readiness/. Contrats : docs/DTO_API.md.
Hash provider Linux corrigé et validé sur Windows/Linux. Jenkins reconstruit et
pipeline unique actif ; ancien job EKS désactivé, historique conservé.
Blocage AWS actuel : STS répond, mais EKS ListClusters est explicitement refusé
par la policy de lab voc-cancel-cred. Redémarrage du lab et renouvellement du
profil demandés. Aucun contournement IAM ni apply effectué.
Sources et preuves publiées sur Tache : fee07ba. Jenkins #14 lancé sur ce commit
avec tests/gate, publication Nexus et image publique ; Terraform/apply/livraison
EKS explicitement désactivés pendant le blocage du lab. #14 SUCCESS, publications
Nexus/Docker réelles. Le détail Sonar signale cependant un NPE historique sur
participant inexistant : garde 404 ajoutée, ainsi que 400 pour ID invalide et 404
pour événement logistique absent. Maven verify final : 52 tests réussis ; succès
HTTP/MySQL et nouveaux rejets 400/404 vérifiés sur le JAR actualisé.
Preuve finale locale : docs/evidence/16-readiness/final-baseline-readiness.json.
Jenkins #15 sur 8a5a96f SUCCESS : 52 tests et gate OK ; zéro bug/vulnérabilité/code
smell/hotspot, couverture lignes 72,3 %. Nexus et Docker Hub réellement publiés.
Image : ayyoub1/events-tache:tache-8a5a96f28c1e-15 ; accès anonyme HTTP 200.
Digest : sha256:5c4ff54095b9ae76310a7ae1a661fffcd7d5d08c2b317289863c131da39dfe22.
Snapshot Nexus : 1.0.0-20261031.023455-10. JAR Jenkins/Nexus/image identiques.
Image exécutée temporairement contre MySQL : readiness, DTO/rôle, 404, export
Prometheus et UID non-root 10001 vérifiés ; conteneur de test supprimé ensuite.
Preuves : docs/evidence/16-readiness/ et synthèse docs/READINESS.md.
AWS reste bloqué : aucun nouveau cluster, apply ou succès monitoring EKS annoncé.

# Pipeline unique EKS — 30 octobre 2026

Reconstruction Jenkins effectuée après demande utilisateur : Terraform 1.17.1,
kubectl 1.36.6 et state AWS lisible/inscriptible vérifiés dans le nouveau conteneur.
Le téléchargement kubectl Docker a échoué (unexpected EOF) ; repli via binaire
local vérifié SHA-256 et plugins existants, contexte ignoré .local/jenkins-cached-build.
Jenkins recréé, historique conservé, ancien job EKS désactivé. Session lab renouvelée
après ExpiredToken ; STS et compte attendu validés. Credentials Terraform configurés.
API CIDR privé actualisé vers la sortie /32 commune hôte/Jenkins. Inventaire EKS vide.
Plan local privé aws-eks-reactivation.tfplan : 4 créations, 0 modification/suppression.
Pipeline unique réactivé ; build infrastructure demandé avec publications et
livraison désactivées, attente d'approbation prévue avant tout apply. Aucun apply effectué.
Grille : JaCoCo vérifié sur les artefacts réels Jenkins #13, 38 tests réussis,
0 échec ; couverture lignes 59,7 %, instructions 52,8 %. HTML/XML deviennent
obligatoires dans le Jenkinsfile local. Prometheus/Grafana présents dans le rendu
Kustomize EKS ; leur disponibilité réelle attend la recréation du cluster.
Preuve : docs/evidence/15-eks-reactivation/coverage-and-monitoring-readiness.json.
Capture authentique : docs/evidence/15-eks-reactivation/jacoco-build-13.png.
Build #13 : tests/gate réussis mais Terraform validate bloqué par le hash Linux
absent du lockfile AWS créé sous Windows. Hash linux_amd64 obtenu via terraform
providers lock (signature HashiCorp vérifiée), hashes Windows conservés.

Sources sur Tache : Jenkinsfile unique pour CI, Terraform AWS/EKS et
livraison de l'image du build courant vers EKS. ci/Jenkinsfile.eks supprimé ;
configuration de l'ancien job adaptée pour conserver seulement son historique.
Étapes kind retirées du pipeline. State AWS canonique partagé, state kind intact.
Guide : docs/JENKINS_TERRAFORM.md. Apply optionnel avec approbation du plan exact.
Commit et publication sur Tache autorisés par l'utilisateur le 30 octobre 2026.
Activation en attente : reconstruction Jenkins après accord,
session lab et credentials renouvelés, nouveau plan/accord et bootstrap EKS.
Aucun nouvel apply, installation ou déploiement AWS effectué.
Contrôles réussis : validateur déclaratif Jenkins, fmt/validate Terraform AWS,
configuration Compose, syntaxe des quatre helpers Python et de tous les blocs
shell multiligne, git diff --check. Aucune exécution cloud du pipeline fusionné.

# AWS/EKS : démonstration vérifiée, ressources supprimées après accord

Nettoyage explicitement approuvé : suppression des trois namespaces/PVC, puis
application du plan destroy examiné. Apply réussi : 0 added, 0 changed, 4 destroyed.
Les trois volumes de données ont été supprimés avant l'infrastructure.
Contrôles AWS finaux réussis : cluster absent, EC2 terminé et IPv4 libérée,
trois volumes de données et disque système absents, ASG, launch template,
security group et interfaces du déploiement absents. State : zéro ressource gérée.
Job EKS désactivé et credential temporaire retiré ; historique conservé.
Les preuves et la présentation sont conservées ; kind est toujours Ready.
CI principale #10 SUCCESS après le push 205d727, avec tests, gate, publication
Nexus/Docker Hub, rollout kind et HTTP. La release EKS démontrée reste celle de #9.

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
Preuves : docs/evidence/14-aws-eks/. Infrastructure AWS désormais supprimée ;
les résultats ci-dessous décrivent la démonstration avant nettoyage.
Persistance : rôle INVITE du participant 2 conservé après remplacement du pod MySQL.
Présentation AWS générée : 34 slides françaises, descriptions visibles, aucune note ;
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
