$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$kubectl = (Get-Command kubectl -ErrorAction Stop).Source
$config = Join-Path $root '.local/kubeconfig'
function Ensure-Forward($name, $namespace, $service, $localPort, $remotePort) {
    $listener = Get-NetTCPConnection -State Listen -LocalPort $localPort -ErrorAction SilentlyContinue
    if ($listener) {
        $owner = Get-CimInstance Win32_Process -Filter "ProcessId = $($listener[0].OwningProcess)"
        if ($owner.Name -ne 'kubectl.exe' -or $owner.CommandLine -notlike "*service/$service*") {
            throw "Port $localPort belongs to another process; it was not changed."
        }
        Write-Output "$name forward already active on 127.0.0.1:$localPort"
        return
    }
    $arguments = @("--kubeconfig=`"$config`"", '--context', 'kind-events-tache', '-n', $namespace,
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
Ensure-Forward app events-tache events 18089 8089
Ensure-Forward prometheus events-monitoring prometheus 19090 9090
