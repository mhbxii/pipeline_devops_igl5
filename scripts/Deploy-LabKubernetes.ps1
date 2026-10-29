param([string]$Image = 'events-logistics:local')
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$env:KUBECONFIG = Join-Path $projectRoot '.local/kube/host-config'
$values = @{}
Get-Content (Join-Path $projectRoot '.env') | ForEach-Object {
    if ($_ -match '^([^#=]+)=(.*)$') { $values[$matches[1]] = $matches[2] }
}
$ErrorActionPreference = 'Continue'
& (Join-Path $projectRoot '.local/bin/kind.exe') load docker-image $Image mysql:8.4 --name events-devops
if ($LASTEXITCODE -ne 0) { throw 'Image loading failed' }
kubectl --context kind-events-devops apply -f "$projectRoot/k8s/namespace.yaml"
if ($LASTEXITCODE -ne 0) { throw 'Namespace creation failed' }
$secret = @{
    apiVersion = 'v1'; kind = 'Secret'
    metadata = @{name = 'events-db'; namespace = 'events'}
    type = 'Opaque'
    data = @{
        password = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($values.MYSQL_PASSWORD))
        'root-password' = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($values.MYSQL_ROOT_PASSWORD))
    }
} | ConvertTo-Json -Depth 5
$secret | kubectl --context kind-events-devops apply -f -
if ($LASTEXITCODE -ne 0) { throw 'Secret creation failed' }
kubectl --context kind-events-devops apply -k "$projectRoot/k8s"
if ($LASTEXITCODE -ne 0) { throw 'Kubernetes apply failed' }
kubectl --context kind-events-devops -n events rollout status statefulset/mysql --timeout=300s
if ($LASTEXITCODE -ne 0) { throw 'MySQL rollout failed' }
kubectl --context kind-events-devops -n events set image deployment/events-app "app=$Image"
if ($LASTEXITCODE -ne 0) { throw 'Application image update failed' }
if ($Image -eq 'events-logistics:local') {
    kubectl --context kind-events-devops -n events rollout restart deployment/events-app
    if ($LASTEXITCODE -ne 0) { throw 'Local image restart failed' }
}
kubectl --context kind-events-devops -n events rollout status deployment/events-app --timeout=300s
if ($LASTEXITCODE -ne 0) { throw 'Application rollout failed' }
python "$PSScriptRoot/smoke-test.py" --base-url http://localhost:8090/events
if ($LASTEXITCODE -ne 0) { throw 'Kubernetes smoke test failed' }
