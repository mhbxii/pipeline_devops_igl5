# Présentation progressive

Diapositives 1–4 préparables maintenant ; résultats de compilation à compléter.
Toutes les captures restent à réaliser : aucun PNG créé ni prétendu.
Voir presentation/screenshots-index.md pour les commandes exactes.
Les autres diapositives restent en attente de résultats réels.


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
