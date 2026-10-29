# Captures a real, visible Windows evidence viewer. No synthetic screenshot rendering.
$ErrorActionPreference = 'Stop'
Add-Type -TypeDefinition 'using System.Runtime.InteropServices; public class EvidenceDpi { [DllImport("user32.dll")] public static extern bool SetProcessDPIAware(); }'
[EvidenceDpi]::SetProcessDPIAware() | Out-Null
Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing
$root = Split-Path $PSScriptRoot -Parent
Set-Location $root
$items = @(
    @{ File='05-git/git-01-branch-history'; Title='Git | commandes executees maintenant'; Command='git branch --show-current; git log -5 --oneline'; Text=(@(git branch --show-current; git log -5 --oneline) | Out-String) },
    @{ File='01-project-setup/setup-02-tache-model'; Title='Sources actuelles | Tache.java et Participant.java'; Command='Get-Content Tache.java; Get-Content Participant.java (paths: src/main/java/tn/esprit/eventsproject/entities)' ; Text=(@(Get-Content src/main/java/tn/esprit/eventsproject/entities/Tache.java; Get-Content src/main/java/tn/esprit/eventsproject/entities/Participant.java) | Out-String) },
    @{ File='02-maven/maven-01-toolchain'; Title='Maven / Java | commande executee maintenant'; Command='. ./scripts/Use-ProjectJava.ps1'; Text=((. ./scripts/Use-ProjectJava.ps1) | Out-String) },
    @{ File='02-maven/maven-02-baseline-build'; Title='Compilation | lecture du journal sauvegarde'; Command='Get-Content docs/evidence/02-maven/baseline-compile.log -Tail 10'; Text=((Get-Content docs/evidence/02-maven/baseline-compile.log -Tail 10) | Out-String) },
    @{ File='01-project-setup/setup-03-database-blocker'; Title='Blocage MySQL | lecture des journaux sauvegardes'; Command="Select-String baseline-startup.log -Pattern 'Access denied'; Get-Content baseline-api.txt"; Text=(@((Select-String docs/evidence/01-project-setup/baseline-startup.log -Pattern 'Access denied' | Select-Object -First 3 | ForEach-Object { $_.Line }); ''; (Get-Content docs/evidence/01-project-setup/baseline-api.txt)) | Out-String) }
)
$extra = @(
    @{File='12-final-validation/local-delivery'; Title='Livraison | Jenkins, Nexus et JAR du pod compares'; Source='12-final-validation/local-delivery.json'},
    @{File='12-final-validation/legacy-dto-api'; Title='Compatibilite | quatre routes historiques et MySQL'; Source='12-final-validation/legacy-dto-api.json'},
    @{File='07-nexus/nexus-verification'; Title='Nexus | publication et integrite du JAR verifiees'; Source='07-nexus/nexus-verification.txt'},
    @{File='10-kubernetes/kubernetes-resources'; Title='Kubernetes | pods prets, Services et stockage'; Source='10-kubernetes/kubernetes-resources.txt'},
    @{File='10-kubernetes/kubernetes-api-success'; Title='Kubernetes | cycle HTTP reel sur le Service'; Source='10-kubernetes/kubernetes-api-success.txt'},
    @{File='11-terraform/terraform-apply'; Title='Terraform | apply approuve et execute'; Source='11-terraform/terraform-apply.txt'},
    @{File='11-terraform/terraform-no-drift'; Title='Terraform | infrastructure conforme, aucune derive'; Source='11-terraform/terraform-no-drift.txt'},
    @{File='01-project-setup/mysql-api-success'; Title='MySQL corrige | verification HTTP reelle'; Source='01-project-setup/mysql-api-success.txt'},
    @{File='04-unit-tests/tests-success'; Title='JUnit / Mockito / JPA sans mocks | journal Maven'; Source='02-maven/maven-verify.log'},
    @{File='04-unit-tests/coverage-service'; Title='JaCoCo | couverture du service Tache'; Source='04-unit-tests/tache-coverage.txt'},
    @{File='03-logging/tache-levels'; Title='Log4j2 | API reelle et panne simulee en test'; Source='03-logging/tache-runtime-levels.txt'},
    @{File='09-docker/docker-api-success'; Title='Docker | cycle HTTP reussi dans le conteneur'; Source='09-docker/docker-api-success.txt'},
    @{File='09-docker/docker-containers'; Title='Docker | conteneurs et utilisateur applicatif'; Source='09-docker/docker-containers.txt'}
)
foreach ($entry in $extra) {
    $source = "docs/evidence/$($entry.Source)"
    if (Test-Path $source) {
        $items += @{File=$entry.File; Title=$entry.Title; Command="Get-Content $source (journal sauvegarde)"; Text=((Get-Content $source -Tail 18) | Out-String)}
    }
}
$manifest = @()
foreach ($item in $items) {
    $form = New-Object Windows.Forms.Form
    $form.Text = 'eventsProject - Evidence viewer (actual sources and command output)'
    $form.AutoScaleMode = 'None'
    $form.StartPosition = 'Manual'
    $form.Location = New-Object Drawing.Point(40,40)
    $form.ClientSize = New-Object Drawing.Size(1420,740)
    $form.BackColor = [Drawing.Color]::FromArgb(15,23,42)
    $form.TopMost = $true
    $title = New-Object Windows.Forms.Label
    $title.SetBounds(24,16,1370,40)
    $title.Font = New-Object Drawing.Font('Segoe UI',19,[Drawing.FontStyle]::Bold)
    $title.ForeColor = [Drawing.Color]::White
    $title.Text = $item.Title
    $form.Controls.Add($title)
    $command = New-Object Windows.Forms.Label
    $command.SetBounds(24,62,1370,55)
    $command.Font = New-Object Drawing.Font('Consolas',12)
    $command.ForeColor = [Drawing.Color]::FromArgb(94,234,212)
    $command.Text = $item.Command
    $form.Controls.Add($command)
    $body = New-Object Windows.Forms.TextBox
    $body.SetBounds(24,125,1370,580)
    $body.Multiline = $true
    $body.ReadOnly = $true
    $body.ScrollBars = 'Both'
    $body.WordWrap = $true
    $body.BorderStyle = 'None'
    $body.Font = New-Object Drawing.Font('Consolas',$(if ($item.File -like '*model') {10} else {15}))
    $body.BackColor = $form.BackColor
    $body.ForeColor = [Drawing.Color]::FromArgb(226,232,240)
    $body.Text = $item.Text.Trim()
    $body.Select(0,0)
    $form.Controls.Add($body)
    $form.Show()
    $form.Activate()
    [Windows.Forms.Application]::DoEvents()
    Start-Sleep -Milliseconds 800
    $bitmap = New-Object Drawing.Bitmap($form.ClientSize.Width,$form.ClientSize.Height)
    $graphics = [Drawing.Graphics]::FromImage($bitmap)
    $graphics.CopyFromScreen($form.PointToScreen([Drawing.Point]::Empty),[Drawing.Point]::Empty,$form.ClientSize)
    $path = Join-Path $root "docs/evidence/$($item.File).png"
    $bitmap.Save($path,[Drawing.Imaging.ImageFormat]::Png)
    $graphics.Dispose()
    $bitmap.Dispose()
    $form.Close()
    $form.Dispose()
    $item.Text.TrimEnd() | Set-Content -Encoding UTF8 (Join-Path $root "docs/evidence/$($item.File)-displayed.txt")
    $manifest += [ordered]@{path="docs/evidence/$($item.File).png"; title=$item.Title; command=$item.Command; capturedAt=(Get-Date -Format o); method='Visible Windows Forms evidence viewer captured using CopyFromScreen'; sha256=(Get-FileHash $path -Algorithm SHA256).Hash}
}
$manifest | ConvertTo-Json -Depth 4 | Set-Content -Encoding UTF8 docs/evidence/01-project-setup/capture-manifest.json
Write-Output "Captured $($items.Count) real evidence-viewer windows."
