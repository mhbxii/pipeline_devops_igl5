$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if (-not (Test-Path .env)) { throw 'Initialize the local environment first.' }
if (-not (Get-Content .env | Where-Object { $_.StartsWith('JENKINS_ADMIN_PASSWORD=') })) {
    $bytes = New-Object byte[] 24
    $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
    $rng.GetBytes($bytes)
    Add-Content -Encoding ASCII .env "JENKINS_ADMIN_PASSWORD=$([Convert]::ToBase64String($bytes))"
    $rng.Dispose()
}
if (-not (Test-Path .local/jenkins-repo/HEAD)) {
    git clone --bare . .local/jenkins-repo
} else {
    git --git-dir=.local/jenkins-repo fetch origin refs/heads/Tache:refs/heads/Tache
}
if ($LASTEXITCODE -ne 0) { throw 'Local SCM snapshot failed.' }
docker compose -f docker-compose.devops.yml --profile jenkins up -d --build jenkins
if ($LASTEXITCODE -ne 0) { throw 'Jenkins failed to start.' }
Write-Output 'Jenkins local URL: http://localhost:8080 ; user: tache-admin'
Write-Output 'Password is stored only in ignored .env. Job: tache-ci. No remote push required.'
