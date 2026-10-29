# Suivi du barème — état vérifié

| Critère | Points | Statut | Fichiers / vérification | Preuve / slide | Question et réponse |
|---|---:|---|---|---|---|
| Jenkins | 2 | Préparé, bloqué par disque | Jenkinsfile, ci/jenkins.Dockerfile ; build réel attendu | 08-jenkins / à ajouter | Comment bloquer après un échec ? La commande échoue et arrête le stage. |
| Git Flow | 1 | Partiel | Branche Tache, commits par jalon ; synchronisation distante non effectuée | 05-git / 3 | Pourquoi une branche ? Isoler les changements et faciliter la revue. |
| JUnit | 1 | Terminé localement | 20 tests : service Mockito, MVC, JPA/H2 sans mocks ; mvn verify/install | 04-unit-tests / 8–9 | H2 prouve-t-il MySQL ? Non, un test HTTP/MySQL distinct a été exécuté. |
| Sonar | 1 | Préparé, bloqué par disque | Jenkinsfile ; analyse et gate à exécuter | 06-sonarqube / à ajouter | Le gate est-il passé ? Aucun résultat encore disponible. |
| Nexus | 1 | Terminé localement | Maven deploy puis JAR téléchargé, SHA-256 identique ; Publish-LocalNexus.ps1 | 07-nexus / 18–19 | Coordonnées ? tn.esprit:eventsProject:1.0.0-SNAPSHOT. |
| Docker | 1 | Terminé localement | Dockerfile, Compose ; cycle HTTP et readiness UP | 09-docker / 11–12 | Image ou conteneur ? Une image décrit le livrable, un conteneur l'exécute. |
| Kubernetes | 3 | Terminé localement | k8s/, Deploy-LocalKubernetes.ps1 ; pods prêts et cycle HTTP complet | 10-kubernetes / 13–14 | Un pod Running suffit-il ? Non : readiness et comportement HTTP/DB vérifiés. |
| Terraform | 2 | Appliqué avec accord | terraform/main.tf ; fmt/init/validate/plan/apply, plan suivant sans différence | 11-terraform / 15–16 | Que gère Terraform ? Les six ressources Prometheus, pas le cluster ni la base applicative. |
| Grafana/Prometheus | 0,5 | Partiel | Prometheus UP, métriques Actuator ; Grafana non démarré | 13-monitoring / 17 | Qui collecte ? Prometheus ; Grafana servira à visualiser. |
| Excellence | 3 | En cours | Preuves réelles, scripts, santé, utilisateur Docker non privilégié, mémoire projet | docs/ et présentation | Comment reproduire ? Scripts documentés et artefacts versionnés. |
| Questions sources | 5 | Préparation en cours | ORAL_DEFENSE.md, README.md, sources et configurations | Toutes | Expliquer chaque couche puis démontrer son comportement réel. |

Total annoncé par l'enseignant : 20 ; somme des valeurs : 20,5. Aucune normalisation.
Aucun nombre de points n'est garanti par l'agent.
