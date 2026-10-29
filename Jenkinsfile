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
                        $sha = [Security.Cryptography.SHA256]::Create()
                        $hash = [BitConverter]::ToString($sha.ComputeHash([Text.Encoding]::UTF8.GetBytes($env:DOCKERHUB_TOKEN))).Replace("-","")
                        Write-Host "Hash Jenkins: $($hash.Substring(0,12))"

                        # Login avec une config Docker vide
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