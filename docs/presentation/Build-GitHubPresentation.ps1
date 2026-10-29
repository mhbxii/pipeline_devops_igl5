# Reuse the shared generator while preserving the presentation open in PowerPoint.
$ErrorActionPreference = 'Stop'
$repoRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$source = Get-Content -Raw -Encoding UTF8 (Join-Path $repoRoot 'scripts/Build-BaselinePresentation.ps1')
$source = $source.Replace('$root = Split-Path $PSScriptRoot -Parent', '$root = (Get-Location).Path')
$source = $source.Replace("'eventsProject-Tache.pptx'", "'eventsProject-Tache-GitHub.pptx'")
$source = $source.Replace("'preview-current'", "'preview-github'")
Push-Location -LiteralPath $repoRoot
try { & ([scriptblock]::Create($source)) }
finally { Pop-Location }
