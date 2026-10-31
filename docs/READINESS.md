# Projet vérifié sur AWS EKS — 31 octobre 2026

- Jenkins **#20 SUCCESS**, source `4dd4e988e514` : chaîne complète jusqu'au rollout EKS et aux vérifications HTTP.
- **52 tests**, rapports JaCoCo HTML/XML, gate Sonar OK.
- DTO explicites en entrée/sortie ; erreurs 400/404 contrôlées et HTTP/MySQL vérifié.
- Terraform : création approuvée dans #19, plan sans différence dans #20.
- EKS : un worker Ready, application/MySQL/Prometheus/Grafana chacun 1/1 Ready.
- Trois PVC gp3 chiffrés Bound : MySQL 4 Gio, Prometheus et Grafana 1 Gio chacun.
- Prometheus : 1/1 cible UP ; Grafana : datasource saine et quatre requêtes non vides.
- Même JAR dans Jenkins, Nexus et le pod ; UID non-root 10001 ; rôle conservé au rollout.

Image : `ayyoub1/events-tache:tache-4dd4e988e514-20`. Snapshot Nexus : `1.0.0-20261031.080707-11`.
Preuves actuelles : `evidence/17-terraform-pipeline/`.

## Accès public AWS vérifié

http://a5c49085a99274081beb23415906953b-415643546.us-east-1.elb.amazonaws.com/events/taches

HTTP 200, readiness UP ; quatre workloads conservés sans redémarrage. Cet accès
HTTP de démonstration expose l'API actuelle sans authentification. Voir AWS_EKS_PUBLIC_ACCESS.md.

## Accès via tunnels locaux

- Jenkins : http://localhost:8080/job/tache-ci/20/
- API : http://localhost:18189/events/taches
- Prometheus : http://localhost:19190/targets
- Grafana : http://localhost:13100 (secret dans .env : EKS_GRAFANA_ADMIN_PASSWORD).

## Exploitation

Le cluster reste actif : estimation environ 0,19 USD/h avec le load balancer public, hors trafic.
Renouveler le profil lab et exécuter Configure-AwsTerraformJenkins.py pour vérifier
le compte et actualiser les /32. Si le plan change, le revoir avant approbation.
Renouveler eks-kubeconfig via Configure-EksJenkins.py (token de deux heures).
Le bootstrap Deploy-Eks.py est réservé à une création initiale du cluster.
Le nettoyage exige un nouveau plan destroy et un accord ; ne pas réutiliser les anciens plans.

La présentation existante reste le bilan de la phase CI #15 ; les preuves EKS
actuelles sont disponibles dans Jenkins #20 et le dossier ci-dessus.
