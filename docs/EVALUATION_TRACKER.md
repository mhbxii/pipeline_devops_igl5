# Suivi du barème

Statuts au diagnostic : aucune réussite CI/CD revendiquée.

| Critère | Points | Statut | Fichiers / validation attendue | Preuve / diapositive | Question orale |
|---|---:|---|---|---|---|
| Jenkins | 2 | Non commencé | Jenkinsfile ; build réel | 08-jenkins / 11 | Comment propager un échec ? |
| Git Flow | 1 | Partiel | Branche Tache vérifiée, historique initial | 05-git / 8 | Pourquoi isoler une branche ? |
| JUnit | 1 | Non commencé | Futurs tests service et intégration ; mvn test | 04-unit-tests / 7 | Que simule Mockito ? |
| Sonar | 1 | Non commencé | Analyse et gate réels | 06-sonarqube / 9 | Tests et analyse : différence ? |
| Nexus | 1 | Non commencé | Publication puis téléchargement vérifié | 07-nexus / 10 | Quelles coordonnées Maven ? |
| Docker | 1 | Non commencé | Image et API en conteneur | 09-docker / 12 | Image ou conteneur ? |
| Kubernetes | 3 | Bloqué : pas de cluster | Deployment/Service et test HTTP | 10-kubernetes / 13 | Que garantit readiness ? |
| Terraform | 2 | Bloqué : outil absent du PATH | Plan puis apply approuvé | 11-terraform / 14 | À quoi sert le state ? |
| Grafana/Prometheus | 0,5 | Non commencé | Collecte et graphe réels | À ajouter / 16 | Qui collecte les métriques ? |
| Excellence | 3 | Partiel : mémoire projet créée | docs/ et preuves reproductibles | 12-final-validation / 16 | Comment reproduire ? |
| Questions sources | 5 | En cours | ORAL_DEFENSE.md | Soutenance | Expliquer Tache et son stockage |

Total officiel annoncé : 20 ; somme : 20,5 ; aucune normalisation.
Réponses : échec de commande arrête le stage ; branche isole les commits ; Mockito
remplace une dépendance ; Sonar inspecte le code sans remplacer les tests ; coordonnées
groupId/artifactId/version ; image immuable, conteneur instance ; readiness conditionne
le trafic ; state associe ressources et configuration ; Prometheus collecte, Grafana affiche.


## Mise a jour : captures et PowerPoint realises
Cinq PNG authentiques sont captures dans un visualiseur Windows visible (CopyFromScreen,
zone cliente uniquement). Git et versions executes a la capture ; sources lues sur disque ;
compilation et erreurs JDBC affichees depuis les journaux sauvegardes et identifiees comme telles.
Les mentions precedentes de captures manuelles attendues sont remplacees par ce statut.
Manifeste : docs/evidence/01-project-setup/capture-manifest.json (dates et SHA-256).
Presentation : docs/presentation/eventsProject-Tache-phase-01.pptx.
Verification : 7 diapositives, 5 images embarquees, 7 notes orales en francais ; archive
PPTX valide et export PowerPoint inspecte visuellement (preview/contact-sheet.jpg).
Le blocage MySQL persiste. Aucune reussite CI/CD supplementaire n'est revendiquee.
Reproduction (bureau Windows deverrouille, PowerPoint installe) :
```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Capture-BaselineEvidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Build-BaselinePresentation.ps1
```
Aucun logiciel supplementaire installe. Aucune capture manuelle requise pour cette phase.
