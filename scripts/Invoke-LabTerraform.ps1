param([ValidateSet('Validate', 'Plan', 'Apply')][string]$Action = 'Plan')
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$terraform = Join-Path $projectRoot '.local/bin/terraform.exe'
if (!(Test-Path -LiteralPath $terraform)) { & "$PSScriptRoot/Install-LabTools.ps1" }
$values = @{}
Get-Content (Join-Path $projectRoot '.env') | ForEach-Object {
    if ($_ -match '^([^#=]+)=(.*)$') { $values[$matches[1]] = $matches[2] }
}
$env:TF_VAR_mysql_password = $values.MYSQL_PASSWORD
$env:TF_VAR_mysql_root_password = $values.MYSQL_ROOT_PASSWORD
$env:TF_VAR_kubeconfig_path = Join-Path $projectRoot '.local/kube/host-config'
$ErrorActionPreference = 'Continue'
try {
    & $terraform "-chdir=$projectRoot/infra" fmt -check
    if ($LASTEXITCODE -ne 0) { throw 'Terraform formatting failed' }
    & $terraform "-chdir=$projectRoot/infra" init -input=false
    if ($LASTEXITCODE -ne 0) { throw 'Terraform initialization failed' }
    & $terraform "-chdir=$projectRoot/infra" validate
    if ($LASTEXITCODE -ne 0) { throw 'Terraform validation failed' }
    if ($Action -ne 'Validate') {
        & $terraform "-chdir=$projectRoot/infra" plan -input=false '-out=lab.tfplan'
        if ($LASTEXITCODE -ne 0) { throw 'Terraform plan failed' }
    }
    if ($Action -eq 'Apply') {
        & $terraform "-chdir=$projectRoot/infra" apply -input=false 'lab.tfplan'
        if ($LASTEXITCODE -ne 0) { throw 'Terraform apply failed' }
        python "$PSScriptRoot/smoke-test.py" --base-url http://localhost:8091/events
        if ($LASTEXITCODE -ne 0) { throw 'Terraform deployment smoke test failed' }
    }
} finally {
    Remove-Item Env:TF_VAR_mysql_password -ErrorAction SilentlyContinue
    Remove-Item Env:TF_VAR_mysql_root_password -ErrorAction SilentlyContinue
}
