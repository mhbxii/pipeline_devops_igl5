param([switch]$Restart, [switch]$IncludeGrafana, [switch]$Eks)
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$kubectl = (Get-Command kubectl -ErrorAction Stop).Source
$config = Join-Path $root '.local/kubeconfig'
$context = 'kind-events-tache'
if ($Eks) {
    $kubectl = Join-Path $root '.local/tools/kubectl-eks.exe'
    $config = Join-Path $root '.local/kubeconfig-eks'
    $context = 'events-tache-eks'
}
function Ensure-Forward($name, $namespace, $service, $localPort, $remotePort) {
    $listener = Get-NetTCPConnection -State Listen -LocalPort $localPort -ErrorAction SilentlyContinue
    if ($listener) {
        $owner = Get-CimInstance Win32_Process -Filter "ProcessId = $($listener[0].OwningProcess)"
        if ($owner.Name -notin @('kubectl.exe', 'kubectl-eks.exe') -or $owner.CommandLine -notlike "*service/$service*" -or $owner.CommandLine -notlike "*$context*") {
            throw "Port $localPort belongs to another process; it was not changed."
        }
        if (-not $Restart) {
            Write-Output "$name forward already active on 127.0.0.1:$localPort"
            return
        }
        Stop-Process -Id $owner.ProcessId
    }
    $arguments = @("--kubeconfig=`"$config`"", '--context', $context, '-n', $namespace,
        'port-forward', '--address', '127.0.0.1', "service/$service", "${localPort}:${remotePort}")
    $process = Start-Process -FilePath $kubectl -ArgumentList $arguments -WindowStyle Hidden -PassThru `
        -RedirectStandardOutput (Join-Path $root ".local/$name-forward.log") `
        -RedirectStandardError (Join-Path $root ".local/$name-forward-error.log")
    for ($attempt=0; $attempt -lt 30; $attempt++) {
        if (Get-NetTCPConnection -State Listen -LocalPort $localPort -ErrorAction SilentlyContinue) {
            Write-Output "$name forward started on 127.0.0.1:$localPort (PID $($process.Id))"
            return
        }
        $process.Refresh()
        if ($process.HasExited) { throw "$name port-forward exited; see .local/$name-forward-error.log" }
        Start-Sleep -Milliseconds 300
    }
    throw "$name port-forward did not become ready."
}
if ($Eks) {
    Ensure-Forward eks-app events-tache events 18189 8089
    Ensure-Forward eks-prometheus events-monitoring prometheus 19190 9090
    if ($IncludeGrafana) { Ensure-Forward eks-grafana events-grafana grafana 13100 3000 }
} else {
    Ensure-Forward app events-tache events 18089 8089
    Ensure-Forward prometheus events-monitoring prometheus 19090 9090
    if ($IncludeGrafana) { Ensure-Forward grafana events-grafana grafana 13000 3000 }
}
