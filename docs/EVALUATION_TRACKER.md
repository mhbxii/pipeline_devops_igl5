# Suivi du barème — résultats vérifiés

## Revue du 31 octobre 2026

DTO explicites en entrée et sortie : voir DTO_API.md. Maven verify : 46 tests
réussis, rapports JaCoCo produits. HTTP réel sur le JAR contre MySQL : routes
historiques, rôles, cas 400/404, readiness et export Prometheus validés.
Preuves actuelles : 16-readiness/baseline-readiness.json et legacy-dto-api.json.
Le redémarrage AWS reste bloqué par la policy voc-cancel-cred du lab ; aucune
nouvelle disponibilité EKS/Grafana/Prometheus n'est annoncée.

## Réactivation du 30 octobre 2026

| Outil demandé | Vérification actuelle | Preuve |
| --- | --- | --- |
| JaCoCo | Plugin Maven 0.8.12 ; HTML/XML réels dans Jenkins #13 ; 38 tests réussis, couverture lignes 59,7 % | 15-eks-reactivation/coverage-and-monitoring-readiness.json et jacoco-build-13.png |
| Prometheus | Deployment, Service, PVC 1 Gio et scrape Actuator présents dans le rendu Kustomize EKS | k8s/eks/prometheus.yaml ; vérification runtime après recréation |
| Grafana | Deployment, Service, PVC 1 Gio, datasource Prometheus et dashboard provisionnés par les manifests/scripts | k8s/grafana.yaml, monitoring/grafana-dashboard.json ; vérification runtime après recréation |

Le cluster précédent est supprimé. Les preuves historiques ci-dessous restent
valides pour cette démonstration passée ; elles ne prouvent pas la disponibilité
du nouveau cluster. JaCoCo est explicitement vérifié sans inventer de point au barème.

## Démonstration précédente

| Critère | Points | Statut | Fichiers et vérification | Preuve et slides | Question / réponse |
|---|---:|---|---|---|---|
| Jenkins | 2 | Chaîne publique vérifiée | Jenkinsfile, ci/ ; #9 automatique SUCCESS ; GitHub, tests, gate, Nexus, image publique, rollout et HTTP | 08-jenkins ; 8, 12 et 13 | Un échec bloque-t-il le rollout ? Oui, #5 le démontre. |
| Git Flow | 1 | Synchronisé | Branche Tache, vrais commits ; push autorisé et SHA distant identique | 05-git ; 3 | Pourquoi isoler ? Préserver les modules et permettre la revue. |
| JUnit | 1 | Vérifié | 38 tests, Mockito, MVC, mapping, JPA/H2 ; MySQL vérifié séparément | 04-unit-tests, 12-final-validation ; 8–9 et 14 | H2 prouve MySQL ? Non : appels HTTP/MySQL distincts. |
| Sonar | 1 | Vérifié | Gate OK avec conditions ; 81,9 % nouveau code, 49,6 % global ; zéro vulnérabilité | 06-sonarqube ; 10–11 | Les deux couvertures ? Périmètres différents, pas des valeurs interchangeables. |
| Nexus | 1 | Vérifié | ci/settings.xml, Maven deploy ; archive Jenkins/Nexus/pod de SHA-256 identique | 07-nexus, 12-final-validation ; 25–26 | Coordonnées ? tn.esprit:eventsProject:1.0.0-SNAPSHOT. |
| Docker | 1 | Publication publique vérifiée | Dockerfile, Compose, Java 17 non-root ; API réelle | 09-docker ; 16–18 | Image ou conteneur ? Livrable immuable par digest ou processus en exécution. |
| Kubernetes | 3 | kind et AWS EKS vérifiés | k8s/eks/, Deploy-Eks.py, Verify-Eks.py ; worker Ready, quatre deployments, trois PVC ; API et monitoring réels | 10-kubernetes et 14-aws-eks ; extension AWS | Running suffit ? Non, readiness et comportement HTTP/DB vérifiés. |
| Terraform | 2 | Local et AWS EKS vérifiés | terraform/ : Prometheus local ; terraform/aws-eks/ : quatre ressources créées après accord, plan suivant sans changement | 11-terraform et 14-aws-eks ; extension AWS | Gère-t-il tous les volumes ? Non, les PVC EBS sont créés par Kubernetes hors state AWS. |
| Grafana/Prometheus | 0,5 | Vérifié | k8s/grafana.yaml, monitoring/, Verify-Grafana.py ; quatre vraies requêtes | 13-monitoring ; 23–24 | Qui collecte ? Prometheus ; Grafana visualise. |
| Excellence | 3 | Éléments démontrés, appréciation de l'évaluateur | Probes, secrets isolés, automatisation, preuves et triple SHA-256 | docs/ et présentation | Reproductibilité ? Scripts, commits et vérifications de contenu. |
| Questions sources | 5 | Support de préparation disponible | ORAL_DEFENSE.md, sources, configurations et démonstrations | Ensemble | Savoir expliquer chaque choix et montrer sa preuve. |

Cycle AWS terminé après accord : trois namespaces et volumes de données supprimés,
quatre ressources Terraform détruites, absence des ressources AWS vérifiée.
Preuves : 14-aws-eks/cleanup-verification.json ; slide 34.
La démonstration EKS reste prouvée par les captures ; le cluster n'est plus actif.
CI principale #10 SUCCESS confirme les changements publiés sans régression du pipeline.

Total annoncé : 20 ; somme des critères : 20,5. Aucune normalisation ni note garantie.
L'index des slides est généré depuis slides.json dans presentation/screenshots-index.md.
#4 : livraison locale ; #7 : publication publique ; #9 : declenchement GitHub automatique et livraison complete.

Exigence complémentaire AWS/EKS satisfaite par le déploiement Learner Lab réel.
Jenkins tache-eks-delivery #2 SUCCESS promeut l'image vérifiée de tache-ci #9 ;
la création Terraform reste une opération séparée avec accord utilisateur.
Le barème reste inchangé ; aucun score supplémentaire AWS inventé.
