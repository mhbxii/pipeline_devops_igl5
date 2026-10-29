$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
$url = 'http://localhost:8081'
# The admin password remains in memory. Never include it in evidence or command arguments.
$adminPassword = (docker compose -f docker-compose.devops.yml exec -T nexus cat /nexus-data/admin.password | Out-String).Trim()
if ($LASTEXITCODE -ne 0 -or -not $adminPassword) { throw 'Nexus is not initialized yet.' }
$adminHeaders = @{Authorization='Basic '+[Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("admin:$adminPassword"))}
$api = "$url/service/rest/v1"
$eula = Invoke-RestMethod "$api/system/eula" -Headers $adminHeaders
if (-not $eula.accepted) { throw "Review and approve the Nexus EULA before publication; do not accept automatically." }
$roles = Invoke-RestMethod "$api/security/roles" -Headers $adminHeaders
if (-not ($roles | Where-Object id -eq 'tache-publisher')) {
    $privileges = @()
    foreach ($repo in @('maven-snapshots','maven-releases')) {
        foreach ($action in @('browse','read','add','edit')) {
            $privileges += "nx-repository-view-maven2-$repo-$action"
        }
    }
    @{id='tache-publisher'; name='Tache Maven publisher'; description='Local project repositories only'; privileges=$privileges; roles=@()} |
        ConvertTo-Json -Depth 4 |
        ForEach-Object { Invoke-RestMethod "$api/security/roles" -Method Post -Headers $adminHeaders -ContentType application/json -Body $_ | Out-Null }
}
$line = Get-Content .env | Where-Object { $_.StartsWith('NEXUS_PASSWORD=') }
if (-not $line) {
    $bytes = New-Object byte[] 24
    $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
    $rng.GetBytes($bytes)
    $password = [Convert]::ToBase64String($bytes)
    $rng.Dispose()
    Add-Content -Encoding ASCII .env "NEXUS_PASSWORD=$password"
} else { $password = $line.Substring('NEXUS_PASSWORD='.Length) }
$users = Invoke-RestMethod "$api/security/users" -Headers $adminHeaders
if (-not ($users | Where-Object userId -eq 'tache-publisher')) {
    $user = @{userId='tache-publisher';firstName='Tache';lastName='Publisher';emailAddress='tache@example.invalid';password=$password;status='active';roles=@('tache-publisher')}
    Invoke-RestMethod "$api/security/users" -Method Post -Headers $adminHeaders -ContentType application/json -Body ($user | ConvertTo-Json -Depth 4) | Out-Null
}
$env:NEXUS_URL=$url
$env:NEXUS_USER='tache-publisher'
$env:NEXUS_PASSWORD=$password
. ./scripts/Use-ProjectJava.ps1
mvn -B -s ci/settings.xml deploy
if ($LASTEXITCODE -ne 0) { throw 'Nexus deployment failed.' }
$publisherHeaders = @{Authorization='Basic '+[Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("tache-publisher:$password"))}
$metadataUrl="$url/repository/maven-snapshots/tn/esprit/eventsProject/1.0.0-SNAPSHOT/maven-metadata.xml"
$metadata = Invoke-RestMethod $metadataUrl -Headers $publisherHeaders
$snapshot = $metadata.metadata.versioning.snapshotVersions.snapshotVersion | Where-Object { $_.extension -eq 'jar' -and -not $_.classifier } | Select-Object -First 1
if (-not $snapshot) { throw 'Published JAR not present in Maven metadata.' }
$artifactUrl="$url/repository/maven-snapshots/tn/esprit/eventsProject/1.0.0-SNAPSHOT/eventsProject-$($snapshot.value).jar"
New-Item -ItemType Directory -Force .local/nexus-verification | Out-Null
Invoke-WebRequest -UseBasicParsing $artifactUrl -Headers $publisherHeaders -OutFile .local/nexus-verification/published.jar
$localHash=(Get-FileHash target/eventsProject-1.0.0-SNAPSHOT.jar -Algorithm SHA256).Hash
$remoteHash=(Get-FileHash .local/nexus-verification/published.jar -Algorithm SHA256).Hash
if ($localHash -ne $remoteHash) { throw 'Downloaded Nexus artifact differs from built JAR.' }
Write-Output "VERIFIED: tn.esprit:eventsProject:1.0.0-SNAPSHOT ($($snapshot.value))"
Write-Output "Repository: $url/repository/maven-snapshots/"
Write-Output "Downloaded JAR SHA256 matches local artifact: $localHash"
