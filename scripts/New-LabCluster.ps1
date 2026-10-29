$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$kind = Join-Path $projectRoot '.local/bin/kind.exe'
if (!(Test-Path -LiteralPath $kind)) { & "$PSScriptRoot/Install-LabTools.ps1" }
$kubeDir = Join-Path $projectRoot '.local/kube'
New-Item -ItemType Directory -Force -Path $kubeDir | Out-Null
$env:KIND_EXPERIMENTAL_DOCKER_NETWORK = 'events-devops'
# Windows PowerShell 5 treats kind's progress on stderr as errors when redirected.
# Native command failures are checked explicitly below.
$ErrorActionPreference = 'Continue'
$clusters = & $kind get clusters
if ($LASTEXITCODE -ne 0) { throw 'Unable to list kind clusters' }
if ($clusters -notcontains 'events-devops') {
    & $kind create cluster --name events-devops --config "$projectRoot/k8s/kind.yaml" --image kindest/node:v1.34.0 --kubeconfig "$kubeDir/host-config" --wait 180s
    if ($LASTEXITCODE -ne 0) { throw 'kind cluster creation failed' }
}
& $kind export kubeconfig --name events-devops --kubeconfig "$kubeDir/host-config"
if ($LASTEXITCODE -ne 0) { throw 'Unable to export host kubeconfig' }
$internal = & $kind get kubeconfig --name events-devops --internal
if ($LASTEXITCODE -ne 0) { throw 'Unable to export container kubeconfig' }
# Jenkins shares the cluster's Docker network; host kubeconfig stays separate.
[IO.File]::WriteAllLines((Join-Path $kubeDir 'config'), $internal, (New-Object Text.UTF8Encoding $false))
$env:KUBECONFIG = Join-Path $kubeDir 'host-config'
kubectl --context kind-events-devops get nodes
