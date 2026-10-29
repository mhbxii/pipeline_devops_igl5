param([string]$BaseUrl = 'http://localhost:8089/events')
$ErrorActionPreference = 'Stop'
function Request($method, $path, $body, $expected) {
    $params = @{Uri="$BaseUrl$path"; Method=$method; UseBasicParsing=$true}
    if ($null -ne $body) { $params.ContentType='application/json'; $params.Body=$body }
    try { $response = Invoke-WebRequest @params; $status = [int]$response.StatusCode }
    catch {
        if (-not $_.Exception.Response) { throw }
        $status = [int]$_.Exception.Response.StatusCode
        $response = $null
    }
    if ($status -ne $expected) { throw "$method $path expected=$expected actual=$status" }
    Write-Host "$method $path -> HTTP $status"
    if ($response -and $response.Content) { return ($response.Content | ConvertFrom-Json) }
}
$roles = Request GET '/taches' $null 200
if ($roles.Count -ne 4) { throw 'Expected four roles.' }
$participant = Request POST '/event/addPart' '{"nom":"Evidence","prenom":"Tache"}' 200
$id = $participant.idPart
if ($id -le 0) { throw 'Participant was not persisted.' }
$role = Request GET "/taches/participants/$id" $null 200
if ($null -ne $role.tache) { throw 'New participant should have no role.' }
$role = Request PUT "/taches/participants/$id" '{"tache":"INVITE"}' 200
if ($role.tache -ne 'INVITE') { throw 'Initial role assignment failed.' }
$role = Request PUT "/taches/participants/$id" '{"tache":"ORGANISATEUR"}' 200
if ($role.tache -ne 'ORGANISATEUR') { throw 'Assignment failed.' }
$role = Request GET "/taches/participants/$id" $null 200
if ($role.tache -ne 'ORGANISATEUR') { throw 'Role was not persisted.' }
Request PUT "/taches/participants/$id" '{"tache":"UNKNOWN"}' 400 | Out-Null
Request PUT "/taches/participants/$id" '{}' 400 | Out-Null
Request GET '/taches/participants/2147483647' $null 404 | Out-Null
Request DELETE "/taches/participants/$id" $null 204 | Out-Null
$role = Request GET "/taches/participants/$id" $null 200
if ($null -ne $role.tache) { throw 'Role not cleared.' }
Request GET '/event/getLogs/2026-01-01/2026-12-31' $null 200 | Out-Null
Write-Host "PASS: MySQL-backed HTTP lifecycle; participantId=$id remains present with no role."
