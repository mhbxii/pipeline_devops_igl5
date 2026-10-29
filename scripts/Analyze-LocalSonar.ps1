$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if (-not (Test-Path target/site/jacoco/jacoco.xml)) { throw 'Run Maven verify first to generate coverage.' }
. ./scripts/Use-ProjectJava.ps1
# Analysis runs separately from compilation: this legacy Lombok version builds on 8/17.
# The current Sonar scanner requires Java 21+, already available here as JDK 22.
$analysisJava = 'C:\Program Files\Java\jdk-22'
if (-not (Test-Path "$analysisJava/bin/java.exe")) { throw 'Select an existing JDK 21+ for analysis.' }
$env:JAVA_HOME = $analysisJava
$env:Path = "$analysisJava\bin;$env:Path"
$env:MAVEN_OPTS = '-Xmx512m'
$env:SONAR_HOST_URL = 'http://localhost:9000'
$tokenLine = Get-Content .env | Where-Object { $_.StartsWith('SONAR_TOKEN=') }
if (-not $tokenLine) { throw 'Initialize local SonarQube first.' }
$env:SONAR_TOKEN = $tokenLine.Substring('SONAR_TOKEN='.Length)
mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:5.8.0.7211:sonar '-Dsonar.scanner.skipJreProvisioning=true' '-Dsonar.projectKey=events-tache' '-Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml'
if ($LASTEXITCODE -ne 0) { throw 'Sonar analysis failed; inspect the log without revealing the token.' }
