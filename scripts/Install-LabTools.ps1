$ErrorActionPreference = 'Stop'
$toolPath = Join-Path (Split-Path $PSScriptRoot -Parent) '.local/bin'
New-Item -ItemType Directory -Force -Path $toolPath | Out-Null
$kindVersion = 'v0.30.0'
$kindFile = Join-Path $toolPath 'kind.exe'
if (!(Test-Path -LiteralPath $kindFile)) {
    $url = "https://github.com/kubernetes-sigs/kind/releases/download/$kindVersion/kind-windows-amd64"
    Invoke-WebRequest -UseBasicParsing -Uri $url -OutFile $kindFile
}
$kindChecksum = Join-Path $toolPath 'kind.sha256sum'
Invoke-WebRequest -UseBasicParsing -Uri "https://github.com/kubernetes-sigs/kind/releases/download/$kindVersion/kind-windows-amd64.sha256sum" -OutFile $kindChecksum
$expected = ((Get-Content -LiteralPath $kindChecksum -Raw) -split '\s+')[0]
if ((Get-FileHash -Algorithm SHA256 -LiteralPath $kindFile).Hash -ne $expected) { throw 'kind checksum mismatch' }
$terraformVersion = '1.13.5'
$terraformFile = Join-Path $toolPath 'terraform.exe'
if (!(Test-Path -LiteralPath $terraformFile)) {
    $zipName = "terraform_${terraformVersion}_windows_amd64.zip"
    $baseUrl = "https://releases.hashicorp.com/terraform/$terraformVersion"
    $zipPath = Join-Path $toolPath $zipName
    Invoke-WebRequest -UseBasicParsing -Uri "$baseUrl/$zipName" -OutFile $zipPath
    $checksumFile = Join-Path $toolPath 'terraform.sha256sums'
    Invoke-WebRequest -UseBasicParsing -Uri "$baseUrl/terraform_${terraformVersion}_SHA256SUMS" -OutFile $checksumFile
    $sums = Get-Content -LiteralPath $checksumFile -Raw
    $expected = (($sums -split "`n" | Where-Object { $_.Trim().EndsWith($zipName) }) -split '\s+')[0]
    if ((Get-FileHash -Algorithm SHA256 -LiteralPath $zipPath).Hash -ne $expected) { throw 'Terraform checksum mismatch' }
    Expand-Archive -LiteralPath $zipPath -DestinationPath $toolPath -Force
    Remove-Item -LiteralPath $zipPath
}
& $kindFile version
& $terraformFile version
