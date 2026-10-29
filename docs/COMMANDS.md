# Commandes

Exécutées : git status --short ; git branch --show-current ; git log -5 --oneline ;
java -version ; docker version ; docker compose version ; kubectl config current-context.
`mvn -version` échoue sur le PATH initial ; Maven du cache utilisé ensuite.
Les journaux de compilation sont conservés dans docs/evidence/02-maven.

La configuration temporaire JAVA_HOME/PATH ne modifie pas Windows globalement.

```powershell
. ./scripts/Use-ProjectJava.ps1
mvn -B compile
# Diagnostic sans création de base ni modification de schéma :
$env:SPRING_DATASOURCE_URL='jdbc:mysql://localhost:3306/eventsProject?serverTimezone=UTC'
$env:SPRING_JPA_HIBERNATE_DDL_AUTO='none'
mvn -B spring-boot:run
```

Compilation initiale : BUILD SUCCESS, 12 sources. Aucune option skipTests utilisée.
Ce résultat ne prouve pas les tests (aucun test initial) ni la connexion DB.
`git ls-remote --heads origin` réussit : branche Tache distante absente.


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
