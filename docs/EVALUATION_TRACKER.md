# Suivi du barème — résultats vérifiés

| Critère | Points | Statut | Fichiers et vérification | Preuve et slides | Question / réponse |
|---|---:|---|---|---|---|
| Jenkins | 2 | Chaîne publique vérifiée | Jenkinsfile, ci/ ; #9 automatique SUCCESS ; GitHub, tests, gate, Nexus, image publique, rollout et HTTP | 08-jenkins ; 8, 12 et 13 | Un échec bloque-t-il le rollout ? Oui, #5 le démontre. |
| Git Flow | 1 | Synchronisé | Branche Tache, vrais commits ; push autorisé et SHA distant identique | 05-git ; 3 | Pourquoi isoler ? Préserver les modules et permettre la revue. |
| JUnit | 1 | Vérifié | 38 tests, Mockito, MVC, mapping, JPA/H2 ; MySQL vérifié séparément | 04-unit-tests, 12-final-validation ; 8–9 et 14 | H2 prouve MySQL ? Non : appels HTTP/MySQL distincts. |
| Sonar | 1 | Vérifié | Gate OK avec conditions ; 81,9 % nouveau code, 49,6 % global ; zéro vulnérabilité | 06-sonarqube ; 10–11 | Les deux couvertures ? Périmètres différents, pas des valeurs interchangeables. |
| Nexus | 1 | Vérifié | ci/settings.xml, Maven deploy ; archive Jenkins/Nexus/pod de SHA-256 identique | 07-nexus, 12-final-validation ; 25–26 | Coordonnées ? tn.esprit:eventsProject:1.0.0-SNAPSHOT. |
| Docker | 1 | Publication publique vérifiée | Dockerfile, Compose, Java 17 non-root ; API réelle | 09-docker ; 16–18 | Image ou conteneur ? Livrable immuable par digest ou processus en exécution. |
| Kubernetes | 3 | kind verifie ; EKS obligatoire restant | k8s/, probes, PVC, rollout ; cycle HTTP via Service | 10-kubernetes ; 19–20 | Running suffit ? Non, readiness et comportement HTTP/DB vérifiés. |
| Terraform | 2 | Local verifie ; AWS EKS restant | terraform/main.tf : six ressources Prometheus, aucune ressource payante | 11-terraform ; 21–22 | Gère-t-il le cluster ? Non, Prometheus et son namespace uniquement. |
| Grafana/Prometheus | 0,5 | Vérifié | k8s/grafana.yaml, monitoring/, Verify-Grafana.py ; quatre vraies requêtes | 13-monitoring ; 23–24 | Qui collecte ? Prometheus ; Grafana visualise. |
| Excellence | 3 | Éléments démontrés, appréciation de l'évaluateur | Probes, secrets isolés, automatisation, preuves et triple SHA-256 | docs/ et présentation | Reproductibilité ? Scripts, commits et vérifications de contenu. |
| Questions sources | 5 | Support de préparation disponible | ORAL_DEFENSE.md, sources, configurations et démonstrations | Ensemble | Savoir expliquer chaque choix et montrer sa preuve. |

Total annoncé : 20 ; somme des critères : 20,5. Aucune normalisation ni note garantie.
L'index des slides est généré depuis slides.json dans presentation/screenshots-index.md.
#4 : livraison locale ; #7 : publication publique ; #9 : declenchement GitHub automatique et livraison complete.

Exigence complementaire confirmee : AWS/EKS obligatoire via Learner Lab.
Les preuves kind et Terraform Prometheus ne remplacent pas la future preuve EKS.
Le bareme reste inchange ; aucun score supplementaire AWS invente.
