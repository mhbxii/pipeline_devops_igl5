pipeline {
    agent any
    stages {
        stage('A - bat') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub_credentials',
                    usernameVariable: 'DOCKERHUB_USERNAME',
                    passwordVariable: 'DOCKERHUB_TOKEN')]) {
                    bat '''
                        @echo off
                        echo %DOCKERHUB_TOKEN%| docker login -u %DOCKERHUB_USERNAME% --password-stdin
                        if errorlevel 1 exit /b 1
                        echo A - LOGIN OK
                        docker logout
                    '''
                }
            }
        }
        stage('B - powershell sans BOM') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub_credentials',
                    usernameVariable: 'DOCKERHUB_USERNAME',
                    passwordVariable: 'DOCKERHUB_TOKEN')]) {
                    powershell '''
                        Write-Host "OutputEncoding: $($OutputEncoding.EncodingName)"
                        $OutputEncoding = New-Object System.Text.UTF8Encoding($false)
                        $env:DOCKERHUB_TOKEN | docker login -u $env:DOCKERHUB_USERNAME --password-stdin
                        if ($LASTEXITCODE -ne 0) { throw "B - LOGIN FAILED" }
                        Write-Host "B - LOGIN OK"
                        docker logout
                    '''
                }
            }
        }
    }
}