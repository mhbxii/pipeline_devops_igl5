# Rapport d'implémentation

## Phase 1 — Diagnostic (en cours)
Branche Tache vérifiée, état initial propre, périmètre enum confirmé.
Sources Spring et configuration inspectées ; pas de tests initiaux.
Environnement inventorié ; Maven et JDK 8 existants sélectionnés temporairement.
Documentation persistante et suivi du barème établis avant les modifications majeures.
Compilation réussie : 12 sources, BUILD SUCCESS. Démarrage HTTP réussi mais connexion
JDBC refusée (root sans mot de passe) et API GET getLogs : HTTP 500.
Socle non fonctionnel à ce stade ; blocage documenté, aucune réussite CI/CD revendiquée.
Preuves textuelles enregistrées ; captures manuelles demandées avant poursuite.
Contribution : Git, préparation aux questions sources, documentation reproductible.
Diapositive : « Diagnostic du socle : compilation réussie, connexion DB à corriger ».


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
