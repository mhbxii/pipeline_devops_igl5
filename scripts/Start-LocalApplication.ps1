$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
. ./scripts/Use-ProjectJava.ps1
if (-not (Test-Path .env)) { throw 'Run Initialize-LocalDatabase.ps1 first.' }
$passwordLine = Get-Content .env | Where-Object { $_.StartsWith('DB_PASSWORD=') }
$env:DB_PASSWORD = $passwordLine.Substring('DB_PASSWORD='.Length)
$env:SPRING_PROFILES_ACTIVE = 'local'
$env:SPRING_DEVTOOLS_RESTART_ENABLED = 'false'
mvn -B spring-boot:run
if ($LASTEXITCODE -ne 0) { throw 'Application exited unsuccessfully.' }
