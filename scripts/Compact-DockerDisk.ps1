#Requires -RunAsAdministrator
[CmdletBinding()]
param([switch]$AllowWslShutdown)
$ErrorActionPreference = 'Stop'
if (-not $AllowWslShutdown) {
    throw 'This operation stops Docker Desktop and ALL WSL distributions. Save their work, then explicitly pass -AllowWslShutdown.'
}
# Fixed, verified Docker Desktop data disk. Never select another disk automatically.
$expected = Join-Path $env:LOCALAPPDATA 'Docker\wsl\disk\docker_data.vhdx'
$resolved = (Resolve-Path -LiteralPath $expected).Path
if ($resolved -ne $expected -or [IO.Path]::GetFileName($resolved) -ne 'docker_data.vhdx') {
    throw 'Unexpected Docker data disk path; review manually.'
}
$before = (Get-Item -LiteralPath $resolved).Length
Write-Host "Compacting only: $resolved"
docker desktop stop
if ($LASTEXITCODE -ne 0) { throw 'Stop Docker Desktop before compacting.' }
wsl.exe --shutdown
if ($LASTEXITCODE -ne 0) { throw 'WSL shutdown failed.' }
# Ensure no VM still has the disk open before invoking DiskPart.
$unlocked = $false
for ($attempt = 0; $attempt -lt 30; $attempt++) {
    try {
        $handle = [IO.File]::Open($resolved,[IO.FileMode]::Open,[IO.FileAccess]::Read,[IO.FileShare]::None)
        $handle.Dispose()
        $unlocked = $true
        break
    } catch { Start-Sleep -Seconds 1 }
}
if (-not $unlocked) { throw 'Docker data disk is still in use. Nothing compacted.' }
$taskFile = Join-Path $env:TEMP "events-tache-compact-$([guid]::NewGuid().ToString('N')).txt"
$detachFile = Join-Path $env:TEMP "events-tache-detach-$([guid]::NewGuid().ToString('N')).txt"
try {
    @("select vdisk file=`"$resolved`"", 'attach vdisk readonly', 'compact vdisk', 'detach vdisk', 'exit') |
        Set-Content -Encoding ASCII -LiteralPath $taskFile
    @("select vdisk file=`"$resolved`"", 'detach vdisk noerr', 'exit') |
        Set-Content -Encoding ASCII -LiteralPath $detachFile
    diskpart.exe /s $taskFile
} finally {
    # DiskPart stops processing after an error; always attempt to detach the selected VHD.
    if (Test-Path -LiteralPath $detachFile) { diskpart.exe /s $detachFile }
    Remove-Item -LiteralPath $taskFile,$detachFile -ErrorAction SilentlyContinue
}
$after = (Get-Item -LiteralPath $resolved).Length
Write-Host "VHD size before=$before after=$after bytes"
Get-PSDrive C | Select-Object Used,Free
docker desktop start
Write-Host 'Restart the project containers and port-forwards using docs/COMMANDS.md.'
