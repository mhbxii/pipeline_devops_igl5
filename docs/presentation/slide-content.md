# Contenu actuel

## 1 — Projet eventsProject : module Tache
- Contribution individuelle : gestion des rôles des participants.
- Branche dédiée : Tache.
Preuve : git-01-branch-history.png, à capturer.
Note orale : le dépôt réel diffère du nom Timesheet indiqué dans l'énoncé initial.

## 3 — Comprendre le modèle existant
- Contrôleur REST → service → repository JPA → MySQL.
- Tache est un enum persisté dans Participant.
Preuve : setup-02-tache-model.png, à capturer dans l'éditeur.
Résultat : périmètre confirmé sans remplacement du modèle partagé.

## 4 — Diagnostic de l'environnement
- Maven trouvé hors PATH ; sélection temporaire de Java 8.
- Docker disponible ; cluster Kubernetes non configuré.
- Ressources locales limitées : démarrage progressif des services.
Preuve : maven-01-toolchain.png, à capturer.
Note orale : un outil installé ne prouve pas que le pipeline fonctionne.

## 5 — Première compilation Maven réussie
- 12 fichiers Java compilés avec le POM existant.
- Maven 3.9.14 et Java 8 sélectionnés pour cette session.
Preuve : maven-02-baseline-build.png, capture attendue du journal réel.
Résultat : BUILD SUCCESS ; cela ne valide pas encore les tests ni l'exécution.

## 4 bis — Identifier le blocage de connexion
- Le serveur HTTP démarre, mais MySQL refuse le compte configuré.
- La requête métier retourne HTTP 500.
Preuve : setup-03-database-blocker.png, capture attendue.
Note orale : isoler une base dédiée permettra de préserver les autres projets.
Ce correctif reste à implémenter et à vérifier.


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
