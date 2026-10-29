# Captures authentiques

État : captures manuelles attendues. Les fichiers .log et .txt sont des journaux,
pas des captures d'écran. Ne pas les présenter comme des PNG.

| Chemin prévu sous docs/evidence | Opération à afficher | Ce que cela démontre |
|---|---|---|
| 05-git/git-01-branch-history.png | Terminal : git branch --show-current puis git log -5 --oneline | Branche et historique réels |
| 01-project-setup/setup-02-tache-model.png | Éditeur : Tache.java et Participant.java côte à côte | Enum existant et mapping JPA |
| 02-maven/maven-01-toolchain.png | Terminal : . ./scripts/Use-ProjectJava.ps1 | Maven et JVM réellement sélectionnés |
| 02-maven/maven-02-baseline-build.png | Terminal : Get-Content docs/evidence/02-maven/baseline-compile.log -Tail 25 | Résultat réel, succès ou erreur |
| 01-project-setup/setup-03-database-blocker.png | Terminal : Select-String -Path docs/evidence/01-project-setup/baseline-startup.log -Pattern 'Access denied' puis Get-Content docs/evidence/01-project-setup/baseline-api.txt | Échec JDBC et HTTP 500 réels |

Utiliser Win+Shift+S, sélectionner seulement la zone pertinente, enregistrer au chemin
indiqué. Masquer toute information sensible avant capture, sans modifier les résultats.
