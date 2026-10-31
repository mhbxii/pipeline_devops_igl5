# Préparation de la démonstration — 31 octobre 2026

## Application et DTO

- Requêtes : listes explicites de champs modifiables, relations par ID positif.
- Réponses : DTO dédiés ; pas de sérialisation récursive des entités JPA.
- Contrat : routes et champs scalaires conservés, relations de réponse par ID.
- Rôles : Tache reste l'enum Participant ; affectation, remplacement et retrait.
- Références inexistantes : HTTP 404 ; identifiants invalides : HTTP 400.
- Sources à expliquer : controllers/dto, TacheService, contrôleurs et tests.

Contrats détaillés : [DTO_API.md](DTO_API.md).

## Vérifications locales terminées

Maven verify : **52 tests réussis**, aucune erreur ni test ignoré. Rapports
JaCoCo HTML et XML produits. Les nouveaux tests couvrent les graphes cycliques,
les champs non affectables et les références manquantes.

Le JAR a été lancé temporairement contre le MySQL dédié. Les routes historiques,
le cycle de rôle complet, les erreurs 400/404, la readiness et les métriques
Prometheus ont été vérifiés. Ce processus de vérification a ensuite été arrêté.
Preuves : `evidence/16-readiness/final-baseline-readiness.json` et
`final-legacy-dto-api.json`.

## Jenkins et infrastructure

Jenkins reconstruit : Terraform 1.17.1 et kubectl 1.36.6. Un seul Jenkinsfile cible
EKS ; l'ancien job est désactivé avec son historique conservé. Les hashes provider
Windows/Linux sont présents et Terraform validate réussit sur les deux plateformes.

Jenkins **#15 SUCCESS**, source `8a5a96f`, après correction du NPE historique.
Sonar : gate OK, zéro bug/vulnérabilité/code smell/hotspot, couverture lignes 72,3 %.
Nexus et Docker Hub publiés. JAR identique dans Jenkins, Nexus et l'image ;
manifeste public HTTP 200 identique au manifeste local.

Image : `ayyoub1/events-tache:tache-8a5a96f28c1e-15`.
Nexus : `1.0.0-20261031.023455-10`.
Cette image a aussi été exécutée temporairement contre MySQL : DTO/rôle,
readiness, 404, métriques et UID 10001 vérifiés. Conteneur de test supprimé ensuite.
Preuves : `evidence/16-readiness/release-15-verification.json`,
`docker-image-15-smoke.json`, `sonar-build-15.json` et captures Jenkins/JUnit/JaCoCo.
Terraform et livraison AWS étaient explicitement désactivés dans ce build.

## Éléments encore bloqués

La policy du Learner Lab `voc-cancel-cred` refuse actuellement EKS. Le cluster
précédent a été supprimé. Les manifests Prometheus/Grafana et leur provisioning
existent ; ils ne constituent pas une preuve de disponibilité sur un nouveau cluster.

Pour terminer : redémarrer le lab, renouveler le profil dédié, vérifier le compte,
les CIDRs /32 et le budget, revoir un nouveau plan Terraform et obtenir l'accord
avant apply. Le bootstrap puis la livraison permettent les contrôles HTTP,
Prometheus (cible UP), Grafana (datasource et quatre requêtes), PVC et persistance.
Guides : JENKINS_TERRAFORM.md et AWS_EKS_OPERATIONS.md.
