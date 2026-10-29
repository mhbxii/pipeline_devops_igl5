pipeline {
    agent any
    stages {
        stage('Test Docker Hub login') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub_credentials',
                        usernameVariable: 'DOCKERHUB_USERNAME',
                        passwordVariable: 'DOCKERHUB_TOKEN'
                    )
                ]) {
                    powershell '''
                        Write-Host "===== 1. HASH DU TOKEN ====="
                        $sha = [Security.Cryptography.SHA256]::Create()
                        $hash = [BitConverter]::ToString($sha.ComputeHash([Text.Encoding]::UTF8.GetBytes($env:DOCKERHUB_TOKEN))).Replace("-","")
                        Write-Host "Hash Jenkins: $($hash.Substring(0,12))"

                        Write-Host "===== 2. DIAGNOSTIC DOCKER ====="
                        whoami
                        docker context ls
                        docker version --format "client={{.Client.Version}} server={{.Server.Version}}"
                        docker info --format "ctx={{.Name}} proxy_http={{.HTTPProxy}} proxy_https={{.HTTPSProxy}}"

                        Write-Host "===== 3. TEST API DOCKER HUB (sans daemon) ====="
                        try {
                            $body = @{ username = $env:DOCKERHUB_USERNAME; password = $env:DOCKERHUB_TOKEN } | ConvertTo-Json
                            Invoke-RestMethod -Uri "https://hub.docker.com/v2/users/login" -Method Post -ContentType "application/json" -Body $body | Out-Null
                            Write-Host "API login OK"
                        } catch {
                            Write-Host "API login FAILED: $($_.Exception.Message)"
                        }

                        Write-Host "===== 4. DOCKER LOGIN ====="
                        $env:DOCKER_CONFIG = Join-Path $env:WORKSPACE "docker-config"
                        New-Item -ItemType Directory -Force -Path $env:DOCKER_CONFIG | Out-Null

                        $env:DOCKERHUB_TOKEN | docker login --username $env:DOCKERHUB_USERNAME --password-stdin
                        if ($LASTEXITCODE -ne 0) { throw "Login failed" }
                        Write-Host "Login OK"
                    '''
                }
            }
        }
    }
}