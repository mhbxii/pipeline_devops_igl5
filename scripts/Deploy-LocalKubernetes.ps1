$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
$env:KUBECONFIG = Join-Path (Get-Location) '.local/kubeconfig'
function Check-Exit { if ($LASTEXITCODE -ne 0) { throw 'Kubernetes operation failed.' } }
if (-not (Test-Path $env:KUBECONFIG)) { throw 'Create the events-tache kind cluster first.' }
# Docker Desktop's containerd image store may include an incomplete multi-platform
# index. Export exactly the local platform before importing into kind/containerd.
foreach ($entry in @(@{Image='events-tache:local'; Archive='.local/application-amd64.tar'}, @{Image='mysql:8.4'; Archive='.local/mysql-amd64.tar'})) {
    docker image save --platform=linux/amd64 -o $entry.Archive $entry.Image
    Check-Exit
    & .local/tools/kind.exe load image-archive $entry.Archive --name events-tache
    Check-Exit
}
kubectl --context kind-events-tache apply -f k8s/namespace.yaml
Check-Exit
$values = @{}
Get-Content .env | ForEach-Object {
    $parts = $_ -split '=',2
    if ($parts.Count -eq 2) { $values[$parts[0]] = $parts[1] }
}
# Build Secret in memory and send over stdin; never print or save its data.
$secret = @{
    apiVersion='v1'; kind='Secret'; type='Opaque'
    metadata=@{name='database'; namespace='events-tache'}
    stringData=@{password=$values['DB_PASSWORD']; 'root-password'=$values['MYSQL_ROOT_PASSWORD']}
}
$secret | ConvertTo-Json -Depth 5 | kubectl --context kind-events-tache apply -f -
Check-Exit
kubectl --context kind-events-tache apply -f k8s/mysql.yaml
Check-Exit
kubectl --context kind-events-tache -n events-tache rollout status deployment/mysql --timeout=240s
Check-Exit
kubectl --context kind-events-tache apply -f k8s/application.yaml
Check-Exit
kubectl --context kind-events-tache -n events-tache rollout status deployment/events --timeout=240s
Check-Exit
kubectl --context kind-events-tache -n events-tache get pods,services,pvc
