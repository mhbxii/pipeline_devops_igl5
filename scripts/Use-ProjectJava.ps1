# Dot-source this script: . ./scripts/Use-ProjectJava.ps1
# Select existing tools for this PowerShell session only; no installation.
$projectJava = 'C:\Program Files\Java\jdk1.8.0_202'
if (-not (Test-Path "$projectJava/bin/java.exe")) {
    throw 'Java 8 baseline toolchain not found. Select an approved compatible JDK.'
}
$projectMaven = Get-ChildItem "$env:USERPROFILE/.m2/wrapper/dists" -Recurse -Filter mvn.cmd -ErrorAction SilentlyContinue |
    Where-Object { $_.FullName -match 'apache-maven-3\.9\.14' } |
    Select-Object -First 1
if (-not $projectMaven) { throw 'Existing Maven 3.9.14 installation not found.' }
$env:JAVA_HOME = $projectJava
$env:Path = "$projectJava\bin;$($projectMaven.DirectoryName);$env:Path"
mvn -version
