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
