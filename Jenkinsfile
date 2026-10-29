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
                        whoami
                        Write-Host "Username: $env:DOCKERHUB_USERNAME"
                        Write-Host "Token length: $($env:DOCKERHUB_TOKEN.Length)"

                        $env:DOCKERHUB_TOKEN | docker login --username $env:DOCKERHUB_USERNAME --password-stdin
                        if ($LASTEXITCODE -ne 0) { throw "Login failed" }

                        Write-Host "Login OK"
                        docker logout
                    '''
                }
            }
        }
    }
}