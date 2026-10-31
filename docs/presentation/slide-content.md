# Contenu visible de la soutenance

Source : slides-ready.json. Aucun de ces textes ne devient une note orateur.

## 1. eventsProject

Gestion des rôles Participant et chaîne DevOps vers AWS EKS.

- **52** : tests réussis
- **#15** : Jenkins SUCCESS
- **72,3 %** : couverture des lignes
- **0** : bug Sonar

Source visible : État vérifié le 31 octobre 2026 • Source testée : 8a5a96f

## 2. Un périmètre métier précis

Tache décrit le rôle d'un participant dans un événement. Le modèle partagé du projet est conservé.

- **Un enum** : INVITE, ORGANISATEUR, SERVEUR et ANIMATEUR.
- **Un participant** : Lire, affecter, remplacer ou retirer son rôle sans supprimer le participant.
- **Un module intégré** : Contrôleurs, services et repositories existants ; aucune entité de tâche indépendante.

Source visible : entities/Tache.java • services/TacheService.java

## 3. Du code au déploiement EKS

Jenkins, SonarQube et Nexus restent locaux ; la destination Kubernetes du pipeline unique est AWS EKS.

- **1 · GitHub / Tache** : Source versionnée et checkout du commit.
- **2 · Jenkins** : Compilation, tests et rapports JaCoCo.
- **3 · SonarQube** : Analyse et quality gate bloquant.
- **4 · Terraform AWS** : Plan puis apply optionnel après revue.
- **5 · Nexus / Docker Hub** : JAR et image publiés et vérifiés.
- **6 · AWS EKS** : Rollout et HTTP après réactivation du lab.

Source visible : Jenkinsfile • Étapes AWS désactivées dans le build #15

## 4. Persister un rôle, conserver le modèle

Le rôle reste une valeur de Participant, enregistrée en chaîne dans MySQL.

- **Lisibilité** : La base contient le nom du rôle, pas sa position dans l'enum.
- **Intégrité** : Le participant existe avant toute affectation ; les valeurs inconnues sont rejetées.

```text
public enum Tache {
    INVITE, ORGANISATEUR,
    SERVEUR, ANIMATEUR
}

// Participant.java
@Enumerated(EnumType.STRING)
Tache tache;
```

Source visible : entities/Tache.java • entities/Participant.java

## 5. Un cycle de rôle vérifié par HTTP

Le JAR et l'image finale ont été testés contre le MySQL dédié, en complément des tests automatisés.

- **GET /taches** : Consulter les quatre rôles possibles.
- **POST /event/addPart** : Créer un participant avec un ID généré.
- **PUT /taches/participants/{id}** : Affecter ou remplacer le rôle.
- **GET /taches/participants/{id}** : Relire le rôle réellement persisté.
- **DELETE /taches/participants/{id}** : Retirer le rôle ; réponse HTTP 204.
- **Le participant reste** : Le retrait du rôle ne supprime pas sa fiche.

Source visible : 16-readiness/final-baseline-readiness.json

## 6. Limiter les champs reçus par l'API

Les DTO de requête définissent les champs modifiables. Les relations entrantes acceptent uniquement des IDs positifs.

- **Créations protégées** : ParticipantCreateRequest, EventCreateRequest et LogisticsCreateRequest.
- **Champs réservés** : Un client ne peut imposer l'ID généré ni le coût calculé.

```text
@Getter
@Setter
public class RoleRequest {
    private Tache tache;
}

// Le participant vient du chemin HTTP :
service.assignRole(id, request.getTache());
```

Source visible : controllers/dto/RoleRequest.java • EventRequests.java

## 7. Des réponses sans graphe JPA récursif

Les DTO de réponse conservent les champs scalaires ; leurs relations exposent des identifiants.

- **Sérialisation bornée** : Évite Participant → Event → Participant dans les réponses JSON.
- **Contrat explicite** : URLs et champs scalaires conservés ; détail imbriqué remplacé par des références ID.

```text
{
  "idPart": 7,
  "nom": "Demo",
  "tache": "SERVEUR",
  "events": [{ "idEvent": 10 }]
}

// Rôle : participantId + tache
```

Source visible : Exemple de JSON conforme au DTO • EventResponses.java • docs/DTO_API.md

## 8. Rejeter les erreurs avant la persistance

Le NPE historique sur une référence absente est corrigé ; les erreurs attendues sont contrôlées.

- **HTTP 400** : Identifiant nul ou négatif, enum inconnu, rôle manquant ou référence invalide.
- **HTTP 404** : Participant absent ou événement introuvable pour l'affectation logistique.
- **HTTP 204** : Retrait du rôle réussi, sans suppression du participant.

Source visible : EventReferenceValidationTest • TacheRestControllerTest • HTTP/MySQL réel

## 9. 52 tests automatisés réussis

Jenkins #15 : zéro échec, zéro erreur et aucun test ignoré.

- **Service / Mockito** : Règles de rôle et références manquantes.
- **HTTP / DTO** : Contrats, allowlists et sérialisation cyclique.
- **JPA / H2** : Persistance réelle ; MySQL vérifié séparément.

Source visible : Jenkins #15 • Rapport JUnit authentique

## 10. JaCoCo : une couverture mesurée

Les rapports HTML et XML sont produits par Maven, archivés dans Jenkins et consommés par SonarQube.

- **72,3 %** : Couverture globale des lignes.
- **45,0 %** : Couverture globale des branches.
- **Rapports obligatoires** : Le build vérifie la présence des fichiers HTML et XML.

Source visible : pom.xml • Jenkinsfile • 16-readiness/sonar-build-15.json

## 11. Un quality gate réellement évalué

SonarQube #15 : gate OK, conditions évaluées, aucune violation nouvelle.

- **0** : Bug, vulnérabilité, code smell et security hotspot.
- **87,9 %** : Couverture du nouveau code, seuil du gate : 80 %.
- **Périmètres distincts** : Couverture globale Sonar : 68,5 % ; couverture des lignes : 72,3 %.

Source visible : 16-readiness/sonar-build-15.json

## 12. Tracer les changements sur Tache

Branche dédiée, commits réels et historique préservé. Les modifications de documentation sont exclues du polling.

- **Reproductibilité** : Le tag d'image reprend le commit et le numéro de build.
- **Traçabilité** : Chaque résultat possède un journal, un rapport ou une capture authentique.

```text
fee07ba  DTO et préparation du pipeline EKS
8a5a96f  Validation des références absentes
7b7f585  Preuves et blocage du Learner Lab

Source testée par Jenkins #15 :
8a5a96f28c1e9d1fd2aa14146af43c6efcfd7bfe
```

Source visible : Jalons Git, résumés des commits • Source du build #15

## 13. Un seul pipeline, une release actuelle

tache-ci #15 SUCCESS. L'ancien job de promotion est désactivé ; son historique est conservé.

- **Compiler et tester** : 52 tests et rapports JaCoCo obligatoires.
- **Analyser** : SonarQube et gate OK avant publication.
- **Préparer AWS** : Terraform : validation, plan et approbation.
- **Publier** : Nexus et Docker Hub réellement exécutés.
- **Déployer EKS** : Image du build courant, plus de dépendance au build #9.
- **Contrôler** : Rollout et HTTP ; étapes AWS ignorées dans #15.

Source visible : 08-jenkins/build-15-summary.json • Jenkinsfile

## 14. Un plan avant toute création AWS

Le module terraform/aws-eks partage le state AWS canonique ; le state local de supervision reste séparé.

- **Garde-fous** : Compte attendu, profil lab dédié et accès API limité aux IPv4 /32.
- **Statut actuel** : Hash provider Linux corrigé ; nouveau plan requis après réactivation du lab.

```text
RUN_TERRAFORM = true
    fmt → init → validate → plan

APPLY_TERRAFORM = false // par défaut
    revue du plan et du coût
    approbation manuelle dans Jenkins
    apply du plan sauvegardé exact
```

Source visible : terraform/aws-eks/ • docs/JENKINS_TERRAFORM.md

## 15. Le même JAR dans toute la chaîne

Le SHA-256 du JAR Jenkins #15 est identique à celui téléchargé de Nexus et à celui présent dans l'image Docker.

- **Jenkins** : JAR archivé après le build réussi.
- **Nexus** : Snapshot 1.0.0-20261031.023455-10.
- **Docker** : /app/app.jar vérifié dans le conteneur de test.

Source visible : 16-readiness/release-15-verification.json

## 16. Une image publique vérifiée

Le manifeste distant est accessible sans compte et identique au manifeste linux/amd64 local.

- **Dépôt** : ayyoub1/events-tache
- **Tag livré** : tache-8a5a96f28c1e-15
- **Intégrité** : HTTP 200 anonyme ; digest SHA-256 local/distant identique.

Source visible : Digest : 5c4ff54095b9ae76… • 16-readiness/release-15-verification.json

## 17. Tester l'image réellement construite

L'image #15 a été lancée temporairement contre MySQL, puis le conteneur de test a été supprimé.

- **Readiness UP** : Application et connexion MySQL opérationnelles.
- **Métier et métriques** : DTO, affectation du rôle, 404 attendu et export Prometheus vérifiés.
- **Utilisateur 10001** : Exécution non-root, confirmée dans le conteneur.

Source visible : 16-readiness/docker-image-15-smoke.json

## 18. Des logs et des métriques exploitables

L'application expose des métriques Actuator ; Prometheus collecte et Grafana visualise.

- **Export vérifié** : Métriques JVM et requêtes HTTP présentes dans le runtime testé.
- **Collecte EKS** : Manifests et dashboard prêts ; vérification live après réactivation.

```text
/events/actuator/health/readiness
/events/actuator/prometheus

DEBUG  lecture du rôle
INFO   affectation / retrait
WARN   entrée invalide / participant absent
ERROR  incident inattendu, sans secret
```

Source visible : application.properties • TacheService.java • TacheExceptionHandler.java

## 19. Une infrastructure de démonstration maîtrisée

La configuration AWS vise us-east-1 et réutilise le VPC, les subnets et LabRole du laboratoire.

- **Quatre ressources** : Cluster EKS, launch template, node group et add-on EBS CSI.
- **Un worker** : t3.medium fixe ; disque système gp3 chiffré de 20 Gio.
- **Stockage applicatif** : MySQL 4 Gio, Prometheus 1 Gio, Grafana 1 Gio ; accès par tunnels authentifiés.

Source visible : terraform/aws-eks/ • k8s/eks/ • Création actuelle en attente de revue

## 20. EKS : démonstration réalisée le 29 octobre

Le déploiement précédent a été vérifié sur AWS puis supprimé après accord ; cette capture ne décrit pas un cluster actif aujourd'hui.

- **1 worker Ready** : Un nœud t3.medium disponible.
- **4 deployments** : Application, MySQL, Prometheus et Grafana.
- **3 PVC Bound** : Volumes gp3 chiffrés vérifiés.

Source visible : Capture authentique du 29/10/2026 • Infrastructure ensuite nettoyée

## 21. Prometheus : une cible réellement UP

Lors de la démonstration AWS du 29 octobre, Prometheus collectait les métriques Actuator du Service applicatif.

- **Collecteur** : Scrape toutes les 15 secondes.
- **Cible applicative** : Service events, port 8089, chemin /events/actuator/prometheus.
- **Reprise** : La cible devra redevenir UP dans le nouveau cluster.

Source visible : Capture du 29/10/2026 • k8s/eks/prometheus.yaml

## 22. Grafana : un dashboard provisionné

La démonstration AWS précédente valide la datasource Prometheus et quatre requêtes non vides.

- **Quatre vues** : Cible UP, CPU Java, mémoire JVM et débit HTTP.
- **Provisioning** : Datasource et dashboard fournis par la configuration du dépôt.
- **Reprise** : Santé de la datasource et valeurs à vérifier après bootstrap.

Source visible : Capture du 29/10/2026 • monitoring/grafana-dashboard.json

## 23. Nettoyer après la démonstration

Le nettoyage précédent a été approuvé et contrôlé : ressources Terraform et volumes de données supprimés.

- **Données d'abord** : Suppression des PVC avant celle du cluster.
- **Plan de destruction** : Revue puis accord avant l'apply destructif.
- **État confirmé** : Cluster précédent absent et volumes EBS supprimés.

Source visible : Preuve historique • 14-aws-eks/cleanup-verification.json

## 24. Les étapes pour relancer la démonstration

La policy voc-cancel-cred refuse actuellement EKS ; le laboratoire doit être redémarré avant de préparer un nouveau plan.

- **1 · Démarrer le lab** : Renouveler les trois credentials du profil dédié.
- **2 · Préparer Jenkins** : Vérifier identité, compte, budget et CIDRs /32 ; renouveler les Secret files.
- **3 · Revoir le plan** : DEPLOY_EKS=false ; approbation explicite avant apply.
- **4 · Bootstrap** : Namespaces, Secrets, MySQL, monitoring et RBAC.
- **5 · Livrer** : Renouveler le token EKS ; lancer le pipeline avec DEPLOY_EKS=true.
- **6 · Prouver** : HTTP/MySQL, PVC, cible UP, datasource et dashboard.

Source visible : docs/AWS_EKS_OPERATIONS.md • docs/JENKINS_TERRAFORM.md

## 25. Des résultats vérifiés, une reprise documentée

La release applicative et ses preuves sont prêtes ; la nouvelle activation AWS dépend du Learner Lab.

- **Application** : DTO entrée/sortie, 52 tests, HTTP/MySQL et erreurs contrôlées.
- **Chaîne CI** : Jenkins #15, gate OK, JaCoCo, Nexus et Docker Hub vérifiés.
- **Infrastructure** : Preuves AWS historiques conservées ; plan et bootstrap à reprendre après activation du lab.

Source visible : docs/READINESS.md • docs/EVALUATION_TRACKER.md

## 26. Trois choix à expliquer

Les réponses sont visibles ici ; aucune note orateur n'est ajoutée au fichier PowerPoint.

- **Pourquoi un DTO ?** : Définir le contrat HTTP, limiter les champs modifiables et empêcher les cycles JPA.
- **Pourquoi un enum ?** : Tache représente quatre rôles du participant, pas une tâche métier autonome.
- **Pourquoi plan puis apply ?** : Examiner les ressources et le coût avant d'autoriser une modification AWS.

Source visible : docs/ORAL_DEFENSE.md • docs/DTO_API.md
