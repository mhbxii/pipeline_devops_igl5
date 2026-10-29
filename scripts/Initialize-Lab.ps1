$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$envPath = Join-Path $projectRoot '.env'
if (Test-Path -LiteralPath $envPath) {
    Write-Host '.env already exists; preserving its credentials.'
} else {
    $keys = @('JENKINS_ADMIN_PASSWORD', 'NEXUS_PASSWORD', 'SONAR_DB_PASSWORD',
        'SONAR_ADMIN_PASSWORD', 'MYSQL_ROOT_PASSWORD', 'MYSQL_PASSWORD', 'GRAFANA_PASSWORD')
    $lines = foreach ($key in $keys) {
        $bytes = New-Object byte[] 24
        $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
        $rng.GetBytes($bytes)
        $rng.Dispose()
        "$key=Aa1!$([Convert]::ToBase64String($bytes))"
    }
    $lines += @('SONAR_TOKEN=', 'DOCKERHUB_USERNAME=', 'DOCKERHUB_TOKEN=')
    [IO.File]::WriteAllLines($envPath, $lines, (New-Object Text.UTF8Encoding $false))
    Write-Host 'Generated .env with unique local passwords. Keep it private.'
}
New-Item -ItemType Directory -Force -Path (Join-Path $projectRoot '.local/kube') | Out-Null
