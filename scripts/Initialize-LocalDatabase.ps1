$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if (-not (Test-Path .env)) {
    $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
    function New-LocalPassword {
        $bytes = New-Object byte[] 24
        $rng.GetBytes($bytes)
        return [Convert]::ToBase64String($bytes)
    }
    @("DB_PASSWORD=$(New-LocalPassword)", "MYSQL_ROOT_PASSWORD=$(New-LocalPassword)") |
        Set-Content -Encoding ASCII .env
    $rng.Dispose()
}
# Never print `docker compose config`: it expands credentials.
docker compose -f docker-compose.devops.yml up -d --wait --wait-timeout 240 mysql
if ($LASTEXITCODE -ne 0) { throw 'MySQL did not become healthy.' }
docker compose -f docker-compose.devops.yml ps
